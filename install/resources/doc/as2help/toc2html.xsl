<?xml version="1.0"?>
<!--
create navigation file from TOC-XML 
-->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:variable="java.util.HashMap">
<xsl:param name="product">mbi</xsl:param>
<xsl:param name="file">mbi_en.jhm</xsl:param>

<xsl:template match="/toc">
<html>
<head>
<title><xsl:value-of select="$product"/> help navigation</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
</head>
<link rel="stylesheet" type="text/css" href="../doc/mec_HTMLdoc.css"/>

<body bgcolor="#FFFFFF">
<h2><xsl:value-of select="$product"/> help navigation</h2>
<ul>
	<xsl:for-each select="tocitem">
		<xsl:call-template name="tocitem">
		</xsl:call-template>
	</xsl:for-each>

</ul>
</body>
</html>

</xsl:template>

<xsl:template name="tocitem">
	<xsl:variable name="url" select="variable:put('url','')"/>
	<xsl:variable name="image_url" select="variable:put('image_url','')"/>
	<xsl:variable name="target" select="variable:put('target',string(@target))"/>
	<xsl:variable name="text" select="variable:put('text',string(@text))"/>
	<xsl:variable name="image" select="variable:put('image',string(@image))"/>
	<xsl:for-each select="document($file)/map/mapID">
		<xsl:if test="@target=variable:get('target')">
			<xsl:variable name="url" select="variable:put('url',string(@url))"/>
		</xsl:if>
		<xsl:if test="@target=variable:get('image')">
			<xsl:variable name="image_url" select="variable:put('image_url',string(@url))"/>
		</xsl:if>
	</xsl:for-each>
	<li>
	<xsl:element name="a">
		<xsl:if test="normalize-space(variable:get('url'))=''">
			<xsl:attribute name="href">blank.html</xsl:attribute>
		</xsl:if>
		<xsl:if test="normalize-space(variable:get('url'))!=''">
			<xsl:attribute name="href"><xsl:value-of select="variable:get('url')"/></xsl:attribute>
		</xsl:if>
			<xsl:if test="normalize-space(variable:get('image_url'))!=''">
				<xsl:element name="img">
					<xsl:attribute name="src"><xsl:value-of select="variable:get('image_url')"/></xsl:attribute>
					<xsl:attribute name="width">16</xsl:attribute>
					<xsl:attribute name="height">16</xsl:attribute>
					<xsl:attribute name="border">0</xsl:attribute>
				</xsl:element>
			</xsl:if>
			<xsl:value-of select="variable:get('text')"/>
	</xsl:element>
	</li>
	<ul>
	<xsl:for-each select="tocitem">
		<xsl:call-template name="tocitem">
		</xsl:call-template>
	</xsl:for-each>
	</ul>
</xsl:template>

</xsl:stylesheet>