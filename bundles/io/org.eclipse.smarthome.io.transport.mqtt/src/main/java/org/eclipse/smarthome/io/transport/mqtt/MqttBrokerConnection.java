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
package org.eclipse.smarthome.io.transport.mqtt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.io.transport.mqtt.reconnect.AbstractReconnectStrategy;
import org.eclipse.smarthome.io.transport.mqtt.reconnect.PeriodicReconnectStrategy;
import org.eclipse.smarthome.io.transport.mqtt.sslcontext.AcceptAllCertificatesSSLContext;
import org.eclipse.smarthome.io.transport.mqtt.sslcontext.SSLContextProvider;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An MQTTBrokerConnection represents a single client connection to a MQTT broker. The connection is configured by the
 * MQTTService with properties from the smarthome.cfg file.
 *
 * When a connection to an MQTT broker is lost, it will try to reconnect every 60 seconds.
 *
 * @author David Graeff - All operations are async now. More flexible sslContextProvider and reconnectStrategy added.
 * @author Davy Vanherbergen
 * @author Markus Rathgeb - added connection state callback
 */
@NonNullByDefault
public class MqttBrokerConnection {

    private final Logger logger = LoggerFactory.getLogger(MqttBrokerConnection.class);
    public static final int DEFAULT_KEEPALIVE_INTERVAL = 60;
    public static final int DEFAULT_QOS = 0;

    /// Connection parameters
    protected final String host;
    protected final int port;
    protected final boolean secure;
    protected final String clientId;
    private @Nullable String user;
    private @Nullable String password;
    /// Configuration variables
    private int qos = DEFAULT_QOS;
    private boolean retain = false;
    private @Nullable MqttWillAndTestament lastWill;
    private @Nullable AbstractReconnectStrategy reconnectStrategy;
    private SSLContextProvider sslContextProvider = new AcceptAllCertificatesSSLContext();
    private int keepAliveInterval = DEFAULT_KEEPALIVE_INTERVAL;

    /// Runtime variables
    protected @Nullable MqttAsyncClient client;
    protected boolean isConnecting = false;
    protected final List<MqttConnectionObserver> connectionObservers = new CopyOnWriteArrayList<>();
    protected final Map<String, List<MqttMessageSubscriber>> consumers = new HashMap<>();
    protected @Nullable IMqttToken connectionToken;

    // Connection timeout handling
    final AtomicReference<@Nullable ScheduledFuture<?>> timeoutFuture = new AtomicReference<>(null);
    @Nullable
    ScheduledExecutorService timeoutExecutor;
    private int timeout = 1200; /* Connection timeout in milliseconds */

    /**
     * A private object to implement the MqttCallback interface.
     * We don't want the MqttBrokerConnection to implement this directly.
     * Developer hint: Unfortunately MqttCallback does not care about annotations for Null-correctness, we therefore
     * use @NonNullByDefault.
     */
    @NonNullByDefault({})
    protected static class ClientCallbacks implements MqttCallback {
        private final MqttBrokerConnection c;

        public ClientCallbacks(MqttBrokerConnection c) {
            this.c = c;
        }

        @Override
        public synchronized void connectionLost(@Nullable Throwable exception) {
            if (exception instanceof MqttException) {
                MqttException e = (MqttException) exception;
                c.logger.info("MQTT connection to '{}' was lost: {} : ReasonCode {} : Cause : {}", c.host,
                        e.getMessage(), e.getReasonCode(),
                        (e.getCause() == null ? "Unknown" : e.getCause().getMessage()));
            } else if (exception != null) {
                c.logger.info("MQTT connection to '{}' was lost: {}", c.host, exception.getMessage());
            }

            c.connectionObservers.forEach(o -> o.connectionStateChanged(MqttConnectionState.DISCONNECTED, exception));
            if (c.reconnectStrategy != null) {
                c.reconnectStrategy.lostConnection();
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            c.logger.trace("Message with id {} delivered.", token.getMessageId());
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            byte[] payload = message.getPayload();
            c.logger.trace("Received message on topic '{}' : {}", topic, new String(payload));
            c.consumers.forEach((target, consumerList) -> {
                if (topic.matches(target)) {
                    c.logger.trace("Topic match for '{}' using regex {}", topic, target);
                    consumerList.forEach(consumer -> consumer.processMessage(topic, payload));
                } else {
                    c.logger.trace("No topic match for '{}' using regex {}", topic, target);

                }
            });
        }
    }

