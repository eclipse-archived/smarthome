/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Converts between a flat map of <key,value> tuples to a nested map and back.
 * `key`s in the flat map express a hierarchy by the hash sign and dots. The schema is:
 *
 * key.instanceName#key.instanceName#...#key=value
 *
 * The `instanceName` can be anything but need to be unique among all instances at the same
 * hierarchy level for a given key.
 * The `key` part identifies the field name for the nested structure.
 *
 * The pairs of `key`.`instanceName`, separated by a dot, that form a hierarchy are separated by dashes.
 *
 * <h2>Example</h2>
 * A flat map looks like this:
 *
 * <pre>
 * autostart=true
 * broker.broker1#host=123.123.123.123
 * broker.broker1#secure=true
 * broker.broker2#host=124.124.124.124
 * broker.broker2#secure=true
 * </pre>
 *
 * and will be converted to:
 *
 * <pre>
 * autostart=true
 * broker
 *    broker1
 *      host=123.123.123.123
 *      secure=true
 *    broker2
 *      host=124.124.124.124
 *      secure=true
 * </pre>
 *
 * This class is useful in conjunction with {@link Configuration} and its
 * (un-)marshaling functionality.
 *
 * @author David Graeff - Initial contribution
 */
public class NestedConfigConverter {
    public final static String LEVEL_SEPARATOR = "#";
    public final static String INSTANCE_KEY_SEPARATOR = ".";

    /**
     * Keep track of the of the key path (example: broker.broker1#host)
     */
    private static class KeyPath implements Iterable<Key> {
        final List<Key> keys;

        private KeyPath(List<Key> keys) {
            this.keys = keys;
        }

        /**
         * Parses a string representation of a KeyPath (example: broker.broker1#host)
         * and returns a KeyPath instance.
         */
        public static KeyPath parse(String path) {
            ArrayList<Key> keys = new ArrayList<>();
            String[] parts = path.split(LEVEL_SEPARATOR);
            for (int i = 0; i < parts.length; ++i) {
                keys.add(Key.parse(parts[i]));
            }
            return new KeyPath(keys);
        }

        /**
         * Returns an iterator for all elements except the last, which
         * can be accessed via getLastKey().
         */
        @Override
        public Iterator<Key> iterator() {
            return new Iterator<NestedConfigConverter.Key>() {
                int index = 0;

                @Override
                public Key next() {
                    return keys.get(index++);
                }

                @Override
                public boolean hasNext() {
                    return index < keys.size() - 1;
                }
            };
        }

        /**
         * Add another hierarchy level to this KeyPath
         */
        public void add(Key key) {
            keys.add(key);
        }

        /**
         * Remove the topmost hierarchy level
         */
        public void cdUp() {
            if (keys.size() > 0) {
                keys.remove(keys.size() - 1);
            }
        }

        /**
         * Return the final key (example "broker.broker1#host" would return "host")
         */
        public String getLastKey() {
            return keys.get(keys.size() - 1).key;
        }

        /**
         * Print the string representation of the KeyPath
         * Example 1: "broker.broker1#host"
         * Example 2: "host"
         * Example 3: "" for an empty KeyPath
         */
        @Override
        public String toString() {
            if (keys.size() > 1) {
                return keys.stream().map(key -> key.toString()).collect(Collectors.joining(LEVEL_SEPARATOR));
            } else if (keys.size() == 1) {
                return getLastKey();
            } else {
                return "";
            }
        }
    }

    /**
     * Represents a KEY.INSTANCENAME tuple in a KeyPath of for instance "broker.broker1#host".
     * The instance name is optional. The example contains two {@link Key} objects:
     * 1) broker.broker1
     * 2) host
     */
    private static class Key {
        final String key;
        final String instaceName;

        public Key(@NonNull String key, @Nullable String instanceName) {
            this.key = key;
            instaceName = instanceName;
        }

        /**
         * Parses the string representation of a KEY.INSTANCENAME tuple.
         * For example: "broker.broker1" or "host"
         * Be aware that an instance name can contain dots.
         * For example "broker.br.1" will be parsed to <broker, br.1>.
         */
        @SuppressWarnings("null")
        public static Key parse(String pathPart) {
            String[] parts = pathPart.split("\\" + INSTANCE_KEY_SEPARATOR, 2);
            switch (parts.length) {
                case 1:
                    return new Key(parts[0], null);
                case 2:
                    return new Key(parts[0], parts[1]);
            }
            return null;
        }

