---
layout: documentation
---

{% include base.html %}

# Coding Guidelines

The following guidelines apply to all code of the Eclipse SmartHome project. They must be followed to ensure a consistent code base for easy readability and maintainability.
Exceptions can certainly be made, but they should be discussed and approved by a project committer upfront.

Note that this list also serves as a checklist for code reviews on pull requests. To speed up the contribution process, we therefore advice to go through this checklist yourself before creating a pull request.

## A. Code Style

1. The [Java naming conventions](http://java.about.com/od/javasyntax/a/nameconventions.htm) should be used.
1. Every Java file must have a license header. You can run ```mvn license:format``` on the root of the repo to automatically add missing headers.
1. Every class, interface and enumeration should have JavaDoc describing its purpose and usage.
1. Every class, interface and enumeration must have an @author tag in its JavaDoc for every author that wrote a substantial part of the file.
1. Every constant, field and method with default, protected or public visibility should have JavaDoc (optional, but encouraged for private visibility as well)
1. Code must be formatted using the provided [code formatter](https://github.com/eclipse/smarthome/blob/master/targetplatform/esh-formatter.xml) and [clean up](https://github.com/eclipse/smarthome/blob/master/targetplatform/esh-clean-up.xml) settings (import them into your IDE if not available; they are automatically set by the official [IDE setup](ide.html)).
1. Generics must be used where applicable.
1. Code should not show any warnings. Warnings that cannot be circumvented should be suppressed by using the @SuppressWarnings annotation. 
1. For dependency injection, OSGi Declarative Services should be used.
1. Packages that contain classes that are not meant to be used by other bundles should have "internal" in their package name.

## B. OSGi Bundles

7. Every bundle must contain a Maven pom.xml with a version and artifact name that is in sync with the manifest entry. The pom.xml must reference the correct parent pom (which is usually in the parent folder).
1. Every bundle must contain an [about.html](https://eclipse.org/legal/epl/about.php) file, providing license information.
1. Every bundle must contain a build.properties file, which lists all resources that should end up in the binary under ```bin.includes```.
1. The manifest must not contain any "Require-Bundle" entries. Instead, "Import-Package" must be used.
1. The manifest must not export any internal package
1. The manifest must not have any version constraint on package imports, unless this is thoughtfully added. Note that Eclipse automatically adds these constraints based on the version in the target platform, which might be too high in many cases.
1. The manifest must include all services in the Service-Component entry. A good approach is to put OSGI-INF/*.xml in there.
1. Every exported package of a bundle must be imported by the bundle itself again.

## C. Language Levels and Libraries

1. Eclipse SmartHome requires at least JavaSE 7. Hence no features of Java 8 must be used within the code. To allow optimized JavaSE 8 runtimes, the set of Java packages to be used is furthermore restricted to [Compact Profile 2](http://www.oracle.com/technetwork/java/embedded/resources/tech/compact-profiles-overview-2157132.html).
1. The minimum OSGi framework version supported is [OSGi R4.2](http://www.osgi.org/Download/Release4V42), no newer features must be used.
1. For logging, slf4j (v1.7.2) is used with [non-static loggers](http://slf4j.org/faq.html#declared_static).
1. A few common utility libraries are available that every Eclipse SmartHome based solution has to provide and which can be used throughout the code (and which are made available in the target platform):
 - Apache Commons IO (v2.2)
 - Apache Commons Lang (v2.6)
 - Google Guava (v10.0.1)

## D. Runtime Behavior

14. Overridden methods from abstract classes or interfaces are expected to return fast unless otherwise stated in their JavaDoc. Expensive operations should therefore rather be scheduled as a job.
1. Creation of threads must be avoided. Instead, resort into using existing schedulers which use pre-configured thread pools. If there is no suitable scheduler available, start a discussion in the forum about it rather than creating a thread by yourself.
1. Bundles need to cleanly start and stop without throwing exceptions or malfunctioning. This can be tested by manually starting and stopping the bundle from the console (```stop <bundle-id>``` resp. ```start <bundle-id>```).
1. Bundles must not require any substantial CPU time. Test this e.g. using "top" or VisualVM and compare CPU utilization with your bundle stopped vs. started.
