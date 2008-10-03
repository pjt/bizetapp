<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet 
    version="2.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="tei">

  <!--
      ============================================================
      Performs Search-and-Replace in Text Nodes
      Perry Trolard
      Humanities Digital Workshop
      Thu 02 Oct 2008
      ============================================================
  -->

  <xsl:output method="xml" encoding="utf-8" indent="yes"/>

  <xsl:param name="search-terms"/>
  <xsl:param name="span-class">search-match</xsl:param>
  <xsl:param name="flags">i</xsl:param>

  <!--
    For allowed flags, see http://www.w3.org/TR/xpath-functions/#flags
  -->

  <!--
      ============================================================
      Set defaults.
      ============================================================
  -->

  <!-- default to copying all nodes -->
  <xsl:template match="node()[. != text()]|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!--
      ============================================================
      Implement changes.
      ============================================================
  -->

  <xsl:template match="text()">
    <xsl:choose>
        <xsl:when test="$search-terms">
            <xsl:analyze-string select="." regex="{$search-terms}" flags="{$flags}">
              <xsl:matching-substring>
                <span class="{$span-class}">
                  <xsl:value-of select="."/>
                </span>
              </xsl:matching-substring>
              <xsl:non-matching-substring>
                <xsl:value-of select="."/>
              </xsl:non-matching-substring>
            </xsl:analyze-string>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
