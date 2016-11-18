/**
 * Copyright (C) 2012-2013, Markus Sprunck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - The name of its contributor may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.eclipse.smarthome.buildtools.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

/**
 * Transforms the results from Findbugs, Checkstyle and PMD into a single HTML Report with XSLT 2.0 
 * 
 * @see <a href="http://www.sw-engineering-candies.com/blog-1/howtotransformtheresultsfromfindbugscheckstyleandpmdintoasinglehtmlreportwithxslt20andjava">http://www.sw-engineering-candies.com/</a>
 * @see <a href="https://github.com/MarkusSprunck/static-code-analysis-report">https://github.com/MarkusSprunck/static-code-analysis-report</a>
 * 
 * @author Markus Sprunck - Initial Implementation
 * @author Svilen Valkanov - Some minor changes and adaptations
 */

public class ScanReportUtility {

    // XSLT files that are used to create the merge report, located in the resources folder
    private static final String CREATE_HTML_XSLT = "create_html.xslt";
    private static final String MERGE_XSLT = "merge.xslt";
    private static final String PREPARE_PMD_XSLT = "prepare_pmd.xslt";
    private static final String PREPARE_CHECKSTYLE_XSLT = "prepare_checkstyle.xslt";
    private static final String PREPARE_FINDBUGS_XSLT = "prepare_findbugs.xslt";
    
    // Input files that contain the reports of the different tools
    private static final String PMD_INPUT_FILE_NAME = "pmd.xml";
    private static final String CHECKSTYLE_INPUT_FILE_NAME = "checkstyle-result.xml";
    private static final String FINDBUGS_INPUT_FILE_NAME = "findbugsXml.xml";

    // Name of the file that contains the merged report
    public static final String RESULT_FILE_NAME = "result.html";
    
    private static final String JAVA_IO_TMPDIR_PROPERTY_KEY = "java.io.tmpdir";
    private static final String EMPTY = "";
    private static final String XML_TRANSFORM_PROPERTY_KEY = "javax.xml.transform.TransformerFactory";
    private static final String XML_TRANSFOMR_PROPERTY_VALUE = "net.sf.saxon.TransformerFactoryImpl";

    private static final Logger LOGGER = Logger.getLogger(ScanReportUtility.class);

    private static void run(final String xslt, final String input, final String output, final String param,
            final String value) {

        FileOutputStream outputStream = null;
        try {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(input + "  > " + xslt + " " + param + " " + value + " >  " + output);
            }

            // Process the Source into a Transformer Object
            System.setProperty(XML_TRANSFORM_PROPERTY_KEY, XML_TRANSFOMR_PROPERTY_VALUE);
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(xslt);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            final StreamSource source = new StreamSource(reader);
            final Transformer transformer = transformerFactory.newTransformer(source);

            // Add a parameter for the transformation
            if (!param.isEmpty()) {
                transformer.setParameter(param, value);
            }

            outputStream = new FileOutputStream(output);
            final StreamResult outputTarget = new StreamResult(outputStream);
            final StreamSource xmlSource = new StreamSource(input);

            // Transform the XML Source to a Result
            transformer.transform(xmlSource, outputTarget);

        } catch (final TransformerConfigurationException e) {
            LOGGER.error(e.getMessage());
        } catch (final TransformerException e) {
            LOGGER.error(e.getMessage());
        } catch (final FileNotFoundException e) {
            LOGGER.error(e.getMessage());
        } finally {
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (final IOException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }
    }

    static void deletefile(final String pathName) {
        final File file = new File(pathName);
        if (!file.delete()) {
            LOGGER.error("Unable to delete file " + pathName);
        }
    }

    public static void main(String[] args) {

        // Prepare userDirectory and tempDirectoryPrefix
        final String currentDirectory = args[0];
        final String userDirectory = currentDirectory.replace('\\', '/') + '/';
        final String timeStamp = Integer.toHexString((int) System.nanoTime());
        final String tempDirectory = System.getProperty(JAVA_IO_TMPDIR_PROPERTY_KEY);
        final String tempDirectoryPrefix = tempDirectory.replace('\\', '/') + timeStamp;

        // 1. Create intermediate xml-file for Findbugs
        final String inputFileFindbugs = userDirectory + FINDBUGS_INPUT_FILE_NAME;
        final String findbugsTempFile = tempDirectoryPrefix + "_PostFB.xml";
        run(PREPARE_FINDBUGS_XSLT, inputFileFindbugs, findbugsTempFile, EMPTY, EMPTY);

        // 2. Create intermediate xml-file for Checkstyle
        final String inputFileCheckstyle = userDirectory + CHECKSTYLE_INPUT_FILE_NAME;
        final String checkstyleTempFile = tempDirectoryPrefix + "_PostCS.xml";
        run(PREPARE_CHECKSTYLE_XSLT, inputFileCheckstyle, checkstyleTempFile, EMPTY, EMPTY);

        // 3. Create intermediate xml-file for PMD
        final String inputFilePMD = userDirectory + PMD_INPUT_FILE_NAME;
        final String pmdTempFile = tempDirectoryPrefix + "_PostPM.xml";
        run(PREPARE_PMD_XSLT, inputFilePMD, pmdTempFile, EMPTY, EMPTY);

        // 4. Merge first two files and create firstMergeResult file
        final String firstMergeResult = tempDirectoryPrefix + "_FirstMerge.xml";
        run(MERGE_XSLT, checkstyleTempFile, firstMergeResult, "with", findbugsTempFile);

        // 5. Merge result file with third file and create secondMergeResult
        // file
        final String secondMergeResult = tempDirectoryPrefix + "_SecondMerge.xml";
        run(MERGE_XSLT, firstMergeResult, secondMergeResult, "with", pmdTempFile);

        // 6. Create html report out of secondMergeResult
        final String htmlOutputFileName = userDirectory + RESULT_FILE_NAME;
        run(CREATE_HTML_XSLT, secondMergeResult, htmlOutputFileName, EMPTY, EMPTY);

        // Delete all temporary files
        deletefile(findbugsTempFile);
        deletefile(checkstyleTempFile);
        deletefile(pmdTempFile);
        deletefile(firstMergeResult);
        deletefile(secondMergeResult);
    }
}
