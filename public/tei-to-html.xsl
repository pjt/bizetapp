<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet 
   version="2.0" 
   xmlns:nav="java:net.sf.saxon.om.Navigator"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns="http://www.w3.org/1999/xhtml"
   xmlns:tei="http://www.tei-c.org/ns/1.0"
   exclude-result-prefixes="nav">

  <!--
     ============================================================
     Perry Trolard
     Humanities Digital Workshop
     30 April 2007
     last modified: Tue 31 Mar 2009 19:26
     ============================================================
  -->

  <xsl:output 
     method="xhtml" 
     encoding="utf-8" 
     omit-xml-declaration="no"
     indent="yes"/>

  <!-- identity transform for comment, atts -->
  <xsl:template match="comment()|@*">
     <xsl:copy>
      <xsl:apply-templates select="@*"/> 
      <xsl:apply-templates/>
   </xsl:copy>
  </xsl:template>

  <!-- kill pi's -->
  <xsl:template match="processing-instruction()"/>

  <!-- elements -->
  <xsl:template name="xpath_for_html">
     <xsl:param name="path"/>
     <xsl:value-of select="concat('path',
                              replace(
                                 replace(
                                    replace($path,'/','--'),
                                 '\[','_-'),
                              '\]','-_'))"/>
  </xsl:template>

  <xsl:template match="element()">
     <span>
        <xsl:attribute name="class">
           <xsl:value-of select="concat('tei-',local-name(.))"/>
           <xsl:for-each select="@*">
              <xsl:value-of select="concat(' teiatt-',name(),'=',replace(.,' ','__'))"/>
           </xsl:for-each>
        </xsl:attribute>
        <xsl:attribute name="id">
           <xsl:call-template name="xpath_for_html">
              <xsl:with-param name="path" select="nav:getPath(.)"/>
           </xsl:call-template>
        </xsl:attribute>
        <xsl:apply-templates/>
     </span>
  </xsl:template>

  <!--
       =============
       Search-term 
         highlighting
       =============
   -->
   <xsl:param name="search-terms"/>
   <xsl:variable name="clean-terms" 
         select="replace($search-terms,'[.$^*]','\\$0')"/>
   <xsl:param name="flags">i</xsl:param>
 
   <xsl:template match="text()">
     <xsl:choose>
         <xsl:when test="$search-terms">
             <xsl:analyze-string select="." regex="{$clean-terms}" flags="{$flags}">
               <xsl:matching-substring>
                 <span class="search-match">
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
