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

import java.util.List;

/**
 * The {@link IotaUtils} defines utils methods to work with IOTA transactions.
 *
 * @author Theo Giovanna - Initial Contribution
 */
public interface IotaUtils {

    String fetchFromTangle(int refresh, String root, String mode, String key);

    boolean executePayment(String fromSeed, String toWallet, String forAmount, String optionalMessage,
            String optionalTag);

    double getBalances(int threshold, List<String> addresses);

    boolean checkAPI();

    void setIotaAPI(String protocol, String host, int port);
}
