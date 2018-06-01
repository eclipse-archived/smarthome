/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.io.mqttembeddedbroker.internal;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.io.mqttembeddedbroker.internal.MqttEmbeddedBrokerDetectStart.MqttEmbeddedBrokerStartedListener;
import org.eclipse.smarthome.io.mqttembeddedbroker.internal.MqttEmbeddedBrokerMetrics.BrokerMetricsListener;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.eclipse.smarthome.io.transport.mqtt.MqttServiceObserver;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.moquette.BrokerConstants;
import io.moquette.server.Server;
import io.moquette.server.config.MemoryConfig;
import io.moquette.spi.security.IAuthorizator;

/**
 * The {@link EmbeddedBrokerService} starts the embedded broker, creates a
 * {@link MqttBrokerConnection} and adds it to the {@link MqttService}.
 *
 * TODO: wait for NetworkServerTls implementation to enable secure connections as well
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = true, service = EmbeddedBrokerService.class, configurationPid = "org.eclipse.smarthome.mqttembeddedbroker", property = {
        Constants.SERVICE_PID + "=org.eclipse.smarthome.mqttembeddedbroker",
        ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI + "=mqtt:mqttembeddedbroker",
        ConfigurableService.SERVICE_PROPERTY_CATEGORY + "=MQTT",
        ConfigurableService.SERVICE_PROPERTY_LABEL + "=MQTT Embedded Broker" })
public class EmbeddedBrokerService implements ConfigurableService, MqttConnectionObserver, MqttServiceObserver,
        MqttEmbeddedBrokerStartedListener, BrokerMetricsListener {
    private MqttService service;
    // private NetworkServerTls networkServerTls; //TODO wait for NetworkServerTls implementation

    private static final String CLIENTID = "embedded-mqtt-broker";
    protected Server server;
    private final Logger logger = LoggerFactory.getLogger(EmbeddedBrokerService.class);
    protected MqttEmbeddedBrokerDetectStart detectStart = new MqttEmbeddedBrokerDetectStart(this);
    protected MqttEmbeddedBrokerMetrics metrics = new MqttEmbeddedBrokerMetrics(this);

    public MqttBrokerConnection connection;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setMqttService(MqttService service) {
        this.service = service;
    }

    public void unsetMqttService(MqttService service) {
        this.service = service;
    }

    // TODO wait for NetworkServerTls implementation
    // @Reference(cardinality = ReferenceCardinality.MANDATORY)
    // public void setNetworkServerTls(NetworkServerTls networkServerTls) {
    // this.networkServerTls = networkServerTls;
    // }

    @Activate
    public void activate(Map<String, Object> data) {
        initialize(new Configuration(data).as(ServiceConfiguration.class));
    }

    public void initialize(ServiceConfiguration config) {
        if (config.port == null) {
            config.port = config.secure ? 8883 : 1883;
        }

        // Create MqttBrokerConnection
        connection = service.getBrokerConnection(CLIENTID);
        if (connection != null) {
            // Close the existing connection and remove it from the service
            connection.stop();
            service.removeBrokerConnection(CLIENTID);
        }

        connection = new MqttBrokerConnection("127.0.0.1", config.port, config.secure, CLIENTID);
        connection.addConnectionObserver(this);

        if (config.username != null) {
            connection.setCredentials(config.username, config.password);
        }

        // Start embedded server
        try {
            startEmbeddedServer(config.port, config.secure, config.username, config.password, config.persistenceFile);
        } catch (IOException e) {
            logger.debug("Could not start embedded broker", e);
            logger.debug(e.getLocalizedMessage());
            return;
        }
    }

    @Deactivate
    public void deactivate() {
        if (connection != null) {
            connection.removeConnectionObserver(this);
            connection = null;
        }
        if (service != null) {
            service.removeBrokersListener(this);
        }
        if (server != null) {
            server.stopServer();
            server = null;
        }
    }

    @Override
    public void brokerAdded(String brokerID, MqttBrokerConnection broker) {
    }

    @Override
    public void brokerRemoved(String brokerID, MqttBrokerConnection broker) {
        if (broker == connection) {
            service.addBrokerConnection(brokerID, broker);
        }
    }

    /**
     * Starts the embedded broker.
     *
     * @param port The broker port.
     * @param secure Allow only secure connections if true or only plain connections otherwise.
     * @param username Broker authentication user name. May be null.
     * @param password Broker authentication password. May be null.
     * @param persistence_filename The filename were persistent data should be stored.
     * @throws IOException If any error happens, like the port is already in use, this exception is thrown.
     */
    protected void startEmbeddedServer(Integer portParam, boolean secure, String username, String password,
            @NonNull String persistenceFilenameParam) throws IOException {
        Integer port = portParam;
        String persistenceFilename = persistenceFilenameParam;
        Server server = new Server();
        Properties properties = new Properties();

        // Host and port
        properties.put(BrokerConstants.HOST_PROPERTY_NAME, "127.0.0.1");
        if (secure) {
            if (port == null) {
                port = 8883;
            }
            properties.put(BrokerConstants.SSL_PORT_PROPERTY_NAME, Integer.toString(port));
            properties.put(BrokerConstants.PORT_PROPERTY_NAME, BrokerConstants.DISABLED_PORT_BIND);
            properties.put(BrokerConstants.KEY_MANAGER_PASSWORD_PROPERTY_NAME, "esheshesh");
        } else {
            if (port == null) {
                port = 1883;
            }
            // with SSL_PORT_PROPERTY_NAME set, netty tries to evaluate the SSL context and shuts down immediately.
            // properties.put(BrokerConstants.SSL_PORT_PROPERTY_NAME, BrokerConstants.DISABLED_PORT_BIND);
            properties.put(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(port));
        }

        // Authentication
        io.moquette.spi.security.IAuthenticator authentificator = null;
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            properties.put(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, false);
            properties.put(BrokerConstants.AUTHENTICATOR_CLASS_NAME,
                    MqttEmbeddedBrokerUserAuthenticator.class.getName());
            authentificator = new MqttEmbeddedBrokerUserAuthenticator(username, password.getBytes());
        } else {
            properties.put(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, true);
        }

        // Persistence:

        // The user may have entered a full path. Check for '/' for Unix based systems and for a
        // a colon as second character for windows based paths ("C:/abc")
        if (!persistenceFilename.startsWith("/")
                && !(persistenceFilename.length() > 2 && persistenceFilename.charAt(1) == ':')) {
            persistenceFilename = ConfigConstants.getUserDataFolder() + File.separator + persistenceFilename;
        }

        // The user may have set an empty string on purpose: Disable persistence file
        if (!persistenceFilename.isEmpty()) {
            properties.put(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, persistenceFilename);
        }

        // We may provide ACL functionality at some point as well
        IAuthorizator authorizer = null;

        // Secure connection support
        // TODO wait for NetworkServerTls implementation
        // try {
        // final SSLContext sslContext = networkServerTls.createSSLContext("mqtt");
        // server.startServer(new MemoryConfig(properties), null, () -> sslContext, authentificator, authorizer);
        // } catch (GeneralSecurityException | IOException e) {
        // logger.error("No SSL available", e);
        server.startServer(new MemoryConfig(properties), null, null, authentificator, authorizer);
        // }

        this.server = server;
        metrics.setServer(server);
        ScheduledExecutorService s = new ScheduledThreadPoolExecutor(1);
        detectStart.startBrokerStartedDetection(port, s);
    }

    /**
     * Stops the embedded broker, if it is started.
     */
    protected void stopEmbeddedServer() {
        if (this.server != null) {
            server.stopServer();
            server = null;
        }
        detectStart.stopBrokerStartDetection();
        metrics.setServer(null);
    }

    /**
     * For testing: Returns true if the embedded server confirms that the MqttBrokerConnection is connected.
     */
    protected boolean serverConfirmsEmbeddedClient() {
        return server != null && server.getConnectionsManager().isConnected(CLIENTID);
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, Throwable error) {
        if (state == MqttConnectionState.CONNECTED) {
            logger.debug("Embedded broker connection connected");
        } else {
            if (error == null) {
                logger.debug("Offline - Reason unknown");
            } else {
                logger.debug(error.getMessage());
            }
        }

        if (state != MqttConnectionState.CONNECTED && state != MqttConnectionState.CONNECTING) {
            stopEmbeddedServer();
        }
    }

    @Override
    public void mqttEmbeddedBrokerStarted(boolean timeout) {
        service.addBrokerConnection(CLIENTID, connection);

        // Try a reconnect if not connected:
        if (connection.connectionState() != MqttConnectionState.CONNECTED) {
            try {
                connection.start();
            } catch (ConfigurationException | MqttException e) {
                logger.debug(e.getLocalizedMessage());
            }
        } else {
            connectionStateChanged(MqttConnectionState.CONNECTED, null);
        }

    }

    @Override
    public void connectedClientIDs(Collection<String> clientIDs) {
        logger.debug("Connected clients: {}", clientIDs.stream().collect(Collectors.joining(", ")));
    }
}
