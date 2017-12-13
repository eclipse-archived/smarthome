package org.eclipse.smarthome.model.script.engine;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.Collection;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.model.script.ScriptServiceUtil;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ScriptEngineOSGiTest extends JavaOSGiTest {

    private static final String ITEM_NAME = "Switch1";
    private static final String NUMBER_ITEM_NAME = "NumberA";

    private ItemProvider itemProvider;

    private ScriptEngine scriptEngine;

    private ItemRegistry itemRegistry;

    @Before
    public void setup() {
        registerVolatileStorageService();

        EventPublisher eventPublisher = event -> {
        };

        registerService(eventPublisher);

        itemRegistry = getService(ItemRegistry.class);
        assertNotNull(itemRegistry);

        itemProvider = new ItemProvider() {

            @Override
            public void addProviderChangeListener(ProviderChangeListener<Item> listener) {
            }

            @Override
            public Collection<Item> getAll() {
                return Lists.newArrayList(new SwitchItem(ITEM_NAME),
                        createNumberItem(NUMBER_ITEM_NAME, Temperature.class, UnDefType.UNDEF));
            }

            @Override
            public void removeProviderChangeListener(ProviderChangeListener<Item> listener) {
            }
        };

        registerService(itemProvider);

        ScriptServiceUtil scriptServiceUtil = getService(ScriptServiceUtil.class);
        assertNotNull(scriptServiceUtil);
        scriptEngine = ScriptServiceUtil.getScriptEngine();
    }

    @After
    public void tearDown() {
        unregisterService(itemProvider);
    }

    @Test
    public void testInterpreter() throws ScriptParsingException, ScriptExecutionException {
        OnOffType switch1State = runScript("Switch1.state = ON;Switch1.state = OFF;Switch1.state = ON;Switch1.state");

        assertNotNull(switch1State);
        assertEquals("org.eclipse.smarthome.core.library.types.OnOffType", switch1State.getClass().getName());
        assertEquals("ON", switch1State.toString());
    }

    @SuppressWarnings("null")
    @Test
    public void testAssignQuantityType() throws ScriptParsingException, ScriptExecutionException {
        runScript("NumberA.state = 20.0 [°C] as org.eclipse.smarthome.core.types.State");

        State numberState = itemRegistry.get(NUMBER_ITEM_NAME).getState();
        assertNotNull(numberState);
        assertEquals("org.eclipse.smarthome.core.library.types.QuantityType", numberState.getClass().getName());
        assertEquals("20.0 ℃", numberState.toString());
    }

    @Test
    public void testCompareGreaterThanQuantityType() throws ScriptParsingException, ScriptExecutionException {
        assertTrue(runScript("20.0 [°C] > 20 [°F]"));
    }

    @Test
    public void testCompareGreaterThanQuantityType_False() throws ScriptParsingException, ScriptExecutionException {
        assertFalse(runScript("20.0 [°F] > 20 [°C]"));
    }

    @Test
    public void testCompareGreaterEqualsThanQuantityType() throws ScriptParsingException, ScriptExecutionException {
        assertTrue(runScript("1 [m] >= 100 [cm]"));
    }

    @Test
    public void testCompareLessThanQuantityType_False() throws ScriptParsingException, ScriptExecutionException {
        assertFalse(runScript("20.0 [°C] < 20 [°F]"));
    }

    @Test
    public void testCompareLessThanQuantityType() throws ScriptParsingException, ScriptExecutionException {
        assertTrue(runScript("20.0 [°F] < 20 [°C]"));
    }

    @Test
    public void testCompareLessEqualsThanQuantityType() throws ScriptParsingException, ScriptExecutionException {
        assertTrue(runScript("100 [cm] <= 1 [m]"));
    }

    @Test
    public void testpostUpdateQuantityType() throws ScriptParsingException, ScriptExecutionException {
        scriptEngine.newScriptFromString("postUpdate(NumberA, 20.0 [°C])").execute();
        scriptEngine.newScriptFromString("sendCommand(NumberA, 20.0 [°F])").execute();
    }

    @Test
    public void testAssignAndCompareQuantityType() throws ScriptParsingException, ScriptExecutionException {
        assertFalse(runScript(
                "NumberA.state = 20.0 [°C] as org.eclipse.smarthome.core.types.State; NumberA.state < 20 [°F]"));
    }

    @Test
    public void testAddQuantityType() throws ScriptParsingException, ScriptExecutionException {
        assertThat((QuantityType<?>) runScript("1 [m] + 20 [cm]"), is(QuantityType.valueOf("1.2 m")));
    }

    @Test
    public void testSubtractQuantityType() throws ScriptParsingException, ScriptExecutionException {
        assertThat((QuantityType<?>) runScript("1 [m] - 20 [cm]"), is(QuantityType.valueOf("0.8 m")));
    }

    @Test
    public void testMultiplyQuantityType() throws ScriptParsingException, ScriptExecutionException {
        assertThat((QuantityType<?>) runScript("1 [m] * 20 [cm]"), is(QuantityType.valueOf("2000 cm^2")));
    }

    @Test
    public void testMultiplyQuantityType_Number() throws ScriptParsingException, ScriptExecutionException {
        assertThat((QuantityType<?>) runScript("1 [m] * 20"), is(QuantityType.valueOf("20 m")));
    }

    @Test
    public void testDivideQuantityType() throws ScriptParsingException, ScriptExecutionException {
        assertThat((QuantityType<?>) runScript("1 [m] / 2 [cm]"), is(QuantityType.valueOf("50")));
    }

    @Test
    public void testDivideQuantityType_Number() throws ScriptParsingException, ScriptExecutionException {
        assertThat((QuantityType<?>) runScript("1 [m] / 2"), is(QuantityType.valueOf("0.5 m")));
    }

    @Test
    public void testDivide_Number_QuantityType() throws ScriptParsingException, ScriptExecutionException {
        assertThat((QuantityType<?>) runScript("1 / 2 [m]"), is(new QuantityType<>("0.5 one/m")));
    }

    @Test
    public void testDivide_Number_QuantityType_1() throws ScriptParsingException, ScriptExecutionException {
        assertThat((QuantityType<?>) runScript("0.5 [one/m] + 0.5 [one/m]"), is(new QuantityType<>("1 one/m")));
    }

    @Test
    public void testToUnit_QuantityType() throws ScriptParsingException, ScriptExecutionException {
        assertThat(runScript("20 [°C].toUnit(\"°F\").toString()"), is("68 °F"));
    }

    @Test
    public void testEquals_QuantityType_Number() throws ScriptParsingException, ScriptExecutionException {
        assertThat(runScript("20 [m].equals(20)"), is(false));
    }

    private Item createNumberItem(String numberItemName, Class<@NonNull Temperature> dimension, UnDefType state) {
        NumberItem item = new NumberItem(numberItemName);
        item.setDimension(dimension);
        return item;
    }

    @SuppressWarnings("unchecked")
    private <T> T runScript(String script) throws ScriptExecutionException, ScriptParsingException {
        return (T) scriptEngine.newScriptFromString(script).execute();
    }

}
