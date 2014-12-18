<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/overduereport">
    <fo:root>
      <fo:layout-master-set>
        <fo:simple-page-master master-name="A4-portrait"
              page-height="29.7cm" page-width="21.0cm" margin="1cm">
          <fo:region-body/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="A4-portrait">
        <fo:flow flow-name="xsl-region-body">
<fo:block text-align="center" space-after="15mm">OVERDUE NOTICES</fo:block>
          <xsl:for-each select="book">

          <fo:block page-break-inside="avoid" space-after="5mm"  > 
          <fo:block text-align="right" margin="1mm"><xsl:value-of select="firstname_teacher"/>'s Class</fo:block>
          <fo:block margin-bottom="15mm" linefeed-treatment="preserve" border-width="1mm" border-bottom-style="dotted" padding-bottom="15mm"   space-after="5mm"  >
          <xsl:variable name="dt" select="checkedout"/>Hello <xsl:value-of select="firstname_borrower"/>! &#xA;
            Your book,<fo:inline font-style="italic"> <xsl:value-of select="booktitle"/>        </fo:inline> is really really late.  You checked it out on        <xsl:value-of select="concat(
                      substring($dt, 9, 2),
                      '/',
                      substring($dt, 6, 2),
                      '/',
                      substring($dt, 1, 4)
                      )"/>. Please return it as soon as possible.

<fo:table  table-layout="fixed" >
<fo:table-column column-width="140mm"/>
<fo:table-column column-width="40mm"/>
<fo:table-body >
  <fo:table-row >
    <fo:table-cell >
     <fo:block text-align="left">Date</fo:block>
    </fo:table-cell>    
    <fo:table-cell text-align="left">
	<fo:block text-align="left">Signature</fo:block>
	</fo:table-cell>
  </fo:table-row>
</fo:table-body>
</fo:table>
          </fo:block>
             </fo:block>
          </xsl:for-each>
          
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
</xsl:stylesheet>