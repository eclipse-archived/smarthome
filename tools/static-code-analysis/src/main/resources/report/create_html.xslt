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
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:uml="http://www.eclipse.org/uml2/2.0.0/UML" xmlns:xmi="http://schema.omg.org/spec/XMI/2.1"
	xmlns:fn="http://www.w3.org/2005/xpath-functions">
	<xsl:output method="html" indent="yes" encoding="ISO-8859-1" />

	<xsl:template match="sca">
		<html>
			<head>
				<title>Analysis Report</title>
				<style type="text/css">
					body {margin-left: 3%; font-size:10pt;font-family:Arial;color:#000000 ; }					
					table.details tr th {font-size:10pt;font-family:Arial;font-weight:bold;background:#AC58FA}
					table.details tr {font-size:10pt;font-family:Arial;font-weight:normal;background:#BEa1Fa}
					table tbody	tr.alternate {background:#BAF4A7;}
					table tbody tr.dark {background:#F4F4A7;}
				</style>
			</head>
			
			<body>
				<h2>Report
					<xsl:call-template name="out_whitespace" />
					(<xsl:value-of select="current-date()" />;
					<xsl:call-template name="out_whitespace" />
					<xsl:value-of select="current-time()" />)
				</h2>
				<h3>Summary Messages</h3>
				<table width="90%" border="0" class="details">
					<tr>
						<th align="left">category</th>
						<th align="left">tool</th>
						<th align="left">priority</th>
						<th align="left">rule</th>
						<th align="left">count</th>
					</tr>

					<xsl:for-each-group select="//message" group-by="@category">
						<xsl:sort select="@category" />
						<xsl:variable name="category1" select="@category" />
						<tr>
							<td colspan="5">
								<xsl:value-of select="$category1" />
							</td>
						</tr>

						<xsl:for-each-group select="//message[$category1=@category]"
							group-by="@rule">
							<xsl:sort select="@priority" order="ascending" />
							<tr class="alternate">
								<td></td>
								<td>
									<xsl:value-of select="@tool" />
								</td>
								<td align="left">
									<xsl:value-of select="@priority" />
								</td>
								<td>
									<xsl:if test="'findbugs'=@tool">
										<xsl:call-template name="findbugs_link" />
									</xsl:if>
									<xsl:if test="'pmd'=@tool">
										<xsl:call-template name="pmd_link" />
									</xsl:if>
									<xsl:if test="'checkstyle'=@tool">
										<xsl:value-of select="@rule" />
									</xsl:if>
								</td>
								<td align="center">
									<xsl:value-of select="count( current-group() )" />
								</td>
							</tr>
						</xsl:for-each-group>
					</xsl:for-each-group>
					<tr class="dark">
						<td colspan="4"></td>
						<td align="center">
							<xsl:value-of select="count( .//message )" />
						</td>
					</tr>
				</table>
				<p/>
				
				<h3>Summary Files</h3>
				<table width="90%" border="0" class="details">
					<tr>
						<th align="left" width="90pt">class file</th>
						<th align="center" width="90pt">high</th>
						<th align="center" width="90pt">medium</th>
						<th align="center" width="90pt">low</th>
						<th align="center" width="90pt">total</th>
					</tr>
					<xsl:for-each-group select="file" group-by="@name">
						<xsl:sort select="@name" order="ascending" />
						<xsl:variable name="fileName" select="@name" />
	
						<tr class="alternate">
							<td>
								<xsl:call-template name="out_element_link" />
							</td>
							<td align="center">
								<div class="p1">
									<xsl:value-of select="count(//file[@name=$fileName]/message[@priority = 1])" />
								</div>
							</td>
							<td align="center">
								<div class="p2">
									<xsl:value-of select="count(//file[@name=$fileName]/message[@priority = 2])" />
								</div>
							</td>
							<td align="center">
								<div class="p3">
									<xsl:value-of select="count(//file[@name=$fileName]/message[@priority = 3])" />
								</div>
							</td>
							<td align="center">
								<div class="p3">
									<xsl:value-of select="count(//file[@name=$fileName]/message)" />
								</div>
							</td>
						</tr>
					</xsl:for-each-group>
					<tr class="dark">
						<td></td>
						<td align="center">
							<div class="p1">
								<xsl:value-of select="count(.//message[@priority = 1])" />
							</div>
						</td>
						<td align="center">
							<div class="p2">
								<xsl:value-of select="count(.//message[@priority = 2])" />
							</div>
						</td>
						<td align="center">
							<div class="p3">
								<xsl:value-of select="count(.//message[@priority = 3])" />
							</div>
						</td>
						<td align="center">
							<div class="p3">
								<xsl:value-of select="count(.//message)" />
							</div>
						</td>
					</tr>
				</table>
				<p />
				
				<h3>Details by Class</h3>
			 	<xsl:for-each-group select="file" group-by="@name"> 
				<xsl:sort select="@name" order="ascending" />
					<xsl:call-template name="file_detail" />
				</xsl:for-each-group> 
								
			</body>
		</html>
	</xsl:template>

	<xsl:template name="file_detail">
		<xsl:variable name="fileName" select="@name" />
		<table width="90%" border="0" class="details">
			<th align="left" colspan="6">
				<xsl:call-template name="out_element_anker" />
			</th>
			<tr>
				<th align="left" width="80pt">tool</th>
				<th align="left" width="50pt">priority</th>
				<th align="left" width="30pt">line</th>
				<th align="left" width="80pt">category</th>
				<th align="left" width="380pt">rule</th>
				<th align="left">message</th>
			</tr>			
				
			<xsl:for-each select="//file[@name=$fileName]/message">
				<xsl:sort select="@priority" order="ascending" />
				<tr class="alternate">
					<td>
						<xsl:value-of select="@tool" />
					</td>
					<td>
						<xsl:value-of select="@priority" />
					</td>
					<td>
						<xsl:value-of select="@line" />
					</td>
					<td>
						<xsl:value-of select="@category" />
					</td>
					<td>
						<xsl:value-of select="@rule" />
					</td>
					<td>
						<xsl:value-of select="@message" />
					</td>
				</tr>
			</xsl:for-each>
		</table>
		<br />
	</xsl:template>

	<xsl:template name="out_whitespace">
		<xsl:text disable-output-escaping="no"> </xsl:text>
	</xsl:template>

	<xsl:template name="out_key">
		<xsl:value-of select="@name" />
	</xsl:template>

	<xsl:template name="out_element">
		<xsl:value-of select="@name" />
	</xsl:template>

	<xsl:template name="out_element_anker">
		<xsl:text disable-output-escaping="yes">&lt;a name="</xsl:text>
		<xsl:call-template name="out_key" />
		<xsl:text disable-output-escaping="yes">"&gt;</xsl:text>
		<xsl:call-template name="out_element" />
		<xsl:text disable-output-escaping="yes">&lt;/a&gt;</xsl:text>
	</xsl:template>

	<xsl:template name="out_element_link">
		<xsl:text disable-output-escaping="yes">&lt;a href="#</xsl:text>
		<xsl:call-template name="out_key" />
		<xsl:text disable-output-escaping="yes">" title="</xsl:text>
		<xsl:call-template name="out_element" />
		<xsl:text disable-output-escaping="yes">"&gt;</xsl:text>
		<xsl:variable name="name1">
			<xsl:value-of select="@name" />
		</xsl:variable>
		<xsl:if test="(''=$name1)">
			<xsl:call-template name="out_element" />
		</xsl:if>
		<xsl:value-of select="@name" />
		<xsl:text disable-output-escaping="yes">&lt;/a&gt;</xsl:text>
	</xsl:template>

	<xsl:template name="findbugs_link">
		<xsl:text disable-output-escaping="yes"> &lt;a href="http://findbugs.sourceforge.net/bugDescriptions.html#</xsl:text>
		<xsl:value-of select="@rule_id" />
		<xsl:text disable-output-escaping="yes">"&gt;</xsl:text>
		<xsl:value-of select="@rule" />
		<xsl:text disable-output-escaping="yes">&lt;/a&gt;</xsl:text>
	</xsl:template>

	<xsl:template name="pmd_link">
		<xsl:text disable-output-escaping="yes"> &lt;a href="</xsl:text>
		<xsl:value-of select="@rule_url" />
		<xsl:text disable-output-escaping="yes">"&gt;</xsl:text>
		<xsl:value-of select="@rule" />
		<xsl:text disable-output-escaping="yes">&lt;/a&gt;</xsl:text>
	</xsl:template>
</xsl:stylesheet>
