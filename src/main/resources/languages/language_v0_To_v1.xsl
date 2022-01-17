<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : language_v0_To_v1.xsl
    Created on : 14 janvier 2022, 16:31
    Author     : PPAC37
    Description:
        A try to transforme a language .xml DTD language "v0" file 
        to a language .xml file DTD language "v1" 
        ( using the key in an ID to get xml validation unique key vérification)        
        
        BUG : converte CDATA contente.
        
        TODO : insert a timestamp ( ? an argument ? xml version / engine ... )
            https://stackoverflow.com/questions/1575111/can-an-xslt-insert-the-current-date
            https://www.oreilly.com/library/view/xslt-2nd-edition/9780596527211/re67.html
             
         
            SSI XSLTv2.0 ? 
                <xsl:value-of  select="current-dateTime()"/> 
                <xsl:value-of select="format-date(current-date(), '[FNn], the [D1o] of [MNn], [Y01]')"/>
            
            if in XSLTv1.0 :
                xsl:stylesheet  xmlns:java="java" , <xsl:value-of select="java:util.Date.new()"/> 
                But ??? secure processing feature is set to false ?
         
        
        TODO : insert a posible src filename/path. ( an argument ? )
        
        DONE : https://stackoverflow.com/questions/42047263/add-a-doctype-declaration-on-xsl-ouptut
        DONE but to reveiw (TODO as a fonction) : https://stackoverflow.com/questions/723226/producing-a-new-line-in-xslt
        
        Documentation :
        (fr) https://analyse-innovation-solution.fr/publication/fr/xslt/tutoriel-xslt-bases
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" >
    <xsl:output method="xml" indent="yes"  encoding="utf-8" omit-xml-declaration="no" 
                standalone="no" doctype-system="language_v0.dtd" 
    />
    <!-- DONE : customize transformation rules 
         TODO : syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">        
        <xsl:comment>Auto-generated file the ? (TODO add date/timestamp) from (TODO find a way to insert the name/path of the original file used...)</xsl:comment>   
        <xsl:text>&#xd;&#xa;</xsl:text><!--Add a \r\n (as "&#xd;" is normaly a carriage return) and ( "&#xa;" is normaly a line feed / new line  ) -->        
        <xsl:comment>WARNING: modifications will be lost during the next generation.</xsl:comment>   
        <xsl:text>&#xd;&#xa;</xsl:text>
        <language>
            <!--<xsl:copy-of select="//language/meta"></xsl:copy-of>-->        
            <meta>
                <name>
                    <xsl:value-of select="//language/meta/name" />
                    <xsl:text>_v1</xsl:text><!-- Altering the language name to avoid confusion and possible lost/overwriting of an existing on the application startup-->
                </name>                         
                <author>
                    <xsl:value-of select="//language/meta/author" />
                </author>      
            </meta>
             
            <xsl:for-each select="//language/string">                
                <xsl:variable name="key_for_id" select="key" />
                <string>
                    <xsl:attribute name="id">
                        <xsl:value-of select="$key_for_id" />
                    </xsl:attribute>    
                    <!--
                    <key><xsl:value-of select="$key_for_id" /></key>                         
                    <value><xsl:value-of select="value" /></value>         
                    <hint><xsl:value-of select="hint" /></hint>
                    -->                    
                    <xsl:copy-of select="*" /><!-- ? disable-output-escaping="yes"  -->                    
                </string>
            </xsl:for-each>
        </language>
    </xsl:template>
</xsl:stylesheet>

