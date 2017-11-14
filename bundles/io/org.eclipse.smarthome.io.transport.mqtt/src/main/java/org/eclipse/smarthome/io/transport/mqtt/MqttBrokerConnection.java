/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.naming.ConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.eclipse.smarthome.io.transport.mqtt.internal.MqttSenderChannelImpl;
import org.eclipse.smarthome.io.transport.mqtt.reconnect.AbstractReconnectStrategy;
import org.eclipse.smarthome.io.transport.mqtt.reconnect.PeriodicReconnectStrategy;
import org.eclipse.smarthome.io.transport.mqtt.sslcontext.AcceptAllCertificatesSSLContext;
import org.eclipse.smarthome.io.transport.mqtt.sslcontext.SSLContextProvider;
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
@SuppressWarnings("deprecation")
public class MqttBrokerConnection {
    private final Logger logger = LoggerFactory.getLogger(MqttBrokerConnection.class);
    public static final int DEFAULT_KEEPALIVE_INTERVAL = 60;
    public static final int DEFAULT_QOS = 0;

    /// Configuration variables
    private final boolean textualConfiguredBroker;
    private final String name;
    private final String url;
    private String user;
    private String password;
    private int qos = DEFAULT_QOS;
    private boolean retain = false;
    private MqttWillAndTestament lastWill;
    private AbstractReconnectStrategy reconnectStrategy;
    private SSLContextProvider sslContextProvider = new AcceptAllCertificatesSSLContext();
    private int keepAliveInterval = DEFAULT_KEEPALIVE_INTERVAL;

    /// Runtime variables
    private String clientId;
    private MqttAsyncClient client;
    private boolean isConnecting = false;

    private final List<MqttConnectionObserver> connectionObservers = new CopyOnWriteArrayList<>();
    private final Map<String, List<MqttMessageSubscriber>> consumers = new HashMap<>();
    // This should be removed by 2018 and before ESH 1.0
    @Deprecated
    private final List<MqttMessageProducer> producers = new CopyOnWriteArrayList<MqttMessageProducer>();

    /**
     * A private object to implement the MqttCallback interface.
     * We don't want the MqttBrokerConnection to implement this directly.
     *
     */
    private class ClientCallbacks implements MqttCallback {
        @Override
        public synchronized void connectionLost(Throwable t) {
            if (t instanceof MqttException) {
                MqttException e = (MqttException) t;
                logger.info("MQTT connection to '{}' was lost: {} : ReasonCode {} : Cause : {}", getName(),
                        e.getMessage(), e.getReasonCode(),
                        (e.getCause() == null ? "Unknown" : e.getCause().getMessage()));
            } else {
                logger.info("MQTT connection to '{}' was lost: {}", getName(), t.getMessage());
            }

            for (final MqttConnectionObserver connectionObserver : connectionObservers) {
                connectionObserver.connectionStateChanged(MqttConnectionState.DISCONNECTED, t);
            }

            reconnectStrategy.lostConnection();
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            logger.trace("Message with id {} delivered.", token.getMessageId());
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            logger.trace("Received message on topic '{}' : {}", topic, new String(message.getPayload()));
            for (Map.Entry<String, List<MqttMessageSubscriber>> entry : consumers.entrySet()) {
                final String target = entry.getKey();
                final List<MqttMessageSubscriber> consumerList = entry.getValue();

                if (topic.matches(target)) {
                    logger.trace("Topic match for '{}' and '{}' using regex {}", topic, target);
                    for (MqttMessageSubscriber consumer : consumerList) {
                        consumer.processMessage(topic, message.getPayload());
                    }
                } else {
                    logger.trace("No topic match for '{}' and '{}' using regex {}", topic, target);
                }
            }
        }
    }

    private ClientCallbacks clientCallbacks = new ClientCallbacks();

    /**
     * Create a new connection with the given name.
     *
     * @param name for the connection.
     * @param url url string for the MQTT broker. Valid URL's are in the format: tcp://localhost:1883 or
     *            ssl://localhost:8883
     * @throws ConfigurationException
     */
    public MqttBrokerConnection(@NonNull String name, @NonNull String url, boolean textualConfiguredBroker)
            throws ConfigurationException {
        this.textualConfiguredBroker = textualConfiguredBroker;

        if (name.isEmpty()) {
            throw new ConfigurationException("No name for the broker set!");
        }
        if (url.isEmpty() || (!url.startsWith("tcp://") && !url.startsWith("ssl://"))) {
            throw new ConfigurationException(
                    "No valid url for the broker set! Must be tcp://localhost:1234 or ssl://localhost:1234. Port is optional.");
        }

        this.name = name;
        this.url = url;
        setReconnectStrategy(new PeriodicReconnectStrategy());
    }

    /**
     * Set the reconnect strategy. The implementor will be called when the connection
     * to the Mqtt broker is lost and also when it is established.
     *
     * @param reconnectStrategy The reconnect strategy. May not be null.
     */
    public void setReconnectStrategy(AbstractReconnectStrategy reconnectStrategy) {
        this.reconnectStrategy = reconnectStrategy;
        reconnectStrategy.setBrokerConnection(this);
    }

