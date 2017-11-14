/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.extensionservice.marketplace;

/**
 * This interface can be implemented by services that want to register as handlers for specific marketplace extension
 * types and formats.
 * In a system there should always only be exactly one handler responsible for a given type+format combination. If
 * multiple handers support it, it is undefined which one will be called.
 * This mechanism allows solutions to add support for specific formats (e.g. Karaf features) that are not supported by
 * ESH out of the box.
 * It also allows to decide which extension types are made available at all.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface MarketplaceExtensionHandler {

    /**
     * Tells whether this handler supports a given extension.
     *
     * @param ext the extension in question
     * @return true, if the extension is supported, false otherwise
     */
    boolean supports(MarketplaceExtension ext);

    /**
     * Tells whether a given extension is currently installed.
     * Note: This method is only called, if the hander claimed support for the extension before.
     *
     * @param ext the extension in question
     * @return true, if the extension is installed, false otherwise
     */
    boolean isInstalled(MarketplaceExtension ext);

    /**
     * Installs a given extension.
     * Note: This method is only called, if the hander claimed support for the extension before.
     *
     * @param ext the extension to install
     * @throws MarketplaceHandlerException if the installation failed for some reason
     */
    void install(MarketplaceExtension ext) throws MarketplaceHandlerException;

    /**
     * Uninstalls a given extension.
     * Note: This method is only called, if the hander claimed support for the extension before.
     *
     * @param ext the extension to uninstall
     * @throws MarketplaceHandlerException if the uninstallation failed for some reason
     */
    void uninstall(MarketplaceExtension ext) throws MarketplaceHandlerException;
}
