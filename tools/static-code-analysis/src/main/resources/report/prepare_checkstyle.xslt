<?xml version="1.0" encoding="UTF-8" ?>
<!-- * Copyright (C) 2012-2013, Markus Sprunck * * All rights reserved. * 
	* Redistribution and use in source and binary forms, with or * without modification, 
	are permitted provided that the following * conditions are met: * * - Redistributions 
	of source code must retain the above copyright * notice, this list of conditions 
	and the following disclaimer. * * - Redistributions in binary form must reproduce 
	the above * copyright notice, this list of conditions and the following * 
	disclaimer in the documentation and/or other materials provided * with the 
	distribution. * * - The name of its contributor may be used to endorse or 
	promote * products derived from this software without specific prior * written 
	permission. * * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND * 
	CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, * INCLUDING, 
	BUT NOT LIMITED TO, THE IMPLIED WARRANTIES * OF MERCHANTABILITY AND FITNESS 
	FOR A PARTICULAR PURPOSE * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
	OWNER OR * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, * 
	SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT * NOT LIMITED 
	TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; * LOSS OF USE, DATA, OR 
	PROFITS; OR BUSINESS INTERRUPTION) HOWEVER * CAUSED AND ON ANY THEORY OF 
	LIABILITY, WHETHER IN CONTRACT, * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
	OR OTHERWISE) * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN 
	IF * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. * -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:output method="xml" indent="yes" encoding="ISO-8859-1" />

	<xsl:template match="/">
		<sca>
			<xsl:for-each select="//checkstyle">
				<xsl:apply-templates />
			</xsl:for-each>
		</sca>
	</xsl:template>

	<xsl:template match="file">
		<xsl:variable name="new_name" select="translate(@name, '/', '.')" />
		<xsl:variable name="new_name" select="translate($new_name, '\', '.')" />
		<xsl:variable name="new_name"
			select="concat(substring-after($new_name,'.src.'),  substring-after($new_name,'.source.'))" />

		<xsl:variable name="msg" select="./error" />
		<xsl:if test="($msg='')">
			<file name="{$new_name}">
				<xsl:apply-templates select="node()" />
			</file>
		</xsl:if>
	</xsl:template>

	<xsl:template match="error">
		<xsl:variable name="priority">
			<xsl:if test="@severity='error'">
				1
			</xsl:if>
			<xsl:if test="@severity='warning'">
				2
			</xsl:if>
			<xsl:if test="@severity='info'">
				3
			</xsl:if>
		</xsl:variable>
		<message>
			<xsl:attribute name="tool">checkstyle</xsl:attribute>
			<xsl:attribute name="line"><xsl:value-of select="@*" /></xsl:attribute>
			<xsl:attribute name="message"><xsl:value-of select="@message" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="+$priority" /></xsl:attribute>
			<xsl:attribute name="rule">
				<xsl:call-template name="substring-after-last">
					<xsl:with-param name="input" select="@source" />
					<xsl:with-param name="marker" select="'.'" />
				</xsl:call-template>
			</xsl:attribute>
			<xsl:attribute name="category">style</xsl:attribute>
		</message>
	</xsl:template>

	<xsl:template name="substring-after-last">
		<xsl:param name="input" />
		<xsl:param name="marker" />
		<xsl:choose>
			<xsl:when test="contains($input,$marker)">
				<xsl:call-template name="substring-after-last">
					<xsl:with-param name="input"
						select="substring-after($input,$marker)" />
					<xsl:with-param name="marker" select="$marker" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$input" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
