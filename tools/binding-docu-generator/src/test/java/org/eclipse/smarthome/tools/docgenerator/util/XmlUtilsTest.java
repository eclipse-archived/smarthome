package org.eclipse.smarthome.tools.docgenerator.util;

import org.eclipse.smarthome.tools.docgenerator.schemas.ChannelGroupType;
import org.eclipse.smarthome.tools.docgenerator.schemas.ChannelType;
import org.eclipse.smarthome.tools.docgenerator.schemas.ThingDescriptions;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class XmlUtilsTest {

    @Test
    public void testHandleXmlFiles() throws Exception {
        String filePath = getClass().getClassLoader().getResource("testXMLs/channel-alerts.xml").getPath();
        File file = new File(filePath);
        File searchDirectory = file.getParentFile();

        @SuppressWarnings("unchecked")
        XmlUtils.Consumer<File> consumer = mock(XmlUtils.Consumer.class);

        XmlUtils.handleXmlFiles(searchDirectory, consumer);

        verify(consumer, times(3)).accept(any(File.class));
    }

    @Test
    public void testConvertXmlToObject() throws Exception {
        String filePath = getClass().getClassLoader().getResource("testXMLs/channel-alerts.xml").getPath();
        File file = new File(filePath);

        ThingDescriptions thingDescriptions = XmlUtils.convertXmlToObject(file, ThingDescriptions.class);

        assertThat(thingDescriptions.getBindingId(), is("dect"));
        assertThat(thingDescriptions.getThingTypeOrBridgeTypeOrChannelType(), notNullValue());
        assertThat(thingDescriptions.getThingTypeOrBridgeTypeOrChannelType().size(), is(3));

        List<Object> items = thingDescriptions.getThingTypeOrBridgeTypeOrChannelType();

        assertThat(items.get(0), is(instanceOf(ChannelGroupType.class)));
        assertThat(items.get(1), is(instanceOf(ChannelType.class)));
        assertThat(items.get(2), is(instanceOf(ChannelType.class)));

        ChannelType channel2 = (ChannelType) items.get(1);
        assertThat(channel2.getLabel(), is("Alarm"));
        assertThat(channel2.getCategory(), is("Alarm"));
        assertThat(channel2.getItemType(), is("Switch"));
        assertThat(channel2.getId(), is("alertAlarm"));
    }
}