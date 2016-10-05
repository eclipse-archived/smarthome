/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import java.util.Locale;

import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.i18n.I18nProvider
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.firmware.Firmware;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUID
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUpdateHandler;
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressCallback;
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressStep;
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.osgi.framework.Bundle;

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
    def usedMessagedKey

    @Before
    void setUp(){
        def thingType = new ThingTypeUID("thing:type")
        expectedThingUID = new ThingUID(thingType, "thingid")
        expectedFirmwareUID = new FirmwareUID(thingType, "1")
        postedEvents = new LinkedList<>();
        def publisher = [
            post : { event -> postedEvents.add(event) }
        ] as EventPublisher
        def i18nProvider = [
            getText:  { bundle, key, defaultText, locale, arguments ->
                usedMessagedKey = key
                return "Dummy Message"
            } 
        ] as I18nProvider
        sut = new ProgressCallbackImpl(new DummyFirmwareHandler(),publisher, i18nProvider, expectedThingUID, expectedFirmwareUID, null)
    }

    @Test
    void 'assert that setting the progess to pending results in a FirmwareUpdateProgressInfo event'(){
        sut.defineSequence(ProgressStep.DOWNLOADING, ProgressStep.TRANSFERRING)
        sut.pending()
        assertThatProgressInfoEventIsValid(postedEvents.get(0), ProgressStep.DOWNLOADING, true)
    }
    
    @Test
    void 'assert that pending does not change ProgressStep'(){
        sut.defineSequence(ProgressStep.DOWNLOADING, ProgressStep.TRANSFERRING)
        sut.pending()
        assertThatProgressInfoEventIsValid(postedEvents.get(0), ProgressStep.DOWNLOADING, true)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(1), ProgressStep.DOWNLOADING, false)
        sut.pending()
        assertThatProgressInfoEventIsValid(postedEvents.get(2), ProgressStep.DOWNLOADING, true)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(3), ProgressStep.DOWNLOADING, false)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(4), ProgressStep.TRANSFERRING, false)
        sut.pending()
        assertThatProgressInfoEventIsValid(postedEvents.get(5), ProgressStep.TRANSFERRING, true)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(6), ProgressStep.TRANSFERRING, false)
        assertThat postedEvents.size(), is(7)
    }

    @Test
    void 'assert that next changes ProgressStep if not pending'(){
        sut.defineSequence(ProgressStep.DOWNLOADING, ProgressStep.TRANSFERRING, ProgressStep.UPDATING)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(0), ProgressStep.DOWNLOADING, false)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(1), ProgressStep.TRANSFERRING, false)
        sut.pending()
        assertThatProgressInfoEventIsValid(postedEvents.get(2), ProgressStep.TRANSFERRING, true)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(3), ProgressStep.TRANSFERRING, false)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(4), ProgressStep.UPDATING, false)
        sut.pending()
        assertThatProgressInfoEventIsValid(postedEvents.get(5), ProgressStep.UPDATING, true)
        sut.next()
        assertThatProgressInfoEventIsValid(postedEvents.get(6), ProgressStep.UPDATING, false)
        assertThat postedEvents.size(), is(7)
    }
        
    @Test(expected=IllegalStateException)
    void 'assert that cancel throws IllegalStateException if update is finished'(){
        sut.defineSequence(ProgressStep.DOWNLOADING, ProgressStep.TRANSFERRING)
        sut.success()
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
        assertThat usedMessagedKey, is(ProgressCallbackImpl.UPDATE_CANCELED_MESSAGE_KEY)
        
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
        def steps = [
            ProgressStep.DOWNLOADING,
            ProgressStep.TRANSFERRING,
            ProgressStep.UPDATING,
            ProgressStep.REBOOTING] as ProgressStep[]
        sut.defineSequence(steps)
        sut.next()
        for (int i = 0; i< steps.length-1;i++){
            assertThat sut.getCurrentStep(), is (steps[i])
            sut.next()
        }
    }

    @Test(expected=IllegalStateException)
    void 'assert that pending throws IllegalStateException if step sequence is not defined'(){
        sut.pending()
    }
    
    @Test(expected=IllegalStateException)
    void 'assert that failed throws IllegalStateException if its called multiple times'(){
        sut.failed("DummyMessageKey")
        sut.failed("DummyMessageKey")
    }

    @Test(expected=IllegalStateException)
    void 'assert that success throws IllegalStateException if its called multiple times'(){
        sut.success()
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
        sut.success()
        sut.pending()
    }

    def assertThatProgressInfoEventIsValid(Event event, ProgressStep expectedStep, boolean isPending){
        assertThat event, is(instanceOf(FirmwareUpdateProgressInfoEvent))
        def fpiEvent = event as FirmwareUpdateProgressInfoEvent
        assertThat fpiEvent.getThingUID(), is(expectedThingUID)
        assertThat fpiEvent.getProgressInfo().getFirmwareUID(), is(expectedFirmwareUID)
        assertThat fpiEvent.getProgressInfo().getProgressStep(), is(expectedStep)
        // Is update in PENDING state?
        assertThat fpiEvent.getProgressInfo().isPending(), (is(isPending))
    }
    
    class DummyFirmwareHandler implements FirmwareUpdateHandler {

        @Override
        public Thing getThing() {
            return null;
        }

        @Override
        public void updateFirmware(Firmware firmware, ProgressCallback progressCallback) {
        }

        @Override
        public void cancel() {
        }

        @Override
        public boolean isUpdateExecutable() {
            return false;
        }
    }
}
