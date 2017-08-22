/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assuming we have a configuration for {@link NetworkServerTlsProvider}. This class computes
 * the difference of this configuration set to a new configuration set, by considering changes
 * to the configuration itself as well as changes to the related keystore files.
 *
 * @author David Graeff - Initial contribution
 *
 */
@NonNullByDefault
public class ComputeConfigurationDifference {
    private final Path baseDir;
    private final MessageDigest messageDigest;
    private final Logger logger = LoggerFactory.getLogger(ComputeConfigurationDifference.class);
    /**
     * Contains a key-value mapping from context name to a hash value of that context.
     * The hash includes the related keystore file.
     */
    protected Map<String, String> contextHash;

    /**
     * Create a new configuration utility instance.
     *
     * @param bundleContext
     *
     * @param contexts The context configurations.
     *            This is used to compute file hashes of the related keystore files.
     * @param baseDir The user configuration directory. Keystore files are expected to reside here.
     * @param messageDigest A messagedigest instance
     */
    public ComputeConfigurationDifference(List<ContextConfiguration> contexts, Path baseDir,
            MessageDigest messageDigest) {
        this.baseDir = baseDir;
        this.messageDigest = messageDigest;
        contextHash = contexts.stream().collect(Collectors.toMap(cc -> cc.context, cc -> computeHash(cc, baseDir)));
    }

    protected String computeHash(ContextConfiguration c, Path baseDir) {
        StringBuffer b = new StringBuffer();
        Path keystoreFilename = c.getAbsoluteKeystoreFilename(baseDir);

        if (Files.exists(keystoreFilename)) {
            try {
                String h = DatatypeConverter
                        .printBase64Binary(messageDigest.digest(Files.readAllBytes(keystoreFilename)));
                assert h != null;
                b.append(h);
            } catch (IOException ignored) {
            }
        }

        b.append(c.hashCode());
        return b.toString();
    }

    /**
     * Computes the changed contexts, returns them and update this object
     *
     * @param newContexts The new contexts
     * @return
     */
    @SuppressWarnings("unchecked")
    public Collection<String> computeDifference(List<ContextConfiguration> newContexts, Path baseDir) {
        // Compute hashes for the new configuration context objects.
        // The map contains: contextName->hash
        Map<String, String> newContextHashs;
        newContextHashs = newContexts.stream()
                .collect(Collectors.toMap(cc -> cc.context, cc -> computeHash(cc, baseDir)));

        // Add all context names that are not in both the new and old configuration
        Set<String> changedContexts = new HashSet<>();
        Collection<String> disjunktion = CollectionUtils.disjunction(contextHash.keySet(), newContextHashs.keySet());
        changedContexts.addAll(disjunktion);

        // Compare the contexts that are in old and new configurations
        for (Map.Entry<String, String> newConfig : newContextHashs.entrySet()) {
            final @Nullable String oldHash = contextHash.get(newConfig.getKey());
            if (oldHash != null && !oldHash.equals(newConfig.getValue())) {
                changedContexts.add(newConfig.getKey());
            }
        }

        // Update this object with the new hashes
        contextHash = newContextHashs;

        // Return changes
        return changedContexts;
    }

}
