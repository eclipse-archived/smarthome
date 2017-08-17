<xsl:stylesheet version="2.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:msxsl="urn:schemas-microsoft-com:xslt"
        xmlns:thing="http://eclipse.org/smarthome/schemas/thing-description/v1.0.0"
        xsi:schemaLocation="http://eclipse.org/smarthome/schemas/thing-description/v1.0.0 http://eclipse.org/smarthome/schemas/thing-description-1.0.0.xsd"
        exclude-result-prefixes="msxsl">
	<xsl:strip-space elements="*"/>
<xsl:output omit-xml-declaration="no" indent="yes" method="xml" cdata-section-elements="description"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

<!-- remove empty nodes -->
<xsl:template match=
    "*[not(@*|*|comment()|processing-instruction()) 
     and normalize-space()=''
      ]"/>
</xsl:stylesheet>
