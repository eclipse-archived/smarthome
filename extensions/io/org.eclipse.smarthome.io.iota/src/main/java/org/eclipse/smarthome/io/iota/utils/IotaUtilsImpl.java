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
package org.eclipse.smarthome.io.iota.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jota.IotaAPI;
import jota.dto.response.GetBalancesResponse;
import jota.dto.response.SendTransferResponse;
import jota.error.ArgumentException;
import jota.model.Transfer;
import jota.utils.InputValidator;
import jota.utils.TrytesConverter;

/**
 * The {@link IotaUtilsImpl} provides utils methods to work with IOTA transactions.
 *
 * @author Theo Giovanna - Initial Contribution
 */
public class IotaUtilsImpl implements IotaUtils {

    private final Logger logger = LoggerFactory.getLogger(IotaUtilsImpl.class);
    private static final String PATH = "../../extensions/io/org.eclipse.smarthome.io.iota/lib/mam/example/";
    private String seed = null;
    private int start = 0;
    private IotaAPI iotaAPI;
    private Process process;
    private String oldResult = "";
    private final String npmPath = isWindows() ? "npm.cmd" : "/usr/local/bin/node";

    public IotaUtilsImpl() {

    }

    /**
     * Instanciate a new Impl of IotaUtils for a specific node configuration
     *
     * @param protocol the protocol
     * @param host     the host
     * @param port     the port
     */
    public IotaUtilsImpl(String protocol, String host, int port) {
        this.iotaAPI = new IotaAPI.Builder().protocol(protocol).host(host).port(String.valueOf(port)).build();
    }

    /**
     * Instanciate a new Impl of IotaUtils for a specific node configuration, MAM seed and tree depth. Allows messages
     * to be published after previous messages on the same Merkle Root Tree.
     *
     * @param protocol the protocol
     * @param host     the host
     * @param port     the port
     * @param seed     the seed (Merkle Root Address)
     * @param start    the count of messages already published on the seed
     */
    public IotaUtilsImpl(String protocol, String host, int port, String seed, int start) {
        this.iotaAPI = new IotaAPI.Builder().protocol(protocol).host(host).port(String.valueOf(port)).build();
        this.seed = seed;
        this.start = start;
    }

    /**
     * Attach data to the Tangle, through MAM
     *
     * @param states the json struct
     * @param mode   the MAM mode
     * @param key    the key if using restricted mode
     */
    public void publishState(JsonElement states, String mode, String key) {

        String payload = states.toString();
        ArrayList<String> param = new ArrayList<>();
        JsonParser parser = new JsonParser();

        switch (mode) {
            case "public":
            case "private":
                param.addAll(Arrays.asList(new String[] { npmPath, PATH + "publish.js",
                        iotaAPI.getProtocol() + "://" + iotaAPI.getHost() + ":" + iotaAPI.getPort(), payload, mode,
                        seed }));
                if (start == -1) {
                    param.add(String.valueOf(start));
                }
                break;
            case "restricted":
                if (key != null && !key.isEmpty()) {
                    param.addAll(Arrays.asList(new String[] { npmPath, PATH + "publish.js",
                            iotaAPI.getProtocol() + "://" + iotaAPI.getHost() + ":" + iotaAPI.getPort(), payload, mode,
                            key, seed }));
                    if (start == -1) {
                        param.add(String.valueOf(start));
                    }
                    break;
                } else {
                    logger.warn("You must provide a key to use the restricted mode. Aborting");
                }
                break;
            default:
                logger.warn("This mode is not supported");
                break;
        }

        try {
            if (!param.isEmpty()) {
                logger.debug("Doing proof of work to attach data to the Tangle.... Processing in mode -- {} -- ", mode);
                process = Runtime.getRuntime().exec(param.toArray(new String[] {}));
                String result = IOUtils.toString(process.getInputStream(), "UTF-8");
                if (result != null && !result.isEmpty()) {
                    JsonObject json = (JsonObject) parser.parse(result);
                    start = json.getAsJsonPrimitive("START").getAsInt();
                    seed = json.getAsJsonPrimitive("SEED").getAsString();
                    logger.debug("Sent: {}", json);
                }
                process.waitFor();
            }
        } catch (IOException | InterruptedException e) {
            logger.debug("Exception happened: {}", e);
        }

    }

