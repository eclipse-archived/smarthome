# XSLT Transformation Service

Transform input using the XML Stylesheet Language for Transformations (XSLT).

It expects the transformation rule to be read from a file which is stored under the `transform` folder. 
To organize the various transformations one should use subfolders.

## Example

(from [here](https://en.wikipedia.org/wiki/Java_API_for_XML_Processing#Example))

input:

```xml
<?xml version='1.0' encoding='UTF-8'?>
<root><node val='hello'/></root>
```

transform/helloworld.xsl:

```xml
<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version='2.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
   <xsl:output method='xml' indent='no'/>
   <xsl:template match='/'>
      <reRoot><reNode><xsl:value-of select='/root/node/@val' /> world</reNode></reRoot>
   </xsl:template>
</xsl:stylesheet>
```

output:

```xml
<reRoot><reNode>hello world</reNode></reRoot>
```

Other examples may be found [here](https://en.wikipedia.org/wiki/XSLT#XSLT_examples).