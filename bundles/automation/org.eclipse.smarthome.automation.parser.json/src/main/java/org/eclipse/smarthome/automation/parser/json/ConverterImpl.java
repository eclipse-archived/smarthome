package org.eclipse.smarthome.automation.parser.json;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.smarthome.automation.parser.Converter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConverterImpl implements Converter {

    private Logger log;

    public ConverterImpl() {
        this.log = LoggerFactory.getLogger(ConverterImpl.class);
    }

    @Override
    public Dictionary getAsDictionary(String source) {
        Dictionary dict = null;
        JSONObject json = null;
        if (source != null && source.length() > 0 && source.charAt(0) == '{') {
            dict = new Hashtable();
            try {
                json = new JSONObject(source.trim());
                dict = parseJSON(json, dict);
            } catch (JSONException e) {
                log.error("Context property of ConfigurationProperty is not a valid JSON.", e);
            }
        }
        return dict;
    }

    private Dictionary parseJSON(JSONObject json, Dictionary dict) {
        Iterator<?> keys = json.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            try {
                if (json.get(key) instanceof JSONObject) {
                    Dictionary inner = parseJSON((JSONObject) json.get(key), new Hashtable());
                    dict.put(key, inner);
                } else {
                    dict.put(key, json.get(key));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return dict;
    }
}
