/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.module.timer.handler;

import java.text.ParseException;

import org.eclipse.smarthome.automation.ModuleHandlerCallback;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseTriggerModuleHandler;
import org.eclipse.smarthome.automation.handler.TriggerHandlerCallback;
import org.eclipse.smarthome.automation.module.timer.factory.TimerModuleHandlerFactory;
import org.eclipse.smarthome.core.scheduler.CronExpression;
import org.eclipse.smarthome.core.scheduler.Expression;
import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager;
import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager.ExpressionThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an ModuleHandler implementation for Triggers which trigger the rule
 * based on a cron expression. The cron expression can be set with the
 * configuration.
 *
 * @author Christoph Knauf - Initial Contribution
 * @author Yordan Mihaylov - Remove Quarz lib dependency
 *
 */
public class GenericCronTriggerHandler extends BaseTriggerModuleHandler implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(GenericCronTriggerHandler.class);

    public static final String MODULE_TYPE_ID = "timer.GenericCronTrigger";
    public static final String CALLBACK_CONTEXT_NAME = "CALLBACK";
    public static final String MODULE_CONTEXT_NAME = "MODULE";

    private static final String CFG_CRON_EXPRESSION = "cronExpression";
    private final ExpressionThreadPoolExecutor scheduler;
    private Expression expression;

    public GenericCronTriggerHandler(Trigger module) {
        super(module);
        String cronExpressionString = (String) module.getConfiguration().get(CFG_CRON_EXPRESSION);

        try {
            expression = new CronExpression(cronExpressionString);
        } catch (ParseException e) {
            throw new IllegalArgumentException(
                    "'" + cronExpressionString + "' parameter is not in valid cron expression.", e);
        }
        scheduler = ExpressionThreadPoolManager.getExpressionScheduledPool(TimerModuleHandlerFactory.THREADPOOLNAME);

    }

    @Override
    public synchronized void setCallback(ModuleHandlerCallback callback) {
        super.setCallback(callback);
        scheduleJob();
    }

    private void scheduleJob() {
        scheduler.schedule(this, expression);
        logger.debug("Scheduled cron job '{}' for trigger '{}'.", module.getConfiguration().get(CFG_CRON_EXPRESSION),
                module.getId());
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        if (scheduler.remove(this)) {
            logger.debug("cancelled job for trigger '{}'.", module.getId());
        } else {
            logger.debug("Failed cancelling job for trigger '{}' - maybe it was never scheduled?", module.getId());
        }

    }

    @Override
    public void run() {
        ((TriggerHandlerCallback) callback).triggered(module, null);
    }
}
