<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet 
	version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:tei="http://www.tei-c.org/ns/1.0"
    xmlns="http://www.w3.org/1999/xhtml"
	exclude-result-prefixes="tei">

  <!--
	  ============================================================
	  Transforms Bizet Catalog sources into HTML
	  Perry Trolard
	  Humanities Digital Workshop
      Fri 12 Sep 2008
	  ============================================================
  -->

  <xsl:output method="html" encoding="utf-8" indent="yes"/>

  <!--
	  ============================================================
	  Set defaults.
	  ============================================================
  -->

  <!-- default to 'span'-ing element nodes, ignoring att nodes -->
  <xsl:template match="tei:*">
	<xsl:element name="span">
	  <xsl:attribute name="class">
		<xsl:value-of select="local-name(.)"/>
	  </xsl:attribute>
	  <xsl:apply-templates select="@*"/>
	  <xsl:apply-templates/>
	</xsl:element>
  </xsl:template>

  <!-- default to ignoring attributes -->
  <xsl:template match="@*"/>

  <!-- default behavior is to copy text nodes -->
  <!-- default behavior is to ignore comment nodes -->

  <!--
	  ============================================================
	  Implement changes.
	  ============================================================
  -->

  <!-- straightforward transformations -->
    <!-- ready for TEI namespace or none -->
  <xsl:template match="tei:p | p">
	<p>
	  <xsl:apply-templates select="@*"/>
	  <xsl:apply-templates/>
	</p>
  </xsl:template>
  <xsl:template match="tei:list[@type='unordered']|tei:list | list">
    <xsl:if test="head | tei:head">
        <span class="head">
          <xsl:apply-templates select="head/@*|tei:head/@*"/>
          <xsl:value-of select="head | tei:head"/>
        </span>
    </xsl:if>
	<ul>
	  <xsl:apply-templates select="@*"/>
	  <xsl:apply-templates/>
	</ul>
  </xsl:template>
  <xsl:template match="tei:list[@type='ordered'] | list[@type='ordered']">
    <xsl:if test="head | tei:head">
        <span class="head">
          <xsl:apply-templates select="head/@*|tei:head/@*"/>
          <xsl:value-of select="head | tei:head"/>
        </span>
    </xsl:if>
	<ol>
	  <xsl:apply-templates select="@*"/>
	  <xsl:apply-templates/>
	</ol>
  </xsl:template>
  <!-- DO INLINE LISTS 
  <xsl:template match="tei:list[@type='inline'] | list[@type='inline']">
  -->

  <xsl:template match="tei:item | item">
	<li>
	  <xsl:if test="preceding-sibling::tei:label[1]">
		<strong><xsl:apply-templates select="preceding-sibling::tei:label[1]"     
            mode="catch"/>: </strong>
	  </xsl:if>
	  <xsl:if test="preceding-sibling::label[1]">
		<strong><xsl:apply-templates select="preceding-sibling::label[1]" 
            mode="catch"/>: </strong>
	  </xsl:if>
	  <xsl:apply-templates select="@*"/>
	  <xsl:apply-templates/>
	</li>
  </xsl:template>
  <xsl:template match="tei:headItem | headItem">
	<li style="font-style: italic; list-style: none; font-size: 80%;">
	  <xsl:if test="preceding-sibling::tei:headLabel[1]">
		<strong><xsl:apply-templates select="preceding-sibling::tei:headLabel[1]" 
            mode="catch"/>: </strong>
      </xsl:if>
	  <xsl:if test="preceding-sibling::headLabel[1]">
		<strong><xsl:apply-templates select="preceding-sibling::headLabel[1]" 
            mode="catch"/>: </strong>
	  </xsl:if>
	  <xsl:apply-templates select="@*"/>
	  <xsl:apply-templates/>
	</li>
  </xsl:template>
  <xsl:template match="tei:label | tei:headLabel | label | headLabel"/>
  <xsl:template match="tei:label | tei:headLabel | label | headLabel" mode="catch">
	<xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="tei:lb | lb">
	<br>
	  <xsl:apply-templates/>
	</br>
  </xsl:template>
  <xsl:template match="tei:ref">
	<a>
	  <xsl:apply-templates select="@*"/>
	  <xsl:attribute name="href"><xsl:value-of select="@target"/></xsl:attribute>
	  <xsl:apply-templates/>
	</a>
  </xsl:template>
  <xsl:template match="tei:mentioned | tei:foreign | tei:term | 
                        mentioned | foreign | term">
	<xsl:element name="em">
		<xsl:attribute name="class"><xsl:value-of select="local-name()"/></xsl:attribute>
		<xsl:apply-templates select="@*"/>
		<xsl:apply-templates/>
	</xsl:element>
  </xsl:template>
  <xsl:template match="tei:soCalled | soCalled">
	<xsl:element name="span">
		<xsl:attribute name="class"><xsl:value-of select="local-name()"/></xsl:attribute>
		<xsl:apply-templates select="@*"/>
		<xsl:text>&apos;</xsl:text><xsl:apply-templates/><xsl:text>&apos;</xsl:text>
	</xsl:element>
  </xsl:template>
  <xsl:template match="tei:emph | emph">
	<xsl:element name="em">
		<xsl:apply-templates select="@*"/>
		<xsl:apply-templates/>
	</xsl:element>
  </xsl:template>
	
  <xsl:template match="tei:div | tei:body | tei:argument | div | body | argument"> 
	<!-- block-level elements -->
	<div>
	  <xsl:choose>
		<xsl:when test="@type">
		  <xsl:attribute name="class"><xsl:value-of select="@type"/></xsl:attribute>
		</xsl:when>
		<xsl:otherwise>
		  <xsl:attribute name="class"><xsl:value-of select="local-name(.)"/></xsl:attribute>
		</xsl:otherwise>
	  </xsl:choose>
	  <xsl:apply-templates select="@*"/>
	  <xsl:apply-templates/>
	</div>
  </xsl:template>

  <!-- attributes -->
  <xsl:template match="@xml:id">
	<xsl:attribute name="id">
	  <xsl:value-of select="."/>
	</xsl:attribute>
  </xsl:template>
  <xsl:template match="@rend">
	<xsl:attribute name="style">
		<xsl:if test="contains(., 'bold')">
			<xsl:text>font-weight: bold; </xsl:text>
		</xsl:if>
		<xsl:if test="contains(.,'italic')">
			<xsl:text>font-style: italic; </xsl:text>
		</xsl:if>
		<xsl:if test="contains(.,'strikethr')">
			<xsl:text>text-decoration: line-through</xsl:text>
		</xsl:if>
	</xsl:attribute>
  </xsl:template>

  <!-- do headings -->
  <xsl:template match="tei:head | head">
	<xsl:choose>
	  <xsl:when test="//tei:div">
		<xsl:variable name="level" select="string(count(ancestor::tei:div) + 1)"/>
		<xsl:element name="{concat('h', $level)}">
		  <xsl:apply-templates select="@*"/>
		  <xsl:apply-templates/>
		</xsl:element>
	  </xsl:when>
	  <xsl:when test="//div">
		<xsl:variable name="level" select="string(count(ancestor::tei:div) + 1)"/>
		<xsl:element name="{concat('h', $level)}">
		  <xsl:apply-templates select="@*"/>
		  <xsl:apply-templates/>
		</xsl:element>
	  </xsl:when> 	
	  <xsl:otherwise>
		<xsl:variable name="level" select="3"/> <!-- ?? -->
		<xsl:element name="{concat('h', $level)}">
		  <xsl:apply-templates select="@*"/>
		  <xsl:apply-templates/>
		</xsl:element>
	  </xsl:otherwise>
	</xsl:choose>
  </xsl:template>

  <!-- do tables -->
  <xsl:template match="tei:table | table">
	<table>
	  <xsl:apply-templates select="@*"/>
	  <xsl:if test="tei:head | head">
		<thead>
		  <xsl:apply-templates select="tei:head | head"/>
		</thead>
	  </xsl:if>
	  <tbody>
		<xsl:apply-templates select="tei:row | row"/>
	  </tbody>
	</table>
  </xsl:template>
  <xsl:template match="tei:cell[@role='label'] | cell[@roll='label']">
	<th>
	  <xsl:apply-templates select="@*"/>	
	  <xsl:apply-templates/>
	</th>
  </xsl:template>
  <xsl:template match="tei:cell[@role='data'] | tei:cell | cell">
	<xsl:choose>
	  <xsl:when test="parent::tei:row[@role='label'] | parent::row[@role='label']">
		<th>
		  <xsl:apply-templates select="@*"/>
		  <xsl:apply-templates/>
		</th>
	  </xsl:when>
	  <xsl:otherwise>
		<td>
		  <xsl:apply-templates select="@*"/>
		  <xsl:apply-templates/>
		</td>
	  </xsl:otherwise>
	</xsl:choose>
  </xsl:template>
  <xsl:template match="tei:row | row">
	<tr>
	  <xsl:apply-templates select="@*"/>
	  <xsl:apply-templates/>
	</tr>
  </xsl:template>
  <xsl:template match="tei:table/tei:head | table/head">
	<tr>
	  <th>
		<xsl:attribute name="colspan"><xsl:value-of select="../@cols"/></xsl:attribute>
		<xsl:apply-templates select="@*"/>
		<xsl:apply-templates/>
	  </th>
	</tr>
  </xsl:template>
  

  <!-- do titles -->
  <xsl:template match="title[@type='main']">
    <h1>
        <xsl:apply-templates select="@*"/>
        <xsl:value-of select="."/>
    </h1>
  </xsl:template>

  <xsl:template match="tei:title | title">
    <em>
        <xsl:apply-templates select="@*"/>
        <xsl:value-of select="."/>
    </em>
  </xsl:template>

  <!-- msContents -->
  <xsl:template match="tei:msContents | msContents">
    <div class="{local-name(.)}">
      <xsl:if test="tei:head[1] | head[1]">
        <strong><xsl:value-of select="tei:head[1] | head[1]"/></strong>
      </xsl:if>
      <ul>
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates/>
      </ul>
    </div>
  </xsl:template>
  <xsl:template match="tei:msItem | msItem">
    <li>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </li>
  </xsl:template>
  <xsl:template match="tei:msItem/tei:* | msItem/*">
    <span class="{local-name}">
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
      <br/>
    </span>
  </xsl:template>

  <!-- 
       TODO
       * titlePage et al
       * note[@type='todo']

  -->
</xsl:stylesheet>
