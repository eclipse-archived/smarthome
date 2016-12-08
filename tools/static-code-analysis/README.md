# Eclipse SmartHome Static Code Analysis Report Tool

This module is used to merge the individual reports from PMD, Checkstyle and FindBugs into a single .html report.
  
## 3rd Party

- Static Code Analysis Report Tool is based completely on source files from the https://github.com/MarkusSprunck/static-code-analysis-report that are distributed under a custom license. More information can be found in the `static-code-analysis-config` LICENSE.txt file;

# Eclipse SmartHome Static Code Analysis Config Tool

This project contains rule sets, metadata, filters and custom rules for PMD, Checkstyle and Findbugs.

## Usage

Execute `mvn clean install -P check` from the root of the Eclipse SmartHome project.

This profile (`check`) applies some general configuration to the Maven plugins for PMD, Checkstyle and FindBugs.

Reports are generated for each module individually and can be found in the `target/code-analysis` directory. The merged report can be found in file named `result.html`. The result report is aggregated by Static Code Analysis Report Tool.

## Failing the build

The build will fail if a problem with highest priority is found by some of the Maven plugins for PMD, Checkstyle and FindBugs. Each of the plugins has its own way to prioritize the detected problems:

- for PMD - the highest priority of a rule is "1";
- for Checkstyle - a rule with severity="Error";
- for Findbugs - a Matcher with priority "1". (This is currently now working as expected - See [Known Problems](#known-problems) below)


# Configuration

The configuration of the Maven plugins for PMD, Checkstyle and Findbugs is made in the `smarthome/pom.xml` file.

You can change the location of the reports by overriding the `report.directory` Maven property. Default value is `${project.build.directory}/code-analysis`.

The build can be configured to fail, when an error is found with the `fail.on.error` property. Default value is `true`;

You can refer to the following links for more configuration options for the specific Maven plugins:

- https://maven.apache.org/plugins/maven-pmd-plugin/check-mojo.html;
- https://maven.apache.org/plugins-archives/maven-checkstyle-plugin-2.16/checkstyle-mojo.html;
- http://gleclaire.github.io/findbugs-maven-plugin/check-mojo.html.

The rulesets for all the plugins are located in the `src/main/resources/rulesets` folder in the `static-code-analysis-config` module.

## Customization 

Different sets of checks can be executed on different types of projects.

At the moment the tool executes different checks on OSGi bundles and ESH Bindings.

If you want to add a custom set of rules for UIs for example you will have to follow these steps:

- add a new profile in the `smarthome/pom.xml` that is activated when a specific file exists in the structure of the project (e.g. the profile for bindings is activated when `ESH-INF/binding/binding.xml` exists);
- in this profile you will have to override the path to the rule sets for the PMD, Checkstyle and FindBugs plugins; 
- in the `Static Code Analysis Config project` add a new rule set for PMD, Checkstyle and FindBugs that includes the rules that have to be executed.

## Reuse checks

All of the used static code analysis tools come with a set of custom rules that can be used directly in a rule set.

Helpful resources with lists of the available checks and information how to use them:

- for PMD - https://pmd.github.io/pmd-5.4.0/pmd-java/rules/index.html;
- for Checkstyle - http://checkstyle.sourceforge.net/checks.html;
- for FindBugs - Keep in mind that the process for adding a check in FindBugs contains two steps: 
   - First you should open the link with [BugDescriptors](http://findbugs.sourceforge.net/bugDescriptions.html), choose the bug that you want to detect and create a Match in `src/main/resources/rulesets/pmd/YOUR_RULESET`;
   - Next you should find the Detector that finds the Bug that you have selected above (you can use [this list](https://github.com/findbugsproject/findbugs/blob/d1e60f8dbeda0a454f2d497ef8dcb878fa8e3852/findbugs/etc/findbugs.xml)). Then add the Detector in the `findbugs-maven-plugin` configuration under the property `visitors`.

## Write custom checks

All of the used static code analysis tools have Java API for writing custom checks.

PMD extends this by giving the possibility to define a rule with the XPath syntax. PMD offers even more - a
Rule Designer that speeds up the process of developing a new rule! See http://nullpointer.debashish.com/pmd-xpath-custom-rules.

Examples of custom checks are implemented in the Static Code Analysis Config project.

PMD rules with XPatch expressions are defined directly in the rule set (`src/main/resources/rulesets/pmd/xpath` folder in `static-code-analysis-config`).

Helpful links when writing a custom check for the first time may be:

- for PMD - http://pmd.sourceforge.net/pmd-4.3.0/howtowritearule.html;
- for Checkstyle - http://checkstyle.sourceforge.net/writingchecks.html;
- for FindBugs - https://www.ibm.com/developerworks/library/j-findbug2/.

## Add tests for the new checks

Currently only tests for the PMD tool are included in the Eclipse SmartHome Build Tools, as the PMD tool has an integrated Test Framework.

In order to add a new test you have to do two things:
- Create a test class in the `src\test\java` folder that extends `SimpleAggregatorTst` and overrides the `setUp()` method;
- Add a .xml file in the `src\test\resources` folder that contains the code to be tested.

For more information: https://pmd.github.io/pmd-5.4.1/customizing/rule-guidelines.html. 


## Known problems 

- If the build is configured to fail on error, combined report will not be generated in the bundle that fails the build. You will see only the error message provided by the plugin that has failed the build;
- .xml files are not proceeded by the PMD Maven Plugin - Maven PMD plugin has goal pmd:pmd (https://maven.apache.org/plugins/maven-pmd-plugin/pmd-mojo.html) that has parameters "language", "includes" and "compileSourceRoots" that can be configured to proceed .xml files, but this was not working as expected. The "language" attribute allows single value, so checking files in multiple languages seems not to be supported);
- Flooded console output when running Checkstyle in debug mode in Maven  (- X ) - https://github.com/checkstyle/checkstyle/issues/3184;
- Can not configure FindBugs Maven Plugin to fail the build only on high priority warnings, but add all warnings to the report (http://stackoverflow.com/questions/10645245/findbugs-maven-plugin-ignores-threshold). This means that currently FindBugs fails the build regardless of the `Priority` of the Bug.

## 3rd Party

- The example checks provided in the `static-code-analysis-config` (`MethodLimitCheck`, `CustomClassNameLengthDetector`, `WhileLoopsMustUseBracesRule`) are based on tutorials how to use the API of Checkstyle, FindBugs and PMD. For more info, see javadoc.
