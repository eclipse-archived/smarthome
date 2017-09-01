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
import org.osgi.framework.InvalidSyntaxException;
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
    /**
     * Represents a result of parseLine().
     */
    private class ParseLineResult {
        public String pid;
        public String property;
        public String value;

        public ParseLineResult(String pid, String property, String value) {
            this.pid = pid;
            this.property = property;
            this.value = value;
        }
    }

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
        } else if (kind == ENTRY_DELETE) {
            // Detect if a service specific configuration file was removed. We want to
            // notify the service in this case with an updated empty configuration.
            File configFile = path.toFile();
            if (configFile.isHidden() || configFile.isDirectory() || !configFile.getName().endsWith(".cfg")) {
                return;
            }
            String pid = pidFromFilename(configFile);
            try {
                Configuration configs[] = configAdmin.listConfigurations("(service.pid=" + pid + ")");
                if (configs.length > 0) {
                    configs[0].update((Dictionary) new Properties());
                }
            } catch (IOException | InvalidSyntaxException ignored) {
                return;
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

    /**
     * The filename of a given configuration file is assumed to be the service PID. If the filename
     * without extension contains ".", we assume it is the fully qualified name.
     *
     * @param configFile The configuration file
     * @return The PID
     */
    private String pidFromFilename(File configFile) {
        String filenameWithoutExt = StringUtils.substringBeforeLast(configFile.getName(), ".");
        if (filenameWithoutExt.contains(".")) {
            // it is a fully qualified namespace
            return filenameWithoutExt;
        } else {
            return getServicePidNamespace() + "." + filenameWithoutExt;
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

        String pid = pidFromFilename(configFile);

        // configuration file contains a PID Marker
        List<String> lines = IOUtils.readLines(new FileInputStream(configFile));
        if (lines.size() > 0 && lines.get(0).startsWith(PID_MARKER)) {
            pid = lines.get(0).substring(PID_MARKER.length()).trim();
        }

        /**
         * Services are also interested if a configuration file is cleared.
         * This is necessary to clean up resources, that were configured before.
         * It only works with configuration files dedicated to a service though.
         */
        boolean isMultiPidConfigFile = false;
        /**
         * Record if we have valid configuration lines in the file
         */
        int changes = 0;

        Configuration configuration = null;

        for (String line : lines) {
            ParseLineResult contents = parseLine(configFile.getPath(), line);
            // no valid configuration line, so continue
            if (contents == null) {
                continue;
            }

            if (contents.pid != null) {
                isMultiPidConfigFile = true;
                pid = contents.pid;
                configuration = configAdmin.getConfiguration(pid, null);
            } else if (configuration == null) {
                configuration = configAdmin.getConfiguration(pid, null);
            }

            if (configuration == null) {
                continue;
            }

            // Combine all configurations for one service in the same configuration object.
            // If it doesn't exist yet, create it.
            Dictionary configProperties = configMap.get(configuration);
            if (configProperties == null) {
                configProperties = new Properties();
                configMap.put(configuration, configProperties);
            }

            // Do not bother services if the configuration tuple key=value hasn't changed
            Object oldValue = configProperties.get(contents.property);
            if (contents.value.equals(oldValue)) {
                continue;
            }

            ++changes;
            configProperties.put(contents.property, contents.value);
            configsToUpdate.put(configuration, configProperties);
        }

        if (!isMultiPidConfigFile) {
            configuration = configAdmin.getConfiguration(pid, null);
            if (configuration == null) {
                return;
            }

            int oldSize = configuration.getProperties() == null ? 0 : configuration.getProperties().size();

            // If the old service configuration has more entries than the new one, it has changed as well
            // We want to inform the services about this, even if the configuration is empty
            // (for example to clean up what was configured before)
            if (changes > 0) {
                // There is only one configuration in the file, just take the first entry therefore
                configuration.update(configsToUpdate.values().iterator().next());
            } else if (oldSize > 0) {
                configuration.update((Dictionary) new Properties());
            }
        } else {
            for (Entry<Configuration, Dictionary> entry : configsToUpdate.entrySet()) {
                entry.getKey().update(entry.getValue());
            }
        }
    }

    private ParseLineResult parseLine(final String filePath, final String line) {
        String trimmedLine = line.trim();
        if (trimmedLine.startsWith("#") || trimmedLine.isEmpty()) {
            return null;
        }

        String pid = null; // no override of the pid is default
        String key = StringUtils.substringBefore(trimmedLine, "=");
        if (key.contains(":")) {
            pid = StringUtils.substringBefore(key, ":");
            trimmedLine = trimmedLine.substring(pid.length() + 1);
            pid = pid.trim();
            // PID is not fully qualified, so prefix with namespace
            if (!pid.contains(".")) {
                pid = getServicePidNamespace() + "." + pid;
            }
        }
        if (!trimmedLine.isEmpty() && trimmedLine.substring(1).contains("=")) {
            String property = StringUtils.substringBefore(trimmedLine, "=");
            String value = trimmedLine.substring(property.length() + 1);
            return new ParseLineResult(pid, property.trim(), value.trim());
        } else {
            logger.warn("Could not parse line '{}'", line);
            return null;
        }
    }

}
