# Binding implementation

When implementing a binding bundle you, most probably, will use packages provided by the Eclipse SmartHome target platform.  
Please keep in mind that not all of the packages provided by the Eclipse SmartHome target platform will be available on different Eclipse SmartHome based solutions. Also not all Eclipse SmartHome bundles will be installed on the target runtime. Therefore, there is a risk your binding to be incompatible with some smart home solutions based on Eclipse SmartHome.  

In order to decrease the chance to end up in such situation we recommend to import only packages from the *List of packages* provided below.  

If you want to use custom libraries outside of this list, please take a look at the *Use custom libraries* section.  

Please, keep in mind that none Eclipse SmartHome based solution is obligated to support exactly these list of packages and also Eclipse SmartHome bundles installed on the OSGi runtime may differ between different Eclipse SmartHome based solutions.  

## List of packages

### Packages exported by Eclipse SmartHome bundles:

 org.eclipse.smarthome.config.core  
 org.eclipse.smarthome.config.discovery  
 
 org.eclipse.smarthome.core.library.types  
 org.eclipse.smarthome.core.types  

 org.eclipse.smarthome.core.thing  
 org.eclipse.smarthome.core.thing.binding  
 org.eclipse.smarthome.core.thing.binding.builder  
 org.eclipse.smarthome.core.thing.type  
 
### OSGi Packages:

 org.osgi.framework  
 org.osgi.util.*
 
### Third Party Libraries:

 guava (Util library)  
 apache.commons.lang (Util library)  
 apache.commons.io (Util library)  
 org.jupnp (UPnP)  
 org.slf4j (Logging)  
 
 
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
	  