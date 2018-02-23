# XSLT Transformation Service

Transform input using the XML Stylesheet Language for Transformations (XSLT).

XSLT is a standard method to transform an XML structure from one document into a new document with an other structure.

The transformation expects the rule to be read from a file which is stored under the `transform` folder. 
To organize the various transformations one should use subfolders.

General transformation rule summary:

* The directive `xsl:output` defines how the output document should be structured.
* The directive `xsl:template` specifies matching attributes for the XML node to find. 
* The `xsl:template` tag contains the rule which specifies what should be done.

The Rule uses XPath to gather the XML node information.
For more information have a look at the [XPath transformaton](https://docs.openhab.org/addons/transformations/jsonpath/readme.html) .

## Simple Example

A simple but complete XSLT transformaion looks like following example, which was taken from [here](https://en.wikipedia.org/wiki/Java_API_for_XML_Processing#Example).

**input XML**

```xml
<?xml version='1.0' encoding='UTF-8'?>
<root><node val='hello'/></root>
```
**transform/helloworld.xsl**
* `xsl:output`: transform incoming document into another XML-like document, without indendation.
* `xsl:template`: `match="/"` "any type of node", so the whole document.
* The `xsl` rule does `select` the node `/root/node` and extracts the `value-of` attribute `val`.

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

This example has a namespace defined, as you would find in real world applications, which has to be matched in the rule.

**input XML**

* The tag `<PTZStatus>` contains a attribute `xmlns=` which defines the namespace `http://www.hikvision.com/ver20/XMLSchema`.

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
In the rule, the tag `<xsl:stylesheet>` has to have an attribute `xmlns:xsl="http://www.w3.org/1999/XSL/Transform"` and a second attribute `xmlns:`. 
This attribute has to be the same as the namespace for the input document.
In the rule each step traversed along the path to the next tag has to be prepended with the `xmlns` namespace, here defined as `h`.

* `xsl:output` transform incoming document into another XML-like document, no indendation, **without XML**.
* `xsl:template`: `match="/"` whole document.
* Full path to node `azimuth` reading out `date` attribute.
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

**Output Document**

```
Fri, 18 Dec 2009 9:38 am PST
450
```

## Further Reading

You can find a very good [introduction](https://www.w3schools.com/xml/xsl_intro.asp) and [tutorial](https://www.w3schools.com/xml/xsl_transformation.asp) at W3School.
Extended introduction and more [examples](https://en.wikipedia.org/wiki/XSLT#XSLT_examples) at Wikipedia.
An informative [tutorial](https://www.ibm.com/developerworks/library/x-xsltmistakes/) of common mistakes.

