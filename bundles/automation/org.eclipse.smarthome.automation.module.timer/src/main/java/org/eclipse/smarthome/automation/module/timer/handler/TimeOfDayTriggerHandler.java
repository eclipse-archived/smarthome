/**
 * Copyright (c) 2017 Kai Kreuzer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.timer.handler;

import java.text.ParseException;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseTriggerModuleHandler;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.module.timer.factory.TimerModuleHandlerFactory;
import org.eclipse.smarthome.core.scheduler.Expression;
import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager;
import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager.ExpressionThreadPoolExecutor;
import org.eclipse.smarthome.core.scheduler.RecurrenceExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an ModuleHandler implementation for Triggers which trigger the rule
 * at a specific time (format 'hh:mm').
 *
 * @author Kai Kreuzer - Initial Contribution
 *
 */
public class TimeOfDayTriggerHandler extends BaseTriggerModuleHandler implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(TimeOfDayTriggerHandler.class);

    public static final String MODULE_TYPE_ID = "timer.TimeOfDayTrigger";
    public static final String MODULE_CONTEXT_NAME = "MODULE";

    private static final String CFG_TIME = "time";

    private final ExpressionThreadPoolExecutor scheduler;
    private final Expression expression;

    public TimeOfDayTriggerHandler(Trigger module) {
        super(module);
        String time = module.getConfiguration().get(CFG_TIME).toString();
        try {
            String[] parts = time.split(":");

            expression = new RecurrenceExpression("FREQ=DAILY;BYHOUR=" + parts[0] + ";BYMINUTE=" + parts[1]);
        } catch (ArrayIndexOutOfBoundsException | ParseException e) {
            throw new IllegalArgumentException("'time' parameter is not in valid format 'hh:mm'.", e);
        }
        scheduler = ExpressionThreadPoolManager.getExpressionScheduledPool(TimerModuleHandlerFactory.THREADPOOLNAME);
    }

    @Override
    public synchronized void setRuleEngineCallback(RuleEngineCallback ruleCallback) {
        super.setRuleEngineCallback(ruleCallback);
        scheduleJob();
    }

    private void scheduleJob() {
        scheduler.schedule(this, expression);
        logger.debug("Scheduled job for trigger '{}' at '{}' each day.", module.getId(),
                module.getConfiguration().get(CFG_TIME));
    }

    @Override
    public void run() {
        ruleEngineCallback.triggered(module, null);
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
}
