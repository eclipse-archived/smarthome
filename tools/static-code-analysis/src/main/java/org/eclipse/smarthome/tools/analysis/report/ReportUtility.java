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
package org.eclipse.smarthome.tools.analysis.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * Transforms the results from FindBugs, Checkstyle and PMD into a single HTML Report with XSLT
 *
 * @see <a href=
 *      "http://www.sw-engineering-candies.com/blog-1/howtotransformtheresultsfromfindbugscheckstyleandpmdintoasinglehtmlreportwithxslt20andjava">
 *      http://www.sw-engineering-candies.com/</a>
 * @see <a href="https://github.com/MarkusSprunck/static-code-analysis-report">https://github.com/MarkusSprunck/static-
 *      code-analysis-report</a>
 *
 * @author Markus Sprunck - Initial Implementation
 * @author Svilen Valkanov - Some minor changes and adaptations
 */

@Mojo(name = "report")
public class ReportUtility extends AbstractMojo {

    /**
     * The directory where the individual report will be generated
     */
    @Parameter(property = "report.targetDir", defaultValue = "${project.build.directory}/code-analysis")
    private String targetDirectory;

    /**
     * Describes of the build should fail if high priority error is found
     */
    @Parameter(property = "report.fail.on.error", defaultValue = "true")
    private boolean failOnError;

    /**
     * The directory where the summary report, containing links to the individual reports will be generated
     */
    @Parameter(property = "report.summary.targetDir", defaultValue = "${session.executionRootDirectory}/target")
    private String summaryReport;

    private static final String REPORT_SUBDIR = "report";

    // XSLT files that are used to create the merged report, located in the resources folder
    private static final String CREATE_HTML_XSLT = REPORT_SUBDIR + "/create_html.xslt";
    private static final String MERGE_XSLT = REPORT_SUBDIR + "/merge.xslt";
    private static final String PREPARE_PMD_XSLT = REPORT_SUBDIR + "/prepare_pmd.xslt";
    private static final String PREPARE_CHECKSTYLE_XSLT = REPORT_SUBDIR + "/prepare_checkstyle.xslt";
    private static final String PREPARE_FINDBUGS_XSLT = REPORT_SUBDIR + "/prepare_findbugs.xslt";

    private static final String SUMMARY_TEMPLATE_FILE_NAME = "summary.html";

    // Input files that contain the reports of the different tools
    private static final String PMD_INPUT_FILE_NAME = "pmd.xml";
    private static final String CHECKSTYLE_INPUT_FILE_NAME = "checkstyle-result.xml";
    private static final String FINDBUGS_INPUT_FILE_NAME = "findbugsXml.xml";

    // Name of the file that contains the merged report
    public static final String RESULT_FILE_NAME = "report.html";
    public static final String SUMMARY_FILE_NAME = "summary_report.html";

    private static final String EMPTY = "";
    private static final String XML_TRANSFORM_PROPERTY_KEY = "javax.xml.transform.TransformerFactory";
    private static final String XML_TRANSFOMR_PROPERTY_VALUE = "net.sf.saxon.TransformerFactoryImpl";

    private static final Logger LOGGER = Logger.getLogger(ReportUtility.class);

    private static void run(final String xslt, final String input, final String output, final String param,
            final String value) {

        FileOutputStream outputStream = null;
        try {

            LOGGER.debug(input + "  > " + xslt + " " + param + " " + value + " >  " + output);

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

    /**
     * Merges the xml reports from PMD, Checkstyle and FindBugs into a single .html report
     *
     * @param relativePathToReport - path to the merged report for a single project
     * @param failOnError - if the tool should throw Exception on errors
     * @param summaryReportDirectory - path to the summary report for the whole build
     * @throws MojoFailureException - thrown if one ore more violation with high priority are found
     */
    public static void mergeReports(String relativePathToReport, boolean failOnError, String summaryReportDirectory)
            throws MojoFailureException {

        // Prepare userDirectory and tempDirectoryPrefix
        final String userDirectory = relativePathToReport.replace('\\', '/') + '/';
        final String timeStamp = Integer.toHexString((int) System.nanoTime());
        final String tempDirectoryPrefix = userDirectory.replace('\\', '/') + timeStamp;

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

        String errorLog = null;
        if (failOnError) {
            errorLog = checkForErrors(secondMergeResult, htmlOutputFileName);
        }

        // Delete all temporary files
        deletefile(findbugsTempFile);
        deletefile(checkstyleTempFile);
        deletefile(pmdTempFile);
        deletefile(firstMergeResult);
        deletefile(secondMergeResult);

        try {
            if (summaryReportDirectory != null) {
                appendToSummary(htmlOutputFileName, summaryReportDirectory);
            }
        } catch (IOException e) {
            LOGGER.warn("Can not read or write to summary report. The summary report might be incomplete!", e);
        }

        if (failOnError && errorLog != null) {
            throw new MojoFailureException(errorLog);
        }

    }

    private static String checkForErrors(String secondMergeResult, String reportLocation) {
        try {
            File file = new File(secondMergeResult);
            SAXReader reader = new SAXReader();
            Document document = reader.read(file);

            @SuppressWarnings("unchecked")
            List<Node> nodes = document.selectNodes("/sca/file/message[@priority=1]");

            if (nodes.isEmpty()) {
                return null;
            }
            StringBuilder result = new StringBuilder();
            result.append("Code Analysis Tool has found ").append(nodes.size()).append(" error(s)! \n");
            result.append("Please fix the errors and rerun the build. \n");
            result.append("Errors list: \n");
            for (Node node : nodes) {
                Node parent = node.getParent();
                String fileName = parent.valueOf("@name");

                String message = node.valueOf("@message");
                String tool = node.valueOf("@tool");
                String line = node.valueOf("@line");

                String logTemplate = "ERROR found by %s: %s:%s %s \n";
                String log = String.format(logTemplate, tool, fileName, line, message);
                result.append(log);
            }
            result.append("Detailed report can be found at: file").append(File.separator).append(File.separator)
                    .append(File.separator).append(reportLocation).append("\n");
            return result.toString();

        } catch (DocumentException e) {
            LOGGER.error(
                    "Error while checking the merged report for high priority violations! The report might be inaccurate!",
                    e);
        }
        return null;

    }

    private static void appendToSummary(String htmlOutputFileName, String summaryReportDirectory) throws IOException {
        File summaryReport = new File(summaryReportDirectory + File.separator + SUMMARY_FILE_NAME);

        if (!summaryReport.exists()) {

            InputStream inputStream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(REPORT_SUBDIR + "/" + SUMMARY_TEMPLATE_FILE_NAME);

            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, Charset.defaultCharset());
            String htmlString = writer.toString();

            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            htmlString = htmlString.replace("$time", now);

            FileUtils.writeStringToFile(summaryReport, htmlString);
        }
        String reportContent = FileUtils.readFileToString(summaryReport);

        final String singleItem = "<tr class=alternate><td><a href=\"%s\">%s</a></td></tr><tr></tr>";
        Path reportPath = FileSystems.getDefault().getPath(htmlOutputFileName);
        String row = String.format(singleItem, reportPath.toUri(), reportPath.getName(reportPath.getNameCount() - 4));

        reportContent = reportContent.replace("<tr></tr>", row);
        FileUtils.writeStringToFile(summaryReport, reportContent);
        LOGGER.info("Individual report appended to summary report.");
    }

    @Override
    public void execute() throws MojoFailureException {
        ReportUtility.mergeReports(targetDirectory, failOnError, summaryReport);
    }
}
