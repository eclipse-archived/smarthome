# XSL Transformation from lwm2m object registry to openhab2 files
The java program (eclipse/maven project) in this directory executes
the XSL transformation described in _res/transform/transform.xsl_ for all
xml input files in _res/lwm2m_object_registry_ which has been successfully validated against the _res/schema/LWM2M.xsd_ schema. Output files are written
to an _out_ directory. The program tries to update the input files before
transforming them.

## Input files
All res/lwm2m_object_registry files are downloaded from
[[http://technical.openmobilealliance.org/Technical/technical-information/omna/lightweight-m2m-lwm2m-object-registry|OMA LWM2M Object Registry]]
including the schema validation file _res/schema/LWM2M.xsd_.
The validation file for openhab2 is named _res/schema/thing-description-1.0.0.xsd_.
Validation after transformation is also performed.

## Output files
Output files are written to the directory called "out" in the working dir.
Things and Channels are written out separately with a file name pattern "thing-id[id].xml" and "channel-id[id].xml". The id is unique for things and channels respectively and defined by the OMA LWM2M Object Registry. Output files are validated against the openhab2 schema file. You can copy the output files directly to the lwm2m_openhab2_addon/ESH-INF folder.