        @Override
        public String toString() {
            if (instaceName != null) {
                return key + INSTANCE_KEY_SEPARATOR + instaceName;
            }
            return key;
        }
    }

    /**
     * Returns the Map<String, Object> in the `map` at the given `key` or create,
     * insert and return a map.
     *
     * @throws ClassCastException If the entry at "key" is not a map as expected.
     */
    private static Map<String, Object> createOrReturn(Map<String, Object> map, String key) throws ClassCastException {
        Object entry = map.get(key);
        if (entry == null) {
            Map<String, Object> next = new HashMap<>();
            map.put(key, next);
            map = next;
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapEntry = (Map<String, Object>) entry;
            map = mapEntry;
        }
        return map;
    }

    /**
     * Converts in-place the key-value pairs of a flat map to a nested map.
     *
     * The algorithm is simple. We iterate over all keys, create the
     * respective nested map structure for a given key and insert the value.
     *
     * @param keyValuePairs Key-value pairs
     * @return A nested map
     */
    public static void mapToNested(@NonNull Map<String, Object> keyValuePairs) {
        Set<Entry<String, Object>> entries = new HashSet<>(keyValuePairs.entrySet());
        keyValuePairs.clear();

        for (Entry<String, Object> keyValuePair : entries) {
            final KeyPath keyPath = KeyPath.parse(keyValuePair.getKey());
            Map<String, Object> current = keyValuePairs;
            for (Key key : keyPath) {
                current = createOrReturn(current, key.key);
                current = createOrReturn(current, key.instaceName);
            }
            current.put(keyPath.getLastKey(), keyValuePair.getValue());
        }
    }

    /**
     * This method will unfold the value and may recursively call itself again until all
     * nested structures are resolved and all flat <key,value> tuples are added to `dest`.
     *
     * @param dest The destination map
     * @param keyPath A KeyPath instance to keep track where we are in the structure
     * @param key A simple non-hierarchical key
     * @param value A value object. Might be another nested map
     * @throws ClassCastException If the nested map is not in the expected format, casts within this method will fail.
     */
    private static void unfold(@NonNull Map<String, Object> dest, @NonNull KeyPath keyPath, @NonNull String key,
            Object value) throws ClassCastException {
        if (value instanceof Map) {
            // If value is a Map, then we always expect 3 nested Maps:
            // Map<KEYNAME,Map<INSTANCENAME,MAP<VALUES>>>
            // The VALUES map can have nested configuration again though
            @SuppressWarnings("unchecked")
            final Map<String, Object> keyInstanceMap = (Map<String, Object>) value;
            for (Entry<String, Object> keyInstance : keyInstanceMap.entrySet()) {
                final String instance = keyInstance.getKey();
                final Key k = new Key(key, instance);
                keyPath.add(k);
                @SuppressWarnings("unchecked")
                final Map<String, Object> keyValueMap = (Map<String, Object>) keyInstance.getValue();
                for (Entry<String, Object> keyValue : keyValueMap.entrySet()) {
                    final String keyOfKeyValue = keyValue.getKey();
                    assert keyOfKeyValue != null;
                    unfold(dest, keyPath, keyOfKeyValue, keyValue.getValue());
                }
                // parse map
                keyPath.cdUp();
            }
        } else {
            keyPath.add(new Key(key, null));
            dest.put(keyPath.toString(), value);
            keyPath.cdUp();
        }
    }

    /**
     * Converts the a nested map structure to flat key-value pairs.
     *
     * @param nested A nested map
     * @return Key-value pairs
     */
    public static Map<String, Object> mapToFlat(@NonNull Map<String, Object> nested) {
        final Map<String, Object> flat = new HashMap<>();
        final KeyPath keyPath = new KeyPath(new ArrayList<>());
        for (Entry<String, Object> entry : nested.entrySet()) {
            final String key = entry.getKey();
            assert key != null;
            unfold(flat, keyPath, key, entry.getValue());
        }
        return flat;
    }
}
