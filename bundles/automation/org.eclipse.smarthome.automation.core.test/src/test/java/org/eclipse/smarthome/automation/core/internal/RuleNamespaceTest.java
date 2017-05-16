/**
 * Copyright (c) 2017 by Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.core.util.RulePredicates;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testing the namespace functionality.
 *
 * @author Victor Toni - Initial contribution
 */
public class RuleNamespaceTest {

    private static final String TESTING_NAMESPACE = "Testing";

    /**
     * Testing Rules without UID / without prefix / empty prefix.
     *
     * @see RulePredicates#PREFIX_SEPARATOR
     *
     */
    @Test
    public void testEmptyNamespace() {
        final Rule rule0 = new Rule();
        Assert.assertNull("Returned an UID instead of null", rule0.getUID());
        Assert.assertNull("Returned a namespace instead of null", RulePredicates.getNamespace(rule0));

        final String somethingWithoutSeparator = "something_without_separator";
        final Rule rule1 = new Rule(somethingWithoutSeparator);
        Assert.assertEquals("Returned wrong UID", somethingWithoutSeparator, rule1.getUID());
        Assert.assertNull("Returned a namespace instead of null", RulePredicates.getNamespace(rule1));

        final String withSeparatorButEmpty = RulePredicates.PREFIX_SEPARATOR + "with_separator_but_empty";
        final Rule rule2 = new Rule(withSeparatorButEmpty);
        Assert.assertEquals("Returned wrong UID", withSeparatorButEmpty, rule2.getUID());
        Assert.assertNull("Returned a namespace instead of null", RulePredicates.getNamespace(rule2));
    }

    /**
     * Testing Rules with manually created namespace / empty parts after the separator / multiple separators.
     *
     * @see RulePredicates#PREFIX_SEPARATOR
     *
     */
    @Test
    public void testManualNamespace() {
        final String testingNamespacePrefix = TESTING_NAMESPACE + RulePredicates.PREFIX_SEPARATOR;

        final String someName = "someName";
        final Rule rule0 = new Rule(testingNamespacePrefix + someName);
        Assert.assertEquals("Returned wrong namespace", TESTING_NAMESPACE, RulePredicates.getNamespace(rule0));
        Assert.assertEquals("Returned wrong UID", testingNamespacePrefix + someName, rule0.getUID());

        final String multipleSeparatorName =  RulePredicates.PREFIX_SEPARATOR + "nameBetweenSeparator" + RulePredicates.PREFIX_SEPARATOR;
        final Rule rule1 = new Rule(testingNamespacePrefix + multipleSeparatorName);
        Assert.assertEquals("Returned wrong namespace", TESTING_NAMESPACE, RulePredicates.getNamespace(rule1));
        Assert.assertEquals("Returned wrong UID", testingNamespacePrefix + someName, rule0.getUID());

        final String emptyName =  "";
        final Rule rule2 = new Rule(testingNamespacePrefix + emptyName);
        Assert.assertEquals("Returned wrong namespace", TESTING_NAMESPACE, RulePredicates.getNamespace(rule2));
        Assert.assertEquals("Returned wrong UID", testingNamespacePrefix + emptyName, rule2.getUID());
    }
}
