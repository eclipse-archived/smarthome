package org.eclipse.smarthome.io.net.http.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
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
        secureHttpClientFactory.activate(createConfigMap(10, 40, 60));

        HttpClient client = secureHttpClientFactory.createHttpClient("consumer");

        assertThat(client, is(notNullValue()));

        client.stop();

        secureHttpClientFactory.deactivate();
    }

    @Test
    public void testGetClientWithEndpoint() throws Exception {
        secureHttpClientFactory.activate(createConfigMap(2, 40, 60));

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
        secureHttpClientFactory.activate(createConfigMap(10, 200, 60));

        ThreadPoolExecutor workers = new ThreadPoolExecutor(20, 80, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(50 * 50), new NamedThreadFactory("workers"));

        List<HttpClient> clients = new ArrayList<>();

        final int CLIENTS = 30;
        final int REQUESTS = 30;

        for (int i = 0; i < CLIENTS; i++) {
            HttpClient httpClient = secureHttpClientFactory.createHttpClient("consumer" + i);
            clients.add(httpClient);
        }

        final List<String> failures = new ArrayList<>();

        for (int i = 0; i < REQUESTS; i++) {
            // System.out.println("Request " + i);
            for (final HttpClient client : clients) {
                // System.out.println("Client " + client);
                workers.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // System.out.println("starting request " + client);
                            ContentResponse response = client.GET("https://www.heise.de");
                            if (response.getStatus() != 200) {
                                System.out.println(response.getStatus());
                                failures.add("Statuscode != 200");
                            }
                            // System.out.println("Content received for " + client);
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            failures.add("Unexpected exception:" + e.getMessage());
                        }
                    }
                });
            }
        }

        workers.shutdown();
        workers.awaitTermination(120, TimeUnit.SECONDS);
        if (!failures.isEmpty()) {
            fail(failures.toString());
        }

        for (HttpClient client : clients) {
            client.stop();
        }

        secureHttpClientFactory.deactivate();
    }

    private Map<String, Object> createConfigMap(int minThreads, int maxThreads, int keepAliveTimeout) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("minThreads", minThreads);
        configMap.put("maxThreads", maxThreads);
        configMap.put("keepAliveTimeout", keepAliveTimeout);
        return configMap;
    }
}
