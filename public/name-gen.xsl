<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns="http://www.tei-c.org/ns/1.0"
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    version="2.0">
    
    
    <xsl:template match="*:div[@type='summary'][not(*:head[@type='gen-head'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Summary</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    
    <xsl:template match="*:div[@type='castlist'][not(*:head[@type='gen-head'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Cast List</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*:div[@type='instrumentation'][not(*:head[@type='gen-head'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Instrumentation</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <!--
    <xsl:template match="*:list[@type='instrumentation'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">General Instrumentation</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    -->
    
    <xsl:template match="*:list[@type='stage-band'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Stage Band</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='contents'][not(*:head[@type='gen-head'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Contents</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='text'][not(*:head[@type='gen-head'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Poem/Libretto</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='composition'][not(*:head[@type='gen-head'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Composition</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='dedicatee'][not(*:head[@type='gen-head'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Dedicatee</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='to'][not(*:head[@type='gen-head'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Self Borrowing To</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='from'][not(*:head[@type='gen-head'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Self Borrowing From</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='manuscripts'][not(*:head[@type='gen-head'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Manuscript Sources</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="*:div[@type='autographs'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Autograph Manuscripts</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="*:div[@type='full-scores'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Scores</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="*:div[@type='arrangements'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Arrangements</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="*:div[@type='vocal-scores'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Vocal Scores</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="*:div[@type='part-scores'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Parts</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='manuscript-sources'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Non-Autograph Manuscripts</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='mises-en-scene'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Mises-en-Scène</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='manuscript-librettos'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Manuscript Librettos</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='publication'][not(*:head[@type='gen-head'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Publication</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='librettos'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Librettos</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='vocal-complete'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Complete</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='vocal-extracts'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Extracts</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='vocal-parts'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Vocal Parts</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='full-complete'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Complete</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='full-extracts'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Extracts</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='orchestral-scores'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Orchestral Scores</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='orchestral-full'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Complete</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='orchestral-extracts'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Extracts</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='references'][not(*:head[@type='gen-head'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">References</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='all-letters'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Letters</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='documents'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Documents</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='articles'][not(*:head[@type='gen-head2'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Articles</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='performances'][not(*:head[@type='gen-head'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Performances</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='discography'][not(*:head[@type='gen-head'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Discography</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='bibliography'][not(*:head[@type='gen-head'])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Bibliography</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
