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
package org.eclipse.smarthome.io.net.http.internal;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.X509ExtendedTrustManager;
import javax.security.auth.x500.X500Principal;

import org.eclipse.smarthome.io.net.http.TlsTrustManagerProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Tests which validate the behaviour of the ExtensibleTrustManager
 *
 * @author Martin van Wingerden - Initial contribution
 */
public class ExtensibleTrustManagerTest {
    private ExtensibleTrustManager subject;

    @Mock
    private TlsTrustManagerProvider trustmanagerProvider;

    @Mock
    private X509ExtendedTrustManager trustmanager;

    @Mock
    private X509Certificate topOfChain;

    @Mock
    private X509Certificate bottomOfChain;

    private X509Certificate[] chain;

    @Before
    public void setup() {
        initMocks(this);

        when(trustmanagerProvider.getHostName()).thenReturn("example.org");
        when(trustmanagerProvider.getTrustManager()).thenReturn(trustmanager);

        subject = new ExtensibleTrustManager();
        subject.addTlsTrustManagerProvider(trustmanagerProvider);

        chain = new X509Certificate[] { topOfChain, bottomOfChain };
    }

    @Test
    public void shouldForwardCallsToMockForMatchingCN() throws CertificateException {
        when(topOfChain.getSubjectX500Principal())
                .thenReturn(new X500Principal("CN=example.org, OU=Smarthome, O=Eclipse, C=DE"));

        subject.checkServerTrusted(chain, "just");

        verify(trustmanager).checkServerTrusted(chain, "just", (Socket) null);
        verifyNoMoreInteractions(trustmanager);
    }

    @Test
    public void shouldForwardCallsToMockForMatchingAlternativeNames() throws CertificateException {
        when(topOfChain.getSubjectX500Principal())
                .thenReturn(new X500Principal("CN=example.com, OU=Smarthome, O=Eclipse, C=DE"));
        when(topOfChain.getSubjectAlternativeNames())
                .thenReturn(constructAlternativeNames("example1.com", "example.org"));

        subject.checkClientTrusted(chain, "just");

        verify(trustmanager).checkClientTrusted(chain, "just", (Socket) null);
        verifyNoMoreInteractions(trustmanager);
    }

    @Test(expected = CertificateException.class)
    public void shouldNotForwardCallsToMockForDifferentCN() throws CertificateException {
        mockSubjectForCertificate(topOfChain, "CN=example.com, OU=Smarthome, O=Eclipse, C=DE");
        mockIssuerForCertificate(topOfChain, "CN=Eclipse, OU=Smarthome, O=Eclipse, C=DE");
        mockSubjectForCertificate(bottomOfChain, "CN=Eclipse, OU=Smarthome, O=Eclipse, C=DE");
        mockIssuerForCertificate(bottomOfChain, "");
        when(topOfChain.getEncoded()).thenReturn(new byte[0]);

        try {
            subject.checkServerTrusted(chain, "just");
        } finally {
            // note that it ends up in the standard TrustManager but lets not build our tests based on that
            verifyZeroInteractions(trustmanager);
        }
    }

    private Collection<List<?>> constructAlternativeNames(String... alternatives) {
        Collection<List<?>> alternativeNames = new ArrayList<>();
        for (String alternative : alternatives) {
            alternativeNames.add(Stream.of(0, alternative).collect(Collectors.toList()));
        }

        return alternativeNames;
    }

    private void mockSubjectForCertificate(X509Certificate certificate, String principal) {
        when(certificate.getSubjectX500Principal()).thenReturn(new X500Principal(principal));

    }

    private void mockIssuerForCertificate(X509Certificate certificate, String principal) {
        when(certificate.getIssuerX500Principal()).thenReturn(new X500Principal(principal));
    }
}
