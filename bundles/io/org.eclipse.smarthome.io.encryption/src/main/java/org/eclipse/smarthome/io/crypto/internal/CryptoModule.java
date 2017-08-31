/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.crypto.internal;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.bind.DatatypeConverter;

import org.eclipse.smarthome.io.crypto.Crypto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Pauli Anttila - Initial contribution
 *
 */
public class CryptoModule implements Crypto {
    public final static String ENV_VARIABLE_ENC_PASSPHRASE = "CFG_ENCRYPTION_PASSPHRASE";

    private final Logger logger = LoggerFactory.getLogger(CryptoModule.class);

    private byte[] salt = { (byte) 0xB8, (byte) 0x62, (byte) 0xF2, (byte) 0x29, (byte) 0x85, (byte) 0xE8, (byte) 0x11,
            (byte) 0x96 };

    private int iterationCount = 2000;
    private Cipher dcipher;
    private Cipher ecipher;
    private SecretKey secretKey;

    public CryptoModule() {
        initEnc();
    }

    private void initEnc() {
        try {
            byte[] encodedKey = CryptoActivator.getKey();
            if (encodedKey != null) {
                setEncPassphrase(encodedKey);
                CryptoActivator.clearKey();
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Secret key initialization failed. ", e);
        }
    }

    @Override
    public String encrypt(String plainText) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (ecipher != null) {
            try {
                AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);
                ecipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
                byte[] plainTextBytes = plainText.getBytes("UTF-8");
                byte[] encryptedBytes = ecipher.doFinal(plainTextBytes);
                Base64.Encoder encoder = Base64.getEncoder();
                String encryptedText = encoder.encodeToString(encryptedBytes);
                return encryptedText;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
            return null;

        } else {
            throw new IllegalStateException("Secret key not initialized");
        }
    }

    @Override
    public String decrypt(String encryptedText)
            throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        if (dcipher != null) {
            try {
                AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);
                dcipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
                Base64.Decoder decoder = Base64.getDecoder();
                byte[] encryptedTextBytes = decoder.decode(encryptedText);
                byte[] decryptedBytes = dcipher.doFinal(encryptedTextBytes);
                String decryptedText = new String(decryptedBytes, "UTF8");
                return decryptedText;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            throw new IllegalStateException("Secret key not initialized");
        }
    }

    @Override
    public void setEncPassphrase(byte[] secret) {
        try {
            String text = new String(secret, "UTF-8");
            KeySpec keySpec = new PBEKeySpec(text.toCharArray(), salt, iterationCount);
            secretKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
            ecipher = Cipher.getInstance(secretKey.getAlgorithm());
            dcipher = Cipher.getInstance(secretKey.getAlgorithm());
        } catch (InvalidKeySpecException e) {
            logger.warn("Error occured during encryption key initialization. ", e);
        } catch (NoSuchPaddingException e) {
            logger.warn("Error occured during encryption key initialization. ", e);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Error occured during encryption key initialization. ", e);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Error occured during encryption key initialization. ", e);
        }
    }

    public static byte[] toByteArray(String s) {
        return DatatypeConverter.parseHexBinary(s);
    }
}
