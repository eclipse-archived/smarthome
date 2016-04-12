/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/**
* Copyright (c) 2010-2013, openHAB.org and others.
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*/
package org.eclipse.smarthome.io.rest.core.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.FilterCriteria.Ordering;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.persistence.QueryablePersistenceService;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.rest.JSONResponse;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This class acts as a REST resource for history data and provides different methods to interact with the persistence
 * store
 *
 * @author Chris Jackson - Initial Contribution
 */
@Path(PersistenceResource.PATH)
@Api(value = PersistenceResource.PATH)
public class PersistenceResource implements RESTResource {

    private final Logger logger = LoggerFactory.getLogger(PersistenceResource.class);
    private final int MILLISECONDS_PER_DAY = 86400000;

    // The URI path to this resource
    public static final String PATH = "persistence";

    static private Map<String, PersistenceService> persistenceServices = new HashMap<String, PersistenceService>();

    public void addPersistenceService(PersistenceService service) {
        persistenceServices.put(service.getName(), service);
    }

    public void removePersistenceService(PersistenceService service) {
        persistenceServices.remove(service.getName());
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Gets a list of persistence services.", response = String.class, responseContainer = "List")
    @ApiResponses(value = @ApiResponse(code = 200, message = "OK") )
    public Response httpGetPersistenceServices(@Context HttpHeaders headers) {
        Object responseObject = getPersistenceServiceList();
        return Response.ok(responseObject).build();
    }

    @GET
    @Path("/{itemname: [a-zA-Z_0-9]*}")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Gets item persistence data from the persistence service.", response = ItemHistoryBean.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Unknown Item or persistence service") })
    public Response httpGetPersistenceItemData(@Context HttpHeaders headers,
            @ApiParam(value = "The item name", required = true) @PathParam("itemname") String itemName,
            @ApiParam(value = "Name of the persistence service. If not provided the default service will be used", required = false) @QueryParam("servicename") String serviceName,
            @ApiParam(value = "Start time of the data to return. Will default to 1 day before endtime", required = false) @QueryParam("starttime") String startTime,
            @ApiParam(value = "End time of the data to return. Will default to current time.", required = false) @QueryParam("endtime") String endTime,
            @ApiParam(value = "Page number of data to return. This parameter will enable paging.", required = false) @QueryParam("page") int pageNumber,
            @ApiParam(value = "The length of each page.", required = false) @QueryParam("pagelength") int pageLength) {

        return getItemHistoryBean(serviceName, itemName, startTime, endTime, pageNumber, pageLength);
    }

    private Date convertTime(String sTime) {
        DateTimeType dateTime = new DateTimeType(sTime);
        return dateTime.getCalendar().getTime();
    }

    private Response getItemHistoryBean(String serviceName, String itemName, String timeBegin, String timeEnd,
            int pageNumber, int pageLength) {
        // Benchmarking timer...
        long timerStart = System.currentTimeMillis();

        // If serviceName is null, then use the default service
        PersistenceService service = null;
        if (serviceName == null) {
            // TODO: Add handler for default service once this is available in ESH
        } else {
            service = persistenceServices.get(serviceName);
        }

        if (service == null) {
            logger.debug("Persistence service not found '{}'.", serviceName);
            return JSONResponse.createErrorResponse(Status.CONFLICT, "Persistence service not found: " + serviceName);
        }

        if (!(service instanceof QueryablePersistenceService)) {
            logger.debug("Persistence service not queryable '{}'.", serviceName);
            return JSONResponse.createErrorResponse(Status.CONFLICT,
                    "Persistence service not queryable: " + serviceName);
        }

        QueryablePersistenceService qService = (QueryablePersistenceService) service;

        Date dateTimeBegin = new Date();
        Date dateTimeEnd = dateTimeBegin;
        if (timeBegin != null) {
            dateTimeBegin = convertTime(timeBegin);
        }

        if (timeEnd != null) {
            dateTimeEnd = convertTime(timeEnd);
        }

        // End now...
        if (dateTimeEnd.getTime() == 0) {
            dateTimeEnd = new Date();
        }
        if (dateTimeBegin.getTime() == 0) {
            dateTimeBegin = new Date(dateTimeEnd.getTime() - MILLISECONDS_PER_DAY);
        }

        // Default to 1 days data if the times are the same or the start time is newer than the end time
        if (dateTimeBegin.getTime() >= dateTimeEnd.getTime()) {
            dateTimeBegin = new Date(dateTimeEnd.getTime() - MILLISECONDS_PER_DAY);
        }

        FilterCriteria filter;
        Iterable<HistoricItem> result;
        State state = null;

        Long quantity = 0l;

        ItemHistoryBean bean = new ItemHistoryBean();
        bean.name = itemName;

        // First, get the value at the start time.
        // This is necessary for values that don't change often otherwise data will start after the start of the graph
        // (or not at all if there's no change during the graph period)
        filter = new FilterCriteria();
        filter.setEndDate(dateTimeBegin);
        filter.setItemName(itemName);
        filter.setPageSize(1);
        filter.setOrdering(Ordering.DESCENDING);
        result = qService.query(filter);
        if (result != null && result.iterator().hasNext()) {
            bean.addData(dateTimeBegin.getTime(), result.iterator().next().getState());
            quantity++;
        }

        filter.setPageSize(pageLength);
        filter.setPageNumber(pageNumber);
        filter.setBeginDate(dateTimeBegin);
        filter.setEndDate(dateTimeEnd);
        filter.setOrdering(Ordering.ASCENDING);
        filter.setPageSize(Integer.MAX_VALUE);

        result = qService.query(filter);
        if (result != null) {
            Iterator<HistoricItem> it = result.iterator();

            // Iterate through the data
            while (it.hasNext()) {
                HistoricItem historicItem = it.next();
                state = historicItem.getState();

                // For 'binary' states, we need to replicate the data
                // to avoid diagonal lines
                if (state instanceof OnOffType || state instanceof OpenClosedType) {
                    bean.addData(historicItem.getTimestamp().getTime(), state);
                }

                bean.addData(historicItem.getTimestamp().getTime(), state);
                quantity++;
            }

            // Add the last value again at the end time
            if (state != null) {
                bean.addData(dateTimeEnd.getTime(), state);
                quantity++;
            }
        }

        bean.datapoints = Long.toString(quantity);
        logger.debug("Persistence returned {} rows in {}ms", bean.datapoints, System.currentTimeMillis() - timerStart);

        return JSONResponse.createResponse(Status.OK, bean, "");
    }

    /**
     * Gets a list of persistence services currently configured in the system
     *
     * @return list of persistence services as {@link ServiceBean}
     */
    private List<ServiceBean> getPersistenceServiceList() {
        List<ServiceBean> beanList = new ArrayList<ServiceBean>();

        for (Map.Entry<String, PersistenceService> service : persistenceServices.entrySet()) {
            ServiceBean serviceBean = new ServiceBean();
            serviceBean.name = service.getKey();

            beanList.add(serviceBean);
        }

        return beanList;
    }
}
