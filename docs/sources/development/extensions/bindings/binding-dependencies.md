# Binding dependencies

Below you can find the packages available for binding development.

### The minimum packages needed 
If you use [create binding scripts](https://github.com/eclipse/smarthome/tree/master/addons/binding) to create a binding these packages will be automatically added as imports in the manifest:  

 org.eclipse.smarthome.config.core  
 org.eclipse.smarthome.core.library.types  
 org.eclipse.smarthome.core.thing  
 org.eclipse.smarthome.core.thing.binding  
 org.eclipse.smarthome.core.thing.binding.builder  
 org.eclipse.smarthome.core.thing.type  
 org.eclipse.smarthome.core.types  
 org.slf4j  

### Java packages 
API packages from Java 8 compact1 and compact2 Additions profiles are fully supported.
The exact list of packages is available at [Java SE Embedded 8 Compact Profiles Overview](http://www.oracle.com/technetwork/java/embedded/resources/tech/compact-profiles-overview-2157132.html) page.
 
## Additional packages

### For XML processing  
 com.thoughtworks.xstream  
 com.thoughtworks.xstream.annotations  
 com.thoughtworks.xstream.converters  
 com.thoughtworks.xstream.io  
 com.thoughtworks.xstream.io.xml  

### For JSON processing  
 com.google.gson.*  
 
### For HTTP operations  
 org.apache.http.*  
 
### OSGi Packages  
 org.osgi.framework  
 org.osgi.util.*  

### For basic utility operations
 com.google.common.*   
 apache.commons.lang.*  
 
### For IO operations 
 org.apache.commons.io.*  
 
### For UPnP device discovery
 org.jupnp.* 
 
 
## Use custom libraries

If you want your binding to rely on a custom implementation you can use a custom library project in form of a JAR file. 
When you have the JAR file you have to take the following steps to be able to use it in your binding:

 - Put your jar file in the file system of your project (e.g. /lib/library.jar).
 - Add the new library to the _bin.includes_ section of your [build.properties](http://help.eclipse.org/luna/index.jsp?topic=/org.eclipse.pde.doc.user/reference/pde_feature_generating_build.htm) file 
 to make sure that the library will be included in the binary.
 - To compile the binding in Eclipse IDE you have to add the library project to your classpath as well. You can do this by adding new classpath entry:
 `<classpathentry kind="lib" path="lib/library.jar"/>` 
 - Add the library project to the bundle classpath in MANIFEST.MF file  
  ```Bundle-ClassPath: .,
      lib/library.jar```
	  
Keep in mind that if you want to use third party libraries they have to be compatible with the [list of licenses approved for use by third-party code redistributed by Eclipse projects](https://eclipse.org/legal/eplfaq.php#3RDPARTY).  
Every bundle must contain an [about.html](https://eclipse.org/legal/epl/about.php) file.
For additional information check the [Guide to the Legal Documentation](https://www.eclipse.org/legal/guidetolegaldoc2.php).  
