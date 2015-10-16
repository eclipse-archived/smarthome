/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.core.provider.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.i18n.I18nUtil;
import org.osgi.framework.Bundle;

/**
 * This class is used as utility for resolving the localized {@link Module}s. It automatically infers the key if the
 * default text is not a constant with the assistance of {@link I18nProvider}.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class ModuleI18nUtil {

    public static <T extends Module> List<T> getLocalizedModules(I18nProvider i18nProvider, List<T> modules,
            Bundle bundle, String uid, String prefix, Locale locale) {
        List<T> lmodules = new ArrayList<T>();
        for (T module : modules) {
            String label = getModuleLabel(i18nProvider, bundle, uid, module.getId(), module.getLabel(), prefix, locale);
            String description = getModuleDescription(i18nProvider, bundle, uid, prefix, module.getId(),
                    module.getDescription(), locale);
            lmodules.add(createLocalizedModule(module, label, description));
        }
        return lmodules;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Module> T createLocalizedModule(T module, String label, String description) {
        if (module instanceof Action) {
            return (T) createLocalizedAction((Action) module, label, description);
        }
        if (module instanceof Condition) {
            return (T) createLocalizedCondition((Condition) module, label, description);
        }
        if (module instanceof Trigger) {
            return (T) createLocalizedTrigger((Trigger) module, label, description);
        }
        return null;
    }

    private static Trigger createLocalizedTrigger(Trigger module, String label, String description) {
        Trigger trigger = new Trigger(module.getId(), module.getTypeUID(), module.getConfiguration());
        trigger.setLabel(label);
        trigger.setDescription(description);
        return trigger;
    }

    private static Condition createLocalizedCondition(Condition module, String label, String description) {
        Condition condition = new Condition(module.getId(), module.getTypeUID(), module.getConfiguration(),
                module.getInputs());
        condition.setLabel(label);
        condition.setDescription(description);
        return condition;
    }

    private static Action createLocalizedAction(Action module, String label, String description) {
        Action action = new Action(module.getId(), module.getTypeUID(), module.getConfiguration(), module.getInputs());
        action.setLabel(label);
        action.setDescription(description);
        return action;
    }

    private static String getModuleLabel(I18nProvider i18nProvider, Bundle bundle, String uid, String moduleName,
            String defaultLabel, String prefix, Locale locale) {
        String key = I18nUtil.isConstant(defaultLabel) ? I18nUtil.stripConstant(defaultLabel)
                : inferModuleKey(prefix, uid, moduleName, "label");
        return i18nProvider.getText(bundle, key, defaultLabel, locale);
    }

    private static String getModuleDescription(I18nProvider i18nProvider, Bundle bundle, String uid, String moduleName,
            String defaultDescription, String prefix, Locale locale) {
        String key = I18nUtil.isConstant(defaultDescription) ? I18nUtil.stripConstant(defaultDescription)
                : inferModuleKey(prefix, uid, moduleName, "description");
        return i18nProvider.getText(bundle, key, defaultDescription, locale);
    }

    private static String inferModuleKey(String prefix, String uid, String moduleName, String lastSegment) {
        return prefix + uid + ".input." + moduleName + "." + lastSegment;
    }
}
