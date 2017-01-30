/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.tools.analysis.checkstyle;

import java.io.File;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.smarthome.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * Checks if a pom file overrides a configuration inherited by the parent pom.
 *
 * @author Aleksandar Kovachev
 *
 */
public class OverridingParentPomConfigurationCheck extends AbstractStaticCheck {

    public static final String POM_CONFIGURATION_EXPRESSION = "/project//*[@combine.self='override']/@combine.self";

    public static final String MESSAGE_OVERRIDING_POM_CONFIGURATION_FOUND = "Avoid overriding a configuration inherited by the parent pom.";

    public static final String EXTENSION = "xml";

    public static final String FILE_NAME = "pom" + "." + EXTENSION;

    public static final String EMPTY_FILE_MESSAGE = "The " + FILE_NAME + " file should not be empty.";

    public static final String MESSAGE_DOCUMENT_PARSING = "An error has occured while parsing the " + FILE_NAME
            + ". Check if the " + FILE_NAME + " file is valid.";

    public OverridingParentPomConfigurationCheck() {
        setFileExtensions(EXTENSION);
    }

    @Override
    protected void processFiltered(File file, List<String> lines) throws CheckstyleException {

        if (file.getName().equals(FILE_NAME)) {
            if (isEmpty(file)) {
                log(0, EMPTY_FILE_MESSAGE);
            } else {
                Document document = parseDomDocumentFromFile(file);

                XPathExpression xpathExpression = compileXPathExpression(POM_CONFIGURATION_EXPRESSION);

                NodeList nodes = null;
                try {
                    nodes = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
                } catch (XPathExpressionException e) {
                    log(0, MESSAGE_DOCUMENT_PARSING);
                }

                if (nodes != null) {
                    int lineNumber = 0;

                    for (int i = 0; i < nodes.getLength(); i++) {
                        lineNumber = findLineNumber(lines, nodes.item(i).getNodeValue(), lineNumber);
                        if (lineNumber != -1) {
                            log(lineNumber, MESSAGE_OVERRIDING_POM_CONFIGURATION_FOUND);
                        } else {
                            throw new CheckstyleException("Unable to read from the " + FILE_NAME);
                        }
                    }
                }
            }
        }
    }
}
