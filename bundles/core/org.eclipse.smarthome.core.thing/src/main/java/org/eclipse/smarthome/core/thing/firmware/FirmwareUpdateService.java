/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware;

import static org.eclipse.smarthome.core.thing.firmware.FirmwareStatusInfo.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.smarthome.config.core.validation.ConfigDescriptionValidator;
import org.eclipse.smarthome.config.core.validation.ConfigValidationException;
import org.eclipse.smarthome.core.common.SafeMethodCaller;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.firmware.Firmware;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUID;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUpdateBackgroundTransferHandler;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUpdateHandler;
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressCallback;
import org.eclipse.smarthome.core.thing.events.ThingStatusInfoChangedEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * The firmware update service is registered as an OSGi service and is responsible for tracking all available
 * {@link FirmwareUpdateHandler}s. It provides access to the current {@link FirmwareStatusInfo} of a thing and is the
 * central instance to start a firmware update.
 *
 * @author Thomas Höfer - Initial contribution
 */
@Component(immediate = true, service = { EventSubscriber.class, FirmwareUpdateService.class })
public final class FirmwareUpdateService implements EventSubscriber {

    private static final String THREAD_POOL_NAME = FirmwareUpdateService.class.getSimpleName();
    private static final Set<String> SUPPORTED_TIME_UNITS = ImmutableSet.of(TimeUnit.SECONDS.name(),
            TimeUnit.MINUTES.name(), TimeUnit.HOURS.name(), TimeUnit.DAYS.name());
    private static final String PERIOD_CONFIG_KEY = "period";
    private static final String DELAY_CONFIG_KEY = "delay";
    private static final String TIME_UNIT_CONFIG_KEY = "timeUnit";
    private static final String CONFIG_DESC_URI_KEY = "system:firmware-status-info-job";

    private final Logger logger = LoggerFactory.getLogger(FirmwareUpdateService.class);

    private int firmwareStatusInfoJobPeriod = 3600;
    private int firmwareStatusInfoJobDelay = 3600;
    private TimeUnit firmwareStatusInfoJobTimeUnit = TimeUnit.SECONDS;

    private ScheduledFuture<?> firmwareStatusInfoJob;

    private int timeout = 30 * 60 * 1000;

    private final Set<String> subscribedEventTypes = ImmutableSet.of(ThingStatusInfoChangedEvent.TYPE);

    private final Map<ThingUID, FirmwareStatusInfo> firmwareStatusInfoMap = new ConcurrentHashMap<>();
    private final Map<ThingUID, ProgressCallbackImpl> progressCallbackMap = new ConcurrentHashMap<>();

    private final List<FirmwareUpdateHandler> firmwareUpdateHandlers = new CopyOnWriteArrayList<>();
    private FirmwareRegistry firmwareRegistry;
    private EventPublisher eventPublisher;
    private TranslationProvider i18nProvider;
    private LocaleProvider localeProvider;

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

    @Activate
    protected void activate(Map<String, Object> config) {
        modified(config);
    }

    @Modified
    protected synchronized void modified(Map<String, Object> config) {
        logger.debug("Modifying the configuration of the firmware update service.");

        if (!isValid(config)) {
            return;
        }

        cancelFirmwareUpdateStatusInfoJob();

        firmwareStatusInfoJobPeriod = config.containsKey(PERIOD_CONFIG_KEY) ? (Integer) config.get(PERIOD_CONFIG_KEY)
                : firmwareStatusInfoJobPeriod;
        firmwareStatusInfoJobDelay = config.containsKey(DELAY_CONFIG_KEY) ? (Integer) config.get(DELAY_CONFIG_KEY)
                : firmwareStatusInfoJobDelay;
        firmwareStatusInfoJobTimeUnit = config.containsKey(TIME_UNIT_CONFIG_KEY)
                ? TimeUnit.valueOf((String) config.get(TIME_UNIT_CONFIG_KEY))
                : firmwareStatusInfoJobTimeUnit;

        if (!firmwareUpdateHandlers.isEmpty()) {
            createFirmwareUpdateStatusInfoJob();
        }
    }

