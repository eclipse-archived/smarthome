package org.eclipse.smarthome.core.thing.factory

import org.eclipse.smarthome.test.OSGiTest
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*


class SampleOSGITest extends OSGiTest {

	@Test
	void 'sample test' () {
		assertThat true, is(true)
	}
}
