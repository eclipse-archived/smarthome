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
package org.eclipse.smarthome.binding.astro.test.cases;

import java.math.BigDecimal;

/**
 * Contains some test data used across different tests
 *
 * @author Svilen Valkanov - Initial implementation
 * @author Christoph Weitkamp - Migrated tests to pure Java
 */
public final class AstroBindingTestsData {

    public static final String TEST_SUN_THING_ID = "testSunThingId";
    public static final String TEST_MOON_THING_ID = "testMoonThingId";
    public static final String TEST_ITEM_NAME = "testItem";
    public static final String DEFAULT_IMEM_TYPE = "DateTime";

    public static final String DEFAULT_TEST_CHANNEL_ID = "rise#start";
    public static final String GEOLOCATION_PROPERTY = "geolocation";
    public static final String GEOLOCATION_VALUE = "51.2,25.4";
    public static final String INTERVAL_PROPERTY = "interval";
    public static final BigDecimal INTERVAL_DEFAULT_VALUE = new BigDecimal(300);
}