    public AbstractReconnectStrategy getReconnectStrategy() {
        return this.reconnectStrategy;
    }

    /**
     * @return name for the connection as defined in smarthome.cfg.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the url for the MQTT broker. Valid URL's are in the format:
     * tcp://localhost:1883 or ssl://localhost:8883
     *
     * @return url for the MQTT broker.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Return true if it is a textual configured broker (textual=true in the constructor).
     */
    public boolean isTextualConfiguredBroker() {
        return textualConfiguredBroker;
    }

    /**
     * Set the optional user name and optional password to use when connecting to the MQTT broker.
     *
     * @param user Name to use for connection.
     * @param password The password
     */
    public void setCredentials(String user, String password) {
        this.user = user;
        this.password = password;
    }

    /**
     * @return connection password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return optional user name for the MQTT connection.
     */
    public String getUser() {
        return user;
    }

    /**
     * @return quality of service level.
     */
    public int getQos() {
        return qos;
    }

    /**
     * Set quality of service. Valid values are 0,1,2
     *
     * @param qos level.
     */
    public void setQos(int qos) {
        if (qos >= 0 && qos <= 2) {
            this.qos = qos;
        }
    }

    /**
     * @return true if messages sent to the broker should be retained by the broker.
     */
    public boolean isRetain() {
        return retain;
    }

    /**
     * Set whether any published messages should be retained by the broker.
     *
     * @param retain true to retain.
     */
    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    /**
     * Return the last will object or null if there is none.
     */
    public MqttWillAndTestament getLastWill() {
        return lastWill;
    }

    /**
     * Set the last will object
     *
     * @param lastWill The last will object or null.
     */
    public void setLastWill(MqttWillAndTestament lastWill) {
        this.lastWill = lastWill;
    }

    /**
     * Set client id to use when connecting to the broker.
     * If none is specified, a default is generated. The client id cannot
     * be longer than 23 characters. Longer strings will be ignored.
     *
     * @param value clientId to use. Can be null.
     */
    public void setClientId(String value) {
        if (value != null && value.length() > 23) {
            return;
        }
        this.clientId = value;
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
     * Returns true if a connection to the Mqtt broker is established
     */
    public boolean isConnected() {
        return client != null && client.isConnected();
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
            return;
        }
        this.keepAliveInterval = keepAliveInterval;
    }

