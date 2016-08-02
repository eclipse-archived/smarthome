---
layout: documentation
---

{% include base.html %}

Testing Eclipse SmartHome
===
There are two different kinds of approaches for testing Eclipse SmartHome. One is to use plain JUnit tests for testing simple classes. The other is to execute JUnit tests within the OSGi environment to test OSGi services and dynamic behaviour. Both approaches are supported through a simple infrastructure, which allows to easily write and execute tests.

Test fragment
---

In OSGi tests are implemented in a separate fragment bundle, which host is the bundle, that should be tested. The name of the test fragment bundle should be the same as the bundle to test with a ".test" suffix. The MANIFEST.MF file must contain a `Fragment-Host` entry. Fragment bundles inherit all imported packages from the host bundle. In addition the fragment bundle must import the `org.junit` package with a minimum version of 4.0.0 specified. The following code snippet shows a manifest file of the test fragment for the `org.eclipse.smarthome.core` bundle.

	Manifest-Version: 1.0
	Bundle-ManifestVersion: 2
	Bundle-Name: Tests for the openHAB Core
	Bundle-SymbolicName: org.eclipse.smarthome.core.test
	Bundle-Version: 0.9.0.qualifier
	Bundle-Vendor: Eclipse.org/SmartHome
	Fragment-Host: org.eclipse.smarthome.core
	Bundle-RequiredExecutionEnvironment: JavaSE-1.7
	Import-Package: org.junit;version="4.0.0"

Tests are typically placed inside the folder `src/test/java`. 

Unit tests
---

Each class inside the test folder, which has a public method with a `@Test` annotation will automatically be executed as a test. Inside the class one can refer to all classes from the host bundle and all imported classes. The following code snippet shows a simple JUnit test which tests the `toString` conversation of a PercentType.

	public class PercentTypeTest {
    	@Test
    	public void DoubleValue() {
            PercentType pt = new PercentType("0.0001");
            assertEquals("0.0001", pt.toString());
    	}
	}

