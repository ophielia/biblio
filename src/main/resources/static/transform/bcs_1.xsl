<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:barcode="org.krysalis.barcode4j.xalan.BarcodeExt" xmlns:common="http://exslt.org/common" xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="barcode common xalan">
    
            <xsl:variable name="barcode-cfg">
              <barcode>
                <code128>
                <height>16.5mm</height>
                <module-width>0.25mm</module-width>
                </code128>
              </barcode>
            </xsl:variable>
        

           <xsl:variable name="ndge" select="barcodes/nudge"/>
 <xsl:variable name="tmar"> 
<xsl:choose>
     <xsl:when test="$ndge='-3'">
        <xsl:text>3.5</xsl:text>
     </xsl:when>
     <xsl:when test="$ndge='-2'">
        <xsl:text>4.5</xsl:text>
     </xsl:when>
     <xsl:when test="$ndge='-1'">
        <xsl:text>5.5</xsl:text>
     </xsl:when>   
          <xsl:when test="$ndge='0'">
             <xsl:text>6.5</xsl:text>
     </xsl:when>   
          <xsl:when test="$ndge='1'">
             <xsl:text>7.5</xsl:text>
     </xsl:when>   
          <xsl:when test="$ndge='2'">
             <xsl:text>8.5</xsl:text>
     </xsl:when>   
          <xsl:when test="$ndge='3'">
             <xsl:text>9.5</xsl:text>
     </xsl:when>       
     <xsl:otherwise>
        <xsl:text>6.5</xsl:text>
    </xsl:otherwise></xsl:choose>
</xsl:variable> 

           <xsl:variable name="brdr" select="barcodes/border"/>
 <xsl:variable name="border"> 
<xsl:choose>
     <xsl:when test="$brdr='1'">
        <xsl:text>black</xsl:text>
     </xsl:when>
     <xsl:otherwise>
        <xsl:text>white</xsl:text>
    </xsl:otherwise></xsl:choose>
</xsl:variable> 

<xsl:template match="/">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
        <fo:layout-master-set>
            <fo:simple-page-master margin-top="{$tmar}mm" margin-left="8mm" margin-bottom=".5mm"
                margin-right="0mm" page-width="210mm" page-height="297mm" master-name="first">
                <fo:region-body margin-top="0cm"/>
            </fo:simple-page-master>
        </fo:layout-master-set>
        <fo:page-sequence master-reference="first"> 
        <fo:flow flow-name="xsl-region-body" font-size="7pt" font-family="Helvetica">
			<xsl:apply-templates/>
		</fo:flow>
        </fo:page-sequence>
    </fo:root>
</xsl:template>

    <xsl:template match="barcodes">
       <fo:table  border-collapse="separate" padding-top=".5mm" border-spacing="0mm 0mm" width="19cm" table-layout="fixed">
       <fo:table-column column-width="39mm"/>
       <fo:table-column column-width="39mm"/>
       <fo:table-column column-width="39mm"/>
       <fo:table-column column-width="39mm"/>
       <fo:table-column column-width="39mm"/>
            <fo:table-body>
                <xsl:apply-templates/>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template match="codes">
        <xsl:variable name="posi" select="pos"/>
        <fo:table-cell border-width="0.1mm" border-style="solid" height="21.6mm" border-color="{$border}" >
            <xsl:if test="not($posi mod 5)">
                <xsl:attribute name="ends-row">true</xsl:attribute>
            </xsl:if>
            <xsl:choose>
            <xsl:when test="msg = 'dummy'">
            <fo:block  color="white" keep-together="always"  text-align="center">
	                <fo:block margin-top="1mm" margin-left="1mm">
	    	    	<xsl:value-of select="description"/>
	                </fo:block>
	    <fo:block>

	    </fo:block>
            </fo:block>
            </xsl:when>
            <xsl:otherwise>
            <fo:block keep-together="always"  text-align="center">
	                <fo:block margin-top="1mm" margin-left="1mm">
	    	    	<xsl:value-of select="description"/>
	                </fo:block>
	                            <fo:block>
	    	                  <fo:instream-foreign-object content-width="37.5mm" content-height="18.75mm">
	    	                    <xsl:copy-of select="barcode:generate($barcode-cfg, msg)"/>
	    	                  </fo:instream-foreign-object>
	                </fo:block>
            </fo:block>
            </xsl:otherwise>
            </xsl:choose>
            
            
        </fo:table-cell>
    </xsl:template>



</xsl:stylesheet>