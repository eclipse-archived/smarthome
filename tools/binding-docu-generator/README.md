# Eclipse Smarthome Binding Documentation Generator
Large parts of a binding for eclipse smarthome can be generated automatically from the XML files you create. This is done via this maven plugin.

## Using the plugin
The plugin executes at the life-cycle `package`. Its aim is to generate a markdown file (give the file a name via `readmeName`) from a given template (`templateFile`).

The plugin will automatically download the templates files into the `src/main/resources/templates/` directory (you can specify the directory with the `templates` property). 

If you want to reset the templates files or update them, simply delete them. The plugin will download the latest ones.

## Data model
To use Mustache without problems it was necessary to implement a strong data model. Since I did not want to extend the generated classes from JAXB, I created another data model. This also empowers us to change the way we parse our XML files later if necessary.

## Generating the XML schema classes
**This is only necessary when the .xsd-files change. This should not happen very often.** You need to regenerate the schema classes. To do this, please download the new `binding-description-x.x.x.xsd`, `thing-description-x.x.x.xsd` and `config-description-x.x.x.xsd` and modify the corresponding paths in the XML schemas. (Exchange the paths to the .xsd-files with your local paths.)

To use JAXB we need to generate Java classes with XML annotations. This can be done via the following command:

```
%JAVA_HOME%\bin\xjc src/test/resources/thing-description-1.0.0.xsd -b src/test/resources/bindings.xsd
```

The files will be created in your current working directory. Please move the contents of the package `org.eclipse.smarthome.config_description.v1_0` and `org.eclipse.smarthome.thing_description.v1_0` into the package `org.openhab.schemas`.

Make sure to **modify the namespaces** of the generated `ConfigDescriptions.java` and `ThingDescriptions.java`:
```
@XmlRootElement(name = "config-descriptions", namespace = "http://eclipse.org/smarthome/schemas/config-description/v1.0.0")
```

```
@XmlRootElement(name = "thing-descriptions", namespace = "http://eclipse.org/smarthome/schemas/thing-description/v1.0.0")
```

