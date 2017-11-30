package org.eclipse.smarthome.model.script.tests.scriptengine;

import static org.junit.Assert.*;

import java.util.Collection;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.i18n.UnitProvider;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.types.MeasurementSystem;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.model.script.ScriptServiceUtil;
import org.eclipse.smarthome.model.script.engine.Script;
import org.eclipse.smarthome.model.script.engine.ScriptEngine;
import org.eclipse.smarthome.model.script.engine.ScriptExecutionException;
import org.eclipse.smarthome.model.script.engine.ScriptParsingException;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import tec.uom.se.unit.Units;

public class ScriptEngineOSGiTest extends JavaOSGiTest {

    private static final String ITEM_NAME = "Switch1";
    private static final String NUMBER_ITEM_NAME = "NumberA";

    private ItemProvider itemProvider;

    private ScriptEngine scriptEngine;

    private UnitProvider unitProvider;
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

        unitProvider = new UnitProvider() {

            @Override
            public @Nullable Unit<?> parseUnit(@Nullable String pattern) {
                return null;
            }

            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public <T extends Quantity<T>> @NonNull Unit<T> getUnit(@NonNull Class<? extends T> dimension) {
                return (Unit) Units.CELSIUS;
            }

            @Override
            public @NonNull MeasurementSystem getMeasurementSystem() {
                return MeasurementSystem.SI;
            }
        };

        registerService(unitProvider);
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
        String parsedScript = "Switch1.state = ON;Switch1.state = OFF;Switch1.state = ON;Switch1.state";
        Script script = scriptEngine.newScriptFromString(parsedScript);
        Object switch1State = script.execute();

        assertNotNull(switch1State);
        assertEquals("org.eclipse.smarthome.core.library.types.OnOffType", switch1State.getClass().getName());
        assertEquals("ON", switch1State.toString());
    }

    @Test
    public void testAssignQuantityType() throws ScriptParsingException, ScriptExecutionException {
        String parsedScript = "NumberA.state = 20.0 [°C] as org.eclipse.smarthome.core.types.State";
        Script script = scriptEngine.newScriptFromString(parsedScript);
        script.execute();

        State numberState = itemRegistry.get(NUMBER_ITEM_NAME).getState();
        assertNotNull(numberState);
        assertEquals("org.eclipse.smarthome.core.library.types.QuantityType", numberState.getClass().getName());
        assertEquals("20.0 ℃", numberState.toString());
    }

    @Test
    public void testCompareQuantityType() throws ScriptParsingException, ScriptExecutionException {
        String parsedScript = "20.0 [°C] > 20 [°F]";
        Script script = scriptEngine.newScriptFromString(parsedScript);
        Object result = script.execute();

        assertTrue((Boolean) result);
    }

    @Test
    public void testCompareLessThenQuantityType() throws ScriptParsingException, ScriptExecutionException {
        String parsedScript = "20.0 [°C] < 20 [°F]";
        Script script = scriptEngine.newScriptFromString(parsedScript);
        Object result = script.execute();

        assertFalse((Boolean) result);
    }

    @Test
    public void testpostUpdateQuantityType() throws ScriptParsingException, ScriptExecutionException {
        scriptEngine.newScriptFromString("postUpdate(NumberA, 20.0 [°C])").execute();
        scriptEngine.newScriptFromString("sendCommand(NumberA, 20.0 [°F])").execute();
    }

    @Test
    public void testAssignAndCompareQuantityType() throws ScriptParsingException, ScriptExecutionException {
        String parsedScript = "NumberA.state = 20.0 [°C] as org.eclipse.smarthome.core.types.State; NumberA.state < 20 [°F]";
        Script script = scriptEngine.newScriptFromString(parsedScript);
        Object result = script.execute();

        assertFalse((Boolean) result);
    }

    @Test
    public void testAddQuantityType() throws ScriptParsingException, ScriptExecutionException {
        String parsedScript = "1 [m] + 20 [cm]";
        Script script = scriptEngine.newScriptFromString(parsedScript);
        Object result = script.execute();

        assertEquals("1.2 m", result.toString());
    }

    @Test
    public void testSubtractQuantityType() throws ScriptParsingException, ScriptExecutionException {
        String parsedScript = "1 [m] - 20 [cm]";
        Script script = scriptEngine.newScriptFromString(parsedScript);
        Object result = script.execute();

        assertEquals("0.8 m", result.toString());
    }

    @Test
    public void testMultiplyQuantityType() throws ScriptParsingException, ScriptExecutionException {
        String parsedScript = "1 [m] * 20 [cm]";
        Script script = scriptEngine.newScriptFromString(parsedScript);
        Object result = script.execute();

        assertEquals("20 m·cm", result.toString());
    }

    @Test
    public void testMultiplyQuantityType_Number() throws ScriptParsingException, ScriptExecutionException {
        String parsedScript = "1 [m] * 20";
        Script script = scriptEngine.newScriptFromString(parsedScript);
        Object result = script.execute();

        assertEquals("20 m", result.toString());
    }

    @Test
    public void testDivideQuantityType() throws ScriptParsingException, ScriptExecutionException {
        String parsedScript = "1 [m] / 2 [cm]";
        Script script = scriptEngine.newScriptFromString(parsedScript);
        Object result = script.execute();

        assertEquals("0.5 m/cm", result.toString());
    }

    @Test
    public void testDivideQuantityType_Number() throws ScriptParsingException, ScriptExecutionException {
        String parsedScript = "1 [m] / 2";
        Script script = scriptEngine.newScriptFromString(parsedScript);
        Object result = script.execute();

        assertEquals("0.5 m", result.toString());
    }

    @Test
    public void testDivide_Number_QuantityType() throws ScriptParsingException, ScriptExecutionException {
        String parsedScript = "1 / 2 [m]";
        Script script = scriptEngine.newScriptFromString(parsedScript);
        Object result = script.execute();

        assertEquals("0.5 1/m", result.toString());
    }

    private Item createNumberItem(String numberItemName, Class<@NonNull Temperature> dimension, UnDefType state) {
        NumberItem item = new NumberItem(numberItemName);
        item.setDimension(dimension);
        item.setUnitProvider(unitProvider);

        return item;
    }

}
