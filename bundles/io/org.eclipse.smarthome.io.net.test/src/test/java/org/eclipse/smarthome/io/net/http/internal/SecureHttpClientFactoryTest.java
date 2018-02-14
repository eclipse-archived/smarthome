package org.eclipse.smarthome.io.net.http.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.smarthome.io.net.http.TrustManagerProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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

    @Ignore("only for manual test")
    @Test
    public void testMultiThreaded() throws Exception {
        secureHttpClientFactory.activate(createConfigMap(10, 40, 60, 1000));

        ThreadPoolExecutor workers = new ThreadPoolExecutor(20, 80, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(50 * 50), new NamedThreadFactory("workers"));

        List<HttpClient> clients = new ArrayList<>();

        // final int CLIENTS = 50;
        // final int REQUESTS = 50;

        final int CLIENTS = 2;
        final int REQUESTS = 2;

        for (int i = 0; i < CLIENTS; i++) {
            clients.add(secureHttpClientFactory.createHttpClient("consumer" + i));
        }

        for (int i = 0; i < REQUESTS; i++) {
            for (final HttpClient client : clients) {
                workers.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ContentResponse response = client.GET("https://www.heise.de");
                            assertThat(response.getStatus(), is(200));
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            Assert.fail("Unexpected exception:" + e.getMessage());
                        }
                    }
                });
            }
        }

        workers.shutdown();
        workers.awaitTermination(60, TimeUnit.SECONDS);

        for (HttpClient client : clients) {
            client.stop();
        }

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
