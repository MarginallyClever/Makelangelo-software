<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : language_v0_To_properties.xsl
    Created on : 14 janvier 2022, 16:31
    Author     : q6
    Description:
        A try to generate a .properties file from a .xml DTD language ( juste so i can use the lang .properties edit under Netbeans ...  )
        
        BUG : multiline CDATA converte  ( normaly a value sould be on a single ligne , but, int CDATA you may have multiple line ... ? \n replacement ??? )
        
        A way to automaticaly rename output file ( MakelangeloLanguage_codeLang_CodeCountry.properties )
        // ? only in the invocation of the convertion ( cf have to do this in a specific .java class ? or in the arguments used to do the transformation ) 
        
        
        As xml engine ( can have diffrent implementation/comportement like xalan vs .NETxmlEmbed vs ... ) this may not work ...
        https://stackoverflow.com/questions/723226/producing-a-new-line-in-xslt
        (TODO : Im using NetBeans 12.6 xml commands via toolsbar icons (so i even dont actualy know what xml engine i use ... )
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text"/>
    <xsl:template match="/">        
        <xsl:text># Fichier auto généré le ??? d'aprés ???</xsl:text>
        <xsl:text>&#xd;&#xa;</xsl:text><!--Add a \r\n (as "&#xd;" is normaly a carriage return) and ( "&#xa;" is normaly a line feed / new line  ) -->
            
        <xsl:text># </xsl:text>
        <xsl:value-of select="//language/meta/name"/>
        <xsl:text>&#xd;&#xa;</xsl:text><!--Add a \r\n (as "&#xd;" is normaly a carriage return) and ( "&#xa;" is normaly a line feed / new line  ) -->
            
        <xsl:text># </xsl:text>
        <xsl:value-of select="//language/meta/author"/>
        <xsl:for-each select="//language/string">
            <xsl:text>&#xd;&#xa;</xsl:text><!--Add a \r\n (as "&#xd;" is normaly a carriage return) and ( "&#xa;" is normaly a line feed / new line  ) -->
            
            <xsl:text># </xsl:text>
            <xsl:value-of select="hint" />
            <xsl:text>&#xd;&#xa;</xsl:text><!--Add a \r\n (as "&#xd;" is normaly a carriage return) and ( "&#xa;" is normaly a line feed / new line  ) -->
            <xsl:value-of select="key" />=<xsl:value-of select="value" />
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
