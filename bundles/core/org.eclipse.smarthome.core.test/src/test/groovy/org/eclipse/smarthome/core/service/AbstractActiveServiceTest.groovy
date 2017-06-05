/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.service

import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.core.service.AbstractActiveService
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.Ignore

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

/**
 * Test class for the {@link AbstractActiveService}
 * 
 * @author Mihaela Memova
 *
 */
class AbstractActiveServiceTest extends OSGiTest {

	private AbstractActiveServiceMock abstractActiveServiceMock

	// boolean variables which are set in the service's methods and then are used in the test's assertions
	boolean isNewThreadStarted
	boolean isInterruptMethodCalled
	boolean isExecuting
	boolean isExceptionThrown

	@Before
	public void setUp() {
		abstractActiveServiceMock = new AbstractActiveServiceMock()
		registerService(abstractActiveServiceMock, AbstractActiveService.class.getName())
	}

	@After
	void tearDown() {
		abstractActiveServiceMock.deactivate()
		abstractActiveServiceMock= null
		resetTheBooleanVariables()
	}

	private void resetTheBooleanVariables() {
		isNewThreadStarted = false
		isInterruptMethodCalled = false
		isExecuting = false
		isExceptionThrown = false
	}

	@Test
	void 'verify that a service without proper configuration is not started'() {

		abstractActiveServiceMock.setProperlyConfigured(false)
		/* 
		 * When the isProperlyConfigured variable is set to false, the start()
		 * method should not be called
		 */
		waitForAssert {
			assertThat ("The service is running even though it is not properly configured", abstractActiveServiceMock.isRunning(), is(false))
			assertThat ("A new refesh thread was started even though the service is not properly configured", isNewThreadStarted, is(false))
		}
	}

	@Test
	void 'verify that not running service with proper configuration is started'() {

		// Assert that the service is not running at the beginning at the test
		waitForAssert {
			assertThat ("The service is already running", abstractActiveServiceMock.isRunning(), is(false))
			assertThat ("A new refesh thread was already started", isNewThreadStarted, is(false))
		}

		/*
		 * When the isProperlyConfigured variable is set to true and the thread is
		 * not already running, the start() method should be called
		 */
		abstractActiveServiceMock.setProperlyConfigured(true)

		waitForAssert {
			assertThat ("The service is not running even though it is properly configured", abstractActiveServiceMock.isRunning(), is(true))
			assertThat ("A new refesh thread was not started even though the service is properly configured", isNewThreadStarted, is(true))
		}
	}

	@Test
	void 'verify that running service with proper configuration is not started again'() {

		// Assert that the service is not running at the beginning at the test
		waitForAssert {
			assertThat ("The service is already running", abstractActiveServiceMock.isRunning(), is(false))
			assertThat ("A new refesh thread was already started", isNewThreadStarted, is(false))
		}

		/*
		 * When the isProperlyConfigured variable is set to true and the thread is
		 * not already running, the start() method should be called.
		 */
		abstractActiveServiceMock.setProperlyConfigured(true)

		waitForAssert {
			assertThat ("The service is not running even though it is properly configured", abstractActiveServiceMock.isRunning(), is(true))
			assertThat ("A new refesh thread was not started even though the service is properly configured", isNewThreadStarted, is(true))
		}

		//reset the needed variable
		isNewThreadStarted = false

		//trying to activate the service again
		abstractActiveServiceMock.setProperlyConfigured(true)

		//the thread should be still running but the start() method should not be called again
		waitForAssert {
			assertThat ("The service is not running anymore when trying to activate it again", abstractActiveServiceMock.isRunning(), is(true))
			assertThat ("A new refresh Thread was started even though the service was already running", isNewThreadStarted, is(false))
		}
	}

	@Test
	void 'verify that the interruption of a running service is handled properly' () {

		//activate the service
		abstractActiveServiceMock.setProperlyConfigured(true)

		waitForAssert {
			assertThat ("The service is not running even though it is properly configured", abstractActiveServiceMock.isRunning(), is(true))
			assertThat ("A new refesh thread was not started even though the service is properly configured", isNewThreadStarted, is(true))
		}

		//interrupt the refresh Thread
		abstractActiveServiceMock.interrupt()

		waitForAssert {
			assertThat ("The interruption of the refresh Thread was not handled properly", isInterruptMethodCalled, is(true))
		}
	}


	@Test
	void 'verify that a running service is shutdown when the properlyConfgured variable is set to false'() {
		/* 
		 * Activating the service in order to be sure it will be running before
		 * setting the isProperlyConfigured variable to false
		 */
		abstractActiveServiceMock.setProperlyConfigured(true)

		waitForAssert {
			assertThat ("The service is not running even though it is properly configured", abstractActiveServiceMock.isRunning(), is(true))
		}

		abstractActiveServiceMock.setProperlyConfigured(false)

		waitForAssert {
			assertThat ("The refresh Thread is still running even though the setProperlyConfigured(false) method was called", abstractActiveServiceMock.isRunning(), is(false))
			assertThat ("The service was not shut down when the setProperlyConfigured(false) method was called", abstractActiveServiceMock.shutdown, is(true))
		}
	}

	@Test
	void 'verify that a RuntimeException during the thread execution is handled' () {

		// setting the needed boolean variable to true
		isExceptionThrown = true

		abstractActiveServiceMock.setProperlyConfigured(true)

		/*
		 * According to the AbstractActiveService original code, when an exception is thrown, it is
		 * logged but the thread continues its execution
		 */
		waitForAssert {
			assertThat ("After an exception was thrown, the service is not running anymore", abstractActiveServiceMock.isRunning(), is(true))
		}
	}


	private class AbstractActiveServiceMock extends AbstractActiveService {
		@Override
		protected void start() {
			boolean isThreadAlreadyRunning = isRunning()
			super.start()
			boolean isThreadFinallyRunning = isRunning()
			if(!isThreadAlreadyRunning && isThreadFinallyRunning) {
				isNewThreadStarted = true
			}
		}

		@Override
		protected void execute() {
			isExecuting = true
			if(isExceptionThrown) {
				throw new RuntimeException()
			}
		}

		@Override
		protected long getRefreshInterval() {
			// The refresh interval value has no effect on the tests, so 100ms is chosen with no particular reason
			return 100
		}

		@Override
		protected String getName() {
			return "RefreshThread";
		}

		@Override
		public void interrupt() {
			super.interrupt()
			isInterruptMethodCalled = true
		}
	}
}
