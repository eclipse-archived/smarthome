/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.test;

/**
 * Builder for the current state of a hue light.
 *
 * @author Dominic Lerbs - Initial contribution
 * @author Markus Mazurczak - Added possibility to set modelId to "PAR16 50 TW" to test osram workaround
 * @author Markus Rathgeb - migrated to plain Java test
 */
public class HueLightState {

    int brightness = 200;
    int hue = 50000;
    int saturation = 0;
    int colorTemperature = 153;
    boolean isOn = true;
    String alert = "none";
    String effect = "none";
    String model = "LCT001";

    public HueLightState() {
    }

    public HueLightState(String model) {
        this.model = model;
    }

    public HueLightState bri(int brightness) {
        this.brightness = brightness;
        return this;
    }

    public HueLightState hue(int hue) {
        this.hue = hue;
        return this;
    }

    public HueLightState sat(int saturation) {
        this.saturation = saturation;
        return this;
    }

    public HueLightState ct(int colorTemperature) {
        this.colorTemperature = colorTemperature;
        return this;
    }

    public HueLightState on(boolean isOn) {
        this.isOn = isOn;
        return this;
    }

    public HueLightState alert(String alert) {
        this.alert = alert;
        return this;
    }

    public HueLightState effect(String effect) {
        this.effect = effect;
        return this;
    }

    @Override
    public String toString() {
        return "" + //
                "{\"lights\":" + //
                "  {" + //
                "    \"1\": {" + //
                "      \"state\": {" + //
                "        \"on\": " + isOn + "," + //
                "        \"bri\": " + brightness + "," + //
                "        \"hue\": " + hue + "," + //
                "        \"sat\": " + saturation + "," + //
                "        \"xy\": [" + //
                "          0," + //
                "          0" + //
                "        ]," + //
                "        \"ct\": " + colorTemperature + "," + //
                "        \"alert\": \"" + alert + "\"," + //
                "        \"effect\": \"" + effect + "\"," + //
                "        \"colormode\": \"hs\"," + //
                "        \"reachable\": true" + //
                "      }," + //
                "      \"type\": \"Extended color light\"," + //
                "      \"name\": \"Hue Light 1\"," + //
                "      \"modelid\": \"" + model + "\"," + //
                "      \"swversion\": \"65003148\"," + //
                "      \"uniqueid\": \"00:17:88:01:00:e1:88:29-0b\"," + //
                "      \"pointsymbol\": {" + //
                "        \"1\": \"none\"," + //
                "        \"2\": \"none\"," + //
                "        \"3\": \"none\"," + //
                "        \"4\": \"none\"," + //
                "        \"5\": \"none\"," + //
                "        \"6\": \"none\"," + //
                "        \"7\": \"none\"," + //
                "        \"8\": \"none\"" + //
                "      }" + //
                "    }" + //
                "  }" + //
                "}";
    }
}
