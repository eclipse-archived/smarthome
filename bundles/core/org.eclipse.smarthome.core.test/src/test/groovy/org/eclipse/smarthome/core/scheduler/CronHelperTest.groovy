/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.scheduler

import static org.eclipse.smarthome.core.scheduler.CronExpression.*
import static org.eclipse.smarthome.core.scheduler.CronHelper.*
import static org.junit.Assert.*

import java.time.LocalDateTime

import org.junit.Test

/**
 * Tests for the {@link CronHelper}
 * <p>
 * This class tests the CronHelper methods
 * </p>
 * @author Amit Kumar Mondal - Initial Contribution
 */
class CronHelperTest {

    @Test
    public void 'create CRON expression from specified date'(){
        def date = LocalDateTime.of(2014, 4, 3, 1, 23, 45)
        def cron = createCronFromTemporal(date)
        assertEquals('45 23 1 3 4 ? 2014', cron)
        assertTrue(isValidExpression(cron))
    }

    @Test
    public void 'create CRON expression to repeat every 59 seconds'(){
        def cron = createCronForRepeatEverySeconds(59)
        assertEquals('*/59 * * * * ? *', cron)
        assertTrue(isValidExpression(cron))
    }

    @Test
    public void 'create CRON expression to repeat every 60 seconds'(){
        def cron = createCronForRepeatEverySeconds(60)
        assertEquals('0 */1 * * * ? *', cron)
        assertTrue(isValidExpression(cron))
    }

    @Test
    public void 'create CRON expression to repeat every 65 seconds'(){
        def cron = createCronForRepeatEverySeconds(65)
        assertEquals('5 */1 * * * ? *', cron)
        assertTrue(isValidExpression(cron))
    }

    @Test
    public void 'create CRON expression to repeat every 3599 seconds'(){
        def cron = createCronForRepeatEverySeconds(3599)
        assertEquals('59 */59 * * * ? *', cron)
    }

    @Test
    public void 'create CRON expression to repeat every 3600 seconds'(){
        def cron = createCronForRepeatEverySeconds(3600)
        assertEquals('0 0 */1 * * ? *', cron)
        assertTrue(isValidExpression(cron))
    }

    @Test
    public void 'create CRON expression to repeat every 3845 seconds'(){
        def cron = createCronForRepeatEverySeconds(3845)
        assertEquals('5 4 */1 * * ? *', cron)
        assertTrue(isValidExpression(cron))
    }

    @Test
    public void 'create CRON expression to repeat every 86399 seconds'(){
        def cron = createCronForRepeatEverySeconds(86399)
        assertEquals('59 59 */23 * * ? *', cron)
        assertTrue(isValidExpression(cron))
    }

    @Test
    public void 'create CRON expression to repeat every 86400 seconds'(){
        def cron = createCronForRepeatEverySeconds(86400)
        def calendar = Calendar.getInstance()
        def date = calendar.getTime()
        def minutes = calendar.get(Calendar.MINUTE)
        def hours = calendar.get(Calendar.HOUR_OF_DAY)
        def expected = '0' + ' ' + minutes + ' ' + hours + ' ' +  '* * ? *';
        assertEquals(expected, cron)
        assertTrue(isValidExpression(cron))
    }
}
