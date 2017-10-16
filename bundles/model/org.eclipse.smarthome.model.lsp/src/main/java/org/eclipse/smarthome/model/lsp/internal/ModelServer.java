/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.lsp.internal;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.smarthome.model.script.ScriptServiceUtil;
import org.eclipse.smarthome.model.script.engine.ScriptEngine;
import org.eclipse.xtext.ide.server.LanguageServerImpl;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;

/**
 * A service component exposing a Language Server via sockets.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@Component(immediate = true)
public class ModelServer {

    private final Logger logger = LoggerFactory.getLogger(ModelServer.class);
    private final int PORT = 5007;
    private ServerSocket socket;

    private ScriptServiceUtil scriptServiceUtil;
    private ScriptEngine scriptEngine;

    @Activate
    public void activate() {
        new Thread(() -> {
            listen();
        }, "Language Server").start();
    }

    @Deactivate
    public void deactivate() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            logger.error("Error shutting down the Language Server", e);
        }
    }

    private void listen() {
        try {
            socket = new ServerSocket(PORT);
            logger.info("Language Server started on port {}", PORT);
            while (!socket.isClosed()) {
                logger.debug("Going to wait for a client to connect");
                try {
                    Socket client = socket.accept();
                    new Thread(() -> {
                        handleConnection(client);
                    }, "Client " + client.getRemoteSocketAddress()).start();
                } catch (IOException e) {
                    logger.error("Error accepting the client connection", e);
                }
            }
        } catch (IOException e) {
            logger.error("Error starting the Language Server", e);
        }
    }

    private void handleConnection(final Socket client) {
        logger.debug("Client {} connected", client.getRemoteSocketAddress());
        try {
            LanguageServerImpl languageServer = Guice
                    .createInjector(new RuntimeServerModule(scriptServiceUtil, scriptEngine))
                    .getInstance(LanguageServerImpl.class);

            Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(languageServer,
                    client.getInputStream(), client.getOutputStream());
            languageServer.connect(launcher.getRemoteProxy());
            Future<?> future = launcher.startListening();
            future.get();
        } catch (IOException e) {
            logger.warn("Error communicating with LSP client {}", client.getRemoteSocketAddress());
        } catch (InterruptedException e) {
            // go on, let the thread finish
        } catch (ExecutionException e) {
            logger.error("Error running the Language Server", e);
        }
        logger.debug("Client {} disconnected", client.getRemoteSocketAddress());
    }

    @Reference
    public void setScriptServiceUtil(ScriptServiceUtil scriptServiceUtil) {
        this.scriptServiceUtil = scriptServiceUtil;
    }

    public void unsetScriptServiceUtil(ScriptServiceUtil scriptServiceUtil) {
        this.scriptServiceUtil = null;
    }

    @Reference
    public void setScriptEngine(ScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    public void unsetScriptEngine(ScriptEngine scriptEngine) {
        this.scriptEngine = null;
    }

}
