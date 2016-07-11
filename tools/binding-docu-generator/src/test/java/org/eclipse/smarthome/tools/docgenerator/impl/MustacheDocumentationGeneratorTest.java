package org.eclipse.smarthome.tools.docgenerator.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.eclipse.smarthome.tools.docgenerator.DocumentationGenerator;
import org.eclipse.smarthome.tools.docgenerator.EshConfigurationParser;
import org.eclipse.smarthome.tools.docgenerator.models.ConfigurationParseResult;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MustacheDocumentationGeneratorTest {

    private static final String templatesTarget = "target/testTemplates/";

    private Log log;

    @Before
    public void setUp() throws Exception {
        log = new SystemStreamLog();
    }

    @Test
    public void testGenerateDocumentation() throws Exception {
        Path eshPath = Paths.get("src/test/resources/testESH-INF/");
        Path partialsDir = Paths.get("src/test/resources/templates/");
        Path readmeTemplate = Paths.get("src/test/resources/templates/README.md.mustache");

        DocumentationGenerator generator = new MustacheDocumentationGenerator(log);
        EshConfigurationParser parser = new DefaultEshConfigurationParser(log);
        ConfigurationParseResult result = parser.parseEshConfiguration(eshPath);

        Path readme = Files.createTempFile("esh", ".md");

        generator.generateDocumentation(result, readme, partialsDir, readmeTemplate);

        assertTrue(Files.exists(readme));
        assertTrue(Files.isRegularFile(readme));
        String content = new String(Files.readAllBytes(readme), org.apache.commons.io.Charsets.UTF_8);

        assertThat(StringUtils.countMatches(content, "# Binding #1 Binding - Author #1"), is(1));
        assertThat(StringUtils.countMatches(content, "<a name=\"channel-id-currentRoomTemperature\"></a>currentRoomTemperature | Number |  Yes    |    | The current measured room temperature."), is(1));
        assertThat(StringUtils.countMatches(content, "|pollingInterval | INTEGER | required=false, readOnly=false, max=300, min=15 |  | &#10;                    The refresh interval for all values.&#10;                 |"), is(1));
        assertThat(StringUtils.countMatches(content, "|<a name=\"bridge-id-bridge\"></a>bridge |  |  | The bridge number one."), is(1));
        assertThat(StringUtils.countMatches(content, "|thermostatId | TEXT | required=false, readOnly=false |  | ID of the Thermostat. |"), is(1));
        assertThat(StringUtils.countMatches(content, "<a href=\"#channel-id-roomSetpoint\">roomSetpoint</a>,    <a href=\"#channel-id-heatingMode\">heatingMode</a>,    <a href=\"#channel-id-currentRoomTemperature\">currentRoomTemperature</a>"), is(1));
    }
}