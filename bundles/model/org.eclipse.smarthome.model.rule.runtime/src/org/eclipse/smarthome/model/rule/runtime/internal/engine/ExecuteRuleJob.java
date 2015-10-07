/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.rule.runtime.internal.engine;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.rule.rules.Rule;
import org.eclipse.smarthome.model.rule.rules.RuleModel;
import org.eclipse.smarthome.model.rule.runtime.internal.RuleRuntimeActivator;
import org.eclipse.smarthome.model.script.engine.Script;
import org.eclipse.smarthome.model.script.engine.ScriptEngine;
import org.eclipse.smarthome.model.script.engine.ScriptExecutionException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Implementation of Quartz {@link Job}-Interface. It takes a rule
 * and simply executes it.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class ExecuteRuleJob implements Job {

    private final Logger logger = LoggerFactory.getLogger(ExecuteRuleJob.class);

    public static final String JOB_DATA_RULEMODEL = "model";
    public static final String JOB_DATA_RULENAME = "rule";

    @Inject
    private Injector injector;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String modelName = (String) context.getJobDetail().getJobDataMap().get(JOB_DATA_RULEMODEL);
        String ruleName = (String) context.getJobDetail().getJobDataMap().get(JOB_DATA_RULENAME);

        ModelRepository modelRepository = RuleRuntimeActivator.modelRepositoryTracker.getService();
        ScriptEngine scriptEngine = RuleRuntimeActivator.scriptEngineTracker.getService();

        if (modelRepository != null && scriptEngine != null) {
            EObject model = modelRepository.getModel(modelName);
            if (model instanceof RuleModel) {
                RuleModel ruleModel = (RuleModel) model;
                Rule rule = getRule(ruleModel, ruleName);
                if (rule != null) {
                    Script script = scriptEngine.newScriptFromXExpression(rule.getScript());
                    logger.debug("Executing scheduled rule '{}'", rule.getName());
                    try {
                        script.execute(RuleContextHelper.getContext(rule, injector));
                    } catch (ScriptExecutionException e) {
                        logger.error("Error during the execution of rule {}", rule.getName(), e.getCause());
                    }
                } else {
                    logger.debug("Scheduled rule '{}' does not exist", ruleName);
                }
            } else {
                logger.debug("Rule file '{}' does not exist", modelName);
            }
        }
    }

    private Rule getRule(RuleModel ruleModel, String ruleName) {
        for (Rule rule : ruleModel.getRules()) {
            if (rule.getName().equals(ruleName)) {
                return rule;
            }
        }
        return null;
    }
}