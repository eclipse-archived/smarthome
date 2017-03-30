/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.extensionservice.marketplace.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.extension.Extension;
import org.eclipse.smarthome.core.extension.ExtensionEventFactory;
import org.eclipse.smarthome.core.extension.ExtensionService;
import org.eclipse.smarthome.core.extension.ExtensionType;
import org.eclipse.smarthome.extensionservice.marketplace.MarketplaceExtension;
import org.eclipse.smarthome.extensionservice.marketplace.MarketplaceExtensionHandler;
import org.eclipse.smarthome.extensionservice.marketplace.MarketplaceHandlerException;
import org.eclipse.smarthome.extensionservice.marketplace.internal.model.Node;
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

    // constants used in marketplace nodes
    private static final String MP_PACKAGETYPE_BINDING = "binding";
    private static final String MP_PACKAGETYPE_RULE_TEMPLATE = "rule_template";

    private static final String MARKETPLACE_HOST = "marketplace.eclipse.org";
    private static final Pattern EXTENSION_ID_PATTERN = Pattern.compile(".*?mpc_install=([^&]+?)(&.*)?");

    private final Logger logger = LoggerFactory.getLogger(MarketplaceExtensionService.class);

    // increased visibility for unit tests
    MarketplaceProxy proxy;
    private EventPublisher eventPublisher;
    private Pattern labelPattern = Pattern.compile("<.*>"); // checks for the existence of any xml element
    private Pattern descriptionPattern = Pattern.compile("<(javascript|div|font)"); // checks for the existence of some
                                                                                    // invalid elements

    private boolean includeBindings = true;
    private boolean includeRuleTemplates = true;
    private int maturityLevel = 1;
    private Set<MarketplaceExtensionHandler> extensionHandlers = new HashSet<>();

    protected void activate(Map<String, Object> config) {
        this.proxy = new MarketplaceProxy();
        modified(config);
    }

    protected void deactivate() {
        this.proxy.dispose();
        this.proxy = null;
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
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    protected void addExtensionHandler(MarketplaceExtensionHandler handler) {
        this.extensionHandlers.add(handler);
    }

    protected void removeExtensionHandler(MarketplaceExtensionHandler handler) {
        this.extensionHandlers.remove(handler);
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

            MarketplaceExtension ext = convertToExtension(node);
            if (ext != null) {
                if (setInstalledFlag(ext)) {
                    exts.add(ext);
                }
            }
        }
        return exts;
    }

    private boolean setInstalledFlag(MarketplaceExtension ext) {
        for (MarketplaceExtensionHandler handler : extensionHandlers) {
            if (handler.supports(ext)) {
                ext.setInstalled(handler.isInstalled(ext));
                return true;
            }
        }
        return false;
    }

    private MarketplaceExtension convertToExtension(Node node) {
        String extId = getExtensionId(node);

        String name = node.name;
        String desc = node.shortdescription;
        String version = StringUtils.isNotEmpty(node.version) ? node.version : "1.0";

        if (!validName(name) || !validDescription(desc)) {
            logger.debug("Ignoring node {} due to invalid content.", node.id);
            return null;
        }
        if (MP_PACKAGETYPE_BINDING.equals(node.packagetypes)) {
            MarketplaceExtension ext = new MarketplaceExtension(extId, MarketplaceExtension.EXT_TYPE_BINDING, name,
                    version, node.supporturl, false, desc, null, node.image, node.updateurl, node.packageformat);
            return ext;
        } else if (MP_PACKAGETYPE_RULE_TEMPLATE.equals(node.packagetypes)) {
            MarketplaceExtension ext = new MarketplaceExtension(extId, MarketplaceExtension.EXT_TYPE_RULE_TEMPLATE,
                    name, version, node.supporturl, false, desc, null, node.image, node.updateurl, node.packageformat);
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
        ArrayList<ExtensionType> types = new ArrayList<>(2);
        List<Extension> exts = getExtensions(locale);
        if (includeBindings) {
            for (Extension ext : exts) {
                if (ext.getType().equals(MarketplaceExtension.EXT_TYPE_BINDING)) {
                    types.add(new ExtensionType(MarketplaceExtension.EXT_TYPE_BINDING, "Bindings"));
                    break;
                }
            }
        }
        if (includeRuleTemplates) {
            for (Extension ext : exts) {
                if (ext.getType().equals(MarketplaceExtension.EXT_TYPE_RULE_TEMPLATE)) {
                    types.add(new ExtensionType(MarketplaceExtension.EXT_TYPE_RULE_TEMPLATE, "Rule Templates"));
                    break;
                }
            }
        }
        return Collections.unmodifiableList(types);
    }

    @Override
    public void install(String extensionId) {
        Extension ext = getExtension(extensionId, null);
        if (ext instanceof MarketplaceExtension) {
            MarketplaceExtension mpExt = (MarketplaceExtension) ext;
            for (MarketplaceExtensionHandler handler : extensionHandlers) {
                if (handler.supports(mpExt)) {
                    if (!handler.isInstalled(mpExt)) {
                        try {
                            handler.install(mpExt);
                            postInstalledEvent(extensionId);
                        } catch (MarketplaceHandlerException e) {
                            postFailureEvent(extensionId, e.getMessage());
                        }
                    } else {
                        postFailureEvent(extensionId, "Extension is already installed.");
                    }
                    return;
                }
            }
        }
        postFailureEvent(extensionId, "Extension not known.");
    }

    @Override
    public void uninstall(String extensionId) {
        Extension ext = getExtension(extensionId, null);
        if (ext instanceof MarketplaceExtension) {
            MarketplaceExtension mpExt = (MarketplaceExtension) ext;
            for (MarketplaceExtensionHandler handler : extensionHandlers) {
                if (handler.supports(mpExt)) {
                    if (handler.isInstalled(mpExt)) {
                        try {
                            handler.uninstall(mpExt);
                            postUninstalledEvent(extensionId);
                        } catch (MarketplaceHandlerException e) {
                            postFailureEvent(extensionId, e.getMessage());
                        }
                    } else {
                        postFailureEvent(extensionId, "Extension is not installed.");
                    }
                    return;
                }
            }
        }
        postFailureEvent(extensionId, "Extension not known.");
    }

    @Override
    public String getExtensionId(URI extensionURI) {
        if (extensionURI != null && extensionURI.getHost().equals(MARKETPLACE_HOST)) {
            return extractExensionId(extensionURI);
        }

        return null;
    }

    private void postInstalledEvent(String extensionId) {
        Event event = ExtensionEventFactory.createExtensionInstalledEvent(extensionId);
        eventPublisher.post(event);
    }

    private void postUninstalledEvent(String extensionId) {
        Event event = ExtensionEventFactory.createExtensionUninstalledEvent(extensionId);
        eventPublisher.post(event);
    }

    private void postFailureEvent(String extensionId, String msg) {
        Event event = ExtensionEventFactory.createExtensionFailureEvent(extensionId, msg);
        eventPublisher.post(event);
    }

    private String getExtensionId(Node node) {
        StringBuilder sb = new StringBuilder(MarketplaceExtension.EXT_PREFIX);
        switch (node.packagetypes) {
            case MP_PACKAGETYPE_RULE_TEMPLATE:
                sb.append(MarketplaceExtension.EXT_TYPE_RULE_TEMPLATE).append("-");
                break;
            case MP_PACKAGETYPE_BINDING:
                sb.append(MarketplaceExtension.EXT_TYPE_BINDING).append("-");
                break;
            default:
                return null;
        }
        sb.append(node.id.replaceAll("[^a-zA-Z0-9_]", ""));
        return sb.toString();
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

    private boolean validName(String name) {
        return !labelPattern.matcher(name).find();
    }

    private boolean validDescription(String desc) {
        return !descriptionPattern.matcher(desc).find();
    }

    private String extractExensionId(URI uri) {
        Matcher idMatcher = EXTENSION_ID_PATTERN.matcher(uri.getQuery());
        String id = null;
        if (idMatcher.matches() && idMatcher.groupCount() > 1) {
            id = idMatcher.group(1);
        }

        Optional<Node> extensionNode = getExtensionNode(id);

        return extensionNode.isPresent() ? getExtensionId(extensionNode.get()) : null;
    }

    private Optional<Node> getExtensionNode(String id) {
        return proxy.getNodes().stream().filter(node -> node != null && node.id.equals(id)).findFirst();
    }

}
