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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.dom4j.dom.DOMNodeHelper.EmptyNodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Transforms the results from FindBugs, Checkstyle and PMD into a single HTML Report with XSLT
 *
 * @see <a href=
 *      "http://www.sw-engineering-candies.com/blog-1/howtotransformtheresultsfromfindbugscheckstyleandpmdintoasinglehtmlreportwithxslt20andjava">
 *      http://www.sw-engineering-candies.com/</a>
 * @see <a href="https://github.com/MarkusSprunck/static-code-analysis-report">https://github.com/
 *      MarkusSprunck/static-
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
   * The directory where the summary report, containing links to the individual reports will be
   * generated
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

  // Setters will be used in the test
  public void setTargetDirectory(String targetDirectory) {
    this.targetDirectory = targetDirectory;
  }

  public void setFailOnError(boolean failOnError) {
    this.failOnError = failOnError;
  }

  public void setSummaryReport(String summaryReport) {
    this.summaryReport = summaryReport;
  }

  @Override
  public void execute() throws MojoFailureException {

    // Prepare userDirectory and tempDirectoryPrefix
    final String userDirectory = targetDirectory.replace('\\', '/') + '/';
    final String timeStamp = Integer.toHexString((int) System.nanoTime());
    final String tempDirectoryPrefix = userDirectory.replace('\\', '/') + timeStamp;

    System.setProperty(XML_TRANSFORM_PROPERTY_KEY, XML_TRANSFOMR_PROPERTY_VALUE);

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

    // 7. Append the individual report to the summary, if it is not empty
    if (summaryReport != null) {
      appendToSummary(htmlOutputFileName, summaryReport, secondMergeResult);
    }

    // 8. Fail the build if the option is enabled and high priority warnings are found
    if (failOnError) {
      String errorLog = checkForErrors(secondMergeResult, htmlOutputFileName);
      if (errorLog != null) {
        throw new MojoFailureException(errorLog);
      }
    }

    // 9. Delete all temporary files
    deletefile(findbugsTempFile);
    deletefile(checkstyleTempFile);
    deletefile(pmdTempFile);
    deletefile(firstMergeResult);
    deletefile(secondMergeResult);

  }

  private void run(final String xslt, final String input, final String output, final String param,
      final String value) {

    FileOutputStream outputStream = null;
    try {

      LOGGER.debug(input + "  > " + xslt + " " + param + " " + value + " >  " + output);

      // Process the Source into a Transformer Object
      final TransformerFactory transformerFactory = TransformerFactory.newInstance();
      final InputStream inputStream = Thread.currentThread().getContextClassLoader()
          .getResourceAsStream(xslt);
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

    } catch (Exception e) {
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

  void deletefile(final String pathName) {
    final File file = new File(pathName);
    if (!file.delete()) {
      LOGGER.error("Unable to delete file " + pathName);
    }
  }

  private String checkForErrors(String secondMergeResult, String reportLocation) {
    NodeList nodes = selectNodes(secondMergeResult, "/sca/file/message[@priority=1]");
    int errorNumber = nodes.getLength();

    if (errorNumber == 0) {
      return null;
    }
    StringBuilder result = new StringBuilder();
    result.append("Code Analysis Tool has found ").append(errorNumber).append(" error(s)! \n");
    result.append("Please fix the errors and rerun the build. \n");
    result.append("Errors list: \n");

    for (int i = 0; i < nodes.getLength(); i++) {
      Node currentNode = nodes.item(i);
      if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
        Element messageNode = (Element) currentNode;
        String message = messageNode.getAttribute("message");
        String tool = messageNode.getAttribute("tool");
        String line = messageNode.getAttribute("line");

        Element fileNode = ((Element) messageNode.getParentNode());
        String fileName = fileNode.getAttribute("name");

        String logTemplate = "ERROR found by %s: %s:%s %s \n";
        String log = String.format(logTemplate, tool, fileName, line, message);
        result.append(log);
      }
    }
    result.append("Detailed report can be found at: file").append(File.separator)
        .append(File.separator).append(File.separator).append(reportLocation).append("\n");
    return result.toString();

  }

  private void appendToSummary(String htmlOutputFileName, String summaryReportDirectory,
      String secondMergeResult) {

    NodeList nodes = selectNodes(secondMergeResult, "/sca/file/message");
    int messagesNumber = nodes.getLength();
    if (messagesNumber == 0) {
      LOGGER.info("Empty report will not be appended to the summary report.");
      return;
    }

    try {
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
      String row = String.format(singleItem, reportPath.toUri(),
          reportPath.getName(reportPath.getNameCount() - 4));

      reportContent = reportContent.replace("<tr></tr>", row);
      FileUtils.writeStringToFile(summaryReport, reportContent);
      LOGGER.info("Individual report appended to summary report.");
    } catch (IOException e) {
      LOGGER.warn("Cann't read or write to summary report. The summary report might be incomplete!",
          e);
    }

  }

  private NodeList selectNodes(String filePath, String xPathExpression) {
    try {
      File file = new File(filePath);
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(file);

      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xPath = xPathFactory.newXPath();
      XPathExpression expression = xPath.compile(xPathExpression);
      return (NodeList) expression.evaluate(document, XPathConstants.NODESET);
    } catch (Exception e) {
      LOGGER.warn("Cann't select" + xPathExpression + " nodes from " + filePath
          + ". Empty NodeList will be returned.", e);
      return new EmptyNodeList();
    }

  }

}
