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
import org.eclipse.smarthome.test.java.FailureReportRule;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.Lists;

import tec.uom.se.unit.Units;

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
        String parsedScript = "Switch1.state = ON;Switch1.state = OFF;Switch1.state = ON;Switch1.state";
        Script script = scriptEngine.newScriptFromString(parsedScript);
        Object switch1State = script.execute();

        assertNotNull(switch1State);
        assertEquals("org.eclipse.smarthome.core.library.types.OnOffType", switch1State.getClass().getName());
        assertEquals("ON", switch1State.toString());
    }

}
