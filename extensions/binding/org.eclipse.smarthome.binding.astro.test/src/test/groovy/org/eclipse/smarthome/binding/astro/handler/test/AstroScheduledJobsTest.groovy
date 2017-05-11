/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.handler.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.ThingStatus
import org.junit.Test
import org.eclipse.smarthome.binding.astro.AstroBindingConstants
import org.eclipse.smarthome.binding.astro.test.AstroOSGiTest
import org.eclipse.smarthome.binding.astro.test.AstroOSGiTest.AcceptedItemType
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.Trigger
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.matchers.GroupMatcher
import org.apache.commons.lang.StringUtils
/**
 * OSGi tests for the {@link AstroThingHandler}
 * <p>
 * This class tests the scheduling of the jobs.
 *
 * @author Petar Valchev - Initial Implementation
 * @author Svilen Valkanov - Added missing configuration parameters and replaced headers
 *
 */
class AstroScheduledJobsTest extends AstroOSGiTest {
    @Test
    public void 'positional jobs for sun thing are scheduled'(){
        assertPositionalJob(TEST_SUN_THING_ID)
    }

    @Test
    public void 'positional jobs for moon thing are scheduled'(){
        assertPositionalJob(TEST_MOON_THING_ID)
    }

    @Test
    public void 'daily jobs for sun thing are scheduled at midnight'(){
        assertDailyJob(TEST_SUN_THING_ID)
    }

    @Test
    public void 'daily jobs for moon thing are scheduled at midnight'(){
        assertDailyJob(TEST_MOON_THING_ID)
    }

    private assertPositionalJob(String thingID){
        setUpThing(thingID,"position#azimuth", AcceptedItemType.NUMBER)
        JobKey jobKey = constructJobKey("job","positional")

        Date fireTime
        waitForAssert({
            fireTime = getJobFireTime(jobKey)
            assertThat "The job $jobKey was not scheduled",
                    fireTime,
                    is(notNullValue())
        })
    }

    private assertDailyJob(String thingID){
        String id = setUpThing(thingID,DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME)
        JobKey jobKey = constructJobKey("job", "daily", id)


        Date fireTime
        waitForAssert({
            fireTime = getJobFireTime(jobKey)
            assertThat "The job $jobKey was not scheduled in the expected hour",
                    fireTime.getHours(),
                    is(equalTo(0))
            assertThat "The job $jobKey was not scheduled in the expected minute",
                    fireTime.getMinutes(),
                    is(equalTo(0))
            assertThat "The job $jobKey was not scheduled in the expected second",
                    fireTime.getSeconds(),
                    is(equalTo(0))
        })
    }

    private String setUpThing(String thingID, String channelID, AcceptedItemType acceptedItemType) {
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        thingConfiguration.put(INTERVAL_PROPERTY, INTERVAL_DEFAULT_VALUE)

        assertThingStatus(thingID, channelID, acceptedItemType, thingConfiguration, ThingStatus.ONLINE)

        return thingID.equals(TEST_SUN_THING_ID) ? AstroBindingConstants.SUN: AstroBindingConstants.MOON
        String thingType
        switch(thingID) {
            case (TEST_SUN_THING_ID) :
                thingType = AstroBindingConstants.SUN
                break
            case(TEST_MOON_THING_ID) :
                thingType = AstroBindingConstants.MOON
                break
        }
    }

    private JobKey constructJobKey(String ... ids){
        String thingGroup = "$AstroBindingConstants.BINDING_ID:${astroThing.getThingTypeUID().getId()}:${astroThing.getUID().getId()}"
        JobKey jobKey = JobKey.jobKey(StringUtils.join(ids,"-"), thingGroup)
        return jobKey
    }

    private Date getJobFireTime(JobKey jobKey){
        try {
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            List<String> jobGroupNames
            waitForAssert({
                jobGroupNames = scheduler.getJobGroupNames()
                assertThat "Could not get any job groups",
                        jobGroupNames.isEmpty(),
                        is(false)
            })
            for (String groupName : scheduler.getJobGroupNames()) {
                GroupMatcher groupMatcher = GroupMatcher.jobGroupEquals(groupName)
                Set<JobKey> jobKeys
                waitForAssert({
                    jobKeys = scheduler.getJobKeys(groupMatcher)
                    assertThat "The job key $jobKey was not found in the group $groupName",
                            jobKeys.contains(jobKey),
                            is(true)
                })
                for (JobKey entry : jobKeys) {
                    String jobName = entry.getName();
                    String jobGroup = entry.getGroup();
                    List<Trigger> triggers
                    waitForAssert({
                        triggers = (List<Trigger>) scheduler.getTriggersOfJob(entry)
                        assertThat "Could not find a trigger for the job $entry",
                                triggers.isEmpty(),
                                is(false)
                    })
                    Date nextFireTime = triggers.get(0).getNextFireTime();
                    if(jobKey.getName().equals(jobName) && jobKey.getGroup().equals(jobGroup)){
                        return nextFireTime
                    }
                }
            }
        } catch (SchedulerException e) {
            fail("An exception $e was thrown, while trying to get a scheduler")
        }
        return null
    }
}
