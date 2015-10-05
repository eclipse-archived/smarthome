/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.documentation;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.smarthome.documentation.data.*;
import org.eclipse.smarthome.documentation.models.Binding;
import org.eclipse.smarthome.documentation.schemas.config_description.v1_0.ConfigDescription;
import org.eclipse.smarthome.documentation.schemas.config_description.v1_0.ConfigDescriptions;
import org.eclipse.smarthome.documentation.schemas.thing_description.v1_0.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

/**
 * Goal which touches a timestamp file.
 * <p/>
 * NOTE: CARE FOR THE NAMESPACE IN {@link ThingDescriptions} and {@link ConfigDescriptions}.
 */
@Mojo(name = "generate-docu", defaultPhase = LifecyclePhase.PACKAGE) public class GenerateDocumentation
        extends AbstractMojo {

    /**
     * ESH-INF/ subdirectories.
     */
    private static final String THING_SUBDIR = "thing/";
    private static final String BINDING_SUBDIR = "binding/";
    private static final String CONFIG_SUBDIR = "config/";

    /**
     * URL for the download of the templates.
     */
    private static final String DOWNLOAD_DIR = "https://github.com/kummerer94/binding-docu-generator/raw/master/src/main/resources/templates/";

    /**
     * Data for the templates.
     */
    private ChannelList channels = new ChannelList();
    private ThingList things = new ThingList();
    private ChannelGroupList channelGroups = new ChannelGroupList();
    private BridgeList bridges = new BridgeList();
    private ConfigurationList configList = new ConfigurationList();
    private Binding binding;

    /**
     * The directory in which your binding xml files are.
     */
    @Parameter(defaultValue = "src/main/java/ESH-INF/") private String eshDir;

    /**
     * Your template files.
     */
    @Parameter(defaultValue = "src/main/resources/templates/") private String templates;

    /**
     * Name of your readme template file.
     */
    @Parameter(defaultValue = "readme.mustache") private String template;

    /**
     * The name of the generated docu file.
     */
    @Parameter(defaultValue = "generated-docu.md") private String readmeName;

    /**
     * Execute the mojo.
     *
     * @throws MojoExecutionException
     */
    public void execute() throws MojoExecutionException {
        prepare();
        scanDir();

        getLog().info(Paths.get("").toAbsolutePath().toString());

        try {
            // Compile mustache template
            MustacheFactory mf = new DefaultMustacheFactory();
            // Get template string
            Mustache mustache = mf.compile(templates + template);
            // Put everything into the scope
            HashMap<String, Object> scope = new HashMap<String, Object>();
            scope.put("binding", binding);
            scope.put("bridgeList", bridges);
            scope.put("thingList", things);
            scope.put("channelList", channels);
            scope.put("channelGroupList", channelGroups);
            scope.put("configList", configList);
            mustache.execute(new FileWriter(readmeName), scope).flush();
        } catch (Exception e) {
            getLog().error(e);
        }
    }

    /**
     * Scans the given eshDir for xml files.
     */
    private void scanDir() {
        // Scan the binding directory.
        File binding = new File(eshDir + BINDING_SUBDIR + "binding.xml");
        if (binding.exists()) {
            getLog().debug("Found binding xml: " + binding.getName());
            parseBindingDescription(binding);
        }

        // Scan the things directory.
        File things = new File(eshDir + THING_SUBDIR);
        if (things.exists() && things.isDirectory()) {
            File[] files = things.listFiles();
            if(files != null) {
                for (File file : files) {
                    if (file != null) {
                        if (file.getName().endsWith(".xml")) {
                            getLog().info("Found thing xml: " + file.getName());
                            parseThingDescriptions(file);
                        }
                    }
                }
            }
        }

        // Scan the config directory.
        File configs = new File(eshDir + CONFIG_SUBDIR);
        if (configs.exists() && configs.isDirectory()) {
            File[] files = configs.listFiles();
            if(files != null) {
                for (File file : files) {
                    if (file != null) {
                        if (file.getName().endsWith(".xml")) {
                            getLog().info("Found config xml: " + file.getName());
                            parseConfigDescriptions(file);
                        }
                    }
                }
            }
        }
    }

    /**
     * Handles the start up of the mojo.
     */
    private void prepare() {
        // Configure loggers
        BasicConfigurator.configure();

        // Prepare the templates
        prepareTemplateDir();
    }

    /**
     * Prepares the template directory.
     * <p/>
     * If needed downloads a set of template files.
     */
    private void prepareTemplateDir() {
        String[] downloads = { template.replace(".mustache", ""), "partials/bridgeConfig", "partials/bridges",
                "partials/channelGroupInfo", "partials/channelGroups", "partials/channelInfo", "partials/channels",
                "partials/config", "partials/configDescriptions", "partials/parameterRow", "partials/paramProperties",
                "partials/thingConfig", "partials/things" };
        // Download all files.
        for (String url : downloads) {
            try {
                // Copy file from URL.
                String downloadUrl = DOWNLOAD_DIR + url + ".mustache";
                String fileName = templates + url + ".mustache";
                if (downloadNeeded(fileName)) {
                    getLog().debug("Downloading " + fileName + " from " + downloadUrl);
                    FileUtils.copyURLToFile(new URL(downloadUrl), new File(fileName));
                }
            } catch (MalformedURLException e) {
                getLog().error(e);
            } catch (IOException e) {
                getLog().error(e);
            }
        }
    }

    /**
     * Are the template files already present?
     *
     * @param fileName The url.
     * @return Is the download needed?
     */
    public boolean downloadNeeded(String fileName) {
        return !new File(fileName).exists();
    }

    /**
     * Parses the xml with the available channels.
     *
     * @param file XML file.
     */
    private void parseThingDescriptions(File file) {
        try {
            JAXBContext jc = JAXBContext.newInstance(ThingDescriptions.class);

            Unmarshaller unmarshaller = jc.createUnmarshaller();
            ThingDescriptions thingDesc = (ThingDescriptions) unmarshaller.unmarshal(file);

            // Go through all the available types
            List<Object> objs = thingDesc.getThingTypeOrBridgeTypeOrChannelType();
            for (Object obj : objs) {
                if (obj instanceof ChannelType) {
                    channels.put(obj);
                } else if (obj instanceof BridgeType) {
                    bridges.put(obj);
                } else if (obj instanceof ChannelGroupType) {
                    channelGroups.put(obj);
                } else if (obj instanceof ThingType) {
                    things.put(obj);
                } else {
                    getLog().debug("Unsupported class. " + obj.getClass().toString());
                }
            }
        } catch (Exception e) {
            getLog().error(e);
        }
    }

    /**
     * Parses the xml with the available configuration.
     *
     * @param file XML file.
     */
    private void parseConfigDescriptions(File file) {
        try {
            JAXBContext jc = JAXBContext.newInstance(ConfigDescriptions.class);

            Unmarshaller unmarshaller = jc.createUnmarshaller();
            ConfigDescriptions configDesc = (ConfigDescriptions) unmarshaller.unmarshal(file);
            for (ConfigDescription c : configDesc.getConfigDescription()) {
                configList.put(c);
            }
        } catch (Exception e) {
            getLog().error(e);
        }
    }

    /**
     * Parses the xml with the available binding.
     *
     * @param file XML file.
     */
    private void parseBindingDescription(File file) {
        try {
            JAXBContext jc = JAXBContext
                    .newInstance(org.eclipse.smarthome.documentation.schemas.binding.v1_0.Binding.class);

            Unmarshaller unmarshaller = jc.createUnmarshaller();
            binding = new Binding(
                    (org.eclipse.smarthome.documentation.schemas.binding.v1_0.Binding) unmarshaller.unmarshal(file));
        } catch (Exception e) {
            getLog().error(e);
        }
    }
}
