<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:barcode="org.krysalis.barcode4j.xalan.BarcodeExt" xmlns:common="http://exslt.org/common" xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="barcode common xalan">
    
 <xsl:attribute-set name="myBorder">
    <xsl:attribute name="border">solid 0.3mm black</xsl:attribute>
<xsl:attribute name="padding">1pt</xsl:attribute>
</xsl:attribute-set>

<xsl:template match="/">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="first"
              page-height="29.7cm" page-width="21.0cm" 
              margin="1.5cm" margin-top="1cm">
          <fo:region-body/>
        </fo:simple-page-master>
      </fo:layout-master-set>
        
        <fo:page-sequence master-reference="first">   
        <fo:flow flow-name="xsl-region-body">
			<xsl:apply-templates/>
		</fo:flow>
        </fo:page-sequence>
    </fo:root>
</xsl:template>

<xsl:variable name="colcnt" select="tablereport/columncount"/>

    <xsl:template match="tablereport">
	       <!-- page title -->
  		<xsl:apply-templates select="title"/>      

	        

	<fo:block >
	<fo:table  table-layout="auto" >
	     <xsl:apply-templates select="colsizes"/>
	<fo:table-header>
	  <fo:table-row>
		<xsl:apply-templates select="colheader"/>	 
	</fo:table-row>
	</fo:table-header>
	<fo:table-body >
	  <!-- values -->
		<xsl:apply-templates select="tablevalue"/>	 
	</fo:table-body>
	</fo:table>
</fo:block>
	</xsl:template>

<!-- values template -->	
	<xsl:template match="tablevalue">
	        <xsl:variable name="posi" select="pos"/>
	        <fo:table-cell xsl:use-attribute-sets="myBorder" >
	            <xsl:if test="not($posi mod $colcnt)">
	                <xsl:attribute name="ends-row">true</xsl:attribute>
	            </xsl:if>
	            <fo:block  text-align="left">
	            <fo:block margin-top="1mm" margin-left="1mm" font-size="10pt" wrap-option="wrap">
		    	<xsl:value-of select="value"/>
	            </fo:block>
	                        
	            </fo:block>
	        </fo:table-cell>
    </xsl:template>


<xsl:template match="colheader">
    <fo:table-cell xsl:use-attribute-sets="myBorder" >
      <fo:block font-weight="bold" font-size="10pt" text-align="center"><xsl:value-of select="."/></fo:block>
    </fo:table-cell>
</xsl:template>


<xsl:template match="colsizes">
<xsl:variable name="sze" select="."/>
<xsl:choose>
<xsl:when test="$sze=''">
<fo:table-column  />
</xsl:when>
<xsl:otherwise>
<fo:table-column column-width="{$sze}" />
</xsl:otherwise>
</xsl:choose>
</xsl:template>


<xsl:template match="title">
      <fo:block font-weight="bold" space-after="1cm" text-align="center"><xsl:value-of select="."/>
      </fo:block>
</xsl:template>


</xsl:stylesheet>