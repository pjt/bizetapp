<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet 
   version="2.0" 
   xmlns:tei="http://www.tei-c.org/ns/1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns="http://www.w3.org/1999/xhtml"
   exclude-result-prefixes="tei">

  <!--
     ============================================================
     Perry Trolard
     Humanities Digital Workshop
     April 2009
     last modified: Thu 07 May 2009 09:52
     ============================================================
  -->

  <!-- import tei-to-html rules: using <span>s, @class, @id -->
  <xsl:import href="tei-to-html.xsl"/>
  <xsl:param name="with-xpath" select="true"/>

  <xsl:output 
     method="xhtml" 
     encoding="utf-8" 
     omit-xml-declaration="no"
     indent="yes"/>

  <!-- Add xml-server specific files: this (SVN repo) is the wrong place
  to add these server-specific paths to css and js files; will change
  later... -->
  <xsl:template match="/">
     <html>
        <head>
           <link rel="stylesheet" type="text/css" href="/bizet/static/css/tei.css" />
           <link rel="stylesheet" type="text/css" href="/bizet/static/facebox/facebox.css" />
           <script type="application/javascript" 
            Xsrc="http://ajax.googleapis.com/ajax/libs/jquery/1.3.1/jquery.min.js">
              <!-- --></script>
           <script type="application/javascript" src="/bizet/static/js/jquery.js">
              <!-- --></script>
           <script type="application/javascript" src="/bizet/static/facebox/facebox.js">
              <!-- --></script>
           <script type="application/javascript" src="/bizet/static/js/tei-and-html.js">
              <!-- --></script>
        </head>
        <body>
           <xsl:apply-templates/>
        </body>
     </html>
    </xsl:template>



</xsl:stylesheet>
