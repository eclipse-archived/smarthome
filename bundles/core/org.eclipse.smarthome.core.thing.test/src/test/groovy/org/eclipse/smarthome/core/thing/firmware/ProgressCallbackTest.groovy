/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import java.util.Locale

import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.i18n.TranslationProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.firmware.Firmware
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUID
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUpdateHandler
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressCallback
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressStep
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.osgi.framework.Bundle

/**
 * Testing the {@link ProgressCallback}.
 *
 * @author Christoph Knauf - Initial contribution
 */
public final class ProgressCallbackTest {

    ProgressCallback sut
    List<Event> postedEvents
    ThingUID expectedThingUID
    FirmwareUID expectedFirmwareUID
    String cancelMessageKey = "update-canceled"
    def usedMessagedKey

    @Before
    void setUp(){
        def thingType = new ThingTypeUID("thing:type")
        expectedThingUID = new ThingUID(thingType, "thingid")
        expectedFirmwareUID = new FirmwareUID(thingType, "1")
        postedEvents = new LinkedList<>()
        def publisher = [
            post : { event -> postedEvents.add(event) }
        ] as EventPublisher
        def i18nProvider = [
            getText:  { bundle, key, defaultText, locale, arguments ->
                usedMessagedKey = key
                return "Dummy Message"
            }
        ] as TranslationProvider
        sut = new ProgressCallbackImpl(new DummyFirmwareHandler(),publisher, i18nProvider, expectedThingUID, expectedFirmwareUID, null)
    }

    @Test(expected=IllegalStateException)
    void 'assert that update throws IllegalStateException if update is finished'(){
        sut.defineSequence(ProgressStep.DOWNLOADING)
        sut.next()
        sut.success()
        assertThatUpdateResultEventIsValid(postedEvents.get(1), null, FirmwareUpdateResult.SUCCESS)
        sut.update(100)
    }
    
    @Test(expected=IllegalArgumentException)
    void 'assert that defineSequence throws IllegalArguumentException if sequence is empty'(){
        sut.defineSequence()
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that defineSequence throws IllegalArguumentException if sequence is null'(){
        sut.defineSequence(null)
    }
    
    
    @Test(expected=IllegalArgumentException)
    void 'assert that failed throws IllegalArguumentException if message key is empty'(){
        sut.failed("", null)
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that failed throws IllegalArguumentException if message key is null'(){
        sut.failed(null, null)
    }


    @Test(expected=IllegalStateException)
    void 'assert that success throws IllegalStateException if progress is not at 100 percent'(){
        sut.update(99)
        sut.success()
    }
    
    @Test(expected=IllegalStateException)
    void 'assert that success throws IllegalStateException for failed updates'(){
        sut.failed(cancelMessageKey, null)
        assertThatUpdateResultEventIsValid(postedEvents.get(0), cancelMessageKey, FirmwareUpdateResult.ERROR)
        sut.success()
    }

    @Test(expected=IllegalStateException)
    void 'assert that success throws IllegalStateException if last progress step is not reached'(){
        sut.defineSequence(ProgressStep.DOWNLOADING, ProgressStep.TRANSFERRING)
        sut.next()
        sut.success()
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that update throws IllegalArgumentException if progress smaller 0'(){
        sut.update(-1)
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that update throws IllegalArgumentException if progress greater 100'(){
        sut.update(101)
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that update throws IllegalArgumentException if new progress is smaller than old progress'(){
        sut.update(10)
        sut.update(9)
    }

    @Test
    void 'assert that defining a sequence is optional if percentage progress is used'(){
        sut.update(5)
        assertThatProgressInfoEventIsValid(postedEvents.get(0), null, false, 5)
        sut.update(11)
        assertThatProgressInfoEventIsValid(postedEvents.get(1), null, false, 11)
        sut.update(22)
        assertThatProgressInfoEventIsValid(postedEvents.get(2), null, false, 22)
        sut.update(44)
        assertThatProgressInfoEventIsValid(postedEvents.get(3), null, false, 44)
        sut.pending()
        assertThatProgressInfoEventIsValid(postedEvents.get(4), null, true, 44)
        sut.update(50)
        assertThatProgressInfoEventIsValid(postedEvents.get(5), null, false, 50)
        sut.update(70)
        assertThatProgressInfoEventIsValid(postedEvents.get(6), null, false, 70)
        sut.update(89)
        assertThatProgressInfoEventIsValid(postedEvents.get(7), null, false, 89)
        sut.update(100)
        assertThatProgressInfoEventIsValid(postedEvents.get(8), null, false, 100)
        sut.success()
        assertThatUpdateResultEventIsValid(postedEvents.get(9), null, FirmwareUpdateResult.SUCCESS)
        assertThat postedEvents.size(), is(10)
    }

