<?xml version="1.0" encoding="UTF-8" ?>
<!--
 * Copyright (C) 2012-2013, Markus Sprunck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - The name of its contributor may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:output method="xml" indent="yes" encoding="ISO-8859-1" />

	<xsl:template match="/">
		<sca>
			<xsl:for-each select="//BugCollection/BugInstance/Class">
				<xsl:call-template name="File" />
			</xsl:for-each>
		</sca>
	</xsl:template>

	<xsl:template name="File">
		<xsl:variable name="catkey" select="SourceLine/@classname" />
		<file name="{$catkey}.java">
			<xsl:call-template name="Source_Line" />
		</file>
	</xsl:template>

	<xsl:template match="SourceLine" name="Source_Line">
		<xsl:variable name="type" select="../@type" />
		<message>
			<xsl:attribute name="tool">findbugs</xsl:attribute>
			<xsl:attribute name="line"><xsl:value-of select="./SourceLine/@start" /></xsl:attribute>
			<xsl:attribute name="message"><xsl:value-of select="../ShortMessage" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of
				select="../../BugInstance/@priority" /></xsl:attribute>
			<xsl:attribute name="rule"><xsl:value-of select="../ShortMessage" /> (<xsl:value-of
				select="../../..//BugPattern[$type=@type]/@abbrev" />)</xsl:attribute>
			<xsl:variable name="category1" select="../../..//BugPattern[$type=@type]/@category" />
			<xsl:variable name="smallCase" select="'abcdefghijklmnopqrstuvwxyz '"/>
			<xsl:variable name="upperCase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ_'"/>
			<xsl:attribute name="category"><xsl:value-of select="translate($category1,$upperCase,$smallCase)"/></xsl:attribute>
			<xsl:attribute name="rule_id"><xsl:value-of
				select="../../..//BugPattern[$type=@type]/@type" /></xsl:attribute>
		</message>
	</xsl:template>

	<xsl:template match="ShortMessage"></xsl:template>
	<xsl:template match="Field"></xsl:template>
	<xsl:template match="LongMessage"></xsl:template>

</xsl:stylesheet>
