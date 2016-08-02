/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt.internal;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttMessageConsumer;
import org.eclipse.smarthome.io.transport.mqtt.MqttMessageProducer;
import org.eclipse.smarthome.io.transport.mqtt.MqttSenderChannel;
import org.eclipse.smarthome.io.transport.mqtt.MqttWillAndTestament;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An MQTTBrokerConnection represents a single client connection to a MQTT broker. The connection is configured by the
 * MQTTService with properties from the smarthome.cfg file.
 *
 * When a connection to an MQTT broker is lost, it will try to reconnect every 60 seconds.
 *
 * @author Davy Vanherbergen
 * @author Markus Rathgeb - added connection state callback
 */
public class MqttBrokerConnection implements MqttCallback {

    private final Logger logger = LoggerFactory.getLogger(MqttBrokerConnection.class);

    private static final int RECONNECT_FREQUENCY = 60000;

    private String name;

    private String url;

    private String user;

    private String password;

    private int qos = 0;

    private boolean retain = false;

    private boolean async = true;

    private MqttWillAndTestament lastWill;

    private String clientId;

    private MqttClient client;

    private boolean started;

    private int keepAliveInterval = 60;

    private List<MqttConnectionObserver> connectionObservers = new CopyOnWriteArrayList<>();

    private List<MqttMessageConsumer> consumers = new CopyOnWriteArrayList<MqttMessageConsumer>();

    private List<MqttMessageProducer> producers = new CopyOnWriteArrayList<MqttMessageProducer>();

    private Timer reconnectTimer;

    /**
     * Create a new connection with the given name.
     *
     * @param name for the connection.
     */
    public MqttBrokerConnection(String name) {
        this.name = name;
    }

    private void setConnected(final MqttConnectionObserver connectionObserver, final boolean connected) {
        try {
            connectionObserver.setConnected(connected);
        } catch (final Exception ex) {
            logger.trace("The recipient of the connection state callback throws an exception.", ex);
        }
    }

    private synchronized void setStarted(final boolean started) {
        if (started) {
            this.started = true;
            for (final MqttConnectionObserver connectionObserver : connectionObservers) {
                setConnected(connectionObserver, true);
            }
        } else {
            for (final MqttConnectionObserver connectionObserver : connectionObservers) {
                setConnected(connectionObserver, false);
            }
            this.started = false;
        }
    }

