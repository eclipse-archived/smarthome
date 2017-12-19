/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RulePredicates;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testing the prefix functionality.
 *
 * @author Victor Toni - Initial contribution
 */
public class RulePrefixTest {

    private static final String TESTING_PREFIX = "Testing";

    /**
     * Testing Rules without UID / without prefix / empty prefix.
     *
     * @see RulePredicates#PREFIX_SEPARATOR
     *
     */
    @Test
    public void testEmptyPrefix() {
        final Rule rule0 = new Rule(null);
        Assert.assertNotNull("Returned UID is null instead of generated one", rule0.getUID());
        Assert.assertNull("Returned a prefix instead of null", RulePredicates.getPrefix(rule0));

        final String somethingWithoutSeparator = "something_without_separator";
        final Rule rule1 = new Rule(somethingWithoutSeparator);
        Assert.assertEquals("Returned wrong UID", somethingWithoutSeparator, rule1.getUID());
        Assert.assertNull("Returned a prefix instead of null", RulePredicates.getPrefix(rule1));

        final String withSeparatorButEmpty = RulePredicates.PREFIX_SEPARATOR + "with_separator_but_empty";
        final Rule rule2 = new Rule(withSeparatorButEmpty);
        Assert.assertEquals("Returned wrong UID", withSeparatorButEmpty, rule2.getUID());
        Assert.assertNull("Returned a prefix instead of null", RulePredicates.getPrefix(rule2));
    }

    /**
     * Testing Rules with manually created prefix / empty parts after the separator / multiple separators.
     *
     * @see RulePredicates#PREFIX_SEPARATOR
     *
     */
    @Test
    public void testManualPrefix() {
        final String testingPrefixPrefix = TESTING_PREFIX + RulePredicates.PREFIX_SEPARATOR;

        final String someName = "someName";
        final Rule rule0 = new Rule(testingPrefixPrefix + someName);
        Assert.assertEquals("Returned wrong prefix", TESTING_PREFIX, RulePredicates.getPrefix(rule0));
        Assert.assertEquals("Returned wrong UID", testingPrefixPrefix + someName, rule0.getUID());

        final String multipleSeparatorName = RulePredicates.PREFIX_SEPARATOR + "nameBetweenSeparator"
                + RulePredicates.PREFIX_SEPARATOR;
        final Rule rule1 = new Rule(testingPrefixPrefix + multipleSeparatorName);
        Assert.assertEquals("Returned wrong prefix", TESTING_PREFIX, RulePredicates.getPrefix(rule1));
        Assert.assertEquals("Returned wrong UID", testingPrefixPrefix + someName, rule0.getUID());

        final String emptyName = "";
        final Rule rule2 = new Rule(testingPrefixPrefix + emptyName);
        Assert.assertEquals("Returned wrong prefix", TESTING_PREFIX, RulePredicates.getPrefix(rule2));
        Assert.assertEquals("Returned wrong UID", testingPrefixPrefix + emptyName, rule2.getUID());
    }
}
