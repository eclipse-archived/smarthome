/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.internal.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;

import org.eclipse.smarthome.io.net.http.HttpUtil;

/**
 * The {@link WemoHttpCall} is responsible for calling a WeMo device to send commands or retrieve status updates.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */

public class WemoHttpCall {

    static String contentHeader = "text/xml; charset=utf-8";

    public static String executeCall(String wemoURL, String soapHeader, String content) {

        try {

            Properties wemoHeaders = new Properties();
            wemoHeaders.setProperty("CONTENT-TYPE", contentHeader);
            wemoHeaders.put("SOAPACTION", soapHeader);

            InputStream wemoContent = new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8")));

            String wemoCallResponse = HttpUtil.executeUrl("POST", wemoURL, wemoHeaders, wemoContent, null, 2000);
            return wemoCallResponse;

        } catch (Exception e) {
            throw new RuntimeException("Could not call WeMo", e);
        }

    }

}