    /**
     * Start the connection. This will try to open an MQTT client connection to the MQTT broker and notify all
     * publishers and subscribers on this connection that the connection has become active.
     *
     * @throws Exception If connection could not be created.
     */
    public synchronized void start() throws Exception {

        if (StringUtils.isEmpty(url)) {
            logger.debug("No url defined for MQTT broker connection '{}'. Not starting.", name);
            return;
        }

        logger.info("Starting MQTT broker connection '{}'", name);

        if (client != null) {
            if (!this.url.equals(client.getServerURI()) || !this.clientId.equals(client.getClientId())) {
                if (client.isConnected()) {
                    logger.info("MQTT broker server URI or client ID changed. Removing previous connection.");
                    client.disconnect();
                }
                client = null;
            }
        }

        openConnection();

        if (reconnectTimer != null) {
            // we are active, so stop trying to reconnect
            reconnectTimer.cancel();
        }

        // start all consumers
        for (MqttMessageConsumer c : consumers) {
            startConsumer(c);
        }

        // start all producers
        for (MqttMessageProducer p : producers) {
            startProducer(p);
        }

        setStarted(true);
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
     * Set the url for the MQTT broker. Valid URL's are in the format:
     * tcp://localhost:1883 or ssl://localhost:8883
     *
     * @param url url string for the MQTT broker.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return optional user name for the MQTT connection.
     */
    public String getUser() {
        return user;
    }

    /**
     * Set the optional user name to use when connecting to the MQTT broker.
     *
     * @param user name to use for connection.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return connection password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the optional password to use when connecting to the MQTT broker.
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
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

    public MqttWillAndTestament getLastWill() {
        return lastWill;
    }

    public void setLastWill(MqttWillAndTestament lastWill) {
        this.lastWill = lastWill;
    }

    /**
     * @return true if messages are sent asynchronously.
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * Set whether messages should be sent synchronously (the message is sent and the thread waits until delivery to the
     * broker has completed) or asynchronously (the message is sent and the sendign thread does not wait for delivery
     * completion). In the case of async, the sending thread currently does not receive any feedback when delivery is
     * completed.
     *
     * @param async
     */
    public void setAsync(boolean async) {
        this.async = async;
    }

    /**
     * Set client id to use when connecting to the broker. If none is specified, a default is generated.
     *
     * @param value clientId to use.
     */
    public void setClientId(String value) {
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
     * Open an MQTT client connection.
     *
     * @throws Exception
     */
    private void openConnection() throws Exception {
        if (client != null && client.isConnected()) {
            return;
        }

        if (StringUtils.isBlank(url)) {
            throw new Exception("Missing url");
        }

        if (client == null) {
            if (StringUtils.isBlank(clientId) || clientId.length() > 23) {
                clientId = MqttClient.generateClientId();
            }

            String tmpDir = System.getProperty("java.io.tmpdir") + "/" + name;
            MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
            logger.debug("Creating new client for '{}' using id '{}' and file store '{}'", url, clientId, tmpDir);
            client = new MqttClient(url, clientId, dataStore);
            client.setCallback(this);
        }

        MqttConnectOptions options = new MqttConnectOptions();

        if (!StringUtils.isBlank(user)) {
            options.setUserName(user);
        }
        if (!StringUtils.isBlank(password)) {
            options.setPassword(password.toCharArray());
        }
        if (url.toLowerCase().contains("ssl")) {

            if (StringUtils.isNotBlank(System.getProperty("com.ibm.ssl.protocol"))) {

                // get all com.ibm.ssl properties from the system properties
                // and set them as the SSL properties to use.

                Properties sslProps = new Properties();
                addSystemProperty("com.ibm.ssl.protocol", sslProps);
                addSystemProperty("com.ibm.ssl.contextProvider", sslProps);
                addSystemProperty("com.ibm.ssl.keyStore", sslProps);
                addSystemProperty("com.ibm.ssl.keyStorePassword", sslProps);
                addSystemProperty("com.ibm.ssl.keyStoreType", sslProps);
                addSystemProperty("com.ibm.ssl.keyStoreProvider", sslProps);
                addSystemProperty("com.ibm.ssl.trustStore", sslProps);
                addSystemProperty("com.ibm.ssl.trustStorePassword", sslProps);
                addSystemProperty("com.ibm.ssl.trustStoreType", sslProps);
                addSystemProperty("com.ibm.ssl.trustStoreProvider", sslProps);
                addSystemProperty("com.ibm.ssl.enabledCipherSuites", sslProps);
                addSystemProperty("com.ibm.ssl.keyManager", sslProps);
                addSystemProperty("com.ibm.ssl.trustManager", sslProps);

                options.setSSLProperties(sslProps);

            } else {

                // use standard JSSE available in the runtime and
                // use TLSv1.2 which is the default for a secured mosquitto
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, new TrustManager[] { getVeryTrustingTrustManager() },
                        new java.security.SecureRandom());
                SSLSocketFactory socketFactory = sslContext.getSocketFactory();
                options.setSocketFactory(socketFactory);
            }
        }

        if (lastWill != null) {
            options.setWill(lastWill.getTopic(), lastWill.getPayload(), lastWill.getQos(), lastWill.isRetain());
        }

        options.setKeepAliveInterval(keepAliveInterval);

        client.connect(options);
    }

    /**
     * Create a trust manager which is not too concerned about validating certificates.
     *
     * @return a trusting trust manager
     */
    private TrustManager getVeryTrustingTrustManager() {

        return new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        };

    }

    private Properties addSystemProperty(String key, Properties props) {
        String value = System.getProperty(key);
        if (StringUtils.isNotBlank(value)) {
            props.put(key, value);
        }
        return props;
    }

    /**
     * Add a new connection observer to this connection.
     *
     * @param connectionObserver The connection observer that should be added.
     */
    public synchronized void addConnectionObserver(MqttConnectionObserver connectionObserver) {
        connectionObservers.add(connectionObserver);
        setConnected(connectionObserver, started);
    }

    /**
     * Add a new message producer to this connection.
     *
     * @param publisher to add.
     */
    public synchronized void addProducer(MqttMessageProducer publisher) {
        producers.add(publisher);
        if (started) {
            startProducer(publisher);
        }
    }

    /**
     * Start a registered producer, so that it can start sending messages.
     *
     * @param publisher to start.
     */
    private void startProducer(MqttMessageProducer publisher) {

        logger.trace("Starting message producer for broker '{}'", name);

        publisher.setSenderChannel(new MqttSenderChannel() {

            @Override
            public void publish(String topic, byte[] payload) throws Exception {

                if (!started) {
                    logger.warn("Broker connection not started. Cannot publish message to topic '{}'", topic);
                    return;
                }

                // Create and configure a message
                MqttMessage message = new MqttMessage(payload);
                message.setQos(qos);
                message.setRetained(retain);

                // publish message asynchronously
                MqttTopic mqttTopic = client.getTopic(topic);
                MqttDeliveryToken deliveryToken = mqttTopic.publish(message);

                logger.debug("Publishing message {} to topic '{}'", deliveryToken.getMessageId(), topic);
                if (!async) {
                    // wait for publish confirmation
                    deliveryToken.waitForCompletion(10000);
                    if (!deliveryToken.isComplete()) {
                        logger.error(
                                "Did not receive completion message within timeout limit whilst publishing to topic '{}'",
                                topic);
                    }
                }

            }
        });

    }

