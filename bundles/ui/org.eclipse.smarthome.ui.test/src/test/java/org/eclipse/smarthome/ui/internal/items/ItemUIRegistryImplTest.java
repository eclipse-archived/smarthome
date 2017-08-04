/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.internal.items;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.model.sitemap.Mapping;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.SitemapFactory;
import org.eclipse.smarthome.model.sitemap.Slider;
import org.eclipse.smarthome.model.sitemap.Switch;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.items.ItemUIProvider;
import org.junit.Before;
import org.junit.Test;

public class ItemUIRegistryImplTest {

    static private ItemRegistry registry;
    static private ItemUIRegistryImpl uiRegistry = new ItemUIRegistryImpl();
    // we need to get the decimal separator of the default locale for our tests
    static private final char sep = (new DecimalFormatSymbols().getDecimalSeparator());

    @Before
    public void prepareRegistry() {
        registry = mock(ItemRegistry.class);
        uiRegistry.setItemRegistry(registry);
    }

    @Test
    public void getLabel_plainLabel() {
        String testLabel = "This is a plain text";
        Widget w = mock(Widget.class);
        when(w.getLabel()).thenReturn(testLabel);
        String label = uiRegistry.getLabel(w);
        assertEquals(testLabel, label);
    }

    @Test
    public void getLabel_labelWithStaticValue() {
        String testLabel = "Label [value]";
        Widget w = mock(Widget.class);
        when(w.getLabel()).thenReturn(testLabel);
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [value]", label);
    }

