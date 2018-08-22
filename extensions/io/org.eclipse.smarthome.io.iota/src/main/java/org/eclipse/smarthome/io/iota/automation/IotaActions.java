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
package org.eclipse.smarthome.io.iota.automation;

import java.util.HashMap;

import org.eclipse.smarthome.io.iota.utils.IotaUtilsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IotaActions} class defines actions that can be used in ESH rules.
 *
 * @author Theo Giovanna - Initial contribution
 */
public class IotaActions {

    private static final Logger logger = LoggerFactory.getLogger(IotaActions.class);
    private static final HashMap<String, IotaUtilsImpl> utilsBySeedName = new HashMap<>();
    private static final HashMap<String, String> seedAddressBySeedName = new HashMap<>();

    /**
     * Send IOTA tokens
     *
     * @param seedName        the seedName which the user has configured in smarthome.cfg
     * @param toWallet        the wallet address on which to send the funds
     * @param forAmount       the amount in Miota
     * @param optionalMessage an optional message that will be included in the bundle transfer
     * @param optionalTag     an optional tag that will be included in the bundle transfer
     * @return true if the transfer was successful
     */
    public static boolean sendIotaTokens(String seedName, String toWallet, String forAmount, String optionalMessage,
            String optionalTag) {
        logger.debug("Requested payment of {} Miota from seed {} to wallet {}", seedAddressBySeedName.get(seedName),
                toWallet, forAmount);
        boolean success = false;
        success = utilsBySeedName.get(seedName).executePayment(seedAddressBySeedName.get(seedName), toWallet, forAmount,
                optionalMessage, optionalTag);
        return success;
    }

    public static void addUtilsBySeed(String seedName, IotaUtilsImpl utils) {
        utilsBySeedName.put(seedName, utils);
    }

    public static void addSeedAddressBySeedName(String seedName, String seedAddress) {
        seedAddressBySeedName.put(seedName, seedAddress);
    }
}
