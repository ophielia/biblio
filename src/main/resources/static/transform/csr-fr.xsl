<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:barcode="org.krysalis.barcode4j.xalan.BarcodeExt" xmlns:common="http://exslt.org/common" xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="barcode common xalan">

<xsl:import href="csr-base.xsl"/>

<xsl:template match="/">
  <xsl:apply-imports/>
</xsl:template>






<xsl:template match="checkedoutcount">
	       <fo:block-container width="175mm">
	       <fo:block text-align="right" font-weight="bold"><xsl:value-of select="."/> Emprunté</fo:block>
</fo:block-container>
</xsl:template>

<xsl:template name="overduesummary">
<fo:block text-align="left">Classe de: <xsl:value-of select="firstname_teacher"/><xsl:text>&#xA0;</xsl:text><xsl:value-of select="lastname_teacher"/></fo:block>          
    <fo:block  margin-bottom="5mm" border-width="1mm" border-bottom-style="dotted" padding-bottom="15mm" >
   <xsl:for-each select="overduelist">
<fo:block text-indent="10mm"><xsl:value-of select="firstname_borrower"/><xsl:text>&#xA0;</xsl:text><xsl:value-of select="lastname_borrower"/>-<xsl:text>&#xA0;</xsl:text><xsl:value-of select="booktitle"/></fo:block>
            </xsl:for-each>
   </fo:block>
</xsl:template>


<xsl:template name="tableheaders">
<fo:table-header>
  <fo:table-row>
    <fo:table-cell xsl:use-attribute-sets="myBorder" >
      <fo:block font-weight="bold">Nr. de </fo:block>
      <fo:block font-weight="bold">Livre</fo:block>
    </fo:table-cell>
  <fo:table-cell xsl:use-attribute-sets="myBorder">
      <fo:block font-weight="bold">Elève</fo:block>
    </fo:table-cell>
 <fo:table-cell xsl:use-attribute-sets="myBorder" >
          <fo:block font-weight="bold">Titre du livre</fo:block>
    </fo:table-cell>
 <fo:table-cell xsl:use-attribute-sets="myBorder" >
          <fo:block font-weight="bold">Emprunté </fo:block>
          <fo:block font-weight="bold"></fo:block>
    </fo:table-cell>    
 <fo:table-cell xsl:use-attribute-sets="myBorder">
          <fo:block font-weight="bold" >Retourné</fo:block>
    </fo:table-cell>     
  </fo:table-row>
</fo:table-header>
</xsl:template>






<xsl:template match="overduelist">
<fo:block page-break-inside="avoid" space-after="5mm"  > 
          <fo:block text-align="right" margin="1mm">Classe de <xsl:value-of select="firstname_teacher"/></fo:block>
          <fo:block margin-bottom="15mm" linefeed-treatment="preserve" border-width="1mm" border-bottom-style="dotted" padding-bottom="15mm"   space-after="5mm"  >
          				<xsl:variable name="dt" select="checkedout" />
				<fo:block>Bonjour, &#xA;</fo:block>
				<fo:block space-after="1cm" space-before="1cm">
				 <xsl:value-of select="firstname_borrower" /> a emprunte le livre, "<xsl:value-of select="booktitle" />" le <xsl:value-of select="concat(
                      substring($dt, 9, 2),
                      '/',
                      substring($dt, 6, 2),
                      '/',
                      substring($dt, 1, 4)
                      )" /> qui est maintenant en retard.  S'il vous plaît rappelez à votre enfant de ramener leur livre dès que possible.
				</fo:block>

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
</xsl:template>


<xsl:template name="returnedbooks">
<fo:block space-before="1cm" font-size="9pt">Livres retourné:<xsl:if test="returnedcount = 0"> Pas de livres retourné</xsl:if>		</fo:block>    
</xsl:template>


<xsl:template name="nonecheckedout">
	      <fo:table-cell number-columns-spanned="5"  xsl:use-attribute-sets="myBorder"><fo:block>Aucune livre emprunté</fo:block></fo:table-cell>
</xsl:template>
</xsl:stylesheet>