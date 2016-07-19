/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.storage.mapdb;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/**
 * TypeAdapterFactory responsible for returning a new instance of {@link PropertiesTypeAdapter} if the given type
 * matches Map<String, Object>
 * or null otherwise.
 *
 * @author Ivan Iliev
 *
 */
public class PropertiesTypeAdapterFactory implements TypeAdapterFactory {

    @SuppressWarnings({ "unused", "unchecked" })
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Type type = typeToken.getType();

        Class<? super T> rawType = typeToken.getRawType();
        if (!PropertiesTypeAdapter.TOKEN.equals(typeToken)) {
            return null;
        }

        return (TypeAdapter<T>) new PropertiesTypeAdapter(gson);
    }

}
