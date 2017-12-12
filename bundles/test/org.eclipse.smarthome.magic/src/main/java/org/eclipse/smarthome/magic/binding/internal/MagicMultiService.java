package org.eclipse.smarthome.magic.binding.internal;

import java.util.Map;
import java.util.Map.Entry;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = "org.eclipse.smarthome.magicMultiInstance")
public class MagicMultiService {

    private final Logger logger = LoggerFactory.getLogger(MagicMultiService.class);

    @Activate
    public void activate(Map<String, Object> props) {
        logger.debug("activate");
        for (Entry<String, Object> e : props.entrySet()) {
            logger.debug(e.getKey() + " : " + e.getValue());
        }
    }

    @Modified
    public void modified(Map<String, Object> props) {
        logger.debug("modified");
        for (Entry<String, Object> e : props.entrySet()) {
            logger.debug(e.getKey() + " : " + e.getValue());
        }
    }

}
