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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SecretsVaultService} can be used to store and retrieve secrets.
 *
 * Secrets can only be retrieved by the bundle that has stored them and by default
 * the backend is required to store secrets encrypted only.
 *
 * Because of the way how the String class is handled internally, you are strongly
 * encouraged to use char or byte arrays or the ByteBuffer class
 * for keeping encrypted secrets in memory.
 *
 * The encryption algorithm, the storage location and format depend on the implementation.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface SecretsVaultService {
    /**
     * Retrieve a secret, identified by the given key.
     *
     * @param key An identifier
     * @return Either the secret as ByteBuffer that was stored by this bundle under the given key
     *         or null if no secret could be found.
     * @throws IOException If the underlying file, database or memory backend can't be opened or read.
     * @throws GeneralSecurityException If the decryption algorithm fails, this exception is thrown
     */
    @Nullable
    ByteBuffer retrieve(String key) throws IOException, GeneralSecurityException;

    /**
     * Store/Delete a secret, identified by the given key.
     *
     * Because the key names are publicly accessible, do not encode any
     * privacy relevant information in those keys (for example usernames etc).
     *
     * @param key An identifier
     * @param secret A secret as a ByteBuffer. If the ByteBuffer is empty, an empty secret will be stored.
     *            If null is given, the secret will be erased.
     * @throws IOException If the underlying file, database or memory backend can't be opened or is not writable.
     * @throws GeneralSecurityException If the encryption algorithm fails, this exception is thrown
     */
    void set(String key, @Nullable ByteBuffer secret) throws IOException, GeneralSecurityException;
}
