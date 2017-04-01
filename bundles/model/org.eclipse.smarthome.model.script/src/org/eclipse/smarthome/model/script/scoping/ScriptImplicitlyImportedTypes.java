/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.scoping;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.model.persistence.extensions.PersistenceExtensions;
import org.eclipse.smarthome.model.script.actions.Audio;
import org.eclipse.smarthome.model.script.actions.BusEvent;
import org.eclipse.smarthome.model.script.actions.Exec;
import org.eclipse.smarthome.model.script.actions.HTTP;
import org.eclipse.smarthome.model.script.actions.LogAction;
import org.eclipse.smarthome.model.script.actions.Ping;
import org.eclipse.smarthome.model.script.actions.ScriptExecution;
import org.eclipse.smarthome.model.script.actions.ThingAction;
import org.eclipse.smarthome.model.script.actions.Voice;
import org.eclipse.smarthome.model.script.engine.IActionServiceProvider;
import org.eclipse.smarthome.model.script.engine.action.ActionService;
import org.eclipse.smarthome.model.script.lib.NumberExtensions;
import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedFeatures;
import org.joda.time.DateTime;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This class registers all statically available functions as well as the
 * extensions for specific jvm types, which should only be available in rules,
 * but not in scripts
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Oliver Libutzki - Xtext 2.5.0 migration
 *
 */

@SuppressWarnings("restriction")
@Singleton
public class ScriptImplicitlyImportedTypes extends ImplicitlyImportedFeatures {

    private List<Class<?>> actionClasses = null;

    @Inject
    IActionServiceProvider actionServiceProvider;

    @Override
    protected List<Class<?>> getExtensionClasses() {
        List<Class<?>> result = super.getExtensionClasses();
        result.remove(Comparable.class);
        result.remove(Double.class);
        result.remove(Integer.class);
        result.remove(BigInteger.class);
        result.remove(BigDecimal.class);
        result.remove(double.class);
        result.add(NumberExtensions.class);
        result.add(StringUtils.class);
        result.add(URLEncoder.class);
        result.add(PersistenceExtensions.class);
        result.add(BusEvent.class);
        result.add(Exec.class);
        result.add(HTTP.class);
        result.add(Ping.class);
        result.add(Audio.class);
        result.add(Voice.class);
        result.add(ThingAction.class);
        return result;
    }

    @Override
    protected List<Class<?>> getStaticImportClasses() {
        List<Class<?>> result = super.getStaticImportClasses();
        result.add(BusEvent.class);
        result.add(Exec.class);
        result.add(HTTP.class);
        result.add(Ping.class);
        result.add(ScriptExecution.class);
        result.add(LogAction.class);
        result.add(Audio.class);
        result.add(Voice.class);
        result.add(ThingAction.class);

        // jodatime static functions
        result.add(DateTime.class);

        result.addAll(getActionClasses());
        return result;
    }

    protected List<Class<?>> getActionClasses() {

        List<ActionService> services = actionServiceProvider.get();
        if (services != null) {
            List<Class<?>> localActionClasses = new ArrayList<Class<?>>();
            for (ActionService actionService : services) {
                localActionClasses.add(actionService.getActionClass());
            }
            actionClasses = localActionClasses;
        }
        return actionClasses;
    }
}