    @Deactivate
    protected void deactivate() {
        cancelFirmwareUpdateStatusInfoJob();
        firmwareStatusInfoMap.clear();
        progressCallbackMap.clear();
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
            logger.trace("No firmware update handler available for thing with UID {}.", thingUID);
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
     *
     * @param thingUID the thing UID (must not be null)
     * @param firmwareUID the UID of the firmware to be updated (must not be null)
     * @param locale the locale to be used to internationalize error messages (if null then the locale provided by the
     *            {@link LocaleProvider} is used)
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
            throw new IllegalArgumentException(
                    String.format("There is no firmware update handler for thing with UID %s.", thingUID));
        }

        final Firmware firmware = getFirmware(firmwareUID);

        validateFirmwareUpdateConditions(firmware, firmwareUpdateHandler);

        final Locale loc = locale != null ? locale : localeProvider.getLocale();

        final ProgressCallbackImpl progressCallback = new ProgressCallbackImpl(firmwareUpdateHandler, eventPublisher,
                i18nProvider, thingUID, firmwareUID, loc);
        progressCallbackMap.put(thingUID, progressCallback);

        logger.debug("Starting firmware update for thing with UID {} and firmware with UID {}", thingUID, firmwareUID);

        getPool().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                        @Override
                        public Void call() {
                            firmwareUpdateHandler.updateFirmware(firmware, progressCallback);
                            return null;
                        }
                    }, timeout);
                } catch (ExecutionException e) {
                    logger.error(
                            "Unexpected exception occurred for firmware update of thing with UID {} and firmware with UID {}.",
                            thingUID, firmwareUID, e.getCause());
                    progressCallback.failedInternal("unexpected-handler-error");
                } catch (TimeoutException e) {
                    logger.error("Timeout occurred for firmware update of thing with UID {} and firmware with UID {}.",
                            thingUID, firmwareUID, e);
                    progressCallback.failedInternal("timeout-error");
                }
            }
        });
    }

    /**
     * Cancels the firmware update of the thing having the given thing UID by invoking the operation
     * {@link FirmwareUpdateHandler#cancel()} of the thing´s firmware update handler.
     *
     * @param thingUID the thing UID (must not be null)
     */
    public void cancelFirmwareUpdate(final ThingUID thingUID) {
        Preconditions.checkNotNull(thingUID, "Thing UID must not be null.");
        final FirmwareUpdateHandler firmwareUpdateHandler = getFirmwareUpdateHandler(thingUID);
        if (firmwareUpdateHandler == null) {
            throw new IllegalArgumentException(
                    String.format("There is no firmware update handler for thing with UID %s.", thingUID));
        }
        final ProgressCallbackImpl progressCallback = getProgressCallback(thingUID);
        getPool().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                        @Override
                        public Void call() {
                            logger.debug("Canceling firmware update for thing with UID {}.", thingUID);
                            firmwareUpdateHandler.cancel();
                            return null;
                        }
                    });
                } catch (ExecutionException e) {
                    logger.error("Unexpected exception occurred while canceling firmware update of thing with UID {}.",
                            thingUID, e.getCause());
                    progressCallback.failedInternal("unexpected-handler-error-during-cancel");
                } catch (TimeoutException e) {
                    logger.error("Timeout occurred while canceling firmware update of thing with UID {}.", thingUID, e);
                    progressCallback.failedInternal("timeout-error-during-cancel");
                }
            }
        });
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return subscribedEventTypes;
    }

    @Override
    public EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
        if (event instanceof ThingStatusInfoChangedEvent) {
            ThingStatusInfoChangedEvent changedEvent = (ThingStatusInfoChangedEvent) event;
            if (changedEvent.getStatusInfo().getStatus() != ThingStatus.ONLINE) {
                return;
            }

            ThingUID thingUID = changedEvent.getThingUID();
            FirmwareUpdateHandler firmwareUpdateHandler = getFirmwareUpdateHandler(thingUID);
            if (firmwareUpdateHandler != null && !firmwareStatusInfoMap.containsKey(thingUID)) {
                initializeFirmwareStatus(firmwareUpdateHandler);
            }
        }
    }

    private ProgressCallbackImpl getProgressCallback(ThingUID thingUID) {
        if (!progressCallbackMap.containsKey(thingUID)) {
            throw new IllegalStateException(
                    String.format("No ProgressCallback available for thing with UID %s.", thingUID));
        }
        return progressCallbackMap.get(thingUID);
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
            }
            return createUpdateAvailableInfo();
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
        getPool().submit(new Runnable() {
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
        if (firmware.getPrerequisiteVersion() != null && !firmware.isPrerequisiteVersion(firmwareVersion)) {
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

    private void createFirmwareUpdateStatusInfoJob() {
        if (firmwareStatusInfoJob == null || firmwareStatusInfoJob.isCancelled()) {
            logger.debug("Creating firmware status info job. [delay:{}, period:{}, time unit: {}]",
                    firmwareStatusInfoJobDelay, firmwareStatusInfoJobPeriod, firmwareStatusInfoJobTimeUnit);

            firmwareStatusInfoJob = getPool().scheduleAtFixedRate(firmwareStatusRunnable, firmwareStatusInfoJobDelay,
                    firmwareStatusInfoJobPeriod, firmwareStatusInfoJobTimeUnit);
        }
    }

    private void cancelFirmwareUpdateStatusInfoJob() {
        if (firmwareStatusInfoJob != null && !firmwareStatusInfoJob.isCancelled()) {
            logger.debug("Cancelling firmware status info job.");
            firmwareStatusInfoJob.cancel(true);
            firmwareStatusInfoJob = null;
        }
    }

    private boolean isValid(Map<String, Object> config) {
        // the config description validator does not support option value validation at the moment; so we will validate
        // the time unit here
        if (!SUPPORTED_TIME_UNITS.contains(config.get(TIME_UNIT_CONFIG_KEY))) {
            logger.debug("Given time unit {} is not supported. Will keep current configuration.",
                    config.get(TIME_UNIT_CONFIG_KEY));
            return false;
        }

        try {
            ConfigDescriptionValidator.validate(config, new URI(CONFIG_DESC_URI_KEY));
        } catch (URISyntaxException | ConfigValidationException e) {
            logger.debug("Validation of new configuration values failed. Will keep current configuration.", e);
            return false;
        }

        return true;
    }

    private void initializeFirmwareStatus(final FirmwareUpdateHandler firmwareUpdateHandler) {
        getPool().submit(new Runnable() {
            @Override
            public void run() {
                ThingUID thingUID = firmwareUpdateHandler.getThing().getUID();
                FirmwareStatusInfo info = getFirmwareStatusInfo(thingUID);
                logger.debug("Firmware status {} for thing {} initialized.", info.getFirmwareStatus(), thingUID);
                firmwareStatusInfoMap.put(thingUID, info);
            }
        });
    }

    private static ScheduledExecutorService getPool() {
        return ThreadPoolManager.getScheduledPool(THREAD_POOL_NAME);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected synchronized void addFirmwareUpdateHandler(FirmwareUpdateHandler firmwareUpdateHandler) {
        if (firmwareUpdateHandlers.isEmpty()) {
            createFirmwareUpdateStatusInfoJob();
        }
        firmwareUpdateHandlers.add(firmwareUpdateHandler);
    }

    protected synchronized void removeFirmwareUpdateHandler(FirmwareUpdateHandler firmwareUpdateHandler) {
        firmwareStatusInfoMap.remove(firmwareUpdateHandler.getThing().getUID());
        firmwareUpdateHandlers.remove(firmwareUpdateHandler);
        if (firmwareUpdateHandlers.isEmpty()) {
            cancelFirmwareUpdateStatusInfoJob();
        }
        progressCallbackMap.remove(firmwareUpdateHandler.getThing().getUID());
    }

    @Reference
    protected void setFirmwareRegistry(FirmwareRegistry firmwareRegistry) {
        this.firmwareRegistry = firmwareRegistry;
    }

    protected void unsetFirmwareRegistry(FirmwareRegistry firmwareRegistry) {
        this.firmwareRegistry = null;
    }

    @Reference
    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    @Reference
    protected void setTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    protected void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = null;
    }

    @Reference
    protected void setLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

}
