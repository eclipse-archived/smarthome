/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.sonos.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers a servlet that serves audio content.
 *
 * @author Karel Goderis - Initial contribution
 *
 */
public class AudioServlet extends HttpServlet {

    private static final long serialVersionUID = -7345087931950359183L;

    private static final Logger logger = LoggerFactory.getLogger(AudioServlet.class);

    private static final String SERVLET_NAME = "/audio";

    private static final String MESSAGE_URL_NOT_SUPPORTED = "Url not supported";

    private static final String MESSAGE_TEXT_PARAMETER_MISSING = "text parameter is required";

    private static final String MESSAGE_AUDIO_NOT_FOUND = "Audio resource not found";

    private static final String MESSAGE_AUDIO_CONTENT_WRONG = "Audio content must be valid mp3 resource";

    private final static String CONTENT_FOLDER_NAME = "audioservlet";
    private String contentFolderName;

    protected HttpService httpService;

    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;

        try {
            logger.debug("Starting up the Sonos audio servlet at " + SERVLET_NAME);

            Hashtable<String, String> props = new Hashtable<String, String>();
            httpService.registerServlet(SERVLET_NAME, this, props, createHttpContext());

            // final String cacheFolderName = MultimediaSonosActivator.getCacheFolder();
            // this.cache = new TTSCacheImpl(new File(cacheFolderName));
        } catch (NamespaceException e) {
            logger.error("Error during servlet startup", e);
        } catch (ServletException e) {
            logger.error("Error during servlet startup", e);
        }

        String userDataDir = System.getProperty(ConfigConstants.USERDATA_DIR_PROG_ARGUMENT);
        if (userDataDir == null) {
            // use current folder as default
            userDataDir = ".";
        }
        contentFolderName = userDataDir + File.separator + CONTENT_FOLDER_NAME;
        File folder = new File(contentFolderName);
        if (!folder.exists()) {
            logger.debug("Creating the content folder to serve audio from: '{}'", folder.getAbsolutePath());
            folder.mkdirs();
        }

    }

    public void unsetHttpService(HttpService httpService) {
        httpService.unregister(SERVLET_NAME);
        this.httpService = null;
    }

    /**
     * Creates a {@link HttpContext}
     *
     * @return a {@link HttpContext}
     */
    protected HttpContext createHttpContext() {
        HttpContext defaultHttpContext = httpService.createDefaultHttpContext();
        return defaultHttpContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("Processing URL {}", req.getRequestURI());
        if (req.getRequestURI().startsWith("/audio/content")) {
            processAudioContent(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, MESSAGE_URL_NOT_SUPPORTED);
        }
    }

    protected void processAudioContent(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO handle security, only host correct URLs
        String uniqueName = StringUtils.substringAfterLast(req.getRequestURI(), "/");
        if (!uniqueName.endsWith(".wav") && !uniqueName.endsWith(".mp3")) {
            logger.error("Unsupported file format");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, MESSAGE_AUDIO_CONTENT_WRONG);
        }

        File audioFile = new File(contentFolderName + File.separator + uniqueName);

        logger.debug("File to serve is {}", audioFile.getAbsolutePath());

        if (audioFile.exists()) {
            if (uniqueName.endsWith(".wav")) {
                resp.setContentType("audio/wav");
            } else if (uniqueName.endsWith(".mp3")) {
                resp.setContentType("audio/mp3");
            }
            long size = audioFile.length();
            resp.setContentLength((int) size);

            ServletOutputStream os = resp.getOutputStream();
            InputStream is = new FileInputStream(audioFile);
            IOUtils.copy(is, os);
            resp.flushBuffer();

            // audioFile.delete();

        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, MESSAGE_AUDIO_NOT_FOUND + "(" + audioFile.getName() + ")");
        }
    }
}
