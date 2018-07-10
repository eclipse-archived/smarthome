package org.eclipse.smarthome.automation.core.internal;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.util.RuleBuilder;
import org.eclipse.smarthome.config.core.Configuration;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SerializationTest {

    private Gson mapper;

    @Before
    public void setup() {
        mapper = new GsonBuilder().create();
    }

    private <T> T serializeDeserialize(Class<T> clazz, T instance) {
        return mapper.fromJson(mapper.toJson(instance), clazz);
    }

    @Test
    public void serializeDeserializeRule() {
        // Create simple rule with one trigger.
        final List<Trigger> triggers = new LinkedList<>();
        triggers.add(new Trigger("id", "typeUID", new Configuration()));
        final Rule rule = RuleBuilder.create("foo").withTriggers(triggers).build();

        // Try to serialize and deserialize rule
        final Rule deserialized = serializeDeserialize(Rule.class, rule);

        // Check if the rules are equals
        assertEquals(rule, deserialized);
    }
}
