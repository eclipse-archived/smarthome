/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.protocol;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * A generic handler that dynamically creates "standard" packet instances.
 *
 * <p>
 * Packet types must have an empty constructor and cannot require any
 * additional logic (other than parsing).
 * </p>
 *
 * @param <T> the packet subtype this handler constructs
 * 
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class GenericHandler<T extends Packet> implements PacketHandler<T> {

    private Constructor<T> constructor;

    private boolean typeFound;
    private int type;

    public boolean isTypeFound() {
        return typeFound;
    }

    public int getType() {
        return type;
    }

    public GenericHandler(Class<T> clazz) {
        try {
            constructor = clazz.getConstructor();
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("Packet class cannot be handled by GenericHandler", ex);
        }

        try {
            Field typeField = clazz.getField("TYPE");
            type = (int) typeField.get(null);
            typeFound = true;
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            // silently ignore
            typeFound = false;
        }

    }

    @Override
    public T handle(ByteBuffer buf) {
        try {
            T ret = constructor.newInstance();
            ret.parse(buf);
            return ret;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException("Unable to instantiate empty packet", ex);
        }
    }

}
