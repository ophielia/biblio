<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:output method="xml" indent="yes"/>
 
 <xsl:attribute-set name="myBorder">
    <xsl:attribute name="border">solid 0.3mm black</xsl:attribute>
<xsl:attribute name="padding">1pt</xsl:attribute>
</xsl:attribute-set>

  <xsl:template match="/classsummaryreport">
    <fo:root>
      <fo:layout-master-set>
        <fo:simple-page-master master-name="inoutreport"
              page-height="29.7cm" page-width="21.0cm" margin="1cm">
          <fo:region-body/>
        </fo:simple-page-master>
      </fo:layout-master-set>
     
     <fo:page-sequence master-reference="inoutreport">
        <fo:flow flow-name="xsl-region-body">


<fo:block space-after="1cm">
<fo:table  table-layout="fixed" >
<fo:table-column column-width="140mm"/>
<fo:table-column column-width="40mm"/>
<fo:table-body >
  <fo:table-row >
    <fo:table-cell >
     <fo:block text-align="left"   font-size="14pt" font-weight="bold">Class of: <xsl:value-of select="firstname_teacher"/><xsl:text>&#xA0;</xsl:text><xsl:value-of select="lastname_teacher"/></fo:block>
    </fo:table-cell>    
    <fo:table-cell text-align="left">
      <fo:block  font-weight="bold" font-size="14pt" >Date: <xsl:variable name="dat" select="rundate"/><xsl:value-of select="concat(
                            substring($dat, 9, 2),
                            '/',
                            substring($dat, 6, 2),
                            '/',
                            substring($dat, 1, 4)
                      )"/></fo:block>
    </fo:table-cell>
  </fo:table-row>
</fo:table-body>
</fo:table>
</fo:block>

<fo:block-container width="175mm">
<fo:block text-align="right" font-weight="bold">8 Books Checked Out</fo:block>
</fo:block-container>
<fo:block text-align="center" space-after="15mm">
<fo:table  table-layout="fixed"  border="0.0pt solid black" text-align="center">
<fo:table-column column-width="15mm"/>
<fo:table-column column-width="44mm"/>
<fo:table-column column-width="65mm"/>
<fo:table-column column-width="25mm"/>
<fo:table-column column-width="25mm"/>
    

<fo:table-header>
  <fo:table-row>
    <fo:table-cell xsl:use-attribute-sets="myBorder" >
      <fo:block font-weight="bold">Book</fo:block>
      <fo:block font-weight="bold">Id</fo:block>
    </fo:table-cell>
  <fo:table-cell xsl:use-attribute-sets="myBorder">
      <fo:block font-weight="bold">Student</fo:block>
    </fo:table-cell>
 <fo:table-cell xsl:use-attribute-sets="myBorder" >
          <fo:block font-weight="bold">Book Title</fo:block>
    </fo:table-cell>
 <fo:table-cell xsl:use-attribute-sets="myBorder" >
          <fo:block font-weight="bold">Checked </fo:block>
          <fo:block font-weight="bold">Out</fo:block>
    </fo:table-cell>    
 <fo:table-cell xsl:use-attribute-sets="myBorder">
          <fo:block font-weight="bold" >Returned</fo:block>
    </fo:table-cell>     
  </fo:table-row>
</fo:table-header>

<fo:table-body>
  
  <xsl:if test="checkedoutcount &gt; 0">
  <xsl:for-each select="checkedoutlist">
  <fo:table-row>
    <fo:table-cell xsl:use-attribute-sets="myBorder">
      <fo:block><xsl:value-of select="bookclientid"/></fo:block>
    </fo:table-cell>
    <fo:table-cell xsl:use-attribute-sets="myBorder">
      <fo:block white-space-collapse="false" text-align="left"  wrap-option="wrap"><xsl:value-of select="firstname_borrower"/> </fo:block>
      <fo:block white-space-collapse="false" text-align="left"  wrap-option="wrap"><xsl:value-of select="lastname_borrower"/></fo:block>
    </fo:table-cell>
        <fo:table-cell xsl:use-attribute-sets="myBorder">
          <fo:block text-align="left"><xsl:value-of select="booktitle"/></fo:block>
           <fo:block text-align="left"><xsl:value-of select="bookauthor"/></fo:block>
    </fo:table-cell>
        <fo:table-cell xsl:use-attribute-sets="myBorder">
          <fo:block><xsl:variable name="dt" select="checkedout"/><xsl:value-of select="concat(
                      substring($dt, 9, 2),
                      '/',
                      substring($dt, 6, 2))"/></fo:block>
    </fo:table-cell>
        <fo:table-cell xsl:use-attribute-sets="myBorder">
          <fo:block></fo:block>
    </fo:table-cell>
  </fo:table-row>
            </xsl:for-each>
            </xsl:if>
            <xsl:if test="checkedoutcount = 0">
            <fo:table-row  xsl:use-attribute-sets="myBorder">
            <fo:table-cell number-columns-spanned="5" >
            <fo:block text-align="center">No Books Checked Out On This Date</fo:block>
            </fo:table-cell>
            </fo:table-row>
            </xsl:if>
</fo:table-body>

</fo:table>
</fo:block>

<fo:block>Returned Books:<xsl:if test="returnedcount = 0"> No Books Returned On This Date</xsl:if><xsl:for-each select="returnedlist">
<xsl:value-of select="firstname_borrower"/><xsl:text>&#xA0;</xsl:text><xsl:value-of select="lastname_borrower"/>: 
<xsl:value-of select="booktitle"/>(<xsl:value-of select="bookclientid"/>);<xsl:text>&#xA0;</xsl:text><xsl:text>&#xA0;</xsl:text>
</xsl:for-each>
</fo:block>



<fo:block page-break-before="always">
<fo:block text-align="center" font-weight="bold" space-after="15mm">Overdue Notices</fo:block>
          
<fo:block text-align="left">Class of: <xsl:value-of select="firstname_teacher"/><xsl:text>&#xA0;</xsl:text><xsl:value-of select="lastname_teacher"/></fo:block>          
    <fo:block  margin-bottom="5mm" border-width="1mm" border-bottom-style="dotted" padding-bottom="15mm" >
   <xsl:for-each select="overduelist">
<fo:block text-indent="10mm"><xsl:value-of select="firstname_borrower"/><xsl:text>&#xA0;</xsl:text><xsl:value-of select="lastname_borrower"/>-<xsl:text>&#xA0;</xsl:text><xsl:value-of select="booktitle"/></fo:block>
            </xsl:for-each>
   </fo:block>
    
    
          <xsl:for-each select="overduelist">

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

<fo:block space-after="1cm">
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
             </fo:block>
          </xsl:for-each>
</fo:block>


          
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
</xsl:stylesheet>