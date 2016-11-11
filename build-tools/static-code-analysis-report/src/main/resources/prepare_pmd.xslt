<?xml version="1.0" ?>
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
	xmlns:functx="http://www.functx.com" version="2.0">

	<xsl:output method="xml" indent="yes" encoding="ISO-8859-1" />

	<xsl:template match="/">
		<sca>
			<xsl:for-each select="//pmd">
				<xsl:apply-templates />
			</xsl:for-each>
		</sca>
	</xsl:template>

 	<xsl:template match="file">
		 <xsl:variable name="package_tmp" select="./violation/@package" />
	     <xsl:variable name="package" select="distinct-values($package_tmp)" />	     
	     <xsl:variable name="class_tmp" select="./violation/@class" />
	     <xsl:variable name="class" select="distinct-values($class_tmp)" />
		<file name="{$package}.{$class}.java">
			<xsl:apply-templates select="node()" />
		</file>
	</xsl:template>

	<xsl:template match="violation">
		<message>
			<xsl:attribute name="tool">pmd</xsl:attribute>
			<xsl:attribute name="line"><xsl:value-of select="@beginline" /></xsl:attribute>
			<xsl:attribute name="message"><xsl:value-of select="@rule" /></xsl:attribute>
			<xsl:attribute name="priority"><xsl:value-of select="@priority" /></xsl:attribute>
			<xsl:attribute name="rule"><xsl:value-of select="@rule" /></xsl:attribute>

			<xsl:variable name="category1" select="@ruleset" />
			<xsl:variable name="smallCase" select="'abcdefghijklmnopqrstuvwxyz '" />
			<xsl:variable name="upperCase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ_'" />
			<xsl:attribute name="category"><xsl:value-of
				select="translate($category1,$upperCase,$smallCase)" /></xsl:attribute>
		</message>
	</xsl:template>

</xsl:stylesheet>
