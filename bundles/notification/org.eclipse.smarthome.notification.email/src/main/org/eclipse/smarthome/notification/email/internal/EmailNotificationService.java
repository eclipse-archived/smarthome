package org.eclipse.smarthome.notification.email.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.notification.NotificationService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailNotificationService implements NotificationService {

    private final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    static String hostname;
    static Integer port;
    static String username;
    static String password;
    static String from;
    static String attachmentUrl;

    static boolean popBeforeSmtp = false;

    public void activate(ComponentContext componentContext) {
        Dictionary<String, Object> properties = componentContext.getProperties();
        hostname = (String) properties.get("hostname");
        port = Integer.valueOf((String) properties.get("port"));
        username = (String) properties.get("username");
        password = (String) properties.get("password");
    }

    public void deactivate() {
    }

    @Override
    public String getName() {
        return "email";
    }

    @Override
    public void notify(String target, List<String> options, Event event) {
        logger.debug("Target '{}', Type '{}', Topic '{}', Payload '{}'",
                new Object[] { target, event.getType(), event.getTopic(), event.getPayload() });

        if (hostname != null && port != null && username != null && password != null) {

            String from = options.get(0);
            String to = options.get(1);
            String subject = event.toString();
            String message = event.getPayload();

            Email email = new SimpleEmail();
            if (attachmentUrl != null) {
                // Create the attachment
                try {
                    email = new MultiPartEmail();
                    EmailAttachment attachment = new EmailAttachment();
                    attachment.setURL(new URL(attachmentUrl));
                    attachment.setDisposition(EmailAttachment.ATTACHMENT);
                    attachment.setName("Attachment");
                    ((MultiPartEmail) email).attach(attachment);
                } catch (MalformedURLException e) {
                    logger.error("Invalid attachment url.", e);
                } catch (EmailException e) {
                    logger.error("Error adding attachment to email.", e);
                }
            }

            email.setHostName(hostname);
            email.setSmtpPort(port);

            if (StringUtils.isNotBlank(username)) {
                if (popBeforeSmtp) {
                    email.setPopBeforeSmtp(true, hostname, username, password);
                } else {
                    DefaultAuthenticator da = new DefaultAuthenticator(username, password);
                    email.setAuthenticator(da);
                    // email.setStartTLSRequired(true);
                }
            }

            try {
                if (to != null) {
                    email.setFrom(from);
                    String[] toList = to.split(";");
                    for (String toAddress : toList) {
                        email.addTo(toAddress);
                    }
                    if (!StringUtils.isEmpty(subject))
                        email.setSubject(subject);
                    if (!StringUtils.isEmpty(message))
                        email.setMsg(message);
                    email.send();
                    logger.debug("Sent email to '{}' with subject '{}'.", to, subject);
                }
            } catch (EmailException e) {
                logger.error("Could not send e-mail to '" + to + "'.", e);
            }

        } else {
            logger.error(
                    "Cannot send e-mail because of missing configuration settings. The current settings are: "
                            + "Host: '{}', port '{}', from '{}', username: '{}', password '{}'",
                    new Object[] { hostname, String.valueOf(port), from, username, password });
        }

    }
}
