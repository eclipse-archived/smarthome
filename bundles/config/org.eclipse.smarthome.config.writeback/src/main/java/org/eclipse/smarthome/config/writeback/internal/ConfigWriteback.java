/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.writeback.internal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service listens to {@link ConfigurationEvent}s from the {@link ConfigurationAdmin} service
 * and writes back the configuration to the respective service cfg file.
 *
 * This is not enabled per default and needs to be requested with the specific
 * property ConfigWriteback.PROP_CONFIG_WRITEBACK=ConfigWriteback.WRITEBACK_ALLOW,
 * assigned to the service in question.
 *
 * If no service file exists so far, it will be created in "smarthome.servicedir" (default is "services").
 * If a file exists already, it will be overwritten. Comments are not kept.
 *
 * The filename will have the pattern of "org.eclipse.smarthome.security.cfg" where "org.eclipse.smarthome.security"
 * is the service-pid. The first line will always be "pid: " followed by the service pid, for instance
 * "pid: org.eclipse.smarthome.security". This marks the file as being the exclusive configuration
 * file for the service.
 *
 * The file format is compatible to a format that {@link ConfigDispatcher} can read.
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = true, service = { ConfigurationListener.class, ConfigWriteback.class })
public class ConfigWriteback implements ConfigurationListener {
    public final static String PROP_CONFIG_WRITEBACK = "configWriteback";
    public final static String WRITEBACK_ALLOW = "allow";

    public final static File SERVICES_FOLDER = new File(ConfigConstants.getConfigFolder(), "services");
    private final static String PID_MARKER = "pid:";

    private final Logger logger = LoggerFactory.getLogger(ConfigWriteback.class);
    protected ConfigurationAdmin configAdmin;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    protected void setConfigurationAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    protected void unsetConfigurationAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = null;
    }

    @Activate
    public void activate() {
        logger.debug("ConfigWriteback ready. Write files to {}", SERVICES_FOLDER.getAbsolutePath());
        SERVICES_FOLDER.mkdirs();
    }

    @Override
    public void configurationEvent(final ConfigurationEvent configurationEvent) {
        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    doConfigurationEvent(configurationEvent);
                    return null;
                }
            });
        } else {
            doConfigurationEvent(configurationEvent);
        }
    }

    public void doConfigurationEvent(ConfigurationEvent configurationEvent) {
        Configuration config;
        try {
            config = configAdmin.getConfiguration(configurationEvent.getPid(), "?");
        } catch (IOException e) {
            logger.warn("Failed to retrieve configuration for {}", configurationEvent.getPid(), e);
            return;
        }

        File configFile = new File(SERVICES_FOLDER, configurationEvent.getPid() + ".cfg");

        if (configurationEvent.getType() == ConfigurationEvent.CM_UPDATED) {
            Dictionary<String, Object> props = config.getProperties();
            if (!WRITEBACK_ALLOW.equals(props.get(PROP_CONFIG_WRITEBACK))) {
                return;
            }

            logger.debug("Write back to service file {}", configFile.getAbsolutePath());
            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(configFile))) {
                // Write header
                out.write(PID_MARKER.getBytes());
                out.write(configurationEvent.getPid().getBytes());
                out.write('\n');
                out.write("# This file will be automatically modified. Comments will be erased!\n".getBytes());
                // Write key=value tuples
                Enumeration<String> e = props.keys();
                while (e.hasMoreElements()) {
                    String key = e.nextElement();
                    // Don't write the writeback property to file.
                    // A service will add this before sending configuration to ConfigurationAdmin.
                    if (PROP_CONFIG_WRITEBACK.equals(key) || Constants.SERVICE_PID.equals(key)
                            || "objectClass".equals(key) || "component.name".equals(key)
                            || "component.id".equals(key)) {
                        continue;
                    }
                    Object value = props.get(key);
                    out.write(key.getBytes());
                    out.write('=');
                    if (value instanceof String) {
                        out.write(((String) value).getBytes());
                    } else if (value instanceof Number) {
                        out.write(String.valueOf(value).getBytes());
                    } else if (value instanceof Boolean) {
                        out.write(String.valueOf(value).getBytes());
                    }
                    out.write('\n');
                }
            } catch (IOException e) {
                logger.warn("Failed to write configuration for {}", configurationEvent.getPid(), e);
            }
        } else if (configurationEvent.getType() == ConfigurationEvent.CM_DELETED) {
            if (configFile.isFile()) {
                try {
                    // Check if it is an exclusive file
                    List<String> readLines = FileUtils.readLines(configFile);
                    if (readLines.size() > 0 && readLines.get(0).startsWith("pid:" + configurationEvent.getPid())) {
                        Files.delete(configFile.toPath());
                    }
                } catch (IOException e) {
                    logger.warn("Unable to delete configuration file {}", configFile.getAbsolutePath(), e);
                }
            }
        }
    }
}
