# Eclipse SmartHome Static Code Analysis Tool

ESH Static Code Analysis Tools is a Maven plugin that executes the Maven plugins for FindBugs, Checkstyle and PMD and generates a merged .html report.

This project contains:
 - properties files for the PMD, Checkstyle and FindBugs Maven plugins configuration in the `src/main/resources/configuration` folder;
 - rule sets for the plugins in the `src/main/resources/rulesets` folder;
 - custom rules for PMD, CheckStyle and FindBugs and unit tests for the rules;
 - tool that merges the reports from the individual plugins in a summary report.

## Maven plugin goals and parameters

**static-code-analysis:pmd**

Description: 
    Executes the `maven-pmd-plugin` goal `pmd` with a ruleset file and configuration properties

Parameters:

| Name | Type| Description |
| ------ | ------| -------- |
| **ruleset** | String | The type of the ruleset that will be used (default value is **bundle**)|
| **maven.pmd.version** | String | The version of the maven-pmd-plugin that will be used (Default value is **3.7**)|

**static-code-analysis:checkstyle**

Description: 
    Executes the `maven-checkstyle-plugin` goal `checkstyle` with a ruleset file and configuration properties

Parameters:

| Name | Type| Description |
| ------ | ------| -------- |
| **ruleset** | String | The type of the ruleset that will be used (Default value is **bundle**)|
| **maven.checkstyle.version** | String | The version of the maven-checkstyle-plugin that will be used (default value is **2.17**)|

**static-code-analysis:findbugs**

Description: 
    Executes the `findbugs-maven-plugin` goal `findbugs` with a  ruleset file and configuration properties

Parameters:

| Name | Type| Description |
| ------ | ------| -------- |
| **ruleset** | String | The type of the ruleset that will be used (default value is **bundle**)|
| **findbugs.maven.version** | String | The version of the findbugs-maven-plugin that will be used (default value is **3.0.1**)|

**static-code-analysis:report**

Description: 
    Transforms the results from FindBugs, Checkstyle and PMD into a single HTML Report with XSLT

Parameters:

| Name | Type| Description |
| ------ | ------| -------- |
| **report.targetDir** | String | The directory where the individual report will be generated (default value is **${project.build.directory}/code-analysis**) |
| **report.summary.targetDir** | String | The directory where the summary report, containing links to the individual reports will be generated (Default value is **${session.executionRootDirectory}/target**)|
| **report.fail.on.error** | Boolean | Describes of the build should fail if high priority error is found (Default value is **true**)|


## Usage

Execute `mvn clean install -P check` from the root of the Eclipse SmartHome project.

Reports are generated for each module individually and can be found in the `target/code-analysis` directory. The merged report can be found in the root target directory.

The build will fail if a problem with high priority is found by some of the Maven plugins for PMD, Checkstyle and FindBugs. Each of the plugins has its own way to prioritize the detected problems:

- for PMD - the build will fail when a rule with Priority "1" is found;
- for Checkstyle - a rule with severity="Error";
- for Findbugs - any Matcher with Rank between 1 and 4.


## Customization 

Different sets of checks can be executed on different types of projects.

At the moment the tool executes different checks on OSGi bundles and ESH Bindings.

If you want to add a custom set of rules for UIs for example you will have to follow these steps:

- in the `src/main/resources/rulesets` add a new rule set for PMD, Checkstyle and FindBugs that includes the rules that have to be executed and follow the naming convention used so far;
- use the **ruleset** configuration property in the Maven plugin, where the ruleset is the name of the newly created ruleset.

### Individual plugin customization

Each of the Maven plugins that are used (for FindBugs, Checkstyle and PMD) are configured by setting a user properties that are located in the `src/main/resources/configuration` directory.

You can refer to the following links for more configuration options for the specific Maven plugins:

- https://maven.apache.org/plugins/maven-pmd-plugin/check-mojo.html;
- https://maven.apache.org/plugins-archives/maven-checkstyle-plugin-2.16/checkstyle-mojo.html;
- http://gleclaire.github.io/findbugs-maven-plugin/check-mojo.html.


## Reuse Checks

PMD, Checkstyle and FindBugs come with a set of custom rules that can be used directly in a rule set.

Helpful resources with lists of the available checks and information how to use them:

- for PMD - https://pmd.github.io/pmd-5.4.0/pmd-java/rules/index.html;
- for Checkstyle - http://checkstyle.sourceforge.net/checks.html;
- for FindBugs - Keep in mind that the process for adding a check in FindBugs contains two steps: 
   - First you should open the link with [BugDescriptors](http://findbugs.sourceforge.net/bugDescriptions.html), choose the bug that you want to detect and create a Match in `src/main/resources/rulesets/findbugs/YOUR_RULESET`;
   - Next you should find the Detector that finds the Bug that you have selected above (you can use [this list](https://github.com/findbugsproject/findbugs/blob/d1e60f8dbeda0a454f2d497ef8dcb878fa8e3852/findbugs/etc/findbugs.xml)) and add the Detector in the `src/main/resources/configuration/findbugs.properties` under the property `visitors`.

## Write Custom Checks

All of the used static code analysis tools have Java API for writing custom checks.

Checkstyle API is easy to use and to implement checks for different file extensions and languages. Examples are included in the project.

PMD extends this by giving the possibility to define a rule with the XPath syntax. PMD offers even more - a Rule Designer that speeds up the process of developing a new rule! See http://nullpointer.debashish.com/pmd-xpath-custom-rules.

PMD rules with XPatch expressions are defined directly in the rule set (`src/main/resources/rulesets/pmd/xpath` folder.

Helpful links when writing a custom check for the first time may be:

- for PMD - http://pmd.sourceforge.net/pmd-4.3.0/howtowritearule.html;
- for Checkstyle - http://checkstyle.sourceforge.net/writingchecks.html;
- for FindBugs - https://www.ibm.com/developerworks/library/j-findbug2/.

## Add Tests For The New Checks

You can easily test your custom rules for PMD and Checkstyle.

In order to add a new test for PMD you have to do two things:
- Create a test class in the `src\test\java` folder that extends `SimpleAggregatorTst` and overrides the `setUp()` method;
- Add a .xml file in the `src\test\resources` folder that contains the code to be tested.

Adding a test for Checkstyle is even easier - extend the `BaseCheckTestSupport`.

For more information: https://pmd.github.io/pmd-5.4.1/customizing/rule-guidelines.html. 

## Known Problems 

- Flooded console output when running Checkstyle in debug mode in Maven  (- X ) - https://github.com/checkstyle/checkstyle/issues/3184;

## 3rd Party

- The example checks provided in the `static-code-analysis-config` (`MethodLimitCheck`, `CustomClassNameLengthDetector`, `WhileLoopsMustUseBracesRule`) are based on tutorials how to use the API of Checkstyle, FindBugs and PMD. For more info, see javadoc;
- The tool that merges the individual reports is based completely on source files from the https://github.com/MarkusSprunck/static-code-analysis-report that are distributed under a custom license. More information can be found in the LICENSE.txt file.