    /**
     * Add a new message consumer to this connection.
     *
     * @param consumer to add.
     */
    public synchronized void addConsumer(MqttMessageConsumer subscriber) {
        consumers.add(subscriber);
        if (started) {
            startConsumer(subscriber);
        }
    }

    /**
     * Start a registered consumer, so that it can start receiving messages.
     *
     * @param subscriber to start.
     */
    private void startConsumer(MqttMessageConsumer subscriber) {

        String topic = subscriber.getTopic();
        logger.debug("Starting message consumer for broker '{}' on topic '{}'", name, topic);

        try {
            client.subscribe(topic, qos);
        } catch (Exception e) {
            logger.error("Error starting consumer", e);
        }
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
     * Remove a previously registered producer from this connection.
     *
     * @param publisher to remove.
     */
    public synchronized void removeProducer(MqttMessageProducer publisher) {
        logger.debug("Removing message producer for broker '{}'", name);
        publisher.setSenderChannel(null);
        producers.remove(publisher);
    }

    /**
     * Remove a previously registered consumer from this connection.
     *
     * @param subscriber to remove.
     */
    public synchronized void removeConsumer(MqttMessageConsumer subscriber) {
        logger.debug("Unsubscribing message consumer for topic '{}' from broker '{}'", subscriber.getTopic(), name);

        try {
            if (started) {
                client.unsubscribe(subscriber.getTopic());
            }
        } catch (Exception e) {
            logger.error("Error unsubscribing topic from broker", e);
        }
        consumers.remove(subscriber);

    }

    /**
     * Close the MQTT connection.
     */
    public synchronized void close() {
        logger.debug("Closing connection to broker '{}'", name);
        try {
            if (started) {
                client.disconnect();
            }
        } catch (MqttException e) {
            logger.error("Error closing connection to broker", e);
        }
        setStarted(false);
    }

    @Override
    public synchronized void connectionLost(Throwable t) {

        logger.error("MQTT connection to broker was lost", t);

        if (t instanceof MqttException) {
            MqttException e = (MqttException) t;
            logger.error("MQTT connection to '{}' was lost: {} : ReasonCode {} : Cause : {}",
                    new Object[] { name, e.getMessage(), e.getReasonCode(),
                            (e.getCause() == null ? "Unknown" : e.getCause().getMessage()) });
        } else {
            logger.error("MQTT connection to '{}' was lost: {}", name, t.getMessage());
        }

        setStarted(false);
        logger.info("Starting connection helper to periodically try restore connection to broker '{}'", name);

        MqttBrokerConnectionHelper helper = new MqttBrokerConnectionHelper(this);
        reconnectTimer = new Timer(true);
        reconnectTimer.schedule(helper, 10000, RECONNECT_FREQUENCY);

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.trace("Message with id {} delivered.", token.getMessageId());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        logger.trace("Received message on topic '{}' : {}", topic, new String(message.getPayload()));
        for (MqttMessageConsumer consumer : consumers) {
            if (isTopicMatch(topic, consumer.getTopic())) {
                consumer.processMessage(topic, message.getPayload());
            }
        }
    }

    /**
     * Check if the topic on which a message was received matches provided target topic. The matching will take into
     * account the + and # wildcards.
     *
     * @param source topic from received message
     * @param target topic as defined with possible wildcards.
     * @return true if both topics match.
     */
    private boolean isTopicMatch(String source, String target) {
        if (source.equals(target)) {
            return true;
        }
        if (target.indexOf('+') == -1 && target.indexOf('#') == -1) {
            return false;
        } else {

            String regex = target;
            regex = StringUtils.replace(regex, "+", "[^/]*");
            regex = StringUtils.replace(regex, "#", ".*");
            boolean result = source.matches(regex);
            if (result) {
                logger.trace("Topic match for '{}' and '{}' using regex {}", source, target, regex);
                return true;
            } else {
                logger.trace("No topic match for '{}' and '{}' using regex {}", source, target, regex);
                return false;
            }
        }

    }

    /**
     * Set the keep alive interval. The default interval is 60 seconds. If no heartbeat is received within this
     * timeframe, the connection will be considered dead. Set this to a higher value on systems which may not always be
     * able to process the heartbeat in time.
     *
     * @param keepAliveInterval interval in seconds
     */
    public void setKeepAliveInterval(int keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }

}
