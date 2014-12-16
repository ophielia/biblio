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

<fo:block text-align="center" space-after="15mm">
<fo:table  table-layout="fixed"  border="0.0pt solid black" text-align="center">
<fo:table-column column-width="25mm"/>
<fo:table-column column-width="35mm"/>
<fo:table-column column-width="55mm"/>
<fo:table-column column-width="35mm"/>
<fo:table-column column-width="35mm"/>
    

<fo:table-header>
  <fo:table-row>
    <fo:table-cell padding="1pt" >
      <fo:block font-weight="bold">Book Id</fo:block>
    </fo:table-cell>
  <fo:table-cell padding="1pt" >
      <fo:block font-weight="bold">Student</fo:block>
    </fo:table-cell>
 <fo:table-cell padding="1pt" >
          <fo:block font-weight="bold">Book Title</fo:block>
    </fo:table-cell>
 <fo:table-cell padding="1pt" >
          <fo:block font-weight="bold">Checked Out</fo:block>
    </fo:table-cell>    
 <fo:table-cell padding="1pt" >
          <fo:block font-weight="bold">Returned</fo:block>
    </fo:table-cell>     
  </fo:table-row>
</fo:table-header>

<fo:table-body>
  
  <xsl:for-each select="book">
  <fo:table-row>
    <fo:table-cell>
      <fo:block><xsl:value-of select="bookclientid"/></fo:block>
    </fo:table-cell>
    <fo:table-cell>
      <fo:block><xsl:value-of select="firstname_borrower"/> <xsl:value-of select="lastname_borrower"/></fo:block>
    </fo:table-cell>
        <fo:table-cell>
          <fo:block><xsl:value-of select="booktitle"/></fo:block>
           <fo:block><xsl:value-of select="bookauthor"/></fo:block>
    </fo:table-cell>
        <fo:table-cell>
          <fo:block><xsl:variable name="dt" select="checkedout"/><xsl:value-of select="concat(
                      substring($dt, 9, 2),
                      '/',
                      substring($dt, 6, 2),
                      '/',
                      substring($dt, 1, 4)
                      )"/></fo:block>
    </fo:table-cell>
        <fo:table-cell>
          <fo:block></fo:block>
    </fo:table-cell>
  </fo:table-row>
            </xsl:for-each>
</fo:table-body>

</fo:table>
</fo:block>




          
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
</xsl:stylesheet>