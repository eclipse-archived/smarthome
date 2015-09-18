package org.eclipse.smarthome.notification.consumer.pushover.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.notification.Notification;
import org.eclipse.smarthome.notification.consumer.NotificationService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class PushoverNotificationService implements NotificationService {

    private final Logger logger = LoggerFactory.getLogger(PushoverNotificationService.class);

    public static final String PUSH_MESSAGE_URL = "https://api.pushover.net/1/messages.json";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    String userIDToken = null;
    String apiIDToken = null;

    protected Client pushoverClient = ClientBuilder.newClient();
    protected Gson gson = new Gson();
    protected WebTarget pushTarget = pushoverClient.target(PUSH_MESSAGE_URL);

    public void activate(ComponentContext componentContext) {
        Dictionary<String, Object> properties = componentContext.getProperties();
        userIDToken = (String) properties.get("userIDToken");
        apiIDToken = (String) properties.get("apiIDToken");
    }

    public void deactivate() {
    }

    @Override
    public String getName() {
        return "pushover";
    }

    @Override
    public void notify(String target, List<String> options, Notification notification) {
        logger.debug("Source '{}' Target '{}', Type '{}', Text '{}'",
                new Object[] { notification.getSource(), target, notification.getType(), notification.getText() });

        final Map<String, String> payLoad = new HashMap<String, String>();

        payLoad.put("token", apiIDToken);
        payLoad.put("user", userIDToken);
        payLoad.put("message", notification.getText());

        String device = options.get(0);
        String priority = options.get(1);

        if (device != null) {
            payLoad.put("device", device);
        }

        if (priority != null) {
            payLoad.put("priority", priority);
        }

        StringBuilder encodedPayLoad = new StringBuilder();
        // char separator = '?';
        char separator = '&';
        for (Entry<String, String> param : payLoad.entrySet()) {
            try {
                encodedPayLoad.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                if (!StringUtils.isEmpty(param.getValue())) {
                    encodedPayLoad.append('=');
                    encodedPayLoad.append(URLEncoder.encode(param.getValue(), "UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            encodedPayLoad.append(separator);
        }

        Response response = pushTarget.request()
                .post(Entity.entity(encodedPayLoad.toString(), MediaType.APPLICATION_FORM_URLENCODED));

        if (response == null || response.getEntity() == null) {
            logger.debug("Unreadable response");
            return;
        }

        ResponseModel m;
        m = gson.fromJson(response.readEntity(String.class), ResponseModel.class);
        String responseId = response.getHeaderString(REQUEST_ID_HEADER);
        logger.debug("Status '{}', Request ID '{}'", m.status, responseId);

    }

    private static class ResponseModel {
        int status;
    }

}
