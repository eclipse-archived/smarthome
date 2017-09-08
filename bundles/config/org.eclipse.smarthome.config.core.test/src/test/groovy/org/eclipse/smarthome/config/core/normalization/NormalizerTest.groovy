/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.normalization;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import java.text.MessageFormat

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type
import org.eclipse.smarthome.config.core.internal.normalization.Normalizer
import org.eclipse.smarthome.config.core.internal.normalization.NormalizerFactory
import org.junit.Test

public class NormalizerTest {

    @Test
    public void testBooleanNormalizer() {
        Normalizer normalizer = NormalizerFactory.getNormalizer(new ConfigDescriptionParameter("test", Type.BOOLEAN));

        assertThat normalizer.normalize(null), is(nullValue())
        assertThat normalizer.normalize(true), is(equalTo(true))
        assertThat normalizer.normalize(1), is(equalTo(true))
        assertThat normalizer.normalize(false), is(equalTo(false))
        assertThat normalizer.normalize(0), is(equalTo(false))
        assertThat normalizer.normalize(new Boolean(true)), is(equalTo(true))
        assertThat normalizer.normalize(new Boolean(false)), is(equalTo(false))
        assertThat normalizer.normalize("true"), is(equalTo(true))
        assertThat normalizer.normalize("false"), is(equalTo(false))
        assertThat normalizer.normalize("yes"), is(equalTo(true))
        assertThat normalizer.normalize("no"), is(equalTo(false))
        assertThat normalizer.normalize("on"), is(equalTo(true))
        assertThat normalizer.normalize("off"), is(equalTo(false))
        assertThat normalizer.normalize("1"), is(equalTo(true))
        assertThat normalizer.normalize("0"), is(equalTo(false))
        assertThat normalizer.normalize("True"), is(equalTo(true))
        assertThat normalizer.normalize("TRUE"), is(equalTo(true))
        assertThat normalizer.normalize(["toString": {-> "true"}] as Normalizer), is(equalTo(true))

        // no chance -> leaving it untouched
        assertThat normalizer.normalize(""), is(equalTo(""))
        assertThat normalizer.normalize("gaga"), is(equalTo("gaga"));
        assertThat normalizer.normalize(2L), is(equalTo(2L));
    }

    @Test
    public void testIntNormalizer() {
        Normalizer normalizer = NormalizerFactory.getNormalizer(new ConfigDescriptionParameter("test", Type.INTEGER));

        assertThat normalizer.normalize(null), is(nullValue())
        assertThat normalizer.normalize(42), is(equalTo(new BigDecimal(42)))
        assertThat normalizer.normalize(42L), is(equalTo(new BigDecimal(42)))
        assertThat normalizer.normalize((byte) 42), is(equalTo(new BigDecimal(42)))
        assertThat normalizer.normalize(42.0), is(equalTo(new BigDecimal(42)))
        assertThat normalizer.normalize(42.0f), is(equalTo(new BigDecimal(42)))
        assertThat normalizer.normalize(42.0d), is(equalTo(new BigDecimal(42)))
        assertThat normalizer.normalize("42"), is(equalTo(new BigDecimal(42)))
        assertThat normalizer.normalize("42.0"), is(equalTo(new BigDecimal(42)))

        // no chance -> leaving it untouched
        def local = new Object()
        assertThat normalizer.normalize(""), is(equalTo(""))
        assertThat normalizer.normalize(local), is(equalTo(local))
        assertThat normalizer.normalize(42.1), is(equalTo(42.1))
        assertThat normalizer.normalize(42.1f), is(equalTo(42.1f))
        assertThat normalizer.normalize(42.1d), is(equalTo(42.1d))
        assertThat normalizer.normalize("42.1"), is(equalTo("42.1"))
        assertThat normalizer.normalize("true"), is(equalTo("true"))
        assertThat normalizer.normalize("gaga"), is(equalTo("gaga"))
    }

