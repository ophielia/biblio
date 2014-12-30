<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:barcode="org.krysalis.barcode4j.xalan.BarcodeExt" xmlns:common="http://exslt.org/common" xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="barcode common xalan">
    
            <xsl:variable name="barcode-cfg">
              <barcode>
                <code128/>
              </barcode>
            </xsl:variable>
            
<xsl:template match="/">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
        <fo:layout-master-set>
            <fo:simple-page-master margin-top="1in" margin-left="1in" margin-bottom="1in"
                margin-right="1in" page-width="8in" page-height="11in" master-name="first">
                <fo:region-body margin-top="1cm"/>
                <fo:region-before extent="2cm"/>
                <fo:region-after extent="0pt"/>
            </fo:simple-page-master>
        </fo:layout-master-set>
        
        <fo:page-sequence master-reference="first">
        <fo:static-content flow-name="xsl-region-before">
            <fo:block text-align="center"><xsl:value-of select="barcodes/title"/></fo:block>
        </fo:static-content>     
        <fo:flow flow-name="xsl-region-body" font-size="7pt" font-family="Helvetica">
			<xsl:apply-templates/>
		</fo:flow>
        </fo:page-sequence>
    </fo:root>
</xsl:template>

    <xsl:template match="barcodes">
       <fo:table>
            <fo:table-body>
                <xsl:apply-templates/>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template match="codes">
        <xsl:variable name="posi" select="pos"/>
        <fo:table-cell >
            <xsl:if test="not($posi mod 3)">
                <xsl:attribute name="ends-row">true</xsl:attribute>
            </xsl:if>
            <fo:block keep-together="always">
            <fo:block margin-left="3mm">
	    	<xsl:value-of select="description"/>
            </fo:block>
                        <fo:block>
	                  <fo:instream-foreign-object>
	                    <xsl:copy-of select="barcode:generate($barcode-cfg, msg)"/>
	                  </fo:instream-foreign-object>
            </fo:block>
            </fo:block>
        </fo:table-cell>
    </xsl:template>



</xsl:stylesheet>