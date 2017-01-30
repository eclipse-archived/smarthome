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
 * Checks if the classpath file has a maven.pomderived attribute. This attribute should be used only if you have
 * problems downloading your maven dependencies.
 *
 * @author Aleksandar Kovachev
 *
 */
public class MavenPomderivedInClasspathCheck extends AbstractStaticCheck {

    public static final String POMDERIVED_EXPRESSION = "/classpath/classpathentry/attributes/attribute[@name='maven.pomderived' and @value='true']/@name";

    public static final String MESSAGE_POMDERIVED_FOUND = "The classpath file contains maven.pomderived attribute. This attribute should be used only if you have problems downloading your maven dependencies.";

    public static final String CLASSPATH_NAME = "classpath";

    public static final String EMPTY_FILE_MESSAGE = "The " + CLASSPATH_NAME + " file should not be empty.";

    public static final String INVALID_FILE_ERROR_MESSAGE = "An error has occured while parsing the  ." + CLASSPATH_NAME
            + ". Check if the " + CLASSPATH_NAME + " file is valid.";

    public MavenPomderivedInClasspathCheck() {
        setFileExtensions(CLASSPATH_NAME);
    }

    @Override
    protected void processFiltered(File file, List<String> lines) throws CheckstyleException {

        if (isEmpty(file)) {
            log(0, EMPTY_FILE_MESSAGE);
        } else {
            Document document = parseDomDocumentFromFile(file);

            XPathExpression xpathExpression = compileXPathExpression(POMDERIVED_EXPRESSION);

            NodeList nodes = null;
            try {
                nodes = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                log(0, INVALID_FILE_ERROR_MESSAGE);
            }

            if (nodes != null) {
                int lineNumber = 0;

                for (int i = 0; i < nodes.getLength(); i++) {
                    lineNumber = findLineNumber(lines, nodes.item(i).getNodeValue(), lineNumber);
                    if (lineNumber != -1) {
                        log(lineNumber, MESSAGE_POMDERIVED_FOUND);
                    } else {
                        throw new CheckstyleException("Unable to read from the " + CLASSPATH_NAME);
                    }
                }
            }
        }
    }
}
