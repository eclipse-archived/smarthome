/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Stream2JSONInputStreamTest {

    private Stream2JSONInputStream collection2InputStream;

    private Gson GSON = new GsonBuilder().create();

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailForNullSource() throws IOException {
        new Stream2JSONInputStream(null).close();
    }

    @Test
    public void shouldReturnForEmptyStream() throws Exception {
        List<Object> emptyList = Collections.emptyList();
        collection2InputStream = new Stream2JSONInputStream(emptyList.stream());

        assertThat(inputStreamToString(collection2InputStream), is(GSON.toJson(emptyList)));
    }

    @Test
    public void shouldStreamSingleObjectToJSON() throws Exception {
        DummyObject dummyObject = new DummyObject("demoKey", "demoValue");
        ArrayList<DummyObject> dummyList = Lists.newArrayList(dummyObject);
        collection2InputStream = new Stream2JSONInputStream(dummyList.stream());

        assertThat(inputStreamToString(collection2InputStream), is(GSON.toJson(dummyList)));
    }

    @Test
    public void shouldStreamCollectionStreamToJSON() throws Exception {
        DummyObject dummyObject1 = new DummyObject("demoKey1", "demoValue1");
        DummyObject dummyObject2 = new DummyObject("demoKey2", "demoValue2");
        ArrayList<DummyObject> dummyCollection = Lists.newArrayList(dummyObject1, dummyObject2);
        collection2InputStream = new Stream2JSONInputStream(dummyCollection.stream());

        assertThat(inputStreamToString(collection2InputStream), is(GSON.toJson(dummyCollection)));
    }

    private String inputStreamToString(InputStream in) throws IOException {
        return IOUtils.toString(in);
    }

    @SuppressWarnings("unused")
    private class DummyObject {
        private String key;
        private String value;

        DummyObject(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