    /**
     * Create a IMqttActionListener object for being used as a callback for a connection attempt.
     * The callback will interact with the {@link AbstractReconnectStrategy} as well as inform registered
     * {@link MqttConnectionObserver}s.
     */
    @NonNullByDefault({})
    protected static class ConnectionCallbacks implements IMqttActionListener {
        private final MqttBrokerConnection c;
        private final Runnable cancelTimeoutFuture;

        public ConnectionCallbacks(MqttBrokerConnection c) {
            this.c = c;
            this.cancelTimeoutFuture = c::cancelTimeoutFuture;
        }

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            cancelTimeoutFuture.run();

            c.isConnecting = false;
            if (c.reconnectStrategy != null) {
                c.reconnectStrategy.connectionEstablished();
            }
            c.consumers.values().stream().flatMap(List::stream).forEach(consumer -> c.trySubscribe(consumer));
            c.connectionObservers.forEach(o -> o.connectionStateChanged(c.connectionState(), null));
        }

        @Override
        public void onFailure(IMqttToken token, @Nullable Throwable exception) {
            cancelTimeoutFuture.run();

            final Throwable e = token.getException();
            final MqttConnectionState connectionState = c.connectionState();
            c.connectionObservers.forEach(o -> o.connectionStateChanged(connectionState, e));

            // If we tried to connect via start(), use the reconnect strategy to try it again
            if (c.isConnecting) {
                c.isConnecting = false;
                if (c.reconnectStrategy != null) {
                    c.reconnectStrategy.lostConnection();
                }
            }
        }
    }

    /** Client callback object. Package local, for testing. */
    protected MqttCallback clientCallbacks = new ClientCallbacks(this);
    /** Connection callback object. Package local, for testing. */
    protected IMqttActionListener connectionCallbacks = new ConnectionCallbacks(this);

    /**
     * Create a new MQTT client connection to a server with the given host and port.
     *
     * @param name for the connection.
     * @param host A host name or address
     * @param port A port or null to select the default port for a secure or insecure connection
     * @param secure A secure connection
     * @param clientId Client id. Each client on a MQTT server has a unique client id. Sometimes client ids are
     *            used for access restriction implementations.
     *            If none is specified, a default is generated. The client id cannot be longer than 65535 characters.
     * @throws IllegalArgumentException If the client id or port is not valid.
     */
    public MqttBrokerConnection(String host, @Nullable Integer port, boolean secure, @Nullable String clientId) {
        this.host = host;
        this.secure = secure;
        String newClientID = clientId;
        if (newClientID == null) {
            newClientID = MqttClient.generateClientId();
        } else if (newClientID.length() > 65535) {
            throw new IllegalArgumentException("Client ID cannot be longer than 65535 characters");
        }
        if (port != null && (port <= 0 || port > 65535)) {
            throw new IllegalArgumentException("Port is not within a valid range");
        }
        this.port = port != null ? port : (secure ? 8883 : 1883);
        this.clientId = newClientID;
        setReconnectStrategy(new PeriodicReconnectStrategy());
    }

    /**
     * Set the reconnect strategy. The implementor will be called when the connection
     * state to the MQTT broker changed.
     *
     * The reconnect strategy will not be informed if the initial connection to the broker
     * timed out. You need a timeout executor additionally, see {@link #setTimeoutExecutor(Executor)}.
     *
     * @param reconnectStrategy The reconnect strategy. May not be null.
     */
    public void setReconnectStrategy(AbstractReconnectStrategy reconnectStrategy) {
        this.reconnectStrategy = reconnectStrategy;
        reconnectStrategy.setBrokerConnection(this);
    }

    /**
     * @return Return the reconnect strategy
     */
    public @Nullable AbstractReconnectStrategy getReconnectStrategy() {
        return this.reconnectStrategy;
    }

    /**
     * Set a timeout executor. If none is set, you will not be notified of connection timeouts, this
     * also includes a non-firing reconnect strategy. The default executor is none.
     *
     * @param executor One timer will be created when a connection attempt happens
     * @param timeoutInMS Timeout in milliseconds
     */
    public void setTimeoutExecutor(@Nullable ScheduledExecutorService executor, int timeoutInMS) {
        timeoutExecutor = executor;
        this.timeout = timeoutInMS;
    }

    /**
     * Get the MQTT broker host
     */
    public String getHost() {
        return host;
    }

    /**
     * Get the MQTT broker port
     */
    public int getPort() {
        return port;
    }

    /**
     * Return true if it is a secure connection to the broker
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Set the optional user name and optional password to use when connecting to the MQTT broker.
     * The connection needs to be restarted for the new settings to take effect.
     *
     * @param user Name to use for connection.
     * @param password The password
     */
    public void setCredentials(@Nullable String user, @Nullable String password) {
        this.user = user;
        this.password = password;
    }

