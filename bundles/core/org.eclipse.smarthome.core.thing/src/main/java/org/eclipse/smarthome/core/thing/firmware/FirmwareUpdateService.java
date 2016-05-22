/**
 * Copyright (c) 2016 Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware;

import static org.eclipse.smarthome.core.thing.firmware.FirmwareStatusInfo.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.smarthome.core.common.SafeMethodCaller;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.firmware.Firmware;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUID;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUpdateBackgroundTransferHandler;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * The firmware update service is registered as an OSGi service and is responsible for tracking all available
 * {@link FirmwareUpdateHandler}s. It provides access to the current {@link FirmwareStatusInfo} of a thing and is the
 * central instance to start a firmware update.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class FirmwareUpdateService {

    private final Logger logger = LoggerFactory.getLogger(FirmwareUpdateService.class);

    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(FirmwareUpdateService.class.getSimpleName());
    private TimeUnit firmwareStatusInfoJobTimeUnit = TimeUnit.HOURS;
    private final int firmwareStatusInfoJobPeriod = 1;
    private ScheduledFuture<?> firmwareStatusInfoJob;

    private int timeout = 30 * 60 * 1000;

    private final Map<ThingUID, FirmwareStatusInfo> firmwareStatusInfoMap = new HashMap<>();

    private final List<FirmwareUpdateHandler> firmwareUpdateHandlers = new CopyOnWriteArrayList<>();
    private FirmwareRegistry firmwareRegistry;
    private EventPublisher eventPublisher;
    private I18nProvider i18nProvider;

    private Runnable firmwareStatusRunnable = new Runnable() {
        @Override
        public void run() {
            logger.debug("Running firmware status check.");
            for (FirmwareUpdateHandler firmwareUpdateHandler : firmwareUpdateHandlers) {
                try {
                    logger.debug("Executing firmware status check for thing with UID {}.",
                            firmwareUpdateHandler.getThing().getUID());

                    Firmware latestFirmware = firmwareRegistry
                            .getLatestFirmware(firmwareUpdateHandler.getThing().getThingTypeUID());

                    FirmwareStatusInfo newFirmwareStatusInfo = getFirmwareStatusInfo(firmwareUpdateHandler,
                            latestFirmware);

                    processFirmwareStatusInfo(firmwareUpdateHandler, newFirmwareStatusInfo, latestFirmware);
                } catch (Exception e) {
                    logger.debug("Exception occurred during firmware status check.", e);
                }
            }
        }
    };

    protected void activate() {
        if (firmwareStatusInfoJob == null || firmwareStatusInfoJob.isCancelled()) {
            firmwareStatusInfoJob = scheduler.scheduleAtFixedRate(firmwareStatusRunnable, 0,
                    firmwareStatusInfoJobPeriod, firmwareStatusInfoJobTimeUnit);
        }
    }

    protected void deactivate() {
        if (firmwareStatusInfoJob != null && !firmwareStatusInfoJob.isCancelled()) {
            firmwareStatusInfoJob.cancel(true);
            firmwareStatusInfoJob = null;
        }
    }

    /**
     * Returns the {@link FirmwareStatusInfo} for the thing having the given thing UID.
     *
     * @param thingUID the UID of the thing (must not be null)
     *
     * @return the firmware status info (is null if there is no {@link FirmwareUpdateHandler} for the thing
     *         available)
     *
     * @throws NullPointerException if the given thing UID is null
     */
    public FirmwareStatusInfo getFirmwareStatusInfo(ThingUID thingUID) {
        Preconditions.checkNotNull(thingUID, "Thing UID must not be null.");

        FirmwareUpdateHandler firmwareUpdateHandler = getFirmwareUpdateHandler(thingUID);

        if (firmwareUpdateHandler == null) {
            logger.debug("No firmware update handler available for thing with UID {}.", thingUID);
            return null;
        }

        Firmware latestFirmware = firmwareRegistry
                .getLatestFirmware(firmwareUpdateHandler.getThing().getThingTypeUID());

        FirmwareStatusInfo firmwareStatusInfo = getFirmwareStatusInfo(firmwareUpdateHandler, latestFirmware);

        processFirmwareStatusInfo(firmwareUpdateHandler, firmwareStatusInfo, latestFirmware);

        return firmwareStatusInfo;
    }

    /**
     * Updates the firmware of the thing having the given thing UID by invoking the operation
     * {@link FirmwareUpdateHandler#updateFirmware(Firmware, ProgressCallback)} of the thing´s firmware update handler.
     * <p>
     * This operation is a non-blocking operation by spawning a new thread around the invocation of the firmware update
     * handler. The time out of the thread is 30 minutes.
     * </p>
     *
     *
     * @param thingUID the thing UID (must not be null)
     * @param firmwareUID the UID of the firmware to be updated (must not be null)
     * @param locale the locale to be used to internationalize error messages (if null then the default locale is used)
     *
     * @throws NullPointerException if given thing UID or firmware UID is null
     * @throws IllegalStateException if
     *             <ul>
     *             <li>there is no firmware update handler for the thing</li>
     *             <li>the firmware update handler is not able to execute the firmware update</li>
     *             </ul>
     * @throws IllegalArgumentException if
     *             <ul>
     *             <li>the firmware cannot be found</li>
     *             <li>the firmware is not suitable for the thing</li>
     *             <li>the firmware requires another prerequisite firmware version</li>
     *             </ul>
     */
    public void updateFirmware(final ThingUID thingUID, final FirmwareUID firmwareUID, final Locale locale) {
        Preconditions.checkNotNull(thingUID, "Thing UID must not be null.");
        Preconditions.checkNotNull(firmwareUID, "Firmware UID must not be null.");

        final FirmwareUpdateHandler firmwareUpdateHandler = getFirmwareUpdateHandler(thingUID);

        if (firmwareUpdateHandler == null) {
            throw new IllegalStateException(
                    String.format("There is no firmware update handler for thing with UID %s.", thingUID));
        }

        final Firmware firmware = getFirmware(firmwareUID);

        validateFirmwareUpdateConditions(firmware, firmwareUpdateHandler);

        logger.debug("Starting firmware update for thing with UID {} and firmware with UID {}", thingUID, firmwareUID);

        scheduler.submit(new Runnable() {
            @Override
            public void run() {
                final ProgressCallbackImpl progressCallback = new ProgressCallbackImpl(firmwareUpdateHandler,
                        eventPublisher, i18nProvider, thingUID, firmwareUID,
                        locale != null ? locale : Locale.getDefault());
                try {
                    SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                        @Override
                        public Void call() {
                            firmwareUpdateHandler.updateFirmware(firmware, progressCallback);
                            return null;
                        }
                    }, timeout);
                } catch (ExecutionException e) {
                    logger.error(String.format(
                            "Unexpected exception occurred for firmware update of thing with UID %s and firmware with UID %s.",
                            thingUID, firmwareUID), e.getCause());
                    progressCallback.failedInternal("unexpected-handler-error");
                } catch (TimeoutException e) {
                    logger.error(String.format(
                            "Timeout occurred for firmware update of thing with UID %s and firmware with UID %s.",
                            thingUID, firmwareUID), e);
                    progressCallback.failedInternal("timeout-error");
                }
            }
        });
    }

    private FirmwareStatusInfo getFirmwareStatusInfo(FirmwareUpdateHandler firmwareUpdateHandler,
            Firmware latestFirmware) {
        String thingFirmwareVersion = getThingFirmwareVersion(firmwareUpdateHandler);

        if (latestFirmware == null || thingFirmwareVersion == null) {
            return createUnknownInfo();
        }

        if (latestFirmware.isSuccessorVersion(thingFirmwareVersion)) {
            if (firmwareUpdateHandler.isUpdateExecutable()) {
                return createUpdateExecutableInfo(latestFirmware.getUID());
            } else {
                return createUpdateAvailableInfo();
            }
        }

        return createUpToDateInfo();
    }

    private synchronized void processFirmwareStatusInfo(FirmwareUpdateHandler firmwareUpdateHandler,
            FirmwareStatusInfo newFirmwareStatusInfo, Firmware latestFirmware) {
        ThingUID thingUID = firmwareUpdateHandler.getThing().getUID();

        FirmwareStatusInfo previousFirmwareStatusInfo = firmwareStatusInfoMap.put(thingUID, newFirmwareStatusInfo);

        if (previousFirmwareStatusInfo == null || !previousFirmwareStatusInfo.equals(newFirmwareStatusInfo)) {
            eventPublisher.post(FirmwareEventFactory.createFirmwareStatusInfoEvent(newFirmwareStatusInfo, thingUID));

            if (newFirmwareStatusInfo.getFirmwareStatus() == FirmwareStatus.UPDATE_AVAILABLE
                    && firmwareUpdateHandler instanceof FirmwareUpdateBackgroundTransferHandler
                    && !firmwareUpdateHandler.isUpdateExecutable()) {
                transferLatestFirmware((FirmwareUpdateBackgroundTransferHandler) firmwareUpdateHandler, latestFirmware,
                        previousFirmwareStatusInfo);
            }
        }
    }

    private void transferLatestFirmware(final FirmwareUpdateBackgroundTransferHandler fubtHandler,
            final Firmware latestFirmware, final FirmwareStatusInfo previousFirmwareStatusInfo) {
        scheduler.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    fubtHandler.transferFirmware(latestFirmware);
                } catch (Exception e) {
                    logger.error("Exception occurred during background firmware transfer.", e);
                    synchronized (this) {
                        // restore previous firmware status info in order that transfer can be re-triggered
                        firmwareStatusInfoMap.put(fubtHandler.getThing().getUID(), previousFirmwareStatusInfo);
                    }
                }
            }
        });
    }

    private void validateFirmwareUpdateConditions(Firmware firmware, FirmwareUpdateHandler firmwareUpdateHandler) {
        if (!firmwareUpdateHandler.isUpdateExecutable()) {
            throw new IllegalStateException(String.format("The firmware update of thing with UID %s is not executable.",
                    firmwareUpdateHandler.getThing().getUID()));
        }
        validateFirmwareSuitability(firmware, firmwareUpdateHandler);
    }

    private void validateFirmwareSuitability(Firmware firmware, FirmwareUpdateHandler firmwareUpdateHandler) {
        Thing thing = firmwareUpdateHandler.getThing();
        if (!firmware.getUID().getThingTypeUID().equals(thing.getThingTypeUID())) {
            throw new IllegalArgumentException(String.format(
                    "Firmware with UID %s is not suitable for thing with UID %s.", firmware.getUID(), thing.getUID()));
        }

        String firmwareVersion = getThingFirmwareVersion(firmwareUpdateHandler);
        if (firmware.getPrerequisiteVersion() != null && !firmware.isPrerequisteVersion(firmwareVersion)) {
            throw new IllegalArgumentException(String.format(
                    "Firmware with UID %s requires at least firmware version %s to get installed. But the current firmware version of the thing with UID %s is %s.",
                    firmware.getUID(), firmware.getPrerequisiteVersion(), thing.getUID(), firmwareVersion));
        }
    }

    private Firmware getFirmware(FirmwareUID firmwareUID) {
        Firmware firmware = firmwareRegistry.getFirmware(firmwareUID);
        if (firmware == null) {
            throw new IllegalArgumentException(String.format("Firmware with UID %s was not found.", firmwareUID));
        }
        return firmware;
    }

    private FirmwareUpdateHandler getFirmwareUpdateHandler(ThingUID thingUID) {
        for (FirmwareUpdateHandler firmwareUpdateHandler : firmwareUpdateHandlers) {
            if (thingUID.equals(firmwareUpdateHandler.getThing().getUID())) {
                return firmwareUpdateHandler;
            }
        }
        return null;
    }

    private String getThingFirmwareVersion(FirmwareUpdateHandler firmwareUpdateHandler) {
        return firmwareUpdateHandler.getThing().getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION);
    }

    protected void addFirmwareUpdateHandler(FirmwareUpdateHandler firmwareUpdateHandler) {
        firmwareUpdateHandlers.add(firmwareUpdateHandler);
    }

    protected void removeFirmwareUpdateHandler(FirmwareUpdateHandler firmwareUpdateHandler) {
        synchronized (this) {
            firmwareStatusInfoMap.remove(firmwareUpdateHandler.getThing().getUID());
        }
        firmwareUpdateHandlers.remove(firmwareUpdateHandler);
    }

    protected void setFirmwareRegistry(FirmwareRegistry firmwareRegistry) {
        this.firmwareRegistry = firmwareRegistry;
    }

    protected void unsetFirmwareRegistry(FirmwareRegistry firmwareRegistry) {
        this.firmwareRegistry = null;
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    protected void setI18nProvider(I18nProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    protected void unsetI18nProvider(I18nProvider i18nProvider) {
        this.i18nProvider = null;
    }

}
