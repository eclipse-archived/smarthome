/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.lsp.internal;

import java.io.File;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.ide.server.UriExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link UriExtensions} implementation.
 *
 * It takes into account the fact that although language server and client both operate on the same set of files, their
 * file system location might be different due to remote access via SMB, SSH and the like.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class MappingUriExtensions extends UriExtensions {

    private final Logger logger = LoggerFactory.getLogger(MappingUriExtensions.class);

    private final String serverLocation;
    private String clientLocation = null;

    public MappingUriExtensions(String configFolder) {
        Path configPath = Paths.get(configFolder);
        Path absoluteConfigPath = configPath.toAbsolutePath();
        java.net.URI configPathURI = absoluteConfigPath.toUri();
        serverLocation = configPathURI.toString();
        logger.debug("The language server is using '{}' as its workspace", serverLocation);
    }

    @Override
    public String toPath(URI uri) {
        return toPath(java.net.URI.create(uri.toString()));
    }

    @Override
    public String toPath(java.net.URI uri) {
        java.net.URI ret = uri;
        try {
            ret = Paths.get(uri).toUri();
        } catch (FileSystemNotFoundException e) {
            // fall-back to the argument
        }
        return ret.toString().replace(serverLocation, clientLocation);
    }

    @Override
    public URI toUri(String pathWithScheme) {
        if (clientLocation != null && pathWithScheme.startsWith(clientLocation)) {
            return map(pathWithScheme, clientLocation);
        }

        clientLocation = guessClientPath(pathWithScheme);
        if (clientLocation != null) {
            logger.debug("Identified client workspace as '{}'", clientLocation);
            return map(pathWithScheme, clientLocation);
        }

        logger.info("Path mapping could not be done for '{}', leaving it untouched", pathWithScheme);
        java.net.URI javaNetUri = java.net.URI.create(pathWithScheme);
        return URI.createURI(super.toPath(javaNetUri));
    }

    private String toURIString(String filename) {
        Path path = Paths.get(filename);
        java.net.URI pathURI = path.toUri();
        return pathURI.toString();
    }

    /**
     * Guess the client path.
     *
     * It works as follows: It starts with replacing the full clients path with the path of the config folder.
     * In the next iteration it shortens the path to be replaced by one subfolder.
     * It repeats that until the resulting filename exists.
     *
     * @param pathWithScheme the filename as coming from the client
     * @return the substring which needs to be replaced with the runtime's config folder path
     */
    protected String guessClientPath(String pathWithScheme) {
        File file = Paths.get(java.net.URI.create(pathWithScheme)).toFile().getParentFile();
        File realFile;
        while (file.getParentFile() != null) {
            String toReplace = toURIString(file.toString()) + File.separator;
            String path = pathWithScheme.replace(toReplace, serverLocation);
            realFile = new File(java.net.URI.create(path));
            if (realFile.exists()) {
                return toReplace;
            }
            file = file.getParentFile();
        }
        return null;
    }

    private URI map(String pathWithScheme, String clientLocation) {
        java.net.URI javaNetUri = java.net.URI.create(pathWithScheme.replace(clientLocation, serverLocation));
        return URI.createURI(super.toPath(javaNetUri));
    }

}
