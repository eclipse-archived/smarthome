/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author Thomas.Eichstaedt-Engelen
 */
public class DecimalTypeTest {

	@Test
	public void testEquals() {
		DecimalType dt1 = new DecimalType("142.8");
		DecimalType dt2 = new DecimalType("142.8");
		DecimalType dt3 = new DecimalType("99.7");
		PercentType pt = new PercentType("99.7");

		assertEquals(true, dt1.equals(dt2));
		assertEquals(false, dt1.equals(dt3));
		assertEquals(true, dt3.equals(pt));
		assertEquals(false, dt1.equals(pt));
	}

	@Test
	public void testFormat() {
		DecimalType dt1 = new DecimalType("87");
		assertEquals("87", dt1.format("%d"));

		DecimalType dt2 = new DecimalType("87");
		assertEquals(" 87", dt2.format("%3d"));

		DecimalType dt3 = new DecimalType("87");
		assertEquals("0x57", dt3.format("%#x"));

		DecimalType dt4 = new DecimalType("87.5");
		try {
			dt4.format("%x");
			fail();
		} catch (Exception e) {
			// That's what we expect, because "87.5" cannot be converted to a
			// hex string.
		}
	}
}
