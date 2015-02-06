/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing;

import org.eclipse.smarthome.model.thing.valueconverter.ThingValueConverters;
import org.eclipse.xtext.conversion.IValueConverterService;

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
@SuppressWarnings("restriction")
public class ThingRuntimeModule extends org.eclipse.smarthome.model.thing.AbstractThingRuntimeModule {

    @Override
    public Class<? extends IValueConverterService> bindIValueConverterService() {
        return ThingValueConverters.class;
    }

    @Override
    public Class<? extends org.eclipse.xtext.serializer.sequencer.ISyntacticSequencer> bindISyntacticSequencer() {
        return org.eclipse.smarthome.model.thing.serializer.ThingSyntacticSequencerExtension.class;
    }
}
