/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.tools.analysis.checkstyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * Checks if a manifest file exports internal packages.
 *
 * @author Svilen Valkanov
 *
 */
public class ExportInternalPackageCheck extends AbstractFileSetCheck {

    public final static String MANIFEST_EXTENSTION = "MF";
    public final static String EXPORT_PACKAGE_HEADER = "Export-Package";

    public ExportInternalPackageCheck() {
        setFileExtensions(MANIFEST_EXTENSTION);
    }

    @Override
    protected void processFiltered(File file, List<String> lines) throws CheckstyleException {
        try {
            Manifest manifest = new Manifest(new FileInputStream(file));
            Attributes attributes = manifest.getMainAttributes();
            String value = attributes.getValue(EXPORT_PACKAGE_HEADER);
            if (value == null) {
                return;
            }
            
            String[] packages = value.split(",");
         
            for (String packageName : packages) {
                if (packageName.contains(".internal")) {
                    log(findLineNumber(lines, packageName), "Remove internal package export " + packageName, new Integer(0));
                }
            }

        } catch (IOException e) {

        }

    }

    private int findLineNumber(List<String> lines, String text) {
        int number = 0;
        for (String line : lines) {
            number++;
            if (line.contains(text)) {
                return number;
            }
        }
        return number;
    }

}
