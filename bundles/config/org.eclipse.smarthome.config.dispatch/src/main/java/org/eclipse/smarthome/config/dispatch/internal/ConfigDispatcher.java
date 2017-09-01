/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.dispatch.internal;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.service.AbstractWatchService;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a mean to read any kind of configuration data from
 * config folder files and dispatch it to the different bundles using the {@link ConfigurationAdmin} service.
 *
 * <p>
 * The name of the configuration folder can be provided as a program argument "smarthome.configdir" (default is "conf").
 * Configurations for OSGi services are kept in a subfolder that can be provided as a program argument
 * "smarthome.servicedir" (default is "services"). Any file in this folder with the extension .cfg will be processed.
 *
 * <p>
 * The format of the configuration file is similar to a standard property file, with the exception that the property
 * name can be prefixed by the service pid of the {@link ManagedService}:
 *
 * <p>
 * &lt;service-pid&gt;:&lt;property&gt;=&lt;value&gt;
 *
 * <p>
 * In case the pid does not contain any ".", the default service pid namespace is prefixed, which can be defined by the
 * program argument "smarthome.servicepid" (default is "org.eclipse.smarthome").
 *
 * <p>
 * If no pid is defined in the property line, the default pid namespace will be used together with the filename. E.g. if
 * you have a file "security.cfg", the pid that will be used is "org.eclipse.smarthome.security".
 *
 * <p>
 * Last but not least, a pid can be defined in the first line of a cfg file by prefixing it with "pid:", e.g.
 * "pid: com.acme.smarthome.security".
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Petar Valchev - Added sort by modification time, when configuration files are read
 * @author Ana Dimova - reduce to a single watch thread for all class instances
 */
public class ConfigDispatcher extends AbstractWatchService {

    private static String getPathToWatch() {
        String progArg = System.getProperty(SERVICEDIR_PROG_ARGUMENT);
        if (progArg != null) {
            return ConfigConstants.getConfigFolder() + File.separator + progArg;
        } else {
            return ConfigConstants.getConfigFolder() + File.separator + SERVICES_FOLDER;
        }
    }

    public ConfigDispatcher() {
        super(getPathToWatch());
    }

    /** The program argument name for setting the service config directory path */
    final static public String SERVICEDIR_PROG_ARGUMENT = "smarthome.servicedir";

    /** The program argument name for setting the service pid namespace */
    final static public String SERVICEPID_PROG_ARGUMENT = "smarthome.servicepid";

    /**
     * The program argument name for setting the default services config file
     * name
     */
    final static public String SERVICECFG_PROG_ARGUMENT = "smarthome.servicecfg";

    /** The default folder name of the configuration folder of services */
    final static public String SERVICES_FOLDER = "services";

    /** The default namespace for service pids */
    final static public String SERVICE_PID_NAMESPACE = "org.eclipse.smarthome";

    /** The default services configuration filename */
    final static public String SERVICE_CFG_FILE = "smarthome.cfg";

    private static final String PID_MARKER = "pid:";

    private final Logger logger = LoggerFactory.getLogger(ConfigDispatcher.class);

    private ConfigurationAdmin configAdmin;

    @Override
    public void activate() {
        super.activate();
        readDefaultConfig();
        readConfigs();
    }

    @Override
    public void deactivate() {
        super.deactivate();

    }

