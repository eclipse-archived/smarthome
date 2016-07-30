/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.persistence.h2sql.internal;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.FilterCriteria.Ordering;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.ModifiablePersistenceService;
import org.eclipse.smarthome.core.persistence.PersistenceItemInfo;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.core.types.UnDefType;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of the H2 SQL {@link PersistenceService}.
 * See http://h2database.com
 *
 * H2 database licensed under EPL (http://h2database.com/html/license.html)
 *
 * In the store method, type conversion is performed where the default type for
 * an item is not as above For example, DimmerType can return OnOffType, so to
 * keep the best resolution, we store as a number in SQL and convert to
 * DecimalType before persisting to H2SQL.
 *
 * @author Chris Jackson - Initial contribution
 */
public class H2SqlPersistenceService implements ModifiablePersistenceService, ManagedService {

    private static final Logger logger = LoggerFactory.getLogger(H2SqlPersistenceService.class);

    private final String driverClass = "org.h2.Driver";
    private final String h2Url = "jdbc:h2:file:";
    private final String schema = "SMARTHOME";

    private final SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    protected ItemRegistry itemRegistry = null;

    private Connection connection = null;
    private final Map<String, String> sqlTypes = new HashMap<String, String>();
    private final List<String> itemCache = new ArrayList<String>();

    @SuppressWarnings("unused")
    private BundleContext bundleContext;

    public H2SqlPersistenceService() {
        // Initialise the type array
        sqlTypes.put("DIMMERITEM", "TINYINT");
        sqlTypes.put("NUMBERITEM", "DECIMAL");
        sqlTypes.put("ROLLERSHUTTERITEM", "TINYINT");
    }

    protected void activate(BundleContext bundleContext, Map<String, Object> properties) {
        logger.info("H2SQL: Persistence bundle activated.");

        this.bundleContext = bundleContext;
    }

    public void deactivate() {
        logger.info("H2SQL: Persistence bundle deactivated.");
        disconnectFromDatabase();
        this.bundleContext = null;
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "h2sql";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store(Item item, String alias) {
        store(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store(Item item) {
        // Do some type conversion to ensure we know the data type.
        // This is necessary for items that have multiple types and may return their
        // state in a format that's not preferred or compatible with the H2SQL type.
        // eg. DimmerItem can return OnOffType (ON, OFF), or PercentType (0-100).
        // We need to make sure we cover the best type for serialisation.
        State state;
        if (item instanceof DimmerItem || item instanceof RollershutterItem) {
            state = item.getStateAs(PercentType.class);
        } else if (item instanceof ColorItem) {
            state = item.getStateAs(HSBType.class);
        } else {
            // All other items should return the best format by default
            state = item.getState();
        }
        logger.trace("H2SQL: State is {}::{}", item.getState(), state);

        store(item, new Date(), state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store(Item item, Date date, State state) {
        if (state == null || state instanceof UnDefType) {
            logger.trace("H2SQL: State of {} [{}] is {}. Store aborted", item, item.getClass().getSimpleName(), state);
            return;
        }

        // Connect to H2SQL server if we're not already connected
        if (!connectToDatabase()) {
            logger.warn("H2SQL: No connection to database. Can not persist item '{}'", item.getName());
            return;
        }

        Statement statement = null;
        String sqlCmd = null;

        // A bit of profiling!
        long timerStart = System.currentTimeMillis();

        if (!itemCache.contains(item.getName())) {
            itemCache.add(item.getName());

            // Default the type to String and get the real type
            String sqlType = new String("VARCHAR");
            String itemType = item.getClass().getSimpleName().toUpperCase();
            if (sqlTypes.get(itemType) != null) {
                sqlType = sqlTypes.get(itemType);
            }

            // Create the table for the data
            sqlCmd = new String("CREATE TABLE IF NOT EXISTS " + getTableName(item.getName()) + " (Time DATETIME, Value "
                    + sqlType + ", PRIMARY KEY(Time));");
            logger.trace("H2SQL: " + sqlCmd);

            try {
                statement = connection.createStatement();
                statement.executeUpdate(sqlCmd);

                logger.trace("H2SQL: Table created for item '{}' with datatype '{}'", item.getName(), sqlType);
            } catch (Exception e) {
                logger.error("H2SQL: Could not create table for item '{}' with statement '{}'", item.getName(), sqlCmd);
                logger.error("     : " + e.getMessage());
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (Exception hidden) {
                    }
                }
            }
        }

        try {
            // Firstly, try an INSERT. This will work 99.9% of the time
            statement = connection.createStatement();
            sqlCmd = new String("INSERT INTO " + getTableName(item.getName()) + " (TIME, VALUE) " + "VALUES('"
                    + sqlDateFormat.format(date) + "','" + state.toString() + "');");
            statement.executeUpdate(sqlCmd);

            long timerStop = System.currentTimeMillis();
            logger.debug("H2SQL: Stored item '{}' as '{}' in {}ms", item.getName(), state.toString(),
                    timerStop - timerStart);
            logger.trace("H2SQL: {}", sqlCmd);
        } catch (Exception e1) {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception hidden) {
                }
            }

            try {
                // The INSERT failed. This might be because we tried persisting data too quickly, or it might be
                // because we really want to UPDATE the data.
                // So, let's try an update. If the reason for the exception isn't due to the primary key already
                // existing, then we'll throw another exception.
                // Note that H2 stores times using the Java Date class, so resolution is milliseconds. We really
                // shouldn't be persisting data that quickly!
                statement = connection.createStatement();
                sqlCmd = new String("UPDATE " + getTableName(item.getName()) + " SET VALUE='" + state.toString() + "'"
                        + " WHERE TIME='" + sqlDateFormat.format(date) + "';");
                statement.executeUpdate(sqlCmd);

                long timerStop = System.currentTimeMillis();
                logger.debug("H2SQL: Stored item '{}' as '{}' in {}ms", item.getName(), state.toString(),
                        timerStop - timerStart);
                logger.trace("H2SQL: {}", sqlCmd);
            } catch (Exception e2) {

                logger.error("H2SQL: Could not store item '{}' in database with statement '{}'", item.getName(),
                        sqlCmd);
                logger.error("     : " + e2.getMessage());
            }
        }

        if (statement != null) {
            try {
                statement.close();
            } catch (Exception hidden) {
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updated(Dictionary<String, ?> config) throws ConfigurationException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        // Connect to H2SQL server if we're not already connected
        if (!connectToDatabase()) {
            logger.debug("Query aborted on item {} - H2SQL not connected!", filter.getItemName());
            return Collections.emptyList();
        }

        // Get the item name from the filter
        // Also get the Item object so we can determine the type
        Item item = null;
        String itemName = filter.getItemName();
        try {
            if (itemRegistry != null) {
                item = itemRegistry.getItem(itemName);
            }
        } catch (ItemNotFoundException e) {
            logger.error("H2SQL: Unable to get item type for {}", itemName);
            logger.error("     : " + e.getMessage());

            // Set type to null - data will be returned as StringType
            item = null;
        }

        if (item instanceof GroupItem) {
            // For Group Items is BaseItem needed to get correct Type of Value.
            item = GroupItem.class.cast(item).getBaseItem();
        }

        String filterString = getFilterStringWhere(filter) + getFilterStringOrder(filter)
                + getFilterStringLimit(filter);

        try {
            long timerStart = System.currentTimeMillis();

            // Retrieve the table array
            Statement st = connection.createStatement();

            String queryString = new String();
            queryString = "SELECT Time, Value FROM " + getTableName(filter.getItemName());
            if (!filterString.isEmpty()) {
                queryString += filterString;
            }

            logger.trace("H2SQL: " + queryString);

            // Turn use of the cursor on.
            st.setFetchSize(50);

            ResultSet rs = st.executeQuery(queryString);

            long count = 0;
            List<HistoricItem> items = new ArrayList<HistoricItem>();
            State state;
            while (rs.next()) {
                count++;

                if (item == null) {
                    state = new StringType(rs.getString(2));
                } else {
                    state = TypeParser.parseState(item.getAcceptedDataTypes(), rs.getString(2));
                }

                H2SqlHistoricItem sqlItem = new H2SqlHistoricItem(itemName, state, rs.getTimestamp(1));
                items.add(sqlItem);
            }

            rs.close();
            st.close();

            long timerStop = System.currentTimeMillis();
            logger.debug("H2SQL: query returned {} rows in {}ms", count, timerStop - timerStart);

            return items;
        } catch (SQLException e) {
            logger.error("H2SQL: Error running query");
            logger.error("     : " + e.getMessage());

            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<PersistenceItemInfo> getItems() {
        // Connect to H2SQL server if we're not already connected
        if (!connectToDatabase()) {
            logger.warn("H2SQL: No connection to database.");
            return Collections.emptySet();
        }

        try {
            long timerStart = System.currentTimeMillis();

            // Retrieve the table array
            Statement st = connection.createStatement();

            String queryString = new String();
            queryString = "SELECT TABLE_NAME, ROW_COUNT_ESTIMATE FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='"
                    + schema + "'";

            // Turn use of the cursor on.
            st.setFetchSize(50);

            ResultSet rs = st.executeQuery(queryString);

            Date earliest;
            Date latest;

            Set<PersistenceItemInfo> items = new HashSet<PersistenceItemInfo>();
            while (rs.next()) {
                Statement stTimes = connection.createStatement();

                queryString = "SELECT MIN(Time), MAX(Time) FROM " + getTableName(rs.getString(1));
                ResultSet rsTimes = stTimes.executeQuery(queryString);

                if (rsTimes.next()) {
                    earliest = rsTimes.getTimestamp(1);
                    latest = rsTimes.getTimestamp(2);
                } else {
                    earliest = null;
                    latest = null;
                }

                rsTimes.close();
                stTimes.close();

                H2SqlPersistenceItem item = new H2SqlPersistenceItem(rs.getString(1), rs.getInt(2), earliest, latest);
                items.add(item);
            }

            rs.close();
            st.close();

            long timerStop = System.currentTimeMillis();
            logger.debug("H2SQL: query returned {} items in {}ms", items.size(), timerStop - timerStart);

            return items;
        } catch (SQLException e) {
            logger.error("H2SQL: Error running query");
            logger.error("     : " + e.getMessage());

            return Collections.emptySet();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(FilterCriteria filter) {
        // Connect to H2SQL server if we're not already connected
        if (!connectToDatabase()) {
            logger.warn("H2SQL: No connection to database.");
            return false;
        }

        String filterString = getFilterStringWhere(filter);

        try {
            long timerStart = System.currentTimeMillis();

            // Retrieve the table array
            Statement st = connection.createStatement();

            String queryString = new String();
            queryString = "DELETE FROM " + getTableName(filter.getItemName()) + filterString;

            st.execute(queryString);
            int rowsDeleted = st.getUpdateCount();

            long timerStop = System.currentTimeMillis();

            // Do some housekeeping...
            // See how many rows remain - if it's 0, we should remove the table from the database
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + getTableName(filter.getItemName()));

            rs.next();
            logger.debug("H2SQL: deleted {} rows from {} in {}ms. {} rows remain.", filter.getItemName(), rowsDeleted,
                    timerStop - timerStart, rs.getInt(1));

            if (rs.getInt(1) == 0) {
                queryString = "DROP TABLE " + getTableName(filter.getItemName());
                st.execute(queryString);

                logger.info("H2SQL: Dropped table for {}", filter.getItemName());
            }

            st.close();

            return true;
        } catch (SQLException e) {
            logger.error("H2SQL: Error running query");
            logger.error("     : " + e.getMessage());

            return false;
        }
    }

    /**
     * Checks if we have a database connection
     *
     * @return true if connection has been established, false otherwise
     */
    private boolean isConnected() {
        // Check if connection is valid
        try {
            if (connection != null && !connection.isValid(5000)) {
                logger.error("H2SQL: Connection is not valid!");
            }
        } catch (SQLException e) {
            logger.error("H2SQL: Error while checking connection");
            logger.error("     : " + e.getMessage());
        }
        return connection != null;
    }

    /**
     * Connects to the database
     */
    private boolean connectToDatabase() {
        // First, check if we're connected
        if (isConnected() == true) {
            return true;
        }

        // We're not connected, so connect
        try {
            logger.debug("H2SQL: Connecting to database");
            Class.forName(driverClass).newInstance();

            final String folderName = ConfigConstants.getUserDataFolder() + "/h2sql";

            // Create path for serialization.
            final File folder = new File(folderName);
            if (!folder.exists()) {
                logger.debug("Creating H2SQL folder {}", folderName);
                folder.mkdirs();
            }

            final String databaseFileName = folderName + "/smarthome";

            String url = h2Url + databaseFileName;

            // Disable logging and defrag on shutdown
            url += ";TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0;DEFRAG_ALWAYS=true;";
            connection = DriverManager.getConnection(url);

            logger.debug("H2SQL: Connected to database {}", databaseFileName);

            Statement statement = connection.createStatement();
            String sqlCmd = new String("CREATE SCHEMA IF NOT EXISTS " + schema + ";");
            statement.executeUpdate(sqlCmd);
            sqlCmd = new String("SET SCHEMA " + schema + ";");
            statement.executeUpdate(sqlCmd);
            statement.close();
        } catch (Exception e) {
            logger.error("H2SQL: Failed connecting to the SQL database");
            logger.error("     : " + e.getMessage());
        }

        return isConnected();
    }

    /**
     * Disconnects from the database
     */
    private void disconnectFromDatabase() {
        logger.debug("H2SQL: Disconnecting from database.");
        if (connection != null) {
            try {
                connection.close();
                logger.debug("H2SQL: Disconnected from database.");
            } catch (Exception e) {
                logger.error("H2SQL: Failed disconnecting from the SQL database");
                logger.error("     : " + e.getMessage());
            }
            connection = null;
        }
    }

    private String getTableName(String itemName) {
        return schema + ".\"" + itemName + "\"";
    }

    private String getFilterStringWhere(FilterCriteria filter) {
        String filterString = new String();

        if (filter.getBeginDate() != null) {
            if (filterString.isEmpty()) {
                filterString += " WHERE";
            } else {
                filterString += " AND";
            }
            filterString += " TIME>='" + sqlDateFormat.format(filter.getBeginDate()) + "'";
        }
        if (filter.getEndDate() != null) {
            if (filterString.isEmpty()) {
                filterString += " WHERE";
            } else {
                filterString += " AND";
            }
            filterString += " TIME<='" + sqlDateFormat.format(filter.getEndDate().getTime()) + "'";
        }
        return filterString;
    }

    private String getFilterStringOrder(FilterCriteria filter) {
        String filterString = new String();

        if (filter.getOrdering() == Ordering.ASCENDING) {
            filterString += " ORDER BY Time ASC";
        } else {
            filterString += " ORDER BY Time DESC";
        }

        return filterString;
    }

    private String getFilterStringLimit(FilterCriteria filter) {
        String filterString = new String();

        if (filter.getPageSize() != 0x7fffffff) {
            filterString += " LIMIT " + filter.getPageSize() + " OFFSET "
                    + (filter.getPageNumber() * filter.getPageSize());
        }

        return filterString;
    }
}
