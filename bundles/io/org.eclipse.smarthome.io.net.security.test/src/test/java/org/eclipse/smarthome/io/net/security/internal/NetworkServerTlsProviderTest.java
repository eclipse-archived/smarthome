/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.xml.bind.DatatypeConverter;

import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.RawData;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.InMemoryPskStore;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/**
 * Tests the {@link ServiceConfiguration} class, especially the Base64 encoded passwords
 * and multi-context entries.
 *
 * @author David Graeff - Initial contribution
 */
public class NetworkServerTlsProviderTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ConfigurationUtilsEx utils;
    private NetworkServerTlsProviderEx provider;
    private File workingDir;

    @Before
    public void setup() throws NoSuchAlgorithmException, IOException {
        workingDir = folder.getRoot(); // Paths.get("").toAbsolutePath().toString();

        utils = spy(
                new ConfigurationUtilsEx(Collections.emptyList(), workingDir, MessageDigest.getInstance("SHA-256")));
        provider = new NetworkServerTlsProviderEx(utils);
        provider.activated(null, Collections.singletonMap("component.name", NetworkServerTlsProvider.CONFIG_PID));

        ContextConfiguration context = provider.utils.getOrCreateContext(provider.configuration, "default");
        provider.utils.getKeystoreFilename(context);
        File keystoreFile = provider.utils.getKeystoreFilename(context);
        if (keystoreFile.exists()) {
            keystoreFile.delete();
        }
    }

    @Test
    public void failToOpenSelfSignedCertificateWithoutConfig() throws GeneralSecurityException, IOException {
        provider.createSSLContext("default");
        // The configuration should have been written back and a keystore file should exist
        verify(utils).writeConfiguration(anyObject());
        assertTrue(new File(workingDir, "default.keystore").exists());
        // Delete config file -> causes modified to be called.
        provider.modified(Collections.singletonMap("component.name", NetworkServerTlsProvider.CONFIG_PID));
        // createSSLContext will fail, because the keystore password is unknown
        thrown.expect(IOException.class);
        thrown.expectCause(IsInstanceOf.<Throwable> instanceOf(UnrecoverableKeyException.class));
        provider.createSSLContext("default");
    }

    @Test
    public void createValidSelfSignedCertificateIfNoExists()
            throws GeneralSecurityException, IOException, NoSuchAlgorithmException, KeyManagementException,
            InterruptedException, ExecutionException, TimeoutException {
        SSLContext s = provider.createSSLContext("default");
        ServerSocket serverAcceptSocket = s.getServerSocketFactory().createServerSocket();
        serverAcceptSocket.bind(new InetSocketAddress(0));

        // Create client socket
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, new TrustManager[] { new X509ExtendedTrustManagerEx() },
                new java.security.SecureRandom());
        SSLSocket clientSocket = (SSLSocket) sslContext.getSocketFactory().createSocket();

        final CompletableFuture<Socket> c = CompletableFuture.supplyAsync(() -> {
            try {
                return serverAcceptSocket.accept();
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });

        // Connect
        clientSocket.connect(serverAcceptSocket.getLocalSocketAddress());
        SSLSocket serverSocket = (SSLSocket) c.get(200, TimeUnit.MILLISECONDS);

        final CompletableFuture<Void> serverHandshake = CompletableFuture.runAsync(() -> {
            try {
                serverSocket.startHandshake();
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });

        final CompletableFuture<Void> clientHandshake = CompletableFuture.runAsync(() -> {
            try {
                clientSocket.startHandshake();
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });

        CompletableFuture.allOf(serverHandshake, clientHandshake).get(500, TimeUnit.MILLISECONDS);

        // Check certificate
        SSLSession session = clientSocket.getSession();
        X509Certificate cert = (X509Certificate) session.getPeerCertificates()[0];
        assertTrue(cert.getSubjectDN().getName().contains("O=\"Eclipse Foundation, Inc.\""));

        // Send
        clientSocket.getOutputStream().write(new byte[] { 'c' });
        clientSocket.getOutputStream().flush();
        serverSocket.getOutputStream().write(new byte[] { 's' });
        serverSocket.getOutputStream().flush();

        // Receive
        clientSocket.setSoTimeout(200);
        serverSocket.setSoTimeout(200);
        byte buffer[] = new byte[10];
        assertThat(clientSocket.getInputStream().read(buffer), is(1));
        assertThat(buffer[0], is((byte) 's'));
        assertThat(serverSocket.getInputStream().read(buffer), is(1));
        assertThat(buffer[0], is((byte) 'c'));
    }

    @Test
    public void udpSecureSocketPublicPrivateKey() throws Exception {
        Connector server = provider.createConnector("default", new InetSocketAddress(0));
        server.start();

        // Client
        KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
        g.initialize(256, new SecureRandom());
        KeyPair keyPair = g.generateKeyPair();
        DtlsConnectorConfig.Builder b = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
        b.setIdentity(keyPair.getPrivate(), keyPair.getPublic());
        b.setClientOnly();
        DTLSConnector client = new DTLSConnector(b.build());
        // Connector client = provider.createConnector("client", new InetSocketAddress(0));
        client.start();

        // Prepare receive
        CompletableFuture<Byte> serverReceiveFuture = new CompletableFuture<Byte>();
        server.setRawDataReceiver(data -> serverReceiveFuture.complete(data.bytes[0]));
        CompletableFuture<Byte> clientReceiveFuture = new CompletableFuture<Byte>();
        client.setRawDataReceiver(data -> clientReceiveFuture.complete(data.bytes[0]));

        // Send
        server.send(new RawData(new byte[] { (byte) 's' },
                new InetSocketAddress("127.0.0.1", client.getAddress().getPort())));

        // TODO The current version of the Connector library has a bug and we need to wait between sends.
        Thread.sleep(100);

        client.send(new RawData(new byte[] { (byte) 'c' },
                new InetSocketAddress("127.0.0.1", server.getAddress().getPort())));

        // Receive (simultaneously, the OS will cache the response until we retrieve it)
        assertThat(serverReceiveFuture.get(500, TimeUnit.MILLISECONDS), is((byte) 'c'));
        assertThat(clientReceiveFuture.get(500, TimeUnit.MILLISECONDS), is((byte) 's'));
    }

    @Test
    public void udpSecureSocketPSK() throws Exception {
        final byte[] PSK = "test".getBytes();

        ContextConfiguration context = provider.utils.getOrCreateContext(provider.configuration, "default");
        context.preSharedKeyBase64 = DatatypeConverter.printBase64Binary(PSK);
        Connector server = provider.createConnector("default", new InetSocketAddress(0));
        server.start();

        // Client
        InMemoryPskStore pskStore = new InMemoryPskStore();
        pskStore.setKey(context.context, PSK);
        DtlsConnectorConfig.Builder b = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
        b.setPskStore(pskStore);
        b.setClientOnly();
        DTLSConnector client = new DTLSConnector(b.build());
        client.start();

        // Prepare receive
        CompletableFuture<Byte> serverReceiveFuture = new CompletableFuture<Byte>();
        server.setRawDataReceiver(data -> serverReceiveFuture.complete(data.bytes[0]));
        CompletableFuture<Byte> clientReceiveFuture = new CompletableFuture<Byte>();
        client.setRawDataReceiver(data -> clientReceiveFuture.complete(data.bytes[0]));

        // Send
        server.send(new RawData(new byte[] { (byte) 's' },
                new InetSocketAddress("127.0.0.1", client.getAddress().getPort())));

        // TODO The current version of the Connector library has a bug and we need to wait between sends.
        Thread.sleep(100);

        client.send(new RawData(new byte[] { (byte) 'c' },
                new InetSocketAddress("127.0.0.1", server.getAddress().getPort())));

        Thread.sleep(100);

        // Receive (simultaneously, the OS will cache the response until we retrieve it)
        assertThat(serverReceiveFuture.get(500, TimeUnit.MILLISECONDS), is((byte) 'c'));
        assertThat(clientReceiveFuture.get(500, TimeUnit.MILLISECONDS), is((byte) 's'));
    }

    @Test
    public void invalidCertificate() throws GeneralSecurityException, IOException {
        // The automatically generated cert sets autoRefreshInvalidCertificate = true
        provider.createSSLContext("default");
        // Assume it is off
        provider.configuration.contexts.get(0).autoRefreshInvalidCertificate = false;
        // Let's assume we are in the future, one day after certificate expiration
        long millis = (provider.configuration.validityInDays.longValue() + 1) * 24 * 60 * 60 * 1000;
        provider.currentDate.setTime(new Date().getTime() + millis);
        thrown.expect(GeneralSecurityException.class);
        provider.createSSLContext("default");
    }

    @Test
    public void refreshCertificate() throws GeneralSecurityException, IOException {
        // The automatically generated cert sets autoRefreshInvalidCertificate = true
        provider.createSSLContext("default");
        // Let's assume we are in the future, one day after certificate expiration
        long millis = (provider.configuration.validityInDays.longValue() + 1) * 24 * 60 * 60 * 1000;
        provider.currentDate.setTime(new Date().getTime() + millis);
        provider.createSSLContext("default");
        // If everything went smooth, we had no exceptions during this procedure
    }
}
