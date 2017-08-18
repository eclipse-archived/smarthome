<xsl:stylesheet version="2.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:msxsl="urn:schemas-microsoft-com:xslt"
				xmlns:thing="http://eclipse.org/smarthome/schemas/thing-description/v1.0.0"
                xsi:schemaLocation="http://eclipse.org/smarthome/schemas/thing-description/v1.0.0 http://eclipse.org/smarthome/schemas/thing-description-1.0.0.xsd"
                exclude-result-prefixes="msxsl">

	<xsl:strip-space elements="*"/>
	<xsl:output omit-xml-declaration="no" indent="yes" method="xml" cdata-section-elements="description"/>
				
  <xsl:template match="/*">
    <xsl:apply-templates select="node()"/>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="Object">
	<xsl:result-document method="xml" href="thing-id{ObjectID}.xml">
    <thing:thing-descriptions bindingId="lwm2mleshan"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:thing="http://eclipse.org/smarthome/schemas/thing-description/v1.0.0"
        xsi:schemaLocation="http://eclipse.org/smarthome/schemas/thing-description/v1.0.0 http://eclipse.org/smarthome/schemas/thing-description-1.0.0.xsd">
			
		<xsl:element name="thing-type">
			<xsl:attribute name="id">
				<xsl:value-of select="self::node()/ObjectID" />
			</xsl:attribute>
            <!-- things with an ID < 6 should not be listed: Those are for internal bridge related actions only -->
            <xsl:if test="not(self::node()/ObjectID &gt;= 6)">
    			<xsl:attribute name="listed">false</xsl:attribute>
            </xsl:if>

			
			<supported-bridge-type-refs>
			  <bridge-type-ref id="client" />
			</supported-bridge-type-refs>
			
			<label><xsl:value-of select="self::node()/Name" /></label>
			
			<description>
				<xsl:value-of select="normalize-space(self::node()/Description1)" />
				<xsl:value-of select="normalize-space(self::node()/Description2)" />
			</description>
			
			<xsl:for-each select="Resources/Item">
                <xsl:variable name="name" select="self::node()/Name" />
                <xsl:variable name="itemid" select="self::node()/@ID" />
                <xsl:if test="self::node()/Mandatory='Optional'">
                    <xsl:comment>Optional channel "<xsl:value-of select="$name" />" (<xsl:value-of select="$itemid" />) will be added dynamically</xsl:comment>
                </xsl:if>
            </xsl:for-each>
            			
			<channel-groups>
            <xsl:for-each select="Resources/Item">
                <xsl:variable name="name" select="self::node()/Name" />
                <xsl:variable name="itemid" select="self::node()/@ID" />
                <xsl:if test="self::node()/Mandatory='Mandatory'">
	                <xsl:element name="channel-group">
	                    <xsl:attribute name="id">
	                        <xsl:value-of select="$itemid" />
	                    </xsl:attribute>
	                    <xsl:attribute name="typeId">
	                        <xsl:value-of select="$itemid" />
	                    </xsl:attribute>
                    <xsl:comment>Channel "<xsl:value-of select="$name" />" (<xsl:value-of select="$itemid" />)</xsl:comment>
	                </xsl:element>
                </xsl:if>
            </xsl:for-each>
			</channel-groups>
			
			<properties>
				<property name="ObjectID"><xsl:value-of select="self::node()/ObjectID" /></property>
				<property name="ObjectURN"><xsl:value-of select="self::node()/ObjectURN" /></property>
				<property name="MultipleInstances"><xsl:value-of select="self::node()/MultipleInstances" /></property>
				<property name="Mandatory"><xsl:value-of select="self::node()/Mandatory" /></property>
			</properties>
		
		</xsl:element> <!--thing-type-->

        <xsl:for-each select="Resources/Item">
            <xsl:variable name="name" select="self::node()/Name" />
	        <xsl:variable name="itemid" select="self::node()/@ID" />
            <xsl:element name="channel-group-type">
                <xsl:attribute name="id">
                    <xsl:value-of select="$itemid" />
                </xsl:attribute>
                <xsl:comment>MultiInstances: <xsl:value-of select="self::node()/MultipleInstances" /></xsl:comment>
                
                <label><xsl:value-of select="$name" /></label>
                <description></description>
                
                <channels>
	            <xsl:element name="channel">
	                <xsl:attribute name="id">0</xsl:attribute>
                    <xsl:attribute name="typeId">
                        <xsl:value-of select="$itemid" />
                    </xsl:attribute>
                    <xsl:comment>Channel "<xsl:value-of select="$name" />", ID: <xsl:value-of select="$itemid" />, InstanceID: 0</xsl:comment>
                </xsl:element>
                </channels>
                
            </xsl:element>
            <xsl:if test="not($itemid &gt;= 2047)">
                <xsl:call-template name="ResourceItem"/>
            </xsl:if>
		</xsl:for-each>

	</thing:thing-descriptions>
	</xsl:result-document>
	
	<xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="Object/Resources/Item">
	<xsl:variable name="itemid" select="self::node()/@ID" />

    <!-- Resources with an id >= 2048 are reusable and should be written to separate files -->
    <xsl:if test="($itemid &gt;= 2048)">
	    <xsl:result-document method="xml" href="channel-id{$itemid}.xml">
	    <thing:thing-descriptions bindingId="lwm2mleshan"
		    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		    xmlns:thing="http://eclipse.org/smarthome/schemas/thing-description/v1.0.0"
		    xsi:schemaLocation="http://eclipse.org/smarthome/schemas/thing-description/v1.0.0 http://eclipse.org/smarthome/schemas/thing-description-1.0.0.xsd">
        <xsl:call-template name="ResourceItem"/>
	    </thing:thing-descriptions>
        </xsl:result-document>
    </xsl:if>
  </xsl:template>

  <xsl:template name="ResourceItem">
	<xsl:variable name="itemid" select="self::node()/@ID" />
	<xsl:element name="channel-type">
		<xsl:attribute name="id">
			<xsl:value-of select="$itemid" />
		</xsl:attribute>
		<xsl:if test="self::node()/Mandatory='Optional'">
			<xsl:attribute name="advanced"><xsl:text>true</xsl:text></xsl:attribute>
		</xsl:if>
		
		<!-- item-type: Switch, Rollershutter, Contact, String, Number, Dimmer, DateTime, Color, Image -->
        <item-type><xsl:choose>
         <xsl:when test="self::node()/Operations='E'">
			<xsl:text>Switch</xsl:text>
         </xsl:when>
         <xsl:otherwise>
			<xsl:choose>
				<xsl:when test="self::node()/Type='Boolean'">
					<xsl:choose>
						 <xsl:when test="self::node()/Operations='R'">
							<xsl:text>Contact</xsl:text>
						 </xsl:when>
						 <xsl:otherwise>
							<xsl:text>Switch</xsl:text>
						 </xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="self::node()/Type='Integer'">
					<xsl:choose>
						 <xsl:when test="$itemid='5851'">
							<xsl:text>Dimmer</xsl:text>
						 </xsl:when>
						 <xsl:otherwise>
							<xsl:text>Number</xsl:text>
						 </xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="self::node()/Type='Float'">
					<xsl:text>Number</xsl:text>
				</xsl:when>
				<xsl:when test="self::node()/Type='Opaque'">
					<xsl:text>String</xsl:text>
				</xsl:when>
                <xsl:when test="self::node()/Type='Objlnk'">
                    <xsl:text>String</xsl:text>
                </xsl:when>
				<xsl:when test="self::node()/Type='String'">
					<xsl:choose>
						 <xsl:when test="$itemid='5706'">
							<xsl:text>Color</xsl:text>
						 </xsl:when>
						 <xsl:otherwise>
							<xsl:text>String</xsl:text>
						 </xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="self::node()/Type='Time'">
					<xsl:text>DateTime</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:message terminate="yes">Type not known: <xsl:apply-templates select="." mode="message"/></xsl:message>
				</xsl:otherwise>
			</xsl:choose>
         </xsl:otherwise>
       </xsl:choose></item-type>

		<label><xsl:value-of select="self::node()/Name" /></label>
		
		<description><xsl:value-of select="normalize-space(self::node()/Description)" /></description>
		
		<!-- category -->
		<xsl:if test="self::node()/Type='Boolean'">
			<category>
			<xsl:choose>
				 <xsl:when test="self::node()/Operations='R'">
					<xsl:text>Sensor</xsl:text>
				 </xsl:when>
				 <xsl:otherwise>
					<xsl:text>Switch</xsl:text>
				 </xsl:otherwise>
			</xsl:choose>
			</category>
		</xsl:if>
		
		<!-- tags: We need a way to distinguish integer, float numbers in openHab -->
        <tags>
		 <xsl:if test="self::node()/Operations='E'">
				<tag>Executable</tag>
         </xsl:if>
        <xsl:choose>
			<xsl:when test="self::node()/Type='Integer'">
				<tag>Integer</tag>
			</xsl:when>
			<xsl:when test="self::node()/Type='Float'">
				<tag>Float</tag>
			</xsl:when>
			<xsl:when test="self::node()/Type='Opaque'">
				<tag>Opaque</tag>
			</xsl:when>
		</xsl:choose>
		</tags>
		 
		<!-- state -->
		<xsl:if test="self::node()/Operations='R'">
			<state readOnly="true"></state>
		</xsl:if>
		
		<xsl:if test="self::node()/Type='Integer' and $itemid='5851'">
			<state min="0" max="100"></state>
		</xsl:if>
		
	</xsl:element> <!--channel-type-->
  </xsl:template>

</xsl:stylesheet>
