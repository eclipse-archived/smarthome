/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.internal.extension;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.crypto.Crypto;

/**
 * Console command extension for crypto module functions.
 *
 * @author Pauli Anttila - Initial contribution
 *
 */
public class CryptoConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private Crypto crypto;

    private static final String SUBCMD_ENCRYPT = "encrypt";
    private static final String SUBCMD_SET_PASSPHRASE = "setpassphrase";

    public CryptoConsoleCommandExtension() {
        super("crypto", "Crypto utils");
    }

    protected void setCryptoModule(Crypto crypto) {
        this.crypto = crypto;
    }

    protected void unsetCryptoModule(Crypto crypto) {
        this.crypto = null;
    }

    @Override
    public List<String> getUsages() {
        return Arrays
                .asList(new String[] { buildCommandUsage(SUBCMD_ENCRYPT + " <plaintext>", "Encrypt plain text value"),
                        buildCommandUsage(SUBCMD_SET_PASSPHRASE + " <passphrase>", "Set encryption passphrase") });
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            String subCommand = args[0];
            switch (subCommand) {
                case SUBCMD_ENCRYPT:
                    if (args.length > 1) {
                        String value = args[1];
                        try {
                            String encryptedText = crypto.encrypt(value);
                            console.println("Encrypted value: " + encryptedText);
                        } catch (Exception e) {
                            console.println("Error occured during value encryption: " + e.getMessage());
                        }
                    } else {
                        console.println("Specify the plaintext to encrypt: " + this.getCommand() + " " + SUBCMD_ENCRYPT
                                + " <plaintext>");
                    }
                    break;
                case SUBCMD_SET_PASSPHRASE:
                    if (args.length > 1) {
                        String key = args[1];
                        crypto.setEncPassphrase(key.getBytes());
                    } else {
                        console.println("Specify the passphrase: " + this.getCommand() + " " + SUBCMD_SET_PASSPHRASE
                                + " <passphrase>");
                    }
                    break;
                default:
                    console.println("Unknown command '" + subCommand + "'");
                    printUsage(console);
                    break;
            }
        } else {
            printUsage(console);
        }
    }

}
