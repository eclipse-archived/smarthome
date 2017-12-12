/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.io.rest.core.internal.item;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.io.rest.core.item.EnrichedItemDTO;
import org.eclipse.smarthome.io.rest.core.item.EnrichedItemDTOMapper;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Lists;

public class EnrichedItemDTOMapperWithTransformOSGiTest extends JavaOSGiTest {

    private static final String ITEM_NAME = "Item1";

    private List<StateDescriptionProvider> stateDescriptionProviders;

    @Mock
    private StateDescriptionProvider stateDescriptionProvider;

    @Before
    public void setup() {
        initMocks(this);

        StateDescription stateDescription = new StateDescription(BigDecimal.ZERO, BigDecimal.valueOf(100),
                BigDecimal.TEN, "%d °C", true, Lists.newArrayList(new StateOption("SOUND", "My great sound.")));
        when(stateDescriptionProvider.getStateDescription(ITEM_NAME, null)).thenReturn(stateDescription);

        stateDescriptionProviders = Lists.newArrayList(stateDescriptionProvider);
    }

    @Test
    public void shouldConsiderTraformationWhenPresent() {
        NumberItem item1 = new NumberItem("Item1");
        item1.setState(new DecimalType("12.34"));
        item1.setStateDescriptionProviders(stateDescriptionProviders);

        EnrichedItemDTO enrichedDTO = EnrichedItemDTOMapper.map(item1, false, null, null);
        assertThat(enrichedDTO, is(notNullValue()));
        assertThat(enrichedDTO.name, is("Item1"));
        assertThat(enrichedDTO.state, is("12.34"));

        StateDescription sd = enrichedDTO.stateDescription;
        assertThat(sd.getMinimum(), is(BigDecimal.valueOf(0)));
        assertThat(sd.getMaximum(), is(BigDecimal.valueOf(100)));
        assertThat(sd.getStep(), is(BigDecimal.valueOf(10)));
        assertThat(sd.getPattern(), is("%d °C"));
        assertThat(sd.getOptions().get(0).getValue(), is("SOUND"));
        assertThat(sd.getOptions().get(0).getLabel(), is("My great sound."));
    }
    
    @Test
    public void testStateDescriptionReadOnly() {

        StateDescriptionProvider stateDescriptionProvider1 = mock(StateDescriptionProvider.class);
        StateDescription stateDescriptionContainingPattern = new StateDescription(null, null, null, "somePattern",
                false, null);
        when(stateDescriptionProvider1.getStateDescription("Item2", null))
                .thenReturn(stateDescriptionContainingPattern);

        StateDescriptionProvider stateDescriptionProvider2 = mock(StateDescriptionProvider.class);
        StateDescription stateDescriptionContainingReadOnlyValue = new StateDescription(null, null, null,
                "defaultPattern", true, null);
        when(stateDescriptionProvider2.getStateDescription("Item2", null))
                .thenReturn(stateDescriptionContainingReadOnlyValue);

        NumberItem item2 = new NumberItem("Item2");
        item2.setStateDescriptionProviders(Arrays.asList(stateDescriptionProvider1, stateDescriptionProvider2));

        EnrichedItemDTO enrichedDTO = EnrichedItemDTOMapper.map(item2, false, null, null);
        StateDescription sd = enrichedDTO.stateDescription;

        assertThat(sd.isReadOnly(), is(true));
        assertThat(sd.getPattern(), is("somePattern"));
    }
}
