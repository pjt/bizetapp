<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
   xmlns="http://www.tei-c.org/ns/1.0"
   version="2.0">

    <xsl:template match="@*|comment()|text()|processing-instruction()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="element()">
       <xsl:element name="{local-name()}">
          <xsl:apply-templates select="@*|node()"/>
       </xsl:element>
    </xsl:template>

</xsl:stylesheet>
