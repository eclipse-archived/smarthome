/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.core.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.core.internal.service.StateDescriptionServiceImpl;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateOption;
import org.junit.Test;

/**
 * Tests for the StateDescriptionService implementation
 *
 * @author Lyubomir Papazov
 *
 */
public class StateDescriptionServiceImplTest {

    private static final String ITEM_NAME = "Item1";
    private static final int STATE_DESCRIPTION_PROVIDER_DEFAULT_SERVICE_RANKING = 0;
    private static final BigDecimal STATE_DESCRIPTION_PROVIDER_DEFAULT_MIN_VALUE = new BigDecimal("0");
    private static final BigDecimal STATE_DESCRIPTION_PROVIDER_DEFAULT_MAX_VALUE = new BigDecimal("0");
    private static final BigDecimal STATE_DESCRIPTION_PROVIDER_DEFAULT_STEP = new BigDecimal("0");
    private static final String STATE_DESCRIPTION_PROVIDER_DEFAULT_PATTERN = "pattern1";
    private static final Boolean STATE_DESCRIPTION_PROVIDER_DEFAULT_IS_READONLY = Boolean.FALSE;
    private static final List<StateOption> STATE_DESCRIPTION_PROVIDER_DEFAULT_OPTIONS = Collections.emptyList();

    private final StateDescriptionServiceImpl mergingService = new StateDescriptionServiceImpl();
    private NumberItem item;

    @Test
    public void testServiceWithOneStateDescriptionProvider() {
        StateDescriptionProvider stateDescriptionProviderDefault = mock(StateDescriptionProvider.class);
        when(stateDescriptionProviderDefault.getRank()).thenReturn(STATE_DESCRIPTION_PROVIDER_DEFAULT_SERVICE_RANKING);
        StateDescription stateDescription1 = new StateDescription(STATE_DESCRIPTION_PROVIDER_DEFAULT_MIN_VALUE,
                STATE_DESCRIPTION_PROVIDER_DEFAULT_MAX_VALUE, STATE_DESCRIPTION_PROVIDER_DEFAULT_STEP,
                STATE_DESCRIPTION_PROVIDER_DEFAULT_PATTERN, STATE_DESCRIPTION_PROVIDER_DEFAULT_IS_READONLY,
                STATE_DESCRIPTION_PROVIDER_DEFAULT_OPTIONS);
        when(stateDescriptionProviderDefault.getStateDescription(ITEM_NAME, null)).thenReturn(stateDescription1);

        mergingService.addStateDescriptionProvider(stateDescriptionProviderDefault);

        item = new NumberItem(ITEM_NAME);
        item.setStateDescriptionService(mergingService);
        StateDescription finalStateDescription = item.getStateDescription();

        assertThat(finalStateDescription.getMinimum(), is(STATE_DESCRIPTION_PROVIDER_DEFAULT_MIN_VALUE));
        assertThat(finalStateDescription.getMaximum(), is(STATE_DESCRIPTION_PROVIDER_DEFAULT_MAX_VALUE));
        assertThat(finalStateDescription.getStep(), is(STATE_DESCRIPTION_PROVIDER_DEFAULT_STEP));
        assertThat(finalStateDescription.getPattern(), is(STATE_DESCRIPTION_PROVIDER_DEFAULT_PATTERN));
        assertThat(finalStateDescription.getReadOnly(), is(STATE_DESCRIPTION_PROVIDER_DEFAULT_IS_READONLY));
        assertThat(finalStateDescription.getOptions(), is(STATE_DESCRIPTION_PROVIDER_DEFAULT_OPTIONS));
    }

    @Test
    public void testMinValueMaxValueStepAndPatternTwoDescriptionProviders() {

        StateDescription stateDescription1 = new StateDescription(new BigDecimal("-1"), new BigDecimal("-1"),
                new BigDecimal("-1"), "pattern1", Boolean.FALSE, null);
        StateDescription stateDescription2 = new StateDescription(new BigDecimal("-2"), new BigDecimal("-2"),
                new BigDecimal("-2"), "pattern2", Boolean.FALSE, null);

        int stateDescriptionProvider1ServiceRanking = -1;
        int stateDescriptionProvider2ServiceRanking = -2;

        StateDescription finalStateDescription = mergeStateDescriptions(stateDescription1, stateDescription2,
                stateDescriptionProvider1ServiceRanking, stateDescriptionProvider2ServiceRanking);

        assertThat(finalStateDescription.getMinimum(), is(stateDescription1.getMinimum()));
        assertThat(finalStateDescription.getMaximum(), is(stateDescription1.getMaximum()));
        assertThat(finalStateDescription.getStep(), is(stateDescription1.getStep()));
        assertThat(finalStateDescription.getPattern(), is(stateDescription1.getPattern()));
    }

