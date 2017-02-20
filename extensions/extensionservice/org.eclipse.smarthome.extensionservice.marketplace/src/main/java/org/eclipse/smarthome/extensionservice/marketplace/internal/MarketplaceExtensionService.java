/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.extensionservice.marketplace.internal;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.automation.parser.ParsingException;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.extension.Extension;
import org.eclipse.smarthome.core.extension.ExtensionEventFactory;
import org.eclipse.smarthome.core.extension.ExtensionService;
import org.eclipse.smarthome.core.extension.ExtensionType;
import org.eclipse.smarthome.extensionservice.marketplace.internal.model.Node;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an {@link ExtensionService}, which accesses the Eclipse IoT Marketplace and makes its content available as
 * extensions.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class MarketplaceExtensionService implements ExtensionService {

    private static final String BINDING_FILE = "installedBindingsMap.csv";
    // constants used in marketplace nodes
    private static final String MP_PACKAGETYPE_BINDING = "binding";
    private static final String MP_PACKAGETYPE_RULE_TEMPLATE = "rule_template";

    // constants used to construct extension IDs
    private static final String EXT_PREFIX = "market:";
    private static final String EXT_TYPE_RULE_TEMPLATE = "ruletemplate";
    private static final String EXT_TYPE_BINDING = "binding";

    private final Logger logger = LoggerFactory.getLogger(MarketplaceExtensionService.class);

    private List<ExtensionType> extensionTypes;
    private MarketplaceProxy proxy;
    private EventPublisher eventPublisher;
    private MarketplaceRuleTemplateProvider marketplaceRuleTemplateProvider;
    private Map<String, Long> installedBindings;

    private boolean includeBindings = true;
    private boolean includeRuleTemplates = true;
    private int maturityLevel = 1;

    private BundleContext bundleContext;

    protected void activate(BundleContext bundleContext, Map<String, Object> config) {
        this.bundleContext = bundleContext;
        installedBindings = loadInstalledBindingsMap();
        this.proxy = new MarketplaceProxy();
        modified(config);
    }

    protected void deactivate() {
        this.proxy = null;
        this.installedBindings = null;
        this.bundleContext = null;
    }

    protected void modified(Map<String, Object> config) {
        Object bindingCfg = config.get("bindings");
        if (bindingCfg != null) {
            this.includeBindings = bindingCfg.toString().equals(Boolean.TRUE.toString());
        }
        Object ruleTemplateCfg = config.get("ruletemplates");
        if (ruleTemplateCfg != null) {
            this.includeRuleTemplates = ruleTemplateCfg.toString().equals(Boolean.TRUE.toString());
        }
        Object cfgMaturityLevel = config.get("maturity");
        if (cfgMaturityLevel != null) {
            try {
                this.maturityLevel = Integer.valueOf(cfgMaturityLevel.toString());
            } catch (NumberFormatException e) {
                logger.warn("Ignoring invalid value '{}' for configuration parameter '{}'", cfgMaturityLevel.toString(),
                        "maturity");
            }
        }
        constructTypeList();
    }

    protected void setMarketplaceRuleTemplateProvider(MarketplaceRuleTemplateProvider marketplaceRuleTemplateProvider) {
        this.marketplaceRuleTemplateProvider = marketplaceRuleTemplateProvider;
    }

    protected void unsetMarketplaceRuleTemplateProvider(
            MarketplaceRuleTemplateProvider marketplaceRuleTemplateProvider) {
        this.marketplaceRuleTemplateProvider = null;
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    @Override
    public List<Extension> getExtensions(Locale locale) {
        List<Node> nodes = proxy.getNodes();
        List<Extension> exts = new ArrayList<>(nodes.size());
        for (Node node : nodes) {
            if (node.id == null) {
                // workaround for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=512493
                continue;
            }
            if (toMaturityLevel(node.status) < this.maturityLevel) {
                continue;
            }
            if (!includeBindings && node.packagetypes.equals(MP_PACKAGETYPE_BINDING)) {
                continue;
            }
            if (!includeRuleTemplates && node.packagetypes.equals(MP_PACKAGETYPE_RULE_TEMPLATE)) {
                continue;
            }

            Extension ext = convertToExtension(node);
            if (ext != null) {
                exts.add(ext);
            }
        }
        return exts;
    }

    private MarketplaceExtension convertToExtension(Node node) {
        String extId = getExtensionId(node);
        if (MP_PACKAGETYPE_BINDING.equals(node.packagetypes)) {
            MarketplaceExtension ext = new MarketplaceExtension(extId, EXT_TYPE_BINDING, node.name, node.version,
                    node.supporturl, isInstalled(extId), node.shortdescription, null, node.image, node.updateurl);
            return ext;
        } else if (MP_PACKAGETYPE_RULE_TEMPLATE.equals(node.packagetypes)) {
            String version = StringUtils.isNotEmpty(node.version) ? node.version : "1.0";
            MarketplaceExtension ext = new MarketplaceExtension(extId, EXT_TYPE_RULE_TEMPLATE, node.name, version,
                    node.supporturl, isInstalled(extId), node.shortdescription, null, node.image, node.updateurl);
            return ext;
        } else {
            return null;
        }
    }

    @Override
    public Extension getExtension(String id, Locale locale) {
        for (Extension extension : getExtensions(locale)) {
            if (extension.getId().equals(id)) {
                return extension;
            }
        }
        return null;
    }

    @Override
    public List<ExtensionType> getTypes(Locale locale) {
        return extensionTypes;
    }

    @Override
    public void install(String extensionId) {
        Extension ext = getExtension(extensionId, null);
        if (ext instanceof MarketplaceExtension && !ext.isInstalled()) {
            MarketplaceExtension mpExt = (MarketplaceExtension) ext;
            String type = getType(extensionId);
            switch (type) {
                case EXT_TYPE_RULE_TEMPLATE:
                    installRuleTemplate(extensionId, mpExt);
                    break;
                case EXT_TYPE_BINDING:
                    installBinding(extensionId, mpExt);
                    break;
                default:
                    postFailureEvent(extensionId, "Id not known.");
                    break;
            }
        }
    }

    @Override
    public void uninstall(String extensionId) {
        Extension ext = getExtension(extensionId, null);
        if (ext != null && ext.isInstalled()) {
            String type = getType(extensionId);
            switch (type) {
                case EXT_TYPE_RULE_TEMPLATE:
                    marketplaceRuleTemplateProvider.remove(extensionId);
                    postUninstalledEvent(extensionId);
                    break;
                case EXT_TYPE_BINDING:
                    uninstallBinding(extensionId);
                    break;
                default:
                    postFailureEvent(extensionId, "Id not known.");
                    break;
            }
        }
    }

    private void installRuleTemplate(String extensionId, MarketplaceExtension mpExt) {
        String url = mpExt.getDownloadUrl();
        try {
            String template = getTemplate(url);
            marketplaceRuleTemplateProvider.addTemplateAsJSON(extensionId, template);
            postInstalledEvent(extensionId);
        } catch (ParsingException e) {
            postFailureEvent(extensionId, "Template is not valid.");
            logger.error("Rule template from marketplace is invalid: {}", e.getMessage());
        } catch (IOException e) {
            postFailureEvent(extensionId, "Template cannot be downloaded.");
            logger.error("Rule template from marketplace cannot be downloaded: {}", e.getMessage());
        }
    }

    private void installBinding(String extensionId, MarketplaceExtension mpExt) {
        String url = mpExt.getDownloadUrl();
        try {
            Bundle bundle = bundleContext.installBundle(url);
            installedBindings.put(extensionId, bundle.getBundleId());
            persistInstalledBindingsMap(installedBindings);
            postInstalledEvent(extensionId);
        } catch (BundleException e) {
            postFailureEvent(extensionId, "Binding cannot be installed: " + e.getMessage());
        }
    }

    private void uninstallBinding(String extensionId) {
        Long id = installedBindings.get(extensionId);
        if (id != null) {
            Bundle bundle = bundleContext.getBundle(id);
            if (bundle != null) {
                try {
                    bundle.uninstall();
                    installedBindings.remove(extensionId);
                    persistInstalledBindingsMap(installedBindings);
                    postUninstalledEvent(extensionId);
                } catch (BundleException e) {
                    postFailureEvent(extensionId, "Failed deinstalling binding: " + e.getMessage());
                }
            } else {
                // we do not have such a bundle, so let's remove it from our internal map
                installedBindings.remove(extensionId);
                persistInstalledBindingsMap(installedBindings);
                postFailureEvent(extensionId, "Id not known.");
            }
        } else {
            postFailureEvent(extensionId, "Id not known.");
        }
    }

    private String getTemplate(String urlString) throws IOException {
        URL url = new URL(urlString);
        return IOUtils.toString(url);
    }

    private void postInstalledEvent(String extensionId) {
        if (eventPublisher != null) {
            Event event = ExtensionEventFactory.createExtensionInstalledEvent(extensionId);
            eventPublisher.post(event);
        }
    }

    private void postUninstalledEvent(String extensionId) {
        if (eventPublisher != null) {
            Event event = ExtensionEventFactory.createExtensionUninstalledEvent(extensionId);
            eventPublisher.post(event);
        }
    }

    private void postFailureEvent(String extensionId, String msg) {
        if (eventPublisher != null) {
            Event event = ExtensionEventFactory.createExtensionFailureEvent(extensionId, msg);
            eventPublisher.post(event);
        }
    }

    private String getExtensionId(Node node) {
        StringBuilder sb = new StringBuilder(EXT_PREFIX);
        switch (node.packagetypes) {
            case MP_PACKAGETYPE_RULE_TEMPLATE:
                sb.append(EXT_TYPE_RULE_TEMPLATE).append("-");
                break;
            case MP_PACKAGETYPE_BINDING:
                sb.append(EXT_TYPE_BINDING).append("-");
                break;
            default:
                return null;
        }
        sb.append(node.id); // todo: remove illegal chars
        return sb.toString();
    }

    private boolean isInstalled(String extensionId) {
        String type = getType(extensionId);
        if (type == null) {
            return false;
        }
        switch (type) {
            case EXT_TYPE_RULE_TEMPLATE:
                return marketplaceRuleTemplateProvider.get(extensionId) != null;
            case EXT_TYPE_BINDING:
                return installedBindings.containsKey(extensionId);
            default:
                return false;
        }
    }

    private String getType(String extensionId) {
        if (extensionId.startsWith(EXT_PREFIX)) {
            String idWithoutPrefix = extensionId.substring(EXT_PREFIX.length());
            return idWithoutPrefix.substring(0, idWithoutPrefix.lastIndexOf('-'));
        } else {
            return null;
        }
    }

    private Map<String, Long> loadInstalledBindingsMap() {
        File dataFile = bundleContext.getDataFile(BINDING_FILE);
        if (dataFile != null && dataFile.exists()) {
            try (FileReader reader = new FileReader(dataFile)) {
                LineIterator lineIterator = IOUtils.lineIterator(reader);
                Map<String, Long> map = new HashMap<>();
                while (lineIterator.hasNext()) {
                    String line = lineIterator.nextLine();
                    String[] parts = line.split(";");
                    try {
                        map.put(parts[0], Long.valueOf(parts[1]));
                    } catch (Exception e) {
                        logger.debug("Invalid line in file {} - ignoring it:\n{}", dataFile.getName(), line);
                    }
                }
                return map;
            } catch (IOException e) {
                logger.debug("File '{}' for installed bindings does not exist.", dataFile.getName());
                // ignore and just return an empty map
            }
        }
        return new HashMap<>();
    }

    private synchronized void persistInstalledBindingsMap(Map<String, Long> map) {
        File dataFile = bundleContext.getDataFile(BINDING_FILE);
        if (dataFile != null) {
            try (FileWriter writer = new FileWriter(dataFile)) {
                for (Entry<String, Long> entry : map.entrySet()) {
                    writer.write(entry.getKey() + ";" + entry.getValue() + System.lineSeparator());
                }
            } catch (IOException e) {
                logger.warn("Failed writing file '{}': {}", dataFile.getName(), e.getMessage());
            }
        } else {
            logger.debug("System does not support bundle data files -> not persisting installed binding info");
        }
    }

    private int toMaturityLevel(String maturity) {
        switch (maturity) {
            case "Alpha":
                return 0;
            case "Beta":
                return 1;
            case "Production/Stable":
                return 2;
            case "Mature":
                return 3;
            default:
                logger.debug("Unknown maturity level value '{}' - using 'Alpha' instead.", maturity);
                return 0;
        }
    }

    private void constructTypeList() {
        ArrayList<ExtensionType> types = new ArrayList<>(2);
        if (includeBindings) {
            types.add(new ExtensionType(EXT_TYPE_BINDING, "Bindings"));
        }
        if (includeRuleTemplates) {
            types.add(new ExtensionType(EXT_TYPE_RULE_TEMPLATE, "Rule Templates"));
        }
        extensionTypes = Collections.unmodifiableList(types);
    }
}
