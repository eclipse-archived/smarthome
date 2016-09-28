/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUID;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUpdateHandler;
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressCallback;
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressStep;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.base.Preconditions;

/**
 * The callback implementation for the {@link ProgressCallback}.
 *
 * @author Thomas Höfer - Initial contribution
 */
final class ProgressCallbackImpl implements ProgressCallback {

    private static String UPDATE_CANCELED_MESSAGE_KEY = "update-canceled"; 
    
    /**
     * Handler instance is needed to retrieve the error messages from the correct bundle.
     */
    private final FirmwareUpdateHandler firmwareUpdateHandler;
    private final EventPublisher eventPublisher;
    private final I18nProvider i18nProvider;
    private final ThingUID thingUID;
    private final FirmwareUID firmwareUID;
    private final Locale locale;

    private Collection<ProgressStep> sequence;
    private Iterator<ProgressStep> progressIterator;
    private ProgressStep current;
    private boolean finished;

    ProgressCallbackImpl(FirmwareUpdateHandler firmwareUpdateHandler, EventPublisher eventPublisher,
            I18nProvider i18nProvider, ThingUID thingUID, FirmwareUID firmwareUID, Locale locale) {
        this.firmwareUpdateHandler = firmwareUpdateHandler;
        this.eventPublisher = eventPublisher;
        this.i18nProvider = i18nProvider;
        this.thingUID = thingUID;
        this.firmwareUID = firmwareUID;
        this.locale = locale;
    }

    @Override
    public void defineSequence(ProgressStep... sequence) {
        Preconditions.checkArgument(sequence != null && sequence.length > 0, "Sequence must not be null or empty.");
        this.sequence = Collections.unmodifiableCollection(Arrays.asList(sequence));
        progressIterator = this.sequence.iterator();
    }

    @Override
    public void next() {
        Preconditions.checkState(sequence != null, "No sequence defined.");
        if (progressIterator.hasNext()) {
            this.current = progressIterator.next();
            post(FirmwareEventFactory.createFirmwareUpdateProgressInfoEvent(
                    new FirmwareUpdateProgressInfo(firmwareUID, current, sequence, false), thingUID));
        } else {
            throw new IllegalStateException("There is no further progress step to be executed.");
        }
    }

    @Override
    public void failed(String errorMessageKey, Object... arguments) {
        Preconditions.checkState(!finished, "Update is finished.");
        finished = true;
        Preconditions.checkArgument(errorMessageKey != null && !errorMessageKey.isEmpty(),
                "The error message key must not be null or empty.");
        String errorMessage = getErrorMessage(FrameworkUtil.getBundle(firmwareUpdateHandler.getClass()),
                errorMessageKey, arguments);
        postFirmwareUpdateResultInfoEvent(FirmwareUpdateResult.ERROR, errorMessage);
    }

    @Override
    public void success() {
        Preconditions.checkState(!finished, "Update is finished.");
        finished = true;
        postFirmwareUpdateResultInfoEvent(FirmwareUpdateResult.SUCCESS, null);
    }

    @Override
    public void pending() {
        Preconditions.checkState(!finished, "Update is finished.");
        post(FirmwareEventFactory.createFirmwareUpdateProgressInfoEvent(
                new FirmwareUpdateProgressInfo(firmwareUID, getCurrentStep(), sequence, true), thingUID));
    }

    @Override
    public void canceled() {
        Preconditions.checkState(!finished, "Update is finished.");
        finished = true;
        String cancelMessage = getErrorMessage(FrameworkUtil.getBundle(ProgressCallbackImpl.class), UPDATE_CANCELED_MESSAGE_KEY);
        postFirmwareUpdateResultInfoEvent(FirmwareUpdateResult.CANCELED, cancelMessage);
    }

    void failedInternal(String errorMessageKey) {
        finished = true; 
        String errorMessage = getErrorMessage(FrameworkUtil.getBundle(ProgressCallbackImpl.class), errorMessageKey,
                new Object[] {});
        postFirmwareUpdateResultInfoEvent(FirmwareUpdateResult.ERROR, errorMessage);
    }

    private String getErrorMessage(Bundle bundle, String errorMessageKey, Object... arguments) {
        String errorMessage = i18nProvider.getText(bundle, errorMessageKey, null, locale, arguments);
        return errorMessage;
    }

    private void postFirmwareUpdateResultInfoEvent(FirmwareUpdateResult result, String message) {
        post(FirmwareEventFactory.createFirmwareUpdateResultInfoEvent(new FirmwareUpdateResultInfo(result, message),
                thingUID));
    }

    private void post(Event event) {
        eventPublisher.post(event);
    }

    private ProgressStep getCurrentStep() {
        Preconditions.checkState(sequence != null, "No sequence defined.");
        if (current != null) {
            return current;
        }
        Iterator<ProgressStep> tempIter = this.sequence.iterator();
        if (tempIter.hasNext()) {
            return tempIter.next();
        }
        throw new IllegalStateException("There is no progress step defined.");
    }
}
