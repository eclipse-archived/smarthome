/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ntp.handler;

import static org.eclipse.smarthome.binding.ntp.NtpBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * The NTP Refresh Service polls the configured timeserver with a configurable
 * interval and posts a new event of type ({@link DateTimeType}.
 * 
 * The {@link NtpHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 * 
 * @author Marcel Verpaalen - Initial contribution OH2 ntp binding
 * @author Thomas.Eichstaedt-Engelen OH1 ntp binding (getTime routine)
 */

public class NtpHandler extends BaseThingHandler {

	private Logger logger = LoggerFactory.getLogger(NtpHandler.class);

	/** timeout for requests to the NTP server */
	private static final int NTP_TIMEOUT = 5000;

	/** for logging purposes */
	private final DateFormat SDF = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.FULL);

	ScheduledFuture<?> refreshJob;

	/** NTP host */
	private String hostname;
	/** refresh interval */
	private BigDecimal refreshInterval;
	/** NTP refresh frequency */
	private BigDecimal refreshNtp = new BigDecimal(0);
	/** Timezone */
	private TimeZone timeZone;
	/** Locale */
	private Locale locale;

	/** NTP refresh counter */
	private int refreshNtpCount = 0;
	/** NTP system time delta */
	private long timeOffset;
	
	private ChannelUID dateTimeChannelUID;

	public NtpHandler(Thing thing) {
		super(thing);
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		// No specific commands tied to this, but we will trigger an update
		this.refreshNtpCount = 0;
		refreshTimeDate();
	}

	@Override
	public void initialize() {

		try {
			logger.debug("Initializing NTP handler for '{}'.",getThing().getUID());

			Configuration config = getThing().getConfiguration();
			this.hostname = (String) config.get(PROPERTY_NTP_SERVER);
			this.refreshInterval = (BigDecimal) config.get(PROPERTY_REFRESH_INTERVAL);
			this.refreshNtp = (BigDecimal) config.get(PROPERTY_REFRESH_NTP);
			this.refreshNtpCount = 0;

			try {
				timeZone = TimeZone.getTimeZone((String) config.get(PROPERTY_TIMEZONE));
			} catch (Exception e) {
				timeZone = TimeZone.getDefault();
				logger.debug("using default TZ: {}", timeZone);
			}

			try {
				String localeString = (String) config.get(PROPERTY_LOCALE);
				locale = new Locale(localeString);
			} catch (Exception e) {
				locale = Locale.getDefault();
				logger.debug("using default locale: {}", locale);
			}
			dateTimeChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_DATE_TIME);
			startAutomaticRefresh();

		} catch (Exception ex) {
			String msg = "Error occured while initializing NTP handler: " + ex.getMessage();
			logger.error(msg, ex);
			updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
		}
	}

	@Override
	public void dispose() {
		refreshJob.cancel(true);
		super.dispose();
	}

	private void startAutomaticRefresh() {

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					refreshTimeDate();
				} catch (Exception e) {
					logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
				}
			}
		};

		refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, refreshInterval.intValue(), TimeUnit.SECONDS);
	}

	private synchronized void refreshTimeDate() {

		long networkTimeInMillis;
		if (refreshNtpCount <= 0) {
			networkTimeInMillis = getTime(hostname);
			timeOffset = networkTimeInMillis - System.currentTimeMillis();
			logger.debug("delta system time: {}", timeOffset);
			refreshNtpCount = refreshNtp.intValue();
		} else {
			networkTimeInMillis = System.currentTimeMillis() + timeOffset;
			refreshNtpCount--;
		}

		Calendar calendar = Calendar.getInstance(timeZone, locale);
		calendar.setTimeInMillis(networkTimeInMillis);

		updateState(dateTimeChannelUID, new DateTimeType(calendar));
	}

	/**
	 * Queries the given timeserver <code>hostname</code> and returns the time
	 * in milliseconds.
	 * 
	 * @param hostname
	 *            the timeserver to query
	 * @return the time in milliseconds or the current time of the system if an
	 *         error occurs.
	 */
	public long getTime(String hostname) {

		try {
			NTPUDPClient timeClient = new NTPUDPClient();
			timeClient.setDefaultTimeout(NTP_TIMEOUT);
			InetAddress inetAddress = InetAddress.getByName(hostname);
			TimeInfo timeInfo = timeClient.getTime(inetAddress);

			logger.debug("Got time update from: {}", hostname, SDF.format(new Date(timeInfo.getReturnTime())));
			updateStatus(ThingStatus.ONLINE,ThingStatusDetail.NONE);
			return timeInfo.getReturnTime();
		} catch (UnknownHostException uhe) {
			String msg = "the given hostname '" + hostname + "' of the timeserver is unknown -> returning current sytem time instead";				
			logger.warn(msg);
			updateStatus(ThingStatus.ONLINE,ThingStatusDetail.COMMUNICATION_ERROR, msg);			
		} catch (IOException ioe) {
			String msg ="couldn't establish network connection [host '" + hostname + "'] -> returning current sytem time instead";
			logger.warn(msg);
			updateStatus(ThingStatus.ONLINE,ThingStatusDetail.COMMUNICATION_ERROR, msg);			
		}

		return System.currentTimeMillis();
	}

}
