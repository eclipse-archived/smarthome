/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab2.lwm2m;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public class Main {
    private static final String DEFAULT_OMAP_URL = "http://www.openmobilealliance.org/wp/DDF.xml";
    private static final String[] ADDITIONAL_DOWNLOAD_RESOURCES = {
            "http://www.openmobilealliance.org/wp/OMNA/LwM2M/Common.xml" };

    /**
     * Check readme.md file in the root directory. This application downloads OMA LWM2M Registry object files,
     * transforms those files to Openhab2 Things and Channels and stores those in an out directory. You need a
     * res directory with schema/thing-description-1.0.0.xsd, schema/LWM2M.xsd, transform/transform.xsl.
     *
     * @param args
     * @throws MalformedURLException
     * @throws InterruptedException
     * @throws SaxonApiException
     * @throws SAXException
     */
    public static void main(String[] args)
            throws MalformedURLException, InterruptedException, SaxonApiException, SAXException {
        System.out.println("Checking directories, schema files and load transformation file");

        Options options = new Options();

        Option input = new Option("r", "res", true,
                "resource file path, with schema and transform subdirectory (default is 'res')");
        input.setRequired(false);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output file path (default is 'out')");
        output.setRequired(false);
        options.addOption(output);

        Option updateOption = new Option("u", "update", false, "update OMA registry files (default is false)");
        updateOption.setRequired(false);
        options.addOption(updateOption);

        Option disableValidationOption = new Option("dv", "disable-validation", false,
                "Disable the validation of downloaded files");
        disableValidationOption.setRequired(false);
        options.addOption(disableValidationOption);

        Option updateURLOption = new Option("url", "update-url", true,
                "OMA registry url (default is " + DEFAULT_OMAP_URL + ")");
        updateURLOption.setRequired(false);
        options.addOption(updateURLOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }

        File basePath = Paths.get(val(cmd, "res", "res")).toAbsolutePath().toFile();
        File openhabSchemaFile = new File(basePath, "schema/thing-description-1.0.0.xsd");
        File lwm2mSchemaFile = new File(basePath, "schema/LWM2M.xsd");
        File transformFile = new File(basePath, "transform/transform.xsl");
        File transformPostFile = new File(basePath, "transform/post_transform.xsl");
        File inputPath = new File(basePath, "lwm2m_object_registry");
        File destPath = Paths.get(val(cmd, "output", "out")).toAbsolutePath().toFile();

        if (!basePath.exists() || !inputPath.exists() || !transformFile.exists() || !transformPostFile.exists()
                || !openhabSchemaFile.exists() || !lwm2mSchemaFile.exists()) {
            System.err.println(
                    "Res directory or subdirectories does not exist in your working directory: " + basePath.toString());
            System.exit(-1);
        }

        if (!destPath.exists()) {
            destPath.mkdirs();
        }

        Processor processor = new Processor(false);
        XsltExecutable templateTransform = processor.newXsltCompiler().compile(new StreamSource(transformFile));
        Processor processorPost = new Processor(false);
        XsltExecutable templateTransformPost = processorPost.newXsltCompiler()
                .compile(new StreamSource(transformPostFile));

        if (templateTransform == null || templateTransformPost == null) {
            System.err.println("Failed to load transform.xsl");
            System.exit(-1);
            return;
        }

        // Setup input validator
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema lwm2mSchema = schemaFactory.newSchema(lwm2mSchemaFile);

        if (cmd.hasOption("update")) {
            System.out.println("Download OMA LWM2M Registry data");
            String updateURL = val(cmd, "updateurl", DEFAULT_OMAP_URL);
            if (!updateFiles(inputPath, updateURL)) {
                System.err.println("Download failed");
                return;
            }
        } else {
            System.out.println("Use cached OMA LWM2M Registry data");
        }

        transformInputfiles(inputPath.listFiles(xmlfilenameFilter), destPath.toURI().toURL().toString(), lwm2mSchema,
                processor, templateTransform, cmd.hasOption("disable-validation"));

        transformOutputfiles(destPath, processorPost, templateTransformPost);

        validateOutput(openhabSchemaFile, destPath);
        System.out.println("Done");
    }

    private static String val(CommandLine cmd, String optionName, String defaultValue) {
        if (cmd.hasOption("input")) {
            try {
                return ((String) cmd.getParsedOptionValue(optionName)).toString();
            } catch (ParseException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    static FilenameFilter xmlfilenameFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".xml");
        }
    };

    private static void transformOutputfiles(File destPath, Processor processor, XsltExecutable template)
            throws MalformedURLException, InterruptedException {
        ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        File[] files = destPath.listFiles(xmlfilenameFilter);
        List<Callable<Object>> todo = new ArrayList<Callable<Object>>(files.length);

        for (File xmlFile : files) {
            todo.add(Executors.callable(() -> {
                System.out.println("Post Transform " + xmlFile.getName().toString());

                try {
                    StringBuilder xmlFileContent = new StringBuilder();
                    String inputLine;
                    BufferedReader in = new BufferedReader(new FileReader(xmlFile));
                    while ((inputLine = in.readLine()) != null) {
                        xmlFileContent.append(inputLine);
                    }
                    in.close();
                    xmlFile.delete();

                    // Setup transformer
                    Serializer serializer = processor.newSerializer();
                    serializer.setOutputFile(xmlFile);

                    XsltTransformer transformer = template.load();

                    transformer.setDestination(serializer);
                    transformer.setInitialContextNode(processor.newDocumentBuilder()
                            .build(new StreamSource(new StringReader(xmlFileContent.toString()))));
                    transformer.transform();
                } catch (SaxonApiException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        }

        exec.invokeAll(todo);
        exec.shutdown();
    }

    private static void transformInputfiles(File[] files, String destPath, Schema lwm2mSchema, Processor processor,
            XsltExecutable template, boolean disableValidation) throws InterruptedException {

        ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Callable<Object>> todo = new ArrayList<Callable<Object>>(files.length);

        for (File xmlFile : files) {
            todo.add(Executors.callable(() -> {
                System.out.println("Transform " + xmlFile.getName().toString());

                StreamSource streamSource = new StreamSource(xmlFile);

                if (!disableValidation) {
                    Validator validator = lwm2mSchema.newValidator();

                    try {
                        validator.validate(streamSource);
                    } catch (SAXException e) {
                        System.err.println("\tInput of " + xmlFile.getName().toString() + " NOT valid");
                        System.err.println("\tReason: " + e.getLocalizedMessage());
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }

                try {
                    // Setup transformer
                    Serializer serializer = processor.newSerializer();
                    serializer.setOutputWriter(new StringWriter());

                    XsltTransformer transformer = template.load();

                    transformer.setErrorListener(new ErrorListener() {
                        @Override
                        public void warning(TransformerException exception) throws TransformerException {
                            throw exception;
                        }

                        @Override
                        public void fatalError(TransformerException exception) throws TransformerException {
                            throw exception;
                        }

                        @Override
                        public void error(TransformerException exception) throws TransformerException {
                            throw exception;
                        }
                    });
                    transformer.setDestination(serializer);
                    transformer.setBaseOutputURI(destPath);
                    transformer.setInitialContextNode(processor.newDocumentBuilder().build(streamSource));
                    transformer.setSource(streamSource);
                    transformer.transform();
                } catch (SaxonApiException e) {
                    System.err.println("Error in file " + xmlFile.getName().toString());
                    System.err.println(e.getMessage());
                }
            }));
        }

        exec.invokeAll(todo);
        exec.shutdown();
    }

    /**
     * Download web page, extract xml file links, download them in parallel, store them in the
     * given directory.
     *
     * @param destPath The dest dir to store files.
     * @parem registryURLs Registry URLs to download the single object files from.
     *        Might be multiple urls separated by ";".
     */
    private static boolean updateFiles(File destPath, String registryURLs) {
        Set<String> links = new TreeSet<String>();

        String[] urls = registryURLs.split(";");

        for (String url : urls) {
            String data;
            try {
                data = downloadFile(url);
            } catch (IOException e1) {
                e1.printStackTrace();
                return false;
            }

            // Extracting the links from an XML document
            if (url.endsWith(".xml")) {
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(new ByteArrayInputStream(data.getBytes()));
                    XPathFactory xPathfactory = XPathFactory.newInstance();
                    XPath xpath = xPathfactory.newXPath();
                    XPathExpression expr = xpath.compile("/DDFList/Item/DDF");
                    NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                    for (int i = 0; i < nl.getLength(); ++i) {
                        links.add(nl.item(i).getTextContent());
                    }
                } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e1) {
                    e1.printStackTrace();
                    return false;
                }
            } else {
                // The following code is for extracting the xml links from a static html page
                Pattern linkPattern = Pattern.compile("<a[^>]+href=[\"']?([^\"'>]*\\.xml)[\"']?[^>]*>(.+?)</a>",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher pageMatcher = linkPattern.matcher(data);
                while (pageMatcher.find()) {
                    links.add(pageMatcher.group(1));
                }
            }

            if (links.size() == 0) {
                System.err.println("No XML files found on page: " + data);
                return false;
            }
        }

        for (String url : ADDITIONAL_DOWNLOAD_RESOURCES) {
            links.add(url);
        }

        ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Callable<Object>> todo = new ArrayList<Callable<Object>>(links.size());

        for (String urlName : links) {
            todo.add(Executors.callable(new Runnable() {
                @Override
                public void run() {
                    String fileName = new File(urlName).getName();
                    String xmlFile;
                    try {
                        xmlFile = downloadFile(urlName);
                    } catch (IOException e) {
                        System.err.println("Failed to download " + urlName);
                        return;
                    }
                    if (xmlFile.length() > 0) {
                        System.out.println("Downloaded " + fileName);
                        File destFile = new File(destPath, fileName);
                        try (PrintWriter out = new PrintWriter(destFile)) {
                            out.println(xmlFile);
                        } catch (FileNotFoundException e) {
                            System.err.println(
                                    "Failed to store " + destFile.getAbsolutePath() + " " + e.getLocalizedMessage());
                        }

                    } else {
                        System.err.println("Failed to download " + urlName);
                    }
                }
            }));
        }
        try {
            exec.invokeAll(todo);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        exec.shutdown();
        return true;
    }

    private static String downloadFile(String urlName) throws IOException {
        URL url = new URL(urlName);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(2500);
        connection.setRequestProperty("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        return response.toString();
    }

    /**
     * Validate output files in parallel
     *
     * @param openhabSchemaFile
     * @param destPath
     * @param filenameFilter
     * @throws InterruptedException
     */
    private static void validateOutput(File openhabSchemaFile, File destPath) throws InterruptedException {
        // Validate
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema;
        try {
            schema = schemaFactory.newSchema(openhabSchemaFile);
        } catch (SAXException e) {
            e.printStackTrace();
            return;
        }

        File[] files = destPath.listFiles(xmlfilenameFilter);

        ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Callable<Object>> todo = new ArrayList<Callable<Object>>(files.length);

        for (File xmlFile : files) {
            todo.add(Executors.callable(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Validate " + xmlFile.getName().toString());
                    Source xmlSource = new StreamSource(xmlFile);
                    try {
                        Validator validator = schema.newValidator();
                        validator.validate(xmlSource);
                    } catch (SAXException e) {
                        System.out.println(xmlFile.getName().toString() + " NOT valid");
                        System.out.println("Reason: " + e.getLocalizedMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }));
        }

        exec.invokeAll(todo);
        exec.shutdown();
    }

}
