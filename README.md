# Eclipse SmartHome Build Instructions

Thanks for your interest in the Eclipse SmartHome project!

Building and running the project is fairly easy if you follow the steps
detailed below.

Please note that Eclipse SmartHome is not a product itself, but a framework to build solutions on top.
This means that what you build is primarily an artifact repository of OSGi bundles that can be used
within smart home products. Besides this repository, a tool called "Designer" is available. The
Designer can be used for editing configuration files with full IDE support.

1. Prerequisites
================

The build infrastructure is based on Maven in order to make it
as easy as possible to get up to speed. If you know Maven already then
there won't be any surprises for you. If you have not worked with Maven
yet, just follow the instructions and everything will miraculously work ;-)

What you need before you start:
- Maven3 from http://maven.apache.org/download.html

Make sure that the "mvn" command is available on your path


2. Checkout
===========

Checkout the source code from GitHub, e.g. by running

git clone https://github.com/eclipse/smarthome.git

3. Building with Maven
======================

To build Eclipse SmartHome from the sources, Maven takes care of everything:
- set MAVEN_OPTS to "-Xms512m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m"
- change into the smarthome directory ("cd smarthome“)
- run "mvn clean install" to compile and package all sources

The build result will be available in the folder 
smarthome/distribution/target. Both the repository as well as
the designer zips are placed in there.

4. Starting the designer
========================

- unzip the file eclipsesmarthome-incubation-0.x.0-designer-<platform>.zip to a local folder
- run the executable „Eclipse-SmartHome-Designer[.exe]"
