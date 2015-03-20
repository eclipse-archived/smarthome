/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import static org.junit.Assert.*;

import javax.measure.unit.Dimension;
import javax.measure.unit.SI;

import org.junit.Test;

public class MeasureTypeTest {
	@Test
	public void testUnits() {
		
		// Check some splits
		MeasureType dt0 = new MeasureType("2°C");
		dt0 = new MeasureType("3µs");
		
		try {
			dt0 = new MeasureType("12"); // incorrect argument
			fail();
		} catch (Exception e) {
			// That's what we expect.
		}	
		
		MeasureType dt2 = MeasureType.valueOf("2m");
		// Check that the unit has correctly been identified
		assertEquals(dt2.unit.getDimension(), Dimension.LENGTH);
		assertEquals(dt2.unit, SI.METRE);
		assertEquals(dt2.toString(), "2 m");

		MeasureType dt1 = new MeasureType("2cm");
		// Check that the unit has correctly been identified
		assertEquals(dt1.unit.getDimension(), Dimension.LENGTH);
		assertEquals(dt1.unit, SI.CENTIMETRE);
		assertEquals(dt1.toString(), "2 cm");
		
		assertEquals(dt1.value,dt2.value);

		MeasureType dt3 = new MeasureType("200cm");
		assertTrue(dt3.equals(dt2));
		assertEquals(dt3.compareTo(dt2),0);
		
		
		MeasureType dt4 = new MeasureType("2kg");
		assertEquals(dt4.toString(), "2 kg");
		// check that beside the fact that we've got the same value, we don't have the same unit
		assertFalse(dt2.equals(dt4));
		try {
			dt2.compareTo(dt4);
			fail();
		} catch (Exception e) {
			// That's what we expect.
		}	
		
		assertEquals(dt2.toUnit(SI.METER), dt3.toUnit(SI.METER));
		assertEquals(dt2.toUnit(SI.CENTIMETER), dt3.toUnit(SI.CENTIMETER));
		
		
	}

}