Using the the [https://code.google.com/p/hamcrest/ hamcrest] matcher library is a good way to write expressive assertions. In contrast to the original assertion statements from JUnit the hamcrest matcher library allows to define the assertion in a more natural order:

	PercentType pt = new PercentType("0.0001");
	assertThat pt.toString(), is(equalTo("0.0001"))

To use the hamcrest library in your test project, you just have to add the following entry to the list of imported packages:

	org.hamcrest;core=split

Tests can be executed from Eclipse by right-clicking the test file and clicking on `Run As => JUnit Test`. From maven one can execute the test with `mvn test` command in the folder of the test fragment bundle.    

Groovy
---

Using the JVM language Groovy tests are very easy and efficient to write. Groovy supports mocking without any frameworks. Language features like closures, type-inference and native syntax for maps and lists allow to implement short and easy to understand tests. Thus Eclipse SmartHome comes with an out-of-the-box-support for Groovy-testing in Eclipse and maven. Each test file which is placed under `src/test/groovy` will be automatically compiled and executed in Eclipse and maven. Moreover the Eclipse SmartHome Yoxos profile contains the Groovy-Eclipse-Plugin.

Although the following examples are presented in Groovy, unit and OSGi tests can also be implemented in Java. If the default mocking capabilities of Groovy do not fulfill the requirements, Groovy can also be combined with Java mocking frameworks like [https://code.google.com/p/mockito/ mockito]. 

== OSGi-Tests ==

Some components of Eclipse SmartHome are heavily bound to the OSGi runtime, because they use OSGi core services like the EventAdmin or the ConfigurationAdmin. That makes it hard to test those components outside of the OSGi container. Equinox provides a possibility to execute a JUnit test inside the OSGi environment, where the test has access to OSGi services.

Eclipse SmartHome comes with an abstract base class `OSGiTest` for OSGi tests. The base class sets up a bundle context and has convenience methods for registering mocks as OSGi services and the retrieval of registered OSGi services. The following Groovy test class shows how to test the `ItemRegistry` by providing a mocked `ItemProvider`.
    
	class ItemRegistryOSGiTest extends OSGiTest {
 
     ItemRegistry itemRegistry
     ItemProvider itemProvider
     def ITEM_NAME = "switchItem"
 
     @Before
     void setUp() {
         itemRegistry = getService(ItemRegistry)
         itemProvider = [
             getItems: {[new SwitchItem(ITEM_NAME)]}, 
             addItemChangeListener: {def itemCHangeListener -> },
             removeItemChangeListener: {def itemCHangeListener -> }] as ItemProvider
     }
 
     @Test
     void 'assert getItems returns item from registered ItemProvider'() {
 
         assertThat itemRegistry.getItems().size, is(0)
 
         registerService itemProvider
 
         def items = itemRegistry.getItems()
         assertThat items.size, is(1)
         assertThat items.first().name, is(equalTo(ITEM_NAME))
 
         unregisterService itemProvider
 
         assertThat itemRegistry.getItems().size, is(0)
     }
	}
    
In the `setUp` method the `ItemRegistry` OSGi service is retrieved through the method `getService` from the base class `OSGiTest` and assigned to a private variable. After it a new `ItemProvider` mock is created, which returns one item. The test method first checks that no item is inside the registry. Afterwards it registers the mocked `ItemProvider` as OSGi service with the method `registerService` and checks if the `ItemRegistry` returns one item now. At the end the mock is unregistered again.

In Eclipse the tests can be executed by right-clicking the test file and clicking on `Run As => JUnit Plug-In Test`. The launch config must be adapted, by selecting the bundle to test under the `Plug-Ins` tab and by clicking on `Add Required Plug-Ins`. Moreover you have to set the Auto-Start option to `true`. If the bundle that should be tested makes use of declarative services (has xml files in OSGI-INF folder), the bundle `org.eclipse.equinox.ds` must also be selected and also the required Plug-Ins of it. The `Validate Plug-Ins` button can be used to check if the launch config is valid. To avoid the manual selection of bundles, one can also choose `all workspace and enabled target plug-ins` with default `Default Auto-Start` set to `true`. The disadvantage is that this will start all bundles, which makes the test execution really slow and will produce a lot of errors on the OSGi console. It is a good practice to store a launch configuration file that launches all test cases for a test fragment.

From maven the test can be executed by calling `mvn integration-test`. For executing the test in maven, tycho calculates the list of depended bundles automatically from package imports. Only if there is no dependency to a bundle, the bundle must be added manually to the test execution environment. For example Eclipse SmartHome makes use of OSGi declarative services. That allows to define service components through XML files. In order to support declarative services in the test environment the according bundle `org.eclipse.equinox.ds` must be added in the pom file within the `tycho-surefire-plugin` configuration section as dependency and furthermore the startlevel has to be defined as shown below. The snippet also shows how to enable `logging` during the test-execution with maven. Therefor you have to add the bundles `ch.qos.logback.classic, ch.qos.logback.core ch.qos.logback.slf4j` as dependency to your tycho-surefire configuration.

	...
	<build>
     <plugins>
         <plugin>
             <groupId>org.eclipse.tycho</groupId>
             <artifactId>tycho-surefire-plugin</artifactId>
             <version>${tycho-version}</version>
             <configuration>
                 <dependencies>
                     <dependency>
                         <type>eclipse-plugin</type>
                         <artifactId>org.eclipse.equinox.ds</artifactId>
                         <version>0.0.0</version>
                     </dependency>
                     <!-- Required Bundles to enable LOGGING -->
                     <dependency>
                         <type>eclipse-plugin</type>
                         <artifactId>ch.qos.logback.classic</artifactId>
                         <version>0.0.0</version>
                     </dependency>
                     <dependency>
                         <type>eclipse-plugin</type>
                         <artifactId>ch.qos.logback.core</artifactId>
                         <version>0.0.0</version>
                     </dependency>
                     <dependency>
                         <type>eclipse-plugin</type>
                         <artifactId>ch.qos.logback.slf4j</artifactId>
                         <version>0.0.0</version>
                     </dependency>
                 </dependencies>
                 <bundleStartLevel>
                     <bundle>
                         <id>org.eclipse.equinox.ds</id>
                         <level>1</level>
                         <autoStart>true</autoStart>
                     </bundle>
                 </bundleStartLevel>
             </configuration>
         </plugin>
     </plugins>
	</build>
	...
    
In the dependency definition the `artifactId` is the name of the required bundle, where the version can always be `0.0.0`. Within the `bundleStartLevel` definition the start level and auto start of the depended bundles can be configured. The `org.eclipse.equinox.ds` bundle must have level 1 and must be started automatically.
