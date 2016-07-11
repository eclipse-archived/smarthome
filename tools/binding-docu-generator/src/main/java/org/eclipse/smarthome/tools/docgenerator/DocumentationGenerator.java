package org.eclipse.smarthome.tools.docgenerator;

import org.eclipse.smarthome.tools.docgenerator.models.ConfigurationParseResult;

import java.nio.file.Path;

/**
 * Generates a binding documentation from a template and an ESH configuration.
 */
public interface DocumentationGenerator {
    /**
     * Generate documentation using the given ESH configuration.
     * <p>
     * Uses the file at readmeTemplatePath as a template and writes the generated
     * documentation to the file at readmePath. Also, the template and its partials
     * get copied to the workingDirectory directory.
     *
     * @param eshConfiguration the parsed ESH configuration
     * @param outputFile       the path where the generated README gets written to
     * @param partialsDir      the path to the partials
     * @param readmeTemplate   the path to the readme template
     */
    void generateDocumentation(ConfigurationParseResult eshConfiguration, Path outputFile, Path partialsDir, Path readmeTemplate);
}
