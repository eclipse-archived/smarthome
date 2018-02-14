package org.eclipse.smarthome.io.net.http.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.io.net.http.TrustManagerProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class SecureHttpClientFactoryTest {

    private SecureHttpClientFactory secureHttpClientFactory;

    @Mock
    private TrustManagerProvider trustmanagerProvider;

    @Before
    public void setup() {
        initMocks(this);
        secureHttpClientFactory = new SecureHttpClientFactory();
        secureHttpClientFactory.setTrustmanagerProvider(trustmanagerProvider);
    }

    @Test
    public void testGetClient() throws Exception {
        secureHttpClientFactory.activate(createConfigMap(10, 40, 60, 80));

        HttpClient client = secureHttpClientFactory.createHttpClient("consumer");

        assertThat(client, is(notNullValue()));

        client.stop();

        secureHttpClientFactory.deactivate();
    }

    @Test
    public void testGetClientWithEndpoint() throws Exception {
        secureHttpClientFactory.activate(createConfigMap(10, 40, 60, 80));

        when(trustmanagerProvider.getTrustManagers("https://www.heise.de")).thenReturn(Stream.empty());

        HttpClient client = secureHttpClientFactory.createHttpClient("consumer", "https://www.heise.de");

        assertThat(client, is(notNullValue()));

        verify(trustmanagerProvider).getTrustManagers("https://www.heise.de");

        client.stop();

        secureHttpClientFactory.deactivate();
    }

    private Map<String, Object> createConfigMap(int minThreads, int maxThreads, int keepAliveTimeout, int queueSize) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("minThreads", minThreads);
        configMap.put("maxThreads", maxThreads);
        configMap.put("keepAliveTimeout", keepAliveTimeout);
        configMap.put("queueSize", queueSize);
        return configMap;
    }
}