    /**
     * Return the keep alive internal in seconds
     */
    public int getKeepAliveInterval() {
        return this.keepAliveInterval;
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
        String topic = StringUtils.replace(StringUtils.replace(subscriber.getTopic(), "+", "[^/]*"), "#", ".*");
        synchronized (consumers) {
            List<MqttMessageSubscriber> subscriberList = consumers.get(topic);
            if (subscriberList == null) {
                subscriberList = new ArrayList<>();
            }
            subscriberList.add(subscriber);
            consumers.put(topic, subscriberList);
        }
        if (isConnected()) {
            try {
                client.subscribe(subscriber.getTopic(), qos);
            } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
                throw new MqttException(e);
            }
        }
        return true;
    }

    /**
     * Remove a previously registered consumer from this connection.
     *
     * @param subscriber to remove.
     */
    public void removeConsumer(MqttMessageSubscriber subscriber) {
        logger.trace("Unsubscribing message consumer for topic '{}' from broker '{}'", subscriber.getTopic(),
                getName());

        try {
            if (isConnected()) {
                client.unsubscribe(subscriber.getTopic());
            }
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            logger.info("Error unsubscribing topic from broker", e);
        }

        synchronized (consumers) {
            List<MqttMessageSubscriber> list = consumers.get(subscriber.getTopic());
            if (list != null) {
                list.remove(subscriber);
                if (list.isEmpty()) {
                    consumers.remove(subscriber.getTopic());
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
     * Add a new message producer to this connection.
     * This is deprecated. Use the publish() method instead.
     *
     * @deprecated
     * @param publisher to add.
     */
    @Deprecated
    public synchronized void addProducer(MqttMessageProducer publisher) {
        producers.add(publisher);
        if (isConnected()) {
            publisher.setSenderChannel(new MqttSenderChannelImpl(this));
        }
    }

    /**
     * Remove a previously registered producer from this connection.
     *
     * @deprecated
     * @param publisher to remove.
     */
    @Deprecated
    public synchronized void removeProducer(MqttMessageProducer publisher) {
        publisher.setSenderChannel(null);
        producers.remove(publisher);
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
        if (!StringUtils.isBlank(password)) {
            options.setPassword(password.toCharArray());
        }
        if (getUrl().toLowerCase().startsWith("ssl")) {
            options.setSocketFactory(sslContextProvider.getContext().getSocketFactory());
        }

        if (lastWill != null) {
            options.setWill(lastWill.getTopic(), lastWill.getPayload(), lastWill.getQos(), lastWill.isRetain());
        }

        options.setKeepAliveInterval(keepAliveInterval);
        return options;
    }

    /**
     * Create a IMqttActionListener object for being used as a callback for a connection attempt.
     * The callback will interact with the {@link AbstractReconnectStrategy} as well as inform registered
     * {@link MqttConnectionObserver}s.
     * Package local, for testing.
     */
    IMqttActionListener createConnectionListener() {
        return new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                isConnecting = false;

                reconnectStrategy.connectionEstablished();

                // start all consumers
                for (List<MqttMessageSubscriber> consumerList : consumers.values()) {
                    for (MqttMessageSubscriber c : consumerList) {
                        try {
                            client.subscribe(c.getTopic(), qos);
                        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
                            logger.debug("Couldn't start subscriber", e);
                        }
                    }
                }

                // start all producers
                for (MqttMessageProducer p : producers) {
                    p.setSenderChannel(new MqttSenderChannelImpl(MqttBrokerConnection.this));
                }

                for (final MqttConnectionObserver connectionObserver : connectionObservers) {
                    connectionObserver.connectionStateChanged(
                            isConnected() ? MqttConnectionState.CONNECTED : MqttConnectionState.DISCONNECTED, null);
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                for (final MqttConnectionObserver connectionObserver : connectionObservers) {
                    connectionObserver.connectionStateChanged(
                            isConnected() ? MqttConnectionState.CONNECTED : MqttConnectionState.DISCONNECTED,
                            asyncActionToken.getException());
                }

                // If we tried to connect via start(), use the reconnect strategy to try it again
                if (isConnecting) {
                    isConnecting = false;
                    reconnectStrategy.lostConnection();
                }
            }
        };
    }

    /**
     * This will establish a connection to the MQTT broker and if successful, notify all
     * publishers and subscribers that the connection has become active. This method will
     * do nothing if there is already an active connection.
     *
     * If you want a synchronised way of establishing a broker connection, you can use the
     * following pattern:
     *
     * Object o = new Object();
     * conn.addConnectionObserver((isConnected, error) -> o.notify() );
     * conn.start();
     * o.wait(timeout_in_ms);
     * boolean success = conn.isConnected();
     *
     * @throws MqttException If a communication error occurred, this exception is thrown.
     * @throws ConfigurationException If no url is given or parameters are invalid, this exception is thrown.
     */
    public synchronized void start() throws MqttException, ConfigurationException {
        if (isConnecting || isConnected()) {
            return;
        }

        // Ensure the reconnect strategy is started
        if (reconnectStrategy != null) {
            reconnectStrategy.start();
        }

        if (StringUtils.isBlank(clientId) || clientId.length() > 23) {
            clientId = MqttClient.generateClientId();
        }

        // Storage
        String tmpDir = System.getProperty("java.io.tmpdir") + "/" + getName();
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);

        // Create client
        try {
            client = new MqttAsyncClient(getUrl(), clientId, dataStore);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e);
        }
        client.setCallback(clientCallbacks);

        logger.info("Starting MQTT broker connection '{}' to '{}' with clientid {} and file store '{}'", getName(),
                getUrl(), getClientId(), tmpDir);

        // Perform the connection attempt
        isConnecting = true;

        try {
            client.connect(createMqttOptions(), null, createConnectionListener());
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e);
        }
    }

    /**
     * Close the MQTT connection.
     *
     * You can re-establish a connection calling {@link #start()} again.
     */
    public synchronized void close() {
        logger.trace("Closing the MQTT broker connection '{}'", getName());

        // Abort a connection attempt
        isConnecting = false;

        // Stop the reconnect strategy
        if (reconnectStrategy != null) {
            reconnectStrategy.stop();
        }

        // Close connection
        try {
            if (isConnected()) {
                client.disconnect();
                client = null;
            }
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            logger.info("Error closing connection to broker", e);
        }

        for (final MqttConnectionObserver connectionObserver : connectionObservers) {
            connectionObserver.connectionStateChanged(MqttConnectionState.DISCONNECTED, null);
        }
    }

    /**
     * Publish a message to the broker.
     *
     * @param topic The topic
     * @param payload The message payload
     * @param listener An optional listener to be notified of success or failure of the delivery.
     * @return The message ID of the published message. Can be used in the callback to identify the asynchronous task.
     * @throws MqttException
     */
    public int publish(String topic, byte[] payload, MqttPublishCallback listener) throws MqttException {
        // publish message asynchronously
        IMqttDeliveryToken deliveryToken;
        try {
            deliveryToken = client.publish(topic, payload, qos, retain, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken token) {
                    listener.onSuccess(new MqttPublishResult(token.getMessageId(), topic));
                }

                @Override
                public void onFailure(IMqttToken token, Throwable error) {
                    listener.onFailure(new MqttPublishResult(token.getMessageId(), topic), error);
                }
            });
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e);
        }
        logger.debug("Publishing message {} to topic '{}'", deliveryToken.getMessageId(), topic);
        return deliveryToken.getMessageId();
    }
}
