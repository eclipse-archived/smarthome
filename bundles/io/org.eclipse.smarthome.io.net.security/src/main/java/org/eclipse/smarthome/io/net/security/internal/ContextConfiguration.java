/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal;

import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Objects;

import org.apache.commons.lang.RandomStringUtils;
import org.eclipse.smarthome.io.net.security.internal.config.Base64Bytes;
import org.eclipse.smarthome.io.net.security.internal.config.Base64Chars;
import org.eclipse.smarthome.io.net.security.internal.config.FieldRequired;
import org.eclipse.smarthome.io.net.security.internal.config.SecretVaultField;

/**
 * Represents configuration of one context.
 *
 * @author David Graeff - Initial contribution
 */
public class ContextConfiguration {
    public @FieldRequired String context = "default";
    public String keystoreAlias = "main";
    public String keystoreFormat = "JCEKS";
    public String keystoreFilename = "%CONTEXT%.keystore";
    public String domain;
    public @SecretVaultField @Base64Bytes char[] keystorePasswordBase64 = randomPassword().toCharArray();
    public @SecretVaultField @Base64Chars char[] privateKeyPasswordBase64 = randomPassword().toCharArray();
    public @SecretVaultField @Base64Bytes byte[] preSharedKeyBase64;
    public Boolean autoRefreshInvalidCertificate = false;

    @Override
    public int hashCode() {
        return Objects.hash(context, domain, keystorePasswordBase64, privateKeyPasswordBase64);
    }

    public static String randomPassword() {
        return RandomStringUtils.random(15, 0, 0, false, false, null, new SecureRandom());
    }

    public Path getAbsoluteKeystoreFilename(Path basedir) {
        return basedir.resolve(keystoreFilename.replace("%CONTEXT%", context));
    }

}