/**
 * Copyright (c) 2016 Deutsche Telekom AG and others.
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

    private final FirmwareUpdateHandler firmwareUpdateHandler;
    private final EventPublisher eventPublisher;
    private final I18nProvider i18nProvider;
    private final ThingUID thingUID;
    private final FirmwareUID firmwareUID;
    private final Locale locale;

    private Collection<ProgressStep> sequence;
    private Iterator<ProgressStep> progressIterator;

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
            ProgressStep next = progressIterator.next();
            post(FirmwareEventFactory.createFirmwareUpdateProgressInfoEvent(
                    new FirmwareUpdateProgressInfo(firmwareUID, next, sequence), thingUID));
        } else {
            throw new IllegalStateException("There is no further progress step to be executed.");
        }
    }

    @Override
    public void failed(String errorMessageKey, Object... arguments) {
        Preconditions.checkArgument(errorMessageKey != null && !errorMessageKey.isEmpty(),
                "The error message key must not be null or empty.");
        String errorMessage = getErrorMessage(FrameworkUtil.getBundle(firmwareUpdateHandler.getClass()),
                errorMessageKey, arguments);
        postError(errorMessage);
    }

    @Override
    public void success() {
        post(FirmwareEventFactory.createFirmwareUpdateResultInfoEvent(
                new FirmwareUpdateResultInfo(FirmwareUpdateResult.SUCCESS, null), thingUID));
    }

    void failedInternal(String errorMessageKey) {
        String errorMessage = getErrorMessage(FrameworkUtil.getBundle(ProgressCallbackImpl.class), errorMessageKey,
                new Object[] {});
        postError(errorMessage);
    }

    private String getErrorMessage(Bundle bundle, String errorMessageKey, Object... arguments) {
        String errorMessage = i18nProvider.getText(bundle, errorMessageKey, null, locale, arguments);
        return errorMessage;
    }

    private void postError(String errorMessage) {
        post(FirmwareEventFactory.createFirmwareUpdateResultInfoEvent(
                new FirmwareUpdateResultInfo(FirmwareUpdateResult.ERROR, errorMessage), thingUID));
    }

    private void post(Event event) {
        eventPublisher.post(event);
    }

}
