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
package org.eclipse.smarthome.automation.core.internal;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.core.util.ModuleBuilder;
import org.eclipse.smarthome.automation.core.util.ReferenceResolver;
import org.eclipse.smarthome.config.core.Configuration;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceResolverUtilTest {
    private static final String CONTEXT_PROPERTY1 = "contextProperty1";
    private static final String CONTEXT_PROPERTY2 = "contextProperty2";
    private static final String CONTEXT_PROPERTY3 = "contextProperty3";
    private static final String CONTEXT_PROPERTY4 = "contextProperty4";

    private static final Map<String, Object> context = new HashMap<String, Object>();
    private static final Map<String, Object> moduleConfiguration = new HashMap<String, Object>();
    private static final Map<String, Object> expectedModuleConfiguration = new HashMap<String, Object>();
    private static final Map<String, String> compositeChildModuleInputsReferences = new HashMap<String, String>();
    private static final Map<String, Object> expectedCompositeChildModuleContext = new HashMap<String, Object>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    static {
        // context from where references will be taken
        context.put(CONTEXT_PROPERTY1, "value1");
        context.put(CONTEXT_PROPERTY2, "value2");
        context.put(CONTEXT_PROPERTY3, "value3");
        context.put(CONTEXT_PROPERTY4, new BigDecimal(12345));

        // module configuration with references
        moduleConfiguration.put("simpleReference", String.format("${%s}", CONTEXT_PROPERTY4));
        moduleConfiguration.put("complexReference",
                String.format("Hello ${%s} ${%s}", CONTEXT_PROPERTY1, CONTEXT_PROPERTY4));
        moduleConfiguration.put("complexReferenceWithMissing",
                String.format("Testing ${UNKNOWN}, ${%s}", CONTEXT_PROPERTY4));
        moduleConfiguration.put("complexReferenceArray",
                String.format("[${%s}, ${%s}, staticText]", CONTEXT_PROPERTY2, CONTEXT_PROPERTY3));
        moduleConfiguration.put("complexReferenceArrayWithMissing",
                String.format("[${UNKNOWN}, ${%s}, staticText]", CONTEXT_PROPERTY3));
        moduleConfiguration.put("complexReferenceObj",
                String.format("{key1: ${%s}, key2: staticText, key3: ${%s}}", CONTEXT_PROPERTY1, CONTEXT_PROPERTY4));
        moduleConfiguration.put("complexReferenceObjWithMissing",
                String.format("{key1: ${UNKNOWN}, key2: ${%s}, key3: ${UNKNOWN2}}", CONTEXT_PROPERTY2));

        // expected resolved module configuration
        expectedModuleConfiguration.put("simpleReference", context.get(CONTEXT_PROPERTY4));
        expectedModuleConfiguration.put("complexReference",
                String.format("Hello %s %s", context.get(CONTEXT_PROPERTY1), context.get(CONTEXT_PROPERTY4)));
        expectedModuleConfiguration.put("complexReferenceWithMissing",
                String.format("Testing ${UNKNOWN}, %s", context.get(CONTEXT_PROPERTY4)));
        expectedModuleConfiguration.put("complexReferenceArray",
                String.format("[%s, %s, staticText]", context.get(CONTEXT_PROPERTY2), context.get(CONTEXT_PROPERTY3)));
        expectedModuleConfiguration.put("complexReferenceArrayWithMissing",
                String.format("[${UNKNOWN}, %s, staticText]", context.get(CONTEXT_PROPERTY3)));
        expectedModuleConfiguration.put("complexReferenceObj", String.format("{key1: %s, key2: staticText, key3: %s}",
                context.get(CONTEXT_PROPERTY1), context.get(CONTEXT_PROPERTY4)));
        expectedModuleConfiguration.put("complexReferenceObjWithMissing",
                String.format("{key1: ${UNKNOWN}, key2: %s, key3: ${UNKNOWN2}}", context.get(CONTEXT_PROPERTY2)));

        // composite child module input with references
        compositeChildModuleInputsReferences.put("moduleInput", String.format("${%s}", CONTEXT_PROPERTY1));
        compositeChildModuleInputsReferences.put("moduleInputMissing", "${UNKNOWN}");
        compositeChildModuleInputsReferences.put("moduleInput2", String.format("${%s}", CONTEXT_PROPERTY2));
        // expected resolved child module context
        expectedCompositeChildModuleContext.put("moduleInput", context.get(CONTEXT_PROPERTY1));
        expectedCompositeChildModuleContext.put("moduleInputMissing", context.get("UNKNOWN"));
        expectedCompositeChildModuleContext.put("moduleInput2", context.get(CONTEXT_PROPERTY2));
    }

    Logger log = LoggerFactory.getLogger(ReferenceResolverUtilTest.class);

    @Test
    public void testModuleConfigurationResolving() {
        // test trigger configuration.
        Module trigger = ModuleBuilder.createTrigger().withConfiguration(new Configuration(moduleConfiguration))
                .build();
        ReferenceResolver.updateConfiguration(trigger.getConfiguration(), context, logger);
        Assert.assertEquals(trigger.getConfiguration(), new Configuration(expectedModuleConfiguration));
        // test condition configuration.
        Module condition = ModuleBuilder.createCondition().withConfiguration(new Configuration(moduleConfiguration))
                .build();
        ReferenceResolver.updateConfiguration(condition.getConfiguration(), context, logger);
        Assert.assertEquals(condition.getConfiguration(), new Configuration(expectedModuleConfiguration));
        // test action configuration.
        Module action = ModuleBuilder.createAction().withConfiguration(new Configuration(moduleConfiguration)).build();
        ReferenceResolver.updateConfiguration(action.getConfiguration(), context, logger);
        Assert.assertEquals(action.getConfiguration(), new Configuration(expectedModuleConfiguration));
    }

    @Test
    public void testModuleInputResolving() {
        // test Composite child ModuleImpl(condition) context
        Module condition = ModuleBuilder.createCondition().withInputs(compositeChildModuleInputsReferences).build();
        Map<String, Object> conditionContext = ReferenceResolver.getCompositeChildContext(condition, context);
        Assert.assertEquals(conditionContext, expectedCompositeChildModuleContext);
        // test Composite child ModuleImpl(action) context
        Module action = ModuleBuilder.createAction().withInputs(compositeChildModuleInputsReferences).build();
        Map<String, Object> actionContext = ReferenceResolver.getCompositeChildContext(action, context);
        Assert.assertEquals(actionContext, expectedCompositeChildModuleContext);
    }

    @Test
    public void testBeanMapAccess() {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        map2.put("b", "bValue");
        map1.put("a", map2);
        B1 bean1 = new B1();
        map.put("result_1", bean1);
        map1.put("a.b", "a.bValue");
        map.put("result_2", map1);

        // test getValue from map
        Assert.assertEquals("bValue", ReferenceResolver.resolveComplexDataReference(map, "[result_1].a[b]"));
        Assert.assertEquals("a.bValue", ReferenceResolver.resolveComplexDataReference(map, "[result_2][a.b]"));
        Assert.assertEquals("bValue", ReferenceResolver.resolveComplexDataReference(map, "[result_2][a][b]"));
        Assert.assertEquals("fValue", ReferenceResolver.resolveComplexDataReference(map, "[result_1].bean2.e[f]"));

        // test getValue from bean
        Assert.assertEquals("bValue", ReferenceResolver.resolveComplexDataReference(bean1, "a[b]"));
        Assert.assertEquals("bValue", ReferenceResolver.resolveComplexDataReference(bean1, ".a[b]"));
        Assert.assertEquals("fValue", ReferenceResolver.resolveComplexDataReference(bean1, "bean2.e[f]"));
        Assert.assertEquals("fValue", ReferenceResolver.resolveComplexDataReference(bean1, ".bean2.e[f]"));
    }

    public class B1 {
        public Map<String, Object> getA() {
            Map<String, Object> map2 = new HashMap<>();
            map2.put("b", "bValue");
            return map2;
        }

        public B2 getBean2() {
            return new B2();
        }
    }

    public class B2 {
        public Map<String, Object> getE() {
            Map<String, Object> map2 = new HashMap<>();
            map2.put("f", "fValue");
            return map2;
        }
    }

}