    /**
     * @return connection password.
     */
    public @Nullable String getPassword() {
        return password;
    }

    /**
     * @return optional user name for the MQTT connection.
     */

    public @Nullable String getUser() {
        return user;
    }

    /**
     * @return quality of service level.
     */
    public int getQos() {
        return qos;
    }

    /**
     * Set quality of service. Valid values are 0, 1, 2 and mean
     * "at most once", "at least once" and "exactly once" respectively.
     * The connection needs to be restarted for the new settings to take effect.
     *
     * @param qos level.
     */
    public void setQos(int qos) {
        if (qos >= 0 && qos <= 2) {
            this.qos = qos;
        } else {
            throw new IllegalArgumentException("The quality of service parameter must be >=0 and <=2.");
        }
    }

    /**
     * @return true if newly messages sent to the broker should be retained by the broker.
     */
    public boolean isRetain() {
        return retain;
    }

    /**
     * Set whether newly published messages should be retained by the broker.
     *
     * @param retain true to retain.
     */
    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    /**
     * Return the last will object or null if there is none.
     */
    public @Nullable MqttWillAndTestament getLastWill() {
        return lastWill;
    }

    /**
     * Set the last will object.
     *
     * @param lastWill The last will object or null.
     * @param applyImmediately If true, the connection will stopped and started for the new last-will to take effect
     *            immediately.
     * @throws MqttException
     * @throws ConfigurationException
     */
    public void setLastWill(@Nullable MqttWillAndTestament lastWill, boolean applyImmediately)
            throws ConfigurationException, MqttException {
        this.lastWill = lastWill;
        if (applyImmediately) {
            stop();
            start();
        }
    }

    /**
     * Set the last will object.
     * The connection needs to be restarted for the new settings to take effect.
     *
     * @param lastWill The last will object or null.
     */
    public void setLastWill(@Nullable MqttWillAndTestament lastWill) {
        this.lastWill = lastWill;
    }

    /**
     * Get client id to use when connecting to the broker.
     *
     * @return value clientId to use.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Returns the connection state
     */
    public MqttConnectionState connectionState() {
        if (isConnecting) {
            return MqttConnectionState.CONNECTING;
        }
        return (client != null && client.isConnected()) ? MqttConnectionState.CONNECTED
                : MqttConnectionState.DISCONNECTED;
    }

    /**
     * Set the keep alive interval. The default interval is 60 seconds. If no heartbeat is received within this
     * timeframe, the connection will be considered dead. Set this to a higher value on systems which may not always be
     * able to process the heartbeat in time.
     *
     * @param keepAliveInterval interval in seconds
     */
    public void setKeepAliveInterval(int keepAliveInterval) {
        if (keepAliveInterval <= 0) {
            throw new IllegalArgumentException("Keep alive cannot be <=0");
        }
        this.keepAliveInterval = keepAliveInterval;
    }

    /**
     * Return the keep alive internal in seconds
     */
    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    /**
     * Return the ssl context provider.
     */
    public SSLContextProvider getSSLContextProvider() {
        return sslContextProvider;
    }

    /**
     * Set the ssl context provider. The default provider is {@see AcceptAllCertifcatesSSLContext}.
     *
     * @return The ssl context provider. Should not be null, but the ssl context will in fact
     *         only be used if a ssl:// url is given.
     */
    public void setSSLContextProvider(SSLContextProvider sslContextProvider) {
        this.sslContextProvider = sslContextProvider;
    }

    /**
     * Return true if there are consumers registered via addConsumer().
     */
    public boolean hasConsumers() {
        return !consumers.isEmpty();
    }

