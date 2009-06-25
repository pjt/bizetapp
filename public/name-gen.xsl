<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns="http://www.tei-c.org/ns/1.0"
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    version="2.0">
    
    <xsl:template match="*:div[@type='castlist']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Cast List</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*:div[@type='instrumentation']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Instrumentation</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:list[@type='instrumentation']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">General Instrumentation</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:list[@type='stage-band']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Stage Band</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='contents']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Contents</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='text']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Poem/Libretto</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='composition']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Composition</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='dedicatee']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Dedicatee</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='to']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Self Borrowing To</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='from']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Self Borrowing From</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='manuscripts']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Manuscript Sources</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="*:div[@type='autographs']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Autograph Manuscripts</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="*:div[@type='full-scores']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Full Scores</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="*:div[@type='arrangements']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Arrangements</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="*:div[@type='vocal-scores']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Vocal Scores</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="*:div[@type='part-scores']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Parts</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='manuscript-sources']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Non-Autograph Manuscripts</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='mises-en-scene']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Mises-en-Scène</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='manuscript-librettos']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Manuscript Librettos</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='publication']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Publication</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='librettos']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Librettos</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='vocal-complete']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Complete</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='vocal-extracts']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Extracts</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='vocal-parts']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Vocal Parts</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='full-complete']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Complete</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='full-extracts']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Extracts</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='orchestral-scores']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Orchestral Scores</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='orchestral-full']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Complete</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='orchestral-extracts']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Extracts</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='references']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">References</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='all-letters']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Letters</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='documents']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Documents</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='articles']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head2">Articles</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='performances']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Performances</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='discography']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <head type="gen-head">Discography</head>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:div[@type='bibliography']">
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
