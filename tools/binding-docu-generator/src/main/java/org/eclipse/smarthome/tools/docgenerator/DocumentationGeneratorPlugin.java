package org.eclipse.smarthome.tools.docgenerator;

import org.apache.log4j.BasicConfigurator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.smarthome.tools.docgenerator.impl.DefaultEshConfigurationParser;
import org.eclipse.smarthome.tools.docgenerator.impl.MustacheDocumentationGenerator;
import org.eclipse.smarthome.tools.docgenerator.models.ConfigurationParseResult;

import java.nio.file.Paths;

/**
 * Goal which generates the documentation for a binding from a template and the ESH XMLs.
 */
@Mojo(name = "generate-docu", defaultPhase = LifecyclePhase.PACKAGE)
public class DocumentationGeneratorPlugin extends AbstractMojo {

    /**
     * The directory in which your binding xml files are.
     */
    @Parameter(defaultValue = "${basedir}/ESH-INF")
    private String eshDir;

    /**
     * Directory which contains the partials.
     */
    @Parameter(defaultValue = "${basedir}/templates")
    private String partialsDir;

    /**
     * Name of your readme template file.
     */
    @Parameter(defaultValue = "${basedir}/README.md.mustache")
    private String template;

    /**
     * The name of the generated docu file.
     */
    @Parameter(defaultValue = "${basedir}/README.md")
    private String outputFile;

    /**
     * Execute the mojo.
     *
     * @throws MojoExecutionException
     */
    @Override
    public void execute() throws MojoExecutionException {
        try {
            // Configure loggers
            BasicConfigurator.configure();

            EshConfigurationParser configurationParser = new DefaultEshConfigurationParser(getLog());
            DocumentationGenerator generator = new MustacheDocumentationGenerator(getLog());

            ConfigurationParseResult eshConfiguration = configurationParser.parseEshConfiguration(Paths.get(eshDir));
            generator.generateDocumentation(eshConfiguration, Paths.get(outputFile), Paths.get(partialsDir), Paths.get(template));
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to generate documentation.", e);
        }
    }
}
