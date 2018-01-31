package org.eclipse.smarthome.core.thing.internal;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.CommandResultPredictionListener;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.AutoUpdatePolicy;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

public class AutoUpdateManagerTest {

    private static final ThingUID THING_UID_ONLINE = new ThingUID("test::mock");
    private static final ThingUID THING_UID_HANDLER_MISSING = new ThingUID("test::handlerMissing");
    private static final ChannelUID CHANNEL_UID_ONLINE_1 = new ChannelUID(THING_UID_ONLINE, "channel1");
    private static final ChannelUID CHANNEL_UID_ONLINE_2 = new ChannelUID(THING_UID_ONLINE, "channel2");
    private static final ChannelUID CHANNEL_UID_ONLINE_GONE = new ChannelUID(THING_UID_ONLINE, "gone");
    private static final ChannelUID CHANNEL_UID_HANDLER_MISSING = new ChannelUID(THING_UID_HANDLER_MISSING, "channel1");
    private ItemCommandEvent event;
    private GenericItem item;
    private @Mock EventPublisher mockEventPublisher;
    private @Mock ItemChannelLinkRegistry mockLinkRegistry;
    private @Mock ThingRegistry mockThingRegistry;
    private @Mock Thing mockThingOnline;
    private @Mock Thing mockThingHandlerMissing;
    private @Mock ThingHandler mockHandler;
    private @Mock CommandResultPredictionListener mockPredictionListener;

    private final List<ItemChannelLink> links = new LinkedList<>();
    private AutoUpdateManager aum;

    @Before
    public void setup() {
        initMocks(this);
        event = ItemEventFactory.createCommandEvent("test", new StringType("AFTER"));
        item = new StringItem("test");
        item.setState(new StringType("BEFORE"));

        when(mockLinkRegistry.stream()).then(answer -> links.stream());
        when(mockThingRegistry.get(eq(THING_UID_ONLINE))).thenReturn(mockThingOnline);
        when(mockThingRegistry.get(eq(THING_UID_HANDLER_MISSING))).thenReturn(mockThingHandlerMissing);
        when(mockThingOnline.getHandler()).thenReturn(mockHandler);
        when(mockThingOnline.getChannel(eq(CHANNEL_UID_ONLINE_1.getId())))
                .thenReturn(ChannelBuilder.create(CHANNEL_UID_ONLINE_1, "String").build());
        when(mockThingOnline.getChannel(eq(CHANNEL_UID_ONLINE_2.getId())))
                .thenReturn(ChannelBuilder.create(CHANNEL_UID_ONLINE_2, "String").build());

        aum = new AutoUpdateManager();
        aum.setItemChannelLinkRegistry(mockLinkRegistry);
        aum.setEventPublisher(mockEventPublisher);
        aum.setThingRegistry(mockThingRegistry);
        aum.addStateRevertListener(mockPredictionListener);
    }