    @Test
    void 'assert that update results not in FirmwareUpdateProgressInfo event if progress not changed'(){
        sut.defineSequence(ProgressStep.UPDATING)
        sut.update(10)
        sut.update(10)
        assertThatProgressInfoEventIsValid(postedEvents.get(0), ProgressStep.UPDATING, false, 10)
        assertThat postedEvents.size(), is(1)
    }

    @Test
    void 'assert that setting the progess to pending results in a FirmwareUpdateProgressInfo event'(){
        sut.defineSequence(ProgressStep.DOWNLOADING, ProgressStep.TRANSFERRING)
        sut.pending()
        assertThatProgressInfoEventIsValid(postedEvents.get(0), ProgressStep.DOWNLOADING, true, null)
    }

    @Test
    void 'assert that pending does not change ProgressStep'(){
        sut.defineSequence(ProgressStep.DOWNLOADING, ProgressStep.TRANSFERRING)
        sut.pending()
        assertThatProgressInfoEventIsValid(postedEvents.get(0), ProgressStep.DOWNLOADING, true, null)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(1), ProgressStep.DOWNLOADING, false, null)
        sut.pending()
        assertThatProgressInfoEventIsValid(postedEvents.get(2), ProgressStep.DOWNLOADING, true, null)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(3), ProgressStep.DOWNLOADING, false, null)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(4), ProgressStep.TRANSFERRING, false, null)
        sut.pending()
        assertThatProgressInfoEventIsValid(postedEvents.get(5), ProgressStep.TRANSFERRING, true, null)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(6), ProgressStep.TRANSFERRING, false, null)
        sut.success()
        assertThatUpdateResultEventIsValid(postedEvents.get(7), null, FirmwareUpdateResult.SUCCESS)
        assertThat postedEvents.size(), is(8)
    }

    @Test
    void 'assert that next changes ProgressStep if not pending'(){
        sut.defineSequence(ProgressStep.DOWNLOADING, ProgressStep.TRANSFERRING, ProgressStep.UPDATING)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(0), ProgressStep.DOWNLOADING, false, null)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(1), ProgressStep.TRANSFERRING, false, null)
        sut.pending()
        assertThatProgressInfoEventIsValid(postedEvents.get(2), ProgressStep.TRANSFERRING, true, null)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(3), ProgressStep.TRANSFERRING, false, null)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(4), ProgressStep.UPDATING, false, null)
        sut.pending()
        assertThatProgressInfoEventIsValid(postedEvents.get(5), ProgressStep.UPDATING, true, null)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(6), ProgressStep.UPDATING, false, null)
        sut.success()
        assertThatUpdateResultEventIsValid(postedEvents.get(7), null, FirmwareUpdateResult.SUCCESS)
        assertThat postedEvents.size(), is(8)
    }

    @Test(expected=IllegalStateException)
    void 'assert that cancel throws IllegalStateException if update is finished'(){
        sut.defineSequence(ProgressStep.DOWNLOADING, ProgressStep.TRANSFERRING)
        sut.next()
        sut.next()
        sut.success()
        assertThatUpdateResultEventIsValid(postedEvents.get(2), null, FirmwareUpdateResult.SUCCESS)
        sut.canceled()
    }

    @Test
    void 'assert that calling cancel results in a FirmwareUpdateResultInfoEvent'(){
        sut.defineSequence(ProgressStep.DOWNLOADING, ProgressStep.TRANSFERRING)
        sut.canceled()
        assertThat postedEvents.size(), is(1)
        assertThat postedEvents.get(0), is(instanceOf(FirmwareUpdateResultInfoEvent))
        FirmwareUpdateResultInfoEvent resultEvent = postedEvents.get(0) as FirmwareUpdateResultInfoEvent
        assertThat resultEvent.getThingUID(), is(expectedThingUID)
        assertThat resultEvent.firmwareUpdateResultInfo.result, is(FirmwareUpdateResult.CANCELED)
        assertThat usedMessagedKey, is(cancelMessageKey)
    }

    /*
     * Special behaviour because of pending state: 
     *
     * Before calling next the ProgressStep is null which means the update was not started 
     * but a valid ProgressStep is needed to create a FirmwareUpdateProgressInfoEvent. 
     * As workaround the first step is returned to provide a valid ProgressStep.
     * This could be the case if the update directly goes in PENDING state after trying to start it.      
     */
    @Test
    void 'assert that getProgressStep returns first step if next was not called before'(){
        sut.defineSequence(ProgressStep.DOWNLOADING, ProgressStep.TRANSFERRING)
        assertThat sut.getCurrentStep(), is (ProgressStep.DOWNLOADING)
    }

    @Test
    void 'assert that getProgressStep returns current step if next was called before'(){
        def steps = [ProgressStep.DOWNLOADING, ProgressStep.TRANSFERRING, ProgressStep.UPDATING, ProgressStep.REBOOTING] as ProgressStep[]
        sut.defineSequence(steps)
        sut.next()
        for (int i = 0; i< steps.length-1; i++){
            assertThat sut.getCurrentStep(), is (steps[i])
            sut.next()
        }
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that pending throws IllegalArgumentException if step sequence is not defined and no progress was set'(){
        sut.pending()
    }

    @Test
    void 'assert that pending throws no IllegalStateException if step sequence is not defined and progress was set'(){
        sut.update(0)
        sut.pending()
        assertThatProgressInfoEventIsValid(postedEvents.get(1), null, true, 0)
    }

    @Test
    void 'assert that cancel throws no IllegalStateException if step sequence is not defined'(){
        sut.canceled()
        assertThatUpdateResultEventIsValid(postedEvents.get(0), cancelMessageKey, FirmwareUpdateResult.CANCELED)
    }

    @Test(expected=IllegalStateException)
    void 'assert that failed throws IllegalStateException if its called multiple times'(){
        sut.failed("DummyMessageKey")
        sut.failed("DummyMessageKey")
    }
    
    @Test(expected=IllegalStateException)
    void 'assert that failed throws IllegalStateException for successful updates'(){
        sut.update(100)
        sut.success()
        assertThatUpdateResultEventIsValid(postedEvents.get(1), null, FirmwareUpdateResult.SUCCESS)
        sut.failed("DummyMessageKey")
    }

    @Test(expected=IllegalStateException)
    void 'assert that success throws IllegalStateException if its called multiple times'(){
        sut.update(100)
        sut.success()
        assertThatUpdateResultEventIsValid(postedEvents.get(1), null, FirmwareUpdateResult.SUCCESS)
        sut.success()
    }

    @Test(expected=IllegalStateException)
    void 'assert that pending throws IllegalStateException if update failed'(){
        sut.defineSequence(ProgressStep.DOWNLOADING, ProgressStep.TRANSFERRING)
        sut.failed("DummyMessageKey")
        sut.pending()
    }

    @Test(expected=IllegalStateException)
    void 'assert that next throws IllegalStateException if update is not pending and no further steps available'(){
        sut.defineSequence(ProgressStep.DOWNLOADING)
        sut.next()
        sut.next()
    }

    void 'assert that next throws no IllegalStateException if update is pending and no further steps available'(){
        sut.defineSequence(ProgressStep.DOWNLOADING)
        sut.next()
        sut.pending()
        sut.next()
        assertThat sut.getCurrentStep(), is(ProgressStep.DOWNLOADING)
    }

    @Test(expected=IllegalStateException)
    void 'assert that pending throws IllegalStateException if update was successful'(){
        sut.defineSequence(ProgressStep.DOWNLOADING, ProgressStep.TRANSFERRING)
        sut.next()
        sut.next()
        sut.success()
        sut.pending()
    }

    def assertThatProgressInfoEventIsValid(Event event, ProgressStep expectedStep, boolean expectedPending, Integer expectedProgress){
        assertThat event, is(instanceOf(FirmwareUpdateProgressInfoEvent))
        def fpiEvent = event as FirmwareUpdateProgressInfoEvent
        assertThat fpiEvent.getThingUID(), is(expectedThingUID)
        assertThat fpiEvent.getProgressInfo().getFirmwareUID(), is(expectedFirmwareUID)
        assertThat fpiEvent.getProgressInfo().getProgressStep(), is(expectedStep)
        assertThat fpiEvent.getProgressInfo().getProgress(), is(expectedProgress)
        assertThat fpiEvent.getProgressInfo().isPending(), (is(expectedPending))
    }
    
    def assertThatUpdateResultEventIsValid(Event event, String expectedMessageKey, FirmwareUpdateResult expectedResult){
        assertThat event, is(instanceOf(FirmwareUpdateResultInfoEvent))
        def fpiEvent = event as FirmwareUpdateResultInfoEvent
        assertThat usedMessagedKey, is(expectedMessageKey)
        assertThat fpiEvent.getThingUID(), is(expectedThingUID)
        assertThat fpiEvent.getFirmwareUpdateResultInfo().getResult(), is(expectedResult)
    }

    class DummyFirmwareHandler implements FirmwareUpdateHandler {

        @Override
        public Thing getThing() {
            return null
        }

        @Override
        public void updateFirmware(Firmware firmware, ProgressCallback progressCallback) {
        }

        @Override
        public void cancel() {
        }

        @Override
        public boolean isUpdateExecutable() {
            return false
        }
    }
}
