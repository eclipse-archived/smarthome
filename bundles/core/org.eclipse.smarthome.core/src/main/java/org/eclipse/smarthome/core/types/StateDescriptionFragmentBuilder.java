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
package org.eclipse.smarthome.core.types;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Builds a {@link StateDescriptionFragment} only with the relevant parts.
 *
 * @author Henning Treu - initial contribution and API.
 *
 */
@NonNullByDefault
public class StateDescriptionFragmentBuilder {

    private @Nullable BigDecimal minimum;
    private @Nullable BigDecimal maximum;
    private @Nullable BigDecimal step;
    private @Nullable String pattern;
    private @Nullable Boolean readOnly;
    private @Nullable List<StateOption> options;

    public static StateDescriptionFragmentBuilder instance() {
        return new StateDescriptionFragmentBuilder();
    }

    public StateDescriptionFragment build() {
        return new StateDescriptionFragment() {

            @Override
            public @Nullable BigDecimal getMinimum() {
                return minimum;
            }

            @Override
            public @Nullable BigDecimal getMaximum() {
                return maximum;
            }

            @Override
            public @Nullable BigDecimal getStep() {
                return step;
            }

            @Override
            public @Nullable String getPattern() {
                return pattern;
            }

            @Override
            public @Nullable Boolean isReadOnly() {
                return readOnly;
            }

            @Override
            public @Nullable List<StateOption> getOptions() {
                return options;
            }

            @Override
            public @Nullable StateDescription toStateDescription() {
                if (minimum == null && maximum == null && step == null && pattern == null && options == null) {
                    return null;
                }
                final Boolean ro = readOnly;
                return new StateDescription(minimum, maximum, step, pattern, ro == null ? false : ro.booleanValue(),
                        options);
            }

        };
    }

    public StateDescriptionFragmentBuilder withMaximum(BigDecimal maximum) {
        this.maximum = maximum;
        return this;
    }

    public StateDescriptionFragmentBuilder withMinimum(BigDecimal minimum) {
        this.minimum = minimum;
        return this;
    }

    public StateDescriptionFragmentBuilder withStep(BigDecimal step) {
        this.step = step;
        return this;
    }

    public StateDescriptionFragmentBuilder withPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public StateDescriptionFragmentBuilder withReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    public StateDescriptionFragmentBuilder withOptions(List<StateOption> options) {
        this.options = options;
        return this;
    }

    /**
     * Set the builder fields from all non-null fields of the given {@link StateDescriptionFragment}.
     *
     * @param fragment a {@link StateDescriptionFragment} this builder should incorporate.
     * @return the builder.
     */
    public StateDescriptionFragmentBuilder withStateDescriptionFragment(StateDescriptionFragment fragment) {
        if (fragment.getMinimum() != null) {
            this.minimum = fragment.getMinimum();
        }
        if (fragment.getMaximum() != null) {
            this.maximum = fragment.getMaximum();
        }
        if (fragment.getStep() != null) {
            this.step = fragment.getStep();
        }
        if (fragment.getPattern() != null) {
            this.pattern = fragment.getPattern();
        }
        if (fragment.isReadOnly() != null) {
            this.readOnly = fragment.isReadOnly();
        }
        if (fragment.getOptions() != null) {
            this.options = fragment.getOptions();
        }

        return this;
    }

    public StateDescriptionFragmentBuilder withStateDescription(StateDescription legacy) {
        if (legacy.getMinimum() != null) {
            this.minimum = legacy.getMinimum();
        }
        if (legacy.getMaximum() != null) {
            this.maximum = legacy.getMaximum();
        }
        if (legacy.getStep() != null) {
            this.step = legacy.getStep();
        }
        if (legacy.getPattern() != null) {
            this.pattern = legacy.getPattern();
        }

        this.readOnly = legacy.isReadOnly();

        if (legacy.getOptions() != null) {
            this.options = legacy.getOptions();
        }

        return this;
    }

    private StateDescriptionFragmentBuilder() {
        //
    }
}