    /**
     * Add a new message consumer to this connection. Multiple subscribers with the same
     * topic are allowed. This method will not protect you from adding a subscriber object
     * multiple times!
     *
     * @param consumer Consumer to add
     * @throws MqttException If connected and the subscribe fails, this exception is thrown.
     */
    public boolean addConsumer(MqttMessageSubscriber subscriber) throws MqttException {
        // Prepare topic for regex pattern matching taking place in messageArrived.

        String topic = prepareTopic(subscriber.getTopic());
        synchronized (consumers) {
            List<MqttMessageSubscriber> subscriberList = consumers.getOrDefault(topic, new ArrayList<>());
            consumers.put(topic, subscriberList);
            subscriberList.add(subscriber);
        }
        if (connectionState() == MqttConnectionState.CONNECTED && client != null) {
            try {
                client.subscribe(subscriber.getTopic(), qos);
            } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
                throw new MqttException(e);
            }
        }
        return true;
    }

    private String prepareTopic(String topic) {
        return StringUtils.replace(StringUtils.replace(topic, "+", "[^/]*"), "#", ".*");
    }

    /**
     * Remove a previously registered consumer from this connection.
     *
     * @param subscriber to remove.
     */
    public void removeConsumer(MqttMessageSubscriber subscriber) {
        logger.trace("Unsubscribing message consumer for topic '{}' from broker '{}'", subscriber.getTopic(), host);

        try {
            if (connectionState() == MqttConnectionState.CONNECTED && client != null) {
                client.unsubscribe(subscriber.getTopic());
            }
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            logger.info("Error unsubscribing topic from broker", e);
        }

        synchronized (consumers) {
            String topic = prepareTopic(subscriber.getTopic());
            final @Nullable List<MqttMessageSubscriber> list = consumers.get(topic);
            if (list != null) {
                list.remove(subscriber);
                if (list.isEmpty()) {
                    consumers.remove(topic);
                }
            }
        }
    }

    /**
     * Add a new connection observer to this connection.
     *
     * @param connectionObserver The connection observer that should be added.
     */
    public synchronized void addConnectionObserver(MqttConnectionObserver connectionObserver) {
        connectionObservers.add(connectionObserver);
    }

    /**
     * Remove a previously registered connection observer from this connection.
     *
     * @param connectionObserver The connection observer that should be removed.
     */
    public synchronized void removeConnectionObserver(MqttConnectionObserver connectionObserver) {
        connectionObservers.remove(connectionObserver);
    }

    /**
     * Return true if there are connection observers registered via addConnectionObserver().
     */
    public boolean hasConnectionObservers() {
        return !connectionObservers.isEmpty();
    }

    /**
     * Create a MqttConnectOptions object using the fields of this MqttBrokerConnection instance.
     * Package local, for testing.
     */
    MqttConnectOptions createMqttOptions() throws ConfigurationException {
        MqttConnectOptions options = new MqttConnectOptions();

        if (!StringUtils.isBlank(user)) {
            options.setUserName(user);
        }
        if (!StringUtils.isBlank(password) && password != null) {
            options.setPassword(password.toCharArray());
        }
        if (secure) {
            options.setSocketFactory(sslContextProvider.getContext().getSocketFactory());
        }

        if (lastWill != null) {
            MqttWillAndTestament lastWill = this.lastWill; // Make eclipse happy
            options.setWill(lastWill.getTopic(), lastWill.getPayload(), lastWill.getQos(), lastWill.isRetain());
        }

        options.setKeepAliveInterval(keepAliveInterval);
        return options;
    }

    /**
     * Tries to call `client.subscribe` on the given topic. Any errors are caught and redirected to the logger.
     *
     */
    private void trySubscribe(MqttMessageSubscriber c) {
        if (client != null) {
            try {
                client.subscribe(c.getTopic(), qos);
            } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
                logger.debug("Couldn't start subscriber", e);
            }
        }
    }

    /**
     * This will establish a connection to the MQTT broker and if successful, notify all
     * publishers and subscribers that the connection has become active. This method will
     * do nothing if there is already an active connection.
     *
     * If you want a synchronized way of establishing a broker connection, you can use the
     * following pattern:
     *
     * <pre>
     * Semaphore semaphore = new Semaphore(1);
     * semaphore.acquire();
     * MqttConnectionObserver mqttConnectionObserver = (state, error) -> {
     *     if (state != MqttConnectionState.CONNECTING) {
     *         semaphore.release();
     *     }
     * };
     * c.addConnectionObserver(mqttConnectionObserver);
     * c.start();
     * semaphore.tryAcquire(3000, TimeUnit.MILLISECONDS);
     * </pre>
     *
     * @throws MqttException If a communication error occurred, this exception is thrown.
     * @throws ConfigurationException If no url is given or parameters are invalid, this exception is thrown.
     */
    public synchronized void start() throws MqttException, ConfigurationException {
        if (connectionState() != MqttConnectionState.DISCONNECTED) {
            return;
        }
        // Ensure the reconnect strategy is started
        if (reconnectStrategy != null) {
            reconnectStrategy.start();
        }

        // Close client if there is still one existing
        if (client != null) {
            try {
                client.close();
            } catch (org.eclipse.paho.client.mqttv3.MqttException ignore) {
            }
            client = null;
        }

        // Perform the connection attempt
        isConnecting = true;
        connectionObservers.forEach(o -> o.connectionStateChanged(MqttConnectionState.CONNECTING, null));

        client = createAndConnectClient();

        ScheduledExecutorService executor = timeoutExecutor;
        if (executor != null && connectionToken != null) {
            final ScheduledFuture<?> timeoutFuture = this.timeoutFuture.getAndSet(
                    executor.schedule(() -> connectionCallbacks.onFailure(connectionToken, new TimeoutException()),
                            timeout, TimeUnit.MILLISECONDS));
            if (timeoutFuture != null) {
                timeoutFuture.cancel(false);
            }
        }
    }

    /**
     * Encapsulates the creation of the paho MqttAsyncClient and client connection.
     *
     * @param dataStore The datastore to save qos!=0 messages until they are delivered.
     * @return Returns a valid and connecting MqttAsyncClient
     * @throws MqttException If an exception of the underlying library happens, this exception is thrown
     * @throws ConfigurationException The configuration is not valid if this exception is thrown
     */
    protected MqttAsyncClient createAndConnectClient() throws MqttException, ConfigurationException {
        StringBuilder serverURI = new StringBuilder();
        serverURI.append((secure ? "ssl://" : "tcp://"));
        serverURI.append(host);
        serverURI.append(":");
        serverURI.append(port);

        // Storage
        Path tmpDir = Paths.get(ConfigConstants.getUserDataFolder());
        try {
            if (!tmpDir.isAbsolute()) {
                throw new IOException("User path not absolute!");
            }
            tmpDir = Files.createDirectories(tmpDir.resolve("mqtt").resolve(host));
        } catch (IOException e) {
            throw new MqttException(e);
        }
        MqttClientPersistence dataStore = new MqttDefaultFilePersistence(tmpDir.toString());

        MqttAsyncClient _client;
        try {
            _client = new MqttAsyncClient(serverURI.toString(), clientId, dataStore);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e);
        }
        _client.setCallback(clientCallbacks);
        try {
            connectionToken = _client.connect(createMqttOptions(), null, connectionCallbacks);
            logger.info("Starting MQTT broker connection to '{}' with clientid {} and file store '{}'", host,
                    getClientId(), tmpDir);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e);
        }
        return _client;
    }

    /**
     * Stop the MQTT connection.
     *
     * You can re-establish a connection calling {@link #start()} again.
     */
    public synchronized void stop() {
        logger.trace("Closing the MQTT broker connection '{}'", host);

        // Abort a connection attempt
        isConnecting = false;
        connectionToken = null;

        // Cancel a timeout future as on an explicit stop there should be no interest in anymore.
        cancelTimeoutFuture();

        // Stop the reconnect strategy
        if (reconnectStrategy != null) {
            reconnectStrategy.stop();
        }

        // Close connection
        try {
            if (connectionState() == MqttConnectionState.CONNECTED && client != null) {
                client.disconnect();
                client = null;
            }
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            logger.info("Error closing connection to broker", e);
        }

        connectionObservers.forEach(o -> o.connectionStateChanged(MqttConnectionState.DISCONNECTED, null));
    }

    /**
     * Publish a message to the broker.
     *
     * @param topic The topic
     * @param payload The message payload
     * @param listener An optional listener to be notified of success or failure of the delivery.
     * @return The message ID of the published message. Can be used in the callback to identify the asynchronous task.
     *         Returns -1 if not connected currently.
     * @throws MqttException
     */
    public int publish(String topic, byte[] payload, MqttPublishCallback listener) throws MqttException {
        MqttAsyncClient client_ = client;
        if (client_ == null) {
            return -1;
        }
        // publish message asynchronously
        IMqttDeliveryToken deliveryToken;
        try {
            deliveryToken = client_.publish(topic, payload, qos, retain, null, new IMqttActionListener() {
                @Override
                public void onSuccess(@Nullable IMqttToken token_) {
                    IMqttToken token = (@NonNull IMqttToken) token_; // token is never null, but the interface is not
                    // annotated correctly
                    listener.onSuccess(new MqttPublishResult(token.getMessageId(), topic));
                }

                @Override
                public void onFailure(@Nullable IMqttToken token, @Nullable Throwable error) {
                    if (token != null && error != null) {
                        listener.onFailure(new MqttPublishResult(token.getMessageId(), topic), error);
                    }
                }
            });
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e);
        }
        logger.debug("Publishing message {} to topic '{}'", deliveryToken.getMessageId(), topic);
        return deliveryToken.getMessageId();
    }

    /**
     * Cancel the timeout future is present.
     */
    protected void cancelTimeoutFuture() {
        final ScheduledFuture<?> timeoutFuture = this.timeoutFuture.getAndSet(null);
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false);
        }
    }

}
