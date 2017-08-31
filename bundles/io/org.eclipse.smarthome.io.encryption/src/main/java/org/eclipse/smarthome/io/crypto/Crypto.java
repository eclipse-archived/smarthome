/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.crypto;

import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 * Crypto module API.
 *
 * @author Pauli Anttila - Initial contribution
 *
 */
public interface Crypto {

    public String encrypt(String plainText) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException;

    public String decrypt(String encryptedText)
            throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException;

    void setEncPassphrase(byte[] secret);
}
