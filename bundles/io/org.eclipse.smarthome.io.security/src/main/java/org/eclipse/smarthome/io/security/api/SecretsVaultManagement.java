/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.io.security.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.AbstractMap.SimpleEntry;
import java.util.Enumeration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SecretsVaultManagement} allows to retrieve all keys for a given
 * bundle or all bundles. The actual secrets cannot be retrieved via this
 * management interface and are only accessible by the {@link SecretsVaultService}
 * for the bundle that stored the secrets.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface SecretsVaultManagement {
    /**
     * Retrieve all stored keys in no specific order.
     *
     * @return A list of <bundlename,key> tuples.
     */
    Enumeration<SimpleEntry<String, String>> getAllKeys();

    /**
     * Retrieve all stored keys for the given bundle in no specific order.
     *
     * @param bundlename The fully qualified bundle name, e.g. org.eclipse.smarthome.binding.abc
     * @return A list of stored keys.
     */
    Enumeration<String> getAllKeys(String bundlename);

    /**
     * Set/Delete a secret, identified by the given key, bundlename and old secret value.
     *
     * @param bundlename The fully qualified bundle name, e.g. org.eclipse.smarthome.binding.abc
     * @param key An identifier
     * @param oldSecret The old secret value. It need to match the currently stored value.
     * @param secret A secret as a ByteBuffer. If the ByteBuffer is empty, an empty secret will be stored.
     *            If null is given, the secret will be erased.
     * @return Return true if the modification was successful
     * @throws IOException If the underlying file can't be opened or is not writable.
     * @throws GeneralSecurityException If the encryption algorithm fails, this exception is thrown
     */
    boolean set(String bundlename, String key, ByteBuffer oldSecret, @Nullable ByteBuffer secret)
            throws IOException, GeneralSecurityException;

    /**
     * All bundle secrets are stored in a protected format. An implementation could be a java {@link KeyStore} for
     * example. A master password is used for encryption. This method allows to change the master password by
     * re-encrypting all bundle secrets with the new password.
     *
     * The implementation need to guarantee that the master password change happens in a transactional way and any
     * changes will be rolled back if an error happens.
     *
     * @param oldSecret The old secret value. It need to match the current master password
     * @param secret
     * @return Returns false if the new master password could not be applied.
     */
    boolean setMasterSecret(ByteBuffer oldSecret, ByteBuffer secret);
}