    private void assertEvent(String expectedContent, String extectedSource) {
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventPublisher).post(eventCaptor.capture());
        Event event = eventCaptor.getValue();
        assertTrue(event instanceof ItemStateEvent);
        assertEquals(expectedContent, ((ItemStateEvent) event).getItemState().toFullString());
        assertEquals(extectedSource, event.getSource());
        assertNothingHappened();
    }

    private void assertChangeStateTo() {
        verify(mockPredictionListener).changeStateTo(isA(Item.class), eq(new StringType("AFTER")));
        assertNothingHappened();
    }

    private void assertKeepCurrentState() {
        verify(mockPredictionListener).keepCurrentState(isA(Item.class));
        assertNothingHappened();
    }

    private void assertNothingHappened() {
        verifyNoMoreInteractions(mockEventPublisher);
        verifyNoMoreInteractions(mockPredictionListener);
    }

    @Test
    public void testAutoUpdate_noLink() {
        aum.receiveCommand(event, item);

        assertEvent("AFTER", AutoUpdateManager.EVENT_SOURCE);
    }

    @Test
    public void testAutoUpdate_noPolicy() {
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_1));
        when(mockThingOnline.getStatus()).thenReturn(ThingStatus.ONLINE);

        aum.receiveCommand(event, item);

        assertChangeStateTo();
    }

    @Test
    public void testAutoUpdate_noPolicy_thingOFFLINE() {
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_1));
        when(mockThingOnline.getStatus()).thenReturn(ThingStatus.OFFLINE);

        aum.receiveCommand(event, item);

        assertKeepCurrentState();
    }

    @Test
    public void testAutoUpdate_noPolicy_noHandler() {
        links.add(new ItemChannelLink("test", CHANNEL_UID_HANDLER_MISSING));

        aum.receiveCommand(event, item);

        assertKeepCurrentState();
    }

    @Test
    public void testAutoUpdate_noPolicy_noThing() {
        links.add(new ItemChannelLink("test", new ChannelUID(new ThingUID("test::missing"), "gone")));

        aum.receiveCommand(event, item);

        assertKeepCurrentState();
    }

    @Test
    public void testAutoUpdate_noPolicy_noChannel() {
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_GONE));

        aum.receiveCommand(event, item);

        assertKeepCurrentState();
    }

    @Test
    public void testAutoUpdate_policyVETO_thingONLINE() {
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_1));
        when(mockThingOnline.getStatus()).thenReturn(ThingStatus.ONLINE);
        aum.setAutoUpdatePolicy(CHANNEL_UID_ONLINE_1, AutoUpdatePolicy.VETO);

        aum.receiveCommand(event, item);

        assertNothingHappened();
    }

    @Test
    public void testAutoUpdate_policyRECOMMEND() {
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_1));
        when(mockThingOnline.getStatus()).thenReturn(ThingStatus.ONLINE);
        aum.setAutoUpdatePolicy(CHANNEL_UID_ONLINE_1, AutoUpdatePolicy.RECOMMEND);

        aum.receiveCommand(event, item);

        assertEvent("AFTER", AutoUpdateManager.EVENT_SOURCE);
    }

    @Test
    public void testAutoUpdate_policyVETObeatsDEFAULT() {
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_1));
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_2));
        when(mockThingOnline.getStatus()).thenReturn(ThingStatus.ONLINE);
        aum.setAutoUpdatePolicy(CHANNEL_UID_ONLINE_1, AutoUpdatePolicy.VETO);
        aum.setAutoUpdatePolicy(CHANNEL_UID_ONLINE_2, AutoUpdatePolicy.DEFAULT);

        aum.receiveCommand(event, item);

        assertNothingHappened();
    }

    @Test
    public void testAutoUpdate_policyVETObeatsRECOMMEND() {
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_1));
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_2));
        when(mockThingOnline.getStatus()).thenReturn(ThingStatus.ONLINE);
        aum.setAutoUpdatePolicy(CHANNEL_UID_ONLINE_1, AutoUpdatePolicy.VETO);
        aum.setAutoUpdatePolicy(CHANNEL_UID_ONLINE_2, AutoUpdatePolicy.RECOMMEND);

        aum.receiveCommand(event, item);

        assertNothingHappened();
    }

    @Test
    public void testAutoUpdate_policyDEFAULTbeatsRECOMMEND() {
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_1));
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_2));
        when(mockThingOnline.getStatus()).thenReturn(ThingStatus.ONLINE);
        aum.setAutoUpdatePolicy(CHANNEL_UID_ONLINE_1, AutoUpdatePolicy.DEFAULT);
        aum.setAutoUpdatePolicy(CHANNEL_UID_ONLINE_2, AutoUpdatePolicy.RECOMMEND);

        aum.receiveCommand(event, item);

        assertChangeStateTo();
    }

    @Test
    public void testAutoUpdate_errorInvalidatesVETO() {
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_1));
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_GONE));
        when(mockThingOnline.getStatus()).thenReturn(ThingStatus.ONLINE);
        aum.setAutoUpdatePolicy(CHANNEL_UID_ONLINE_1, AutoUpdatePolicy.RECOMMEND);
        aum.setAutoUpdatePolicy(CHANNEL_UID_ONLINE_GONE, AutoUpdatePolicy.VETO);

        aum.receiveCommand(event, item);

        assertEvent("AFTER", AutoUpdateManager.EVENT_SOURCE);
    }

    @Test
    public void testAutoUpdate_errorInvalidatesVETO2() {
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_1));
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_GONE));
        when(mockThingOnline.getStatus()).thenReturn(ThingStatus.ONLINE);
        aum.setAutoUpdatePolicy(CHANNEL_UID_ONLINE_1, AutoUpdatePolicy.DEFAULT);
        aum.setAutoUpdatePolicy(CHANNEL_UID_ONLINE_GONE, AutoUpdatePolicy.VETO);

        aum.receiveCommand(event, item);

        assertChangeStateTo();
    }

    @Test
    public void testAutoUpdate_errorInvalidatesDEFAULT() {
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_1));
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_GONE));
        when(mockThingOnline.getStatus()).thenReturn(ThingStatus.ONLINE);
        aum.setAutoUpdatePolicy(CHANNEL_UID_ONLINE_1, AutoUpdatePolicy.RECOMMEND);
        aum.setAutoUpdatePolicy(CHANNEL_UID_ONLINE_GONE, AutoUpdatePolicy.DEFAULT);

        aum.receiveCommand(event, item);

        assertEvent("AFTER", AutoUpdateManager.EVENT_SOURCE);
    }

    @Test
    public void testAutoUpdate_multipleErrors() {
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_GONE));
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_GONE));
        when(mockThingOnline.getStatus()).thenReturn(ThingStatus.ONLINE);
        aum.setAutoUpdatePolicy(CHANNEL_UID_ONLINE_GONE, AutoUpdatePolicy.DEFAULT);

        aum.receiveCommand(event, item);

        assertKeepCurrentState();
    }

    @Test
    public void testAutoUpdate_disabled() {
        aum.modified(Collections.singletonMap(AutoUpdateManager.PROPERTY_ENABLED, "false"));

        aum.receiveCommand(event, item);

        assertNothingHappened();
    }

    @Test
    public void testAutoUpdate_sendOptimisticUpdates() {
        links.add(new ItemChannelLink("test", CHANNEL_UID_ONLINE_1));
        when(mockThingOnline.getStatus()).thenReturn(ThingStatus.ONLINE);
        aum.modified(Collections.singletonMap(AutoUpdateManager.PROPERTY_SEND_OPTIMISTIC_UPDATES, "true"));

        aum.receiveCommand(event, item);

        verify(mockPredictionListener).changeStateTo(isA(Item.class), eq(new StringType("AFTER")));
        assertEvent("AFTER", AutoUpdateManager.EVENT_SOURCE_OPTIMISTIC);
    }

}
