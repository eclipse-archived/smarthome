# XSL Transformation from lwm2m object registry to openhab2 files

The java program (eclipse/maven project) in this directory executes
the XSL transformation described in _res/transform/transform.xsl_ for all
xml input files in _res/lwm2m_object_registry_ that has been successfully validated against the _res/schema/LWM2M.xsd_ schema. Output files are written
to an _out_ directory, if they passed the validation against the _res/schema/thing-description-1.0.0.xsd_ file.

The program tries to update the input files before transforming them if the "-u" command line parameter is set.

## Arguments

* -dv,--disable-validation   Disable the validation of downloaded files
* -o,--output <arg>          output file path (default is 'out')
* -r,--res <arg>             resource file path, with schema and transform subdirectory (default is 'res')
* -u,--update                update OMA registry files (default is false)
* -url,--update-url <arg>    OMA registry url (default is http://www.openmobilealliance.org/wp/DDF.xml)

## Input files

All res/lwm2m_object_registry files are downloaded from
[[http://www.openmobilealliance.org/wp/DDF.xml]] and [[http://www.openmobilealliance.org/wp/OMNA/LwM2M/Common.xml]].
The schema validation file is _res/schema/LWM2M.xsd_.
The validation file for ESH is named _res/schema/thing-description-1.0.0.xsd_.

## Output files

Output files are written to the directory called "out" in the working directory.
Things and Channels are written out to one file if the LwM2M resources are defined for only the one specific LwM2M Object or separately for common resources with a file name pattern "thing-id[id].xml" and "channel-id[id].xml". The id is unique for things and channels respectively and defined by the OMA LWM2M Object Registry.

Output files are validated against the ESH schema file, so you can copy the generated files directly to the lwm2m_esh_addon/ESH-INF folder.
