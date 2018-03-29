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
 * Builds a {@link StateDescriptionFragment} with the relevant parts only.
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

    /**
     * Return a fresh builder instance.
     *
     * @return a fresh {@link StateDescriptionFragmentBuilder} instance.
     */
    public static StateDescriptionFragmentBuilder instance() {
        return new StateDescriptionFragmentBuilder();
    }

    /**
     * Build a {@link StateDescriptionFragment} from the values of this builder.
     *
     * @return a {@link StateDescriptionFragment} from the values of this builder.
     */
    public StateDescriptionFragment build() {
        return new StateDescriptionFragment() {

            private @Nullable final BigDecimal minimum = StateDescriptionFragmentBuilder.this.minimum;
            private @Nullable final BigDecimal maximum = StateDescriptionFragmentBuilder.this.maximum;
            private @Nullable final BigDecimal step = StateDescriptionFragmentBuilder.this.step;
            private @Nullable final String pattern = StateDescriptionFragmentBuilder.this.pattern;
            private @Nullable final Boolean readOnly = StateDescriptionFragmentBuilder.this.readOnly;
            private @Nullable final List<StateOption> options = StateDescriptionFragmentBuilder.this.options;

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
                if (minimum == null && maximum == null && step == null && readOnly == null && pattern == null
                        && options == null) {
                    return null;
                }
                final Boolean ro = readOnly;
                return new StateDescription(minimum, maximum, step, pattern, ro == null ? false : ro.booleanValue(),
                        options);
            }

        };
    }

    /**
     * Set the maximum for the resulting {@link StateDescriptionFragment}.
     *
     * @param maximum the maximum for the resulting {@link StateDescriptionFragment}.
     * @return this builder.
     */
    public StateDescriptionFragmentBuilder withMaximum(BigDecimal maximum) {
        this.maximum = maximum;
        return this;
    }

    /**
     * Set the minimum for the resulting {@link StateDescriptionFragment}.
     *
     * @param minimum the minimum for the resulting {@link StateDescriptionFragment}.
     * @return this builder.
     */
    public StateDescriptionFragmentBuilder withMinimum(BigDecimal minimum) {
        this.minimum = minimum;
        return this;
    }

    /**
     * Set the step for the resulting {@link StateDescriptionFragment}.
     *
     * @param step the step for the resulting {@link StateDescriptionFragment}.
     * @return this builder.
     */
    public StateDescriptionFragmentBuilder withStep(BigDecimal step) {
        this.step = step;
        return this;
    }

    /**
     * Set the pattern for the resulting {@link StateDescriptionFragment}.
     *
     * @param pattern the pattern for the resulting {@link StateDescriptionFragment}.
     * @return this builder.
     */
    public StateDescriptionFragmentBuilder withPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * Set readOnly for the resulting {@link StateDescriptionFragment}.
     *
     * @param readOnly readOnly for the resulting {@link StateDescriptionFragment}.
     * @return this builder.
     */
    public StateDescriptionFragmentBuilder withReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    /**
     * Set the {@link StateOption}s for the resulting {@link StateDescriptionFragment}.
     *
     * @param options the {@link StateOption}s for the resulting {@link StateDescriptionFragment}.
     * @return this builder.
     */
    public StateDescriptionFragmentBuilder withOptions(List<StateOption> options) {
        this.options = options;
        return this;
    }

    /**
     * Merge the given {@link StateDescriptionFragment}. Set all unset ({@code null}) fields of this builder to the
     * values
     * from the
     * given {@link StateDescriptionFragment}.
     *
     * @param fragment a {@link StateDescriptionFragment} this builder should merge in.
     * @return the builder.
     */
    public StateDescriptionFragmentBuilder mergeStateDescriptionFragment(StateDescriptionFragment fragment) {
        if (this.minimum == null) {
            this.minimum = fragment.getMinimum();
        }
        if (this.maximum == null) {
            this.maximum = fragment.getMaximum();
        }
        if (this.step == null) {
            this.step = fragment.getStep();
        }
        if (this.pattern == null) {
            this.pattern = fragment.getPattern();
        }
        if (this.readOnly == null) {
            this.readOnly = fragment.isReadOnly();
        }
        if (this.options == null) {
            this.options = fragment.getOptions();
        }

        return this;
    }

    /**
     * Merge the given {@link StateDescription}. Set all unset ({@code null}) fields of this builder to the values from
     * the
     * given {@link StateDescription}.
     *
     * @param legacy a {@link StateDescription} this builder should merge in.
     * @return the builder.
     */
    public StateDescriptionFragmentBuilder mergeStateDescription(StateDescription legacy) {
        if (this.minimum == null) {
            this.minimum = legacy.getMinimum();
        }
        if (this.maximum == null) {
            this.maximum = legacy.getMaximum();
        }
        if (this.step == null) {
            this.step = legacy.getStep();
        }
        if (this.pattern == null) {
            this.pattern = legacy.getPattern();
        }

        if (this.readOnly == null) {
            this.readOnly = Boolean.valueOf(legacy.isReadOnly());
        }

        if (this.options == null && legacy.getOptions() != null && !legacy.getOptions().isEmpty()) {
            this.options = legacy.getOptions();
        }

        return this;
    }

    private StateDescriptionFragmentBuilder() {
        // avoid public instantiation
    }
}
