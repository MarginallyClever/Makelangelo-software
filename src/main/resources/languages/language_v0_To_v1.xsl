<?xml version="1.0" encoding="UTF-8"?>

<!--
https://analyse-innovation-solution.fr/publication/fr/xslt/tutoriel-xslt-bases

    Document   : language_v0_To_v1.xsl
    Created on : 14 janvier 2022, 16:31
    Author     : q6
    Description:
        A try to transforme a language .xml DTD language v0 file to a language .xml file DTD language v1 ( using the key in an ID to get xml validation unique key vérification)
        BUG ? should embed the DTD to validate in stand alone ...
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" >
    <xsl:output method="xml" indent="yes"  encoding="utf-8" omit-xml-declaration="no" />
    <!-- 
<xsl:include href="functions_urls.xslt" />
TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
        
         https://stackoverflow.com/questions/1575111/can-an-xslt-insert-the-current-date
         https://www.oreilly.com/library/view/xslt-2nd-edition/9780596527211/re67.html
         
          <xsl:value-of 
      select="format-date(current-date(), 
              '[FNn], the [D1o] of [MNn], [Y01]')"/>
         
         SSI XSTLv2.0 ? <xsl:value-of  select="current-dateTime()"/> 
         
         xsl:stylesheet  xmlns:java="java" , <xsl:value-of select="java:util.Date.new()"/> + secure processing feature is set to false ?
    -->
    <xsl:template match="/">        
        <xsl:comment>Fichier auto généré le ??? (todo find the way to add a date/timestamp ... only posible if ... v2.0 or ? xml engine ?)
           
            d'aprés (todo find a way to insert the origine base file used ... if posible )</xsl:comment>        
        <language>
            <xsl:copy-of select="//language/meta"></xsl:copy-of>
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