    @Test
    public void getLabel_labelWithStringValue() throws ItemNotFoundException {
        String testLabel = "Label [%s]";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getState()).thenReturn(new StringType("State"));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [State]", label);
    }

    @Test
    public void getLabel_labelWithIntegerValue() throws ItemNotFoundException {
        String testLabel = "Label [%d]";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getState()).thenReturn(new DecimalType(20));
        when(item.getStateAs(DecimalType.class)).thenReturn(new DecimalType(20));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [20]", label);
    }

    @Test
    public void getLabel_labelWithIntegerValueAndWidth() throws ItemNotFoundException {
        String testLabel = "Label [%3d]";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getState()).thenReturn(new DecimalType(20));
        when(item.getStateAs(DecimalType.class)).thenReturn(new DecimalType(20));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [ 20]", label);
    }

    @Test
    public void getLabel_labelWithHexValueAndWidth() throws ItemNotFoundException {
        String testLabel = "Label [%3x]";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getState()).thenReturn(new DecimalType(20));
        when(item.getStateAs(DecimalType.class)).thenReturn(new DecimalType(20));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [ 14]", label);
    }

    @Test
    public void getLabel_labelWithDecimalValue() throws ItemNotFoundException {
        String testLabel = "Label [%.3f]";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getState()).thenReturn(new DecimalType(10f / 3f));
        when(item.getStateAs(DecimalType.class)).thenReturn(new DecimalType(10f / 3f));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [3" + sep + "333]", label);
    }

    @Test
    public void getLabel_labelWithPercent() throws ItemNotFoundException {
        String testLabel = "Label [%.1f %%]";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getState()).thenReturn(new DecimalType(10f / 3f));
        when(item.getStateAs(DecimalType.class)).thenReturn(new DecimalType(10f / 3f));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [3" + sep + "3 %]", label);
    }

    @Test
    public void getLabel_labelWithDate() throws ItemNotFoundException {
        String testLabel = "Label [%1$td.%1$tm.%1$tY]";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getState()).thenReturn(new DateTimeType("2011-06-01T00:00:00"));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [01.06.2011]", label);
    }

    @Test
    public void getLabel_labelWithTime() throws ItemNotFoundException {
        String testLabel = "Label [%1$tT]";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getState()).thenReturn(new DateTimeType("2011-06-01T15:30:59"));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [15:30:59]", label);
    }

    @Test
    public void getLabel_widgetWithoutLabelAndItem() throws ItemNotFoundException {
        Widget w = mock(Widget.class);
        String label = uiRegistry.getLabel(w);
        assertEquals("", label);
    }

    @Test
    public void getLabel_widgetWithoutLabel() throws ItemNotFoundException {
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        String label = uiRegistry.getLabel(w);
        assertEquals("Item", label);
    }

    @Test
    public void getLabel_labelFromUIProvider() throws ItemNotFoundException {
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        ItemUIProvider provider = mock(ItemUIProvider.class);
        uiRegistry.addItemUIProvider(provider);
        when(provider.getLabel(anyString())).thenReturn("ProviderLabel");
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        String label = uiRegistry.getLabel(w);
        assertEquals("ProviderLabel", label);
        uiRegistry.removeItemUIProvider(provider);
    }

    @Test
    public void getLabel_labelForUndefinedStringItemState() throws ItemNotFoundException {
        String testLabel = "Label [%s]";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getState()).thenReturn(UnDefType.UNDEF);
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [-]", label);
    }

    @Test
    public void getLabel_labelForUndefinedIntegerItemState() throws ItemNotFoundException {
        String testLabel = "Label [%d]";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getState()).thenReturn(UnDefType.UNDEF);
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [-]", label);
    }

    @Test
    public void getLabel_labelForUndefinedDecimalItemState() throws ItemNotFoundException {
        String testLabel = "Label [%.2f]";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getState()).thenReturn(UnDefType.UNDEF);
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [-]", label);
    }

    @Test
    public void getLabel_labelForUndefinedDateItemState() throws ItemNotFoundException {
        String testLabel = "Label [%1$td.%1$tm.%1$tY]";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getState()).thenReturn(UnDefType.UNDEF);
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [-.-.-]", label);
    }

    @Test
    public void getLabel_itemNotFound() throws ItemNotFoundException {
        String testLabel = "Label [%s]";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(w.eClass()).thenReturn(SitemapFactory.eINSTANCE.createText().eClass());
        when(registry.getItem("Item")).thenThrow(new ItemNotFoundException("Item"));
        when(item.getState()).thenReturn(new StringType("State"));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [-]", label);
    }

    @Test
    public void getLabel_labelWithFunctionValue() throws ItemNotFoundException {
        String testLabel = "Label [MAP(de.map):%s]";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getState()).thenReturn(new StringType("State"));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [State]", label);
    }

    @Test
    public void getLabel_groupLabelWithValue() throws ItemNotFoundException {
        String testLabel = "Label [%d]";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getState()).thenReturn(OnOffType.ON);
        when(item.getStateAs(DecimalType.class)).thenReturn(new DecimalType(5));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [5]", label);
    }

    @Test
    public void getWidget_UnknownPageId() throws ItemNotFoundException {
        Sitemap sitemap = SitemapFactory.eINSTANCE.createSitemap();
        when(registry.getItem("unknown")).thenThrow(new ItemNotFoundException("unknown"));
        Widget w = uiRegistry.getWidget(sitemap, "unknown");
        assertNull(w);
    }

    @Test
    public void testFormatDefault() {
        assertEquals("Server [(-)]", uiRegistry.formatUndefined("Server [(%d)]"));
        assertEquals("Anruf [von - an -]", uiRegistry.formatUndefined("Anruf [von %2$s an %1$s]"));
        assertEquals("Zeit [-.-.- -]", uiRegistry.formatUndefined("Zeit [%1$td.%1$tm.%1$tY %1$tT]"));
        assertEquals("Temperatur [- °C]", uiRegistry.formatUndefined("Temperatur [%.1f °C]"));
        assertEquals("Luftfeuchte [- %]", uiRegistry.formatUndefined("Luftfeuchte [%.1f %%]"));
    }

    @Test
    public void testStateConversionForSwitchWidgetThroughGetState() throws ItemNotFoundException {
        State colorState = new HSBType("23,42,50");

        ColorItem colorItem = new ColorItem("myItem");
        colorItem.setLabel("myItem");
        colorItem.setState(colorState);

        when(registry.getItem("myItem")).thenReturn(colorItem);

        Switch switchWidget = mock(Switch.class);
        when(switchWidget.getItem()).thenReturn("myItem");
        when(switchWidget.getMappings()).thenReturn(new BasicEList<Mapping>());

        State stateForSwitch = uiRegistry.getState(switchWidget);

        assertEquals(OnOffType.ON, stateForSwitch);
    }

    @Test
    public void testStateConversionForSwitchWidgetWithMappingThroughGetState() throws ItemNotFoundException {
        State colorState = new HSBType("23,42,50");

        ColorItem colorItem = new ColorItem("myItem");
        colorItem.setLabel("myItem");
        colorItem.setState(colorState);

        when(registry.getItem("myItem")).thenReturn(colorItem);

        Switch switchWidget = mock(Switch.class);
        when(switchWidget.getItem()).thenReturn("myItem");

        Mapping mapping = mock(Mapping.class);
        BasicEList<Mapping> mappings = new BasicEList<Mapping>();
        mappings.add(mapping);
        when(switchWidget.getMappings()).thenReturn(mappings);

        State stateForSwitch = uiRegistry.getState(switchWidget);

        assertEquals(colorState, stateForSwitch);
    }

    @Test
    public void testStateConversionForSliderWidgetThroughGetState() throws ItemNotFoundException {
        State colorState = new HSBType("23,42,75");

        ColorItem colorItem = new ColorItem("myItem");
        colorItem.setLabel("myItem");
        colorItem.setState(colorState);

        when(registry.getItem("myItem")).thenReturn(colorItem);

        Slider sliderWidget = mock(Slider.class);
        when(sliderWidget.getItem()).thenReturn("myItem");

        State stateForSlider = uiRegistry.getState(sliderWidget);

        assertTrue(stateForSlider instanceof PercentType);

        PercentType pt = (PercentType) stateForSlider;

        assertEquals(75, pt.longValue());
    }

    @Test
    public void getLabel_labelWithoutStateDescription() throws ItemNotFoundException {
        String testLabel = "Label";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getStateDescription()).thenReturn(null);
        when(item.getState()).thenReturn(new StringType("State"));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label", label);
    }

    @Test
    public void getLabel_labelWithoutPatternInStateDescription() throws ItemNotFoundException {
        String testLabel = "Label";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        StateDescription stateDescription = mock(StateDescription.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getStateDescription()).thenReturn(stateDescription);
        when(stateDescription.getPattern()).thenReturn(null);
        when(item.getState()).thenReturn(new StringType("State"));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label", label);
    }

    @Test
    public void getLabel_labelWithPatternInStateDescription() throws ItemNotFoundException {
        String testLabel = "Label";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        StateDescription stateDescription = mock(StateDescription.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getStateDescription()).thenReturn(stateDescription);
        when(stateDescription.getPattern()).thenReturn("%s");
        when(item.getState()).thenReturn(new StringType("State"));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [State]", label);
    }

    @Test
    public void getLabel_labelWithEmptyPattern() throws ItemNotFoundException {
        String testLabel = "Label []";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        StateDescription stateDescription = mock(StateDescription.class);
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getStateDescription()).thenReturn(stateDescription);
        when(stateDescription.getPattern()).thenReturn("%s");
        when(item.getState()).thenReturn(new StringType("State"));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label", label);
    }

    @Test
    public void getLabel_labelWithMappedOption() throws ItemNotFoundException {
        String testLabel = "Label";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        StateDescription stateDescription = mock(StateDescription.class);
        List<StateOption> options = new ArrayList<>();
        options.add(new StateOption("State0", "This is the state 0"));
        options.add(new StateOption("State1", "This is the state 1"));
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getStateDescription()).thenReturn(stateDescription);
        when(stateDescription.getPattern()).thenReturn("%s");
        when(stateDescription.getOptions()).thenReturn(options);
        when(item.getState()).thenReturn(new StringType("State1"));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [This is the state 1]", label);
    }

    @Test
    public void getLabel_labelWithUnmappedOption() throws ItemNotFoundException {
        String testLabel = "Label";
        Widget w = mock(Widget.class);
        Item item = mock(Item.class);
        StateDescription stateDescription = mock(StateDescription.class);
        List<StateOption> options = new ArrayList<>();
        options.add(new StateOption("State0", "This is the state 0"));
        options.add(new StateOption("State1", "This is the state 1"));
        when(w.getLabel()).thenReturn(testLabel);
        when(w.getItem()).thenReturn("Item");
        when(registry.getItem("Item")).thenReturn(item);
        when(item.getStateDescription()).thenReturn(stateDescription);
        when(stateDescription.getPattern()).thenReturn("%s");
        when(stateDescription.getOptions()).thenReturn(options);
        when(item.getState()).thenReturn(new StringType("State"));
        String label = uiRegistry.getLabel(w);
        assertEquals("Label [State]", label);
    }

}
