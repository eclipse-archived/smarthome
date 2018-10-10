/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Map;

import org.eclipse.smarthome.io.net.security.internal.providerImpl.SSLContextConfigurableProvider;
import org.junit.rules.TemporaryFolder;
import org.osgi.service.component.annotations.Activate;

// Set the path were the certificate is created to the working directory
class SSLContextConfigurableProviderEx extends SSLContextConfigurableProvider {
    Date currentDate = new Date();
    Path folder;

    public SSLContextConfigurableProviderEx(TemporaryFolder folder) {
        this.folder = folder.getRoot().toPath();
    }

    @Override
    protected Path getKeystoreFilename() {
        return folder.resolve(serviceContext + ".keystore");
    }

    @Override
    @Activate
    public void activate(Map<String, Object> properties) throws IOException, GeneralSecurityException {
        super.activate(properties);
    }

    @Override
    @Activate
    public void modified(Map<String, Object> properties) throws IOException, GeneralSecurityException {
        super.modified(properties);
    }

    @Override
    protected void loadKeyStore() throws IOException, GeneralSecurityException {

        super.loadKeyStore();
    }

    @Override
    protected Date dateNow() {
        return currentDate;
    }
}