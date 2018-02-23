# XSLT Transformation Service

Transform input using the XML Stylesheet Language for Transformations (XSLT).

XSLT is a standard method to transform an XML structure into a document with the structure other structure.

Trhe transformation expects the transformation rule to be read from a file which is stored under the `transform` folder. 
To organize the various transformations one should use subfolders.

You can find a very good tutorial here: [XSLT tutorial at W3Schools](https://www.w3schools.com/xml/xsl_intro.asp)

General transformation rule summary:

* The directive `xsl:output` defines how the output document should be structured.
* The directive `xsl:template` specifies matching attributes for the XML node to find. 
* The `xsl:template` tag contains the rule which specifies what should be done.



## Simple Example

(from [here](https://en.wikipedia.org/wiki/Java_API_for_XML_Processing#Example))

**input XML**

```xml
<?xml version='1.0' encoding='UTF-8'?>
<root><node val='hello'/></root>
```

* `xsl:output`: transform incoming document into another XML-like document, without intendation.
* `xsl:template`: `match="/"` "any type of node", so the whole document.
* Rule: extract value from the selected node

**transform/helloworld.xsl**

```xml
<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version='2.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
   <xsl:output method='xml' indent='no'/>
   <xsl:template match='/'>
      <reRoot><reNode><xsl:value-of select='/root/node/@val' /> world</reNode></reRoot>
   </xsl:template>
</xsl:stylesheet>
```

**Output XML**

```xml
<reRoot><reNode>hello world</reNode></reRoot>
```

## Advanced Example

**input XML**

This example has an namespace defined, as you would find in real world applications, which has to be matched in the rule.

* The tag `<PTZStatus>` contains a namespace `xmlns=` 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<PTZStatus version="2.0" xmlns="http://www.hikvision.com/ver20/XMLSchema">
	<AbsoluteHigh>
		<elevation>0</elevation>
		<azimuth date="Fri, 18 Dec 2009 9:38 am PST" >450</azimuth>
		<absoluteZoom>10</absoluteZoom>
	</AbsoluteHigh>
</PTZStatus>
```

**transform/azimut.xsl**

In the rule the tag `<xsl:stylesheet>` has to have the  `xmlns:xsl="http://www.w3.org/1999/XSL/Transform"` attribute and a second attribut `xmlns:` which has to be the same as the namespace for the input document. In the rule each step traversed along the path to the next tag has to prepend the `xmlns` namespace, here defined as `h`.

* `xsl:output` transform incoming document into another XML-like document, no intendation, **without XML**.
* `xsl:template`: `match="/"` whole document.
* Full path to node `azimuth` reading out `date` attribut.
* Search for node `azimuth` by prepending `//` and get the `text`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:h="http://www.hikvision.com/ver20/XMLSchema">
   <xsl:output method="xml" indent="no" encoding="UTF-8" omit-xml-declaration="yes"  />
   <xsl:template match="/">
      <xsl:value-of select="/h:PTZStatus/h:AbsoluteHigh/h:azimuth/@date" />
      <xsl:text>&#10;</xsl:text>
      <xsl:value-of select="//h:azimuth/text()" />
   </xsl:template>
</xsl:stylesheet>
```

**Output XML**

```xml
Fri, 18 Dec 2009 9:38 am PST
450
```

Other examples may be found [here](https://en.wikipedia.org/wiki/XSLT#XSLT_examples).