    @Test
    public void testDecimalNormalizer() {
        Normalizer normalizer = NormalizerFactory.getNormalizer(new ConfigDescriptionParameter("test", Type.DECIMAL));

        assertThat normalizer.normalize(null), is(nullValue())
        assertThat normalizer.normalize(42), is(equalTo(new BigDecimal("42.0")))
        assertThat normalizer.normalize(42L), is(equalTo(new BigDecimal("42.0")))
        assertThat normalizer.normalize((byte) 42), is(equalTo(new BigDecimal("42.0")))
        assertThat normalizer.normalize(42.0), is(equalTo(new BigDecimal("42.0")))
        assertThat normalizer.normalize(42.0f), is(equalTo(new BigDecimal("42.0")))
        assertThat normalizer.normalize(42.0d), is(equalTo(new BigDecimal("42.0")))
        assertThat normalizer.normalize(42.1), is(equalTo(new BigDecimal("42.1")))
        assertThat normalizer.normalize(42.88f), is(equalTo(new BigDecimal("42.88")))
        assertThat normalizer.normalize(42.88d), is(equalTo(new BigDecimal("42.88")))
        assertThat normalizer.normalize("42"), is(equalTo(new BigDecimal("42.0")))
        assertThat normalizer.normalize("42.0"), is(equalTo(new BigDecimal("42.0")))
        assertThat normalizer.normalize("42.1"), is(equalTo(new BigDecimal("42.1")))
        assertThat normalizer.normalize("42.11"), is(equalTo(new BigDecimal("42.11")))
        assertThat normalizer.normalize("42.00"), is(equalTo(new BigDecimal("42.0")))

        // no chance -> leaving it untouched
        def local = new Object()
        assertThat normalizer.normalize(""), is(equalTo(""))
        assertThat normalizer.normalize(local), is(equalTo(local))
        assertThat normalizer.normalize("true"), is(equalTo("true"))
        assertThat normalizer.normalize("gaga"), is(equalTo("gaga"))
    }

    @Test
    public void testTextNormalizer() {
        Normalizer normalizer = NormalizerFactory.getNormalizer(new ConfigDescriptionParameter("test", Type.TEXT));

        assertThat normalizer.normalize(null), is(nullValue())
        assertThat normalizer.normalize(""), is(equalTo(""))
        assertThat normalizer.normalize(42), is(equalTo("42"))
        assertThat normalizer.normalize(42L), is(equalTo("42"))
        assertThat normalizer.normalize((byte) 42), is(equalTo("42"))
        assertThat normalizer.normalize(42.0), is(equalTo("42.0"))
        assertThat normalizer.normalize(42.0f), is(equalTo("42.0"))
        assertThat normalizer.normalize(42.0d), is(equalTo("42.0"))
        assertThat normalizer.normalize(42.1), is(equalTo("42.1"))
        assertThat normalizer.normalize(42.88f), is(equalTo("42.88"))
        assertThat normalizer.normalize(42.88d), is(equalTo("42.88"))
        assertThat normalizer.normalize(true), is(equalTo("true"))
        assertThat normalizer.normalize("true"), is(equalTo("true"))
        assertThat normalizer.normalize("null"), is(equalTo("null"))
        assertThat normalizer.normalize("gaga"), is(equalTo("gaga"))
    }

    @Test
    public void testListNormalizer() {
        Normalizer normalizer = NormalizerFactory.getNormalizer(["getType": {-> Type.BOOLEAN}, "isMultiple": {-> true}] as ConfigDescriptionParameter);

        assertThat normalizer.normalize(null), is(nullValue())
        assertThat normalizer.normalize([true, false, true]), is(equalTo([true, false, true]))
        assertThat normalizer.normalize([true, false, true].toArray()), is(equalTo([true, false, true]))
        assertThat normalizer.normalize(new TreeSet([false, true])), is(equalTo([false, true]))
        assertThat normalizer.normalize([true, "false", true]), is(equalTo([true, false, true]))
        assertThat normalizer.normalize([true, 0, "true"]), is(equalTo([true, false, true]))
    }

    private void expectThat(Closure<?> c, Class<? extends Throwable> expected) {
        try {
            c()
        } catch (Throwable e) {
            if (!expected.isAssignableFrom(e.getClass())) {
                throw e;
            }
            return
        }
        throw new AssertionError(MessageFormat.format("Expected exception {0} was not thrown.", expected.getName()))
    }

    private <T extends Throwable> Class<T> causes(Class<T> clazz) {
        return clazz;
    }
}
