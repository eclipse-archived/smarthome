/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.internal;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

/**
 * The {@link TradfriCoapClient} provides some convenience features over the
 * plain {@link CoapClient} from californium.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class TradfriCoapClient extends CoapClient {

    private static final int TIMEOUT = 2000;

    public TradfriCoapClient(URI uri) {
        super(uri);
        setTimeout(TIMEOUT);
    }

    /**
     * Starts observation of the resource and uses the given callback to provide updates.
     *
     * @param callback the callback to use for updates
     */
    public void startObserve(CoapCallback callback) {
        observe(new TradfriCoapHandler(callback));
    }

    /**
     * Asynchronously executes a GET on the resource and provides the result through a {@link CompletableFuture}.
     *
     * @return the future that will hold the result
     */
    public CompletableFuture<String> asyncGet() {
        CompletableFuture<String> future = new CompletableFuture<>();
        get(new TradfriCoapHandler(future));
        return future;
    }

    /**
     * Asynchronously executes a GET on the resource and provides the result to a given callback.
     *
     * @param callback the callback to use for the response
     */
    public void asyncGet(CoapCallback callback) {
        get(new TradfriCoapHandler(callback));
    }

    /**
     * Asynchronously executes a PUT on the resource with a payload and provides the result to a given callback.
     *
     * @param payload the payload to send with the PUT request
     * @param callback the callback to use for the response
     */
    public void asyncPut(String payload, CoapCallback callback) {
        put(new TradfriCoapHandler(callback), payload, MediaTypeRegistry.TEXT_PLAIN);
    }
}