    /**
     * Retrieve a message from the Tangle, through MAM
     *
     * @param refresh the refresh interval
     * @param root    the root address on which to listen to
     * @param mode    the mode (public, private, restricted) for MAM
     * @param key     the key if restricted mode is used
     * @return the fetched message
     */
    @Override
    public String fetchFromTangle(int refresh, String root, String mode, String key) {

        String[] cmd;
        JsonParser parser = new JsonParser();
        JsonObject json = new JsonObject();

        if (key == null || key.isEmpty()) {
            cmd = refresh == 0
                    ? new String[] { npmPath, PATH + "fetchSync.js",
                            iotaAPI.getProtocol() + "://" + iotaAPI.getHost() + ":" + iotaAPI.getPort(), root, mode }
                    : new String[] { npmPath, PATH + "fetchAsync.js",
                            iotaAPI.getProtocol() + "://" + iotaAPI.getHost() + ":" + iotaAPI.getPort(), root, mode };
        } else {
            cmd = refresh == 0 ? new String[] { npmPath, PATH + "fetchSync.js",
                    iotaAPI.getProtocol() + "://" + iotaAPI.getHost() + ":" + iotaAPI.getPort(), root, mode, key }
                    : new String[] { npmPath, PATH + "fetchAsync.js",
                            iotaAPI.getProtocol() + "://" + iotaAPI.getHost() + ":" + iotaAPI.getPort(), root, mode,
                            key };
        }

        try {
            Process process = Runtime.getRuntime().exec(cmd);
            String result = IOUtils.toString(process.getInputStream(), "UTF-8");
            if (result != null && !result.isEmpty()) {
                json = (JsonObject) parser.parse(result);
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.debug("Exception happened: {}", e);
        }

        if (json.toString().equals("{}")) { // no new data fetched yet, empty json
            return oldResult;
        } else {
            oldResult = json.toString();
            return json.toString();
        }
    }

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
    @Override
    public boolean executePayment(String fromSeed, String toWallet, String forAmount, String optionalMessage,
            String optionalTag) {
        boolean success = false;
        List<Transfer> transfers = new ArrayList<>();
        String tag = optionalTag.isEmpty() ? "999999999999999999999999999" : optionalTag;
        String message = optionalMessage.isEmpty() ? "" : optionalMessage;
        Transfer t = new Transfer(toWallet, (int) (Double.parseDouble(forAmount) * 1000000),
                TrytesConverter.toTrytes(message), tag);
        transfers.add(t);
        SendTransferResponse transfer = null;
        if (iotaAPI != null) {
            try {
                transfer = iotaAPI.sendTransfer(fromSeed, 2, 9, 9, transfers, null, null, true, true);
                logger.debug("IOTA transfer was successfull: {}", transfer);
                success = true;
            } catch (ArgumentException e) {
                logger.debug("Exception occured while sending IOTA: {}", e);
            }
        }
        return success;
    }

    /**
     * Get balance of a wallet address
     *
     * @param threshold the threshold above which balance is detected
     * @param addresses the addresses to check
     * @return the balance
     */
    @Override
    public double getBalances(int threshold, List<String> addresses) {
        double balance = 0.;
        GetBalancesResponse balanceAPI = null;
        try {
            balanceAPI = this.iotaAPI.getBalances(threshold, addresses);
        } catch (ArgumentException e) {
            logger.debug("Error: invalid or empty wallet: {}", e.getMessage());
        }
        if (balanceAPI != null) {
            // The result is in IOTA, so converting it to Miota
            balance = Double.parseDouble(balanceAPI.getBalances()[0]) / 1000000;
            logger.debug("Balance detected: value is {} Miota", balance);
        }
        return balance;
    }

    /**
     * Check the validity of the Iota API node
     *
     * @return true if connexion is successful
     */
    @Override
    public boolean checkAPI() {
        return iotaAPI.getNodeInfo() != null;
    }

    /**
     * Check the validity of a seed
     *
     * @param seed the seed to validate
     * @return true if the seed is valid
     */
    public boolean checkSeed(String seed) {
        return InputValidator.isValidSeed(seed);
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    @Override
    public void setIotaAPI(String protocol, String host, int port) {
        this.iotaAPI = new IotaAPI.Builder().protocol(protocol).host(host).port(String.valueOf(port)).build();
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
