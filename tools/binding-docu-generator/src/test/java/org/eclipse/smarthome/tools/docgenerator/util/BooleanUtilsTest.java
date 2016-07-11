package org.eclipse.smarthome.tools.docgenerator.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BooleanUtilsTest {

    @Test
    public void testYesOrNo() throws Exception {
        assertThat(BooleanUtils.booleanToYesOrNo(true), is("Yes"));
        assertThat(BooleanUtils.booleanToYesOrNo(false), is("No"));
        assertThat(BooleanUtils.booleanToYesOrNo(null), is("No"));
    }

    @Test
    public void testTrueOrFalse() throws Exception {
        assertThat(BooleanUtils.booleanToTrueOrFalse(true), is("true"));
        assertThat(BooleanUtils.booleanToTrueOrFalse(false), is("false"));
        assertThat(BooleanUtils.booleanToTrueOrFalse(null), is("false"));
    }
}