    protected void setConfigurationAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    protected void unsetConfigurationAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = null;
    }

    @Override
    protected boolean watchSubDirectories() {
        return false;
    }

    @Override
    protected Kind<?>[] getWatchEventKinds(Path subDir) {
        return new Kind<?>[] { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY };
    }

    @Override
    protected void processWatchEvent(WatchEvent<?> event, Kind<?> kind, Path path) {
        if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
            try {
                File f = path.toFile();
                if (!f.isHidden()) {
                    processConfigFile(f);
                }
            } catch (IOException e) {
                logger.warn("Could not process config file '{}': {}", path, e);
            }
        }
    }

    private String getDefaultServiceConfigFile() {
        String progArg = System.getProperty(SERVICECFG_PROG_ARGUMENT);
        if (progArg != null) {
            return progArg;
        } else {
            return ConfigConstants.getConfigFolder() + File.separator + SERVICE_CFG_FILE;
        }
    }

    private void readDefaultConfig() {
        File defaultCfg = new File(getDefaultServiceConfigFile());
        try {
            processConfigFile(defaultCfg);
        } catch (IOException e) {
            logger.warn("Could not process default config file '{}': {}", getDefaultServiceConfigFile(), e);
        }
    }

    private void readConfigs() {
        File dir = getSourcePath().toFile();
        if (dir.exists()) {
            File[] files = dir.listFiles();
            // Sort the files by modification time,
            // so that the last modified file is processed last.
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File left, File right) {
                    return Long.valueOf(left.lastModified()).compareTo(right.lastModified());
                }
            });
            for (File file : files) {
                try {
                    processConfigFile(file);
                } catch (IOException e) {
                    logger.warn("Could not process config file '{}': {}", file.getName(), e);
                }
            }
        } else {
            logger.debug("Configuration folder '{}' does not exist.", dir.toString());
        }
    }

    private static String getServicePidNamespace() {
        String progArg = System.getProperty(SERVICEPID_PROG_ARGUMENT);
        if (progArg != null) {
            return progArg;
        } else {
            return SERVICE_PID_NAMESPACE;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void processConfigFile(File configFile) throws IOException, FileNotFoundException {
        if (configFile.isDirectory() || !configFile.getName().endsWith(".cfg")) {
            logger.debug("Ignoring file '{}'", configFile.getName());
            return;
        }
        logger.debug("Processing config file '{}'", configFile.getName());

        // we need to remember which configuration needs to be updated
        // because values have changed.
        Map<Configuration, Dictionary> configsToUpdate = new HashMap<Configuration, Dictionary>();

        // also cache the already retrieved configurations for each pid
        Map<Configuration, Dictionary> configMap = new HashMap<Configuration, Dictionary>();

        String pid;
        String filenameWithoutExt = StringUtils.substringBeforeLast(configFile.getName(), ".");
        if (filenameWithoutExt.contains(".")) {
            // it is a fully qualified namespace
            pid = filenameWithoutExt;
        } else {
            pid = getServicePidNamespace() + "." + filenameWithoutExt;
        }

        // configuration file contains a PID Marker
        List<String> lines = IOUtils.readLines(new FileInputStream(configFile));
        if (lines.size() > 0 && lines.get(0).startsWith(PID_MARKER)) {
            pid = lines.get(0).substring(PID_MARKER.length()).trim();
            lines = lines.subList(1, lines.size());
        }

        for (String line : lines) {
            String[] contents = parseLine(line);
            // no valid configuration line, so continue
            if (contents == null) {
                continue;
            }

            if (contents[0] != null) {
                pid = contents[0];
                // PID is not fully qualified, so prefix with namespace
                if (!pid.contains(".")) {
                    pid = getServicePidNamespace() + "." + pid;
                }
            }

            String property = contents[1];
            String value = contents[2];
            Configuration configuration = configAdmin.getConfiguration(pid, null);
            if (configuration != null) {
                Dictionary configProperties = configMap.get(configuration);
                if (configProperties == null) {
                    configProperties = configuration.getProperties() != null ? configuration.getProperties()
                            : new Properties();
                    configMap.put(configuration, configProperties);
                }
                if (!value.equals(configProperties.get(property))) {
                    configProperties.put(property, value);
                    configsToUpdate.put(configuration, configProperties);
                }
            }
        }

        for (Entry<Configuration, Dictionary> entry : configsToUpdate.entrySet()) {
            entry.getKey().update(entry.getValue());
        }
    }

    private String[] parseLine(final String line) {
        String trimmedLine = line.trim();
        if (trimmedLine.startsWith("#") || trimmedLine.isEmpty()) {
            return null;
        }

        String pid = null; // no override of the pid
        String key = StringUtils.substringBefore(trimmedLine, "=");
        if (key.contains(":")) {
            pid = StringUtils.substringBefore(key, ":");
            trimmedLine = trimmedLine.substring(pid.length() + 1);
            pid = pid.trim();
        }
        if (!trimmedLine.isEmpty() && trimmedLine.substring(1).contains("=")) {
            String property = StringUtils.substringBefore(trimmedLine, "=");
            String value = trimmedLine.substring(property.length() + 1);
            return new String[] { pid, property.trim(), value.trim() };
        } else {
            logger.warn("Could not parse line '{}'", line);
            return null;
        }
    }

}