    @Test
    public void testIsReadOnlyWhenTwoDescriptionProvidersHigherRankingIsNotReadOnly() {
        StateDescription stateDescription1 = new StateDescription(null, null, null, null, Boolean.FALSE, null);
        StateDescription stateDescription2 = new StateDescription(null, null, null, null, Boolean.TRUE, null);

        int stateDescriptionProvider1ServiceRanking = -1;
        int stateDescriptionProvider2ServiceRanking = -2;

        StateDescription finalStateDescription = mergeStateDescriptions(stateDescription1, stateDescription2,
                stateDescriptionProvider1ServiceRanking, stateDescriptionProvider2ServiceRanking);

        assertThat(finalStateDescription.getReadOnly(), is(stateDescription1.getReadOnly()));
    }

    @Test
    public void testIsReadOnlyWhenTwoDescriptionProvidersHigherRankingIsReadOnly() {
        StateDescription stateDescription1 = new StateDescription(null, null, null, null, Boolean.TRUE, null);
        StateDescription stateDescription2 = new StateDescription(null, null, null, null, Boolean.FALSE, null);

        int stateDescriptionProvider1ServiceRanking = -1;
        int stateDescriptionProvider2ServiceRanking = -2;

        StateDescription finalStateDescription = mergeStateDescriptions(stateDescription1, stateDescription2,
                stateDescriptionProvider1ServiceRanking, stateDescriptionProvider2ServiceRanking);

        assertThat(finalStateDescription.getReadOnly(), is(stateDescription1.getReadOnly()));
    }

    @Test
    public void testIsReadOnlyWhenTwoDescriptionProvidersHigherRankingIsNull() {
        StateDescription stateDescription1 = new StateDescription(null, null, null, null, null, null);
        StateDescription stateDescription2 = new StateDescription(null, null, null, null, Boolean.FALSE, null);

        int stateDescriptionProvider1ServiceRanking = -1;
        int stateDescriptionProvider2ServiceRanking = -2;

        StateDescription finalStateDescription = mergeStateDescriptions(stateDescription1, stateDescription2,
                stateDescriptionProvider1ServiceRanking, stateDescriptionProvider2ServiceRanking);

        assertThat(finalStateDescription.getReadOnly(), is(stateDescription2.getReadOnly()));
    }

    @Test
    public void testOptionsWhenTwoDescriptionProvidersHigherRankingProvidesOptions() {
        StateDescription stateDescription1 = new StateDescription(null, null, null, null, Boolean.FALSE,
                Arrays.asList(new StateOption("value", "label")));
        StateDescription stateDescription2 = new StateDescription(null, null, null, null, Boolean.FALSE,
                Collections.emptyList());

        int stateDescriptionProvider1ServiceRanking = -1;
        int stateDescriptionProvider2ServiceRanking = -2;

        StateDescription finalStateDescription = mergeStateDescriptions(stateDescription1, stateDescription2,
                stateDescriptionProvider1ServiceRanking, stateDescriptionProvider2ServiceRanking);

        assertThat(finalStateDescription.getOptions(), is(stateDescription1.getOptions()));
    }

    @Test
    public void testOptionsWhenTwoDescriptionProvidersHigherRankingDoesntProvideOptions() {
        StateDescription stateDescription1 = new StateDescription(null, null, null, null, Boolean.FALSE,
                Collections.emptyList());
        StateDescription stateDescription2 = new StateDescription(null, null, null, null, Boolean.FALSE,
                Arrays.asList(new StateOption("value", "label")));

        int stateDescriptionProvider1ServiceRanking = -1;
        int stateDescriptionProvider2ServiceRanking = -2;

        StateDescription finalStateDescription = mergeStateDescriptions(stateDescription1, stateDescription2,
                stateDescriptionProvider1ServiceRanking, stateDescriptionProvider2ServiceRanking);

        assertThat(finalStateDescription.getOptions(), is(stateDescription2.getOptions()));
    }

    private StateDescription mergeStateDescriptions(StateDescription stateDescription1,
            StateDescription stateDescription2, int stateDescriptionProvider1ServiceRanking,
            int stateDescriptionProvider2ServiceRanking) {
        StateDescriptionProvider stateDescriptionProvider1 = mock(StateDescriptionProvider.class);
        StateDescriptionProvider stateDescriptionProvider2 = mock(StateDescriptionProvider.class);

        when(stateDescriptionProvider1.getRank()).thenReturn(stateDescriptionProvider1ServiceRanking);
        when(stateDescriptionProvider2.getRank()).thenReturn(stateDescriptionProvider2ServiceRanking);

        when(stateDescriptionProvider1.getStateDescription(ITEM_NAME, null)).thenReturn(stateDescription1);
        when(stateDescriptionProvider2.getStateDescription(ITEM_NAME, null)).thenReturn(stateDescription2);

        mergingService.addStateDescriptionProvider(stateDescriptionProvider1);
        mergingService.addStateDescriptionProvider(stateDescriptionProvider2);
        item = new NumberItem(ITEM_NAME);
        item.setStateDescriptionService(mergingService);

        StateDescription finalStateDescription = item.getStateDescription();
        return finalStateDescription;
    }

}
