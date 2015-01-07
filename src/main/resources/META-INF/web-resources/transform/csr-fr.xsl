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
              margin="1.5cm" margin-top="3cm">
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



    <xsl:template match="classreport">
        
	       <!-- page title -->
	<fo:block space-after="1cm">
	<fo:table  table-layout="fixed" >
	<fo:table-column column-width="140mm"/>
	<fo:table-column column-width="40mm"/>
	<fo:table-body >
	  <fo:table-row >
	    <fo:table-cell >
	     <fo:block text-align="left"   font-size="14pt" font-weight="bold">Classe de: <xsl:value-of select="firstname_teacher"/><xsl:text>&#xA0;</xsl:text><xsl:value-of select="lastname_teacher"/></fo:block>
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
	
	
	
	       <!-- begin table of checkedout books -->
	       <!-- first the checkedout count -->
		<xsl:apply-templates select="checkedoutcount"/>	       
	       <!-- then the table -->
	       <fo:table>
	       <!-- call template for table headers (keep the text out of here...)-->
		<xsl:call-template name="tableheaders" />
	                   <fo:table-body>
	       <!-- apply templates here for checkedoutlist -->
	       <xsl:apply-templates select="checkedoutlist"/>
	       </fo:table-body>
               </fo:table>
	            
	       <!-- after the table, list of returns -->
	        <fo:block space-before="1cm" font-size="9pt">Livres retournés:<xsl:if test="returnedcount = 0"> Pas de livres retourné à cette date</xsl:if>
	        <!-- apply templates here for returns -->
		    <xsl:apply-templates select="returnedlist"/>
		</fo:block>    
		<!-- overdue books -->
		<xsl:if test="overduecount > 0">
		<fo:block page-break-before="always">
		<fo:block text-align="center" font-weight="bold" space-after="15mm">Avis de retard</fo:block>
		<!-- apply templates here for overdue books -->
		<xsl:call-template name="overduesummary">
		</xsl:call-template>
		</fo:block>
		<!-- now, the notices themselves -->
		<fo:block page-break-after="always">
		<xsl:apply-templates select="overduelist"/>
		</fo:block>
		</xsl:if>
		
            
    </xsl:template>

<xsl:template match="checkedoutlist">
 <fo:table-row>
<fo:table-cell xsl:use-attribute-sets="myBorder">
            <fo:block >
	    	<xsl:value-of select="bookclientid"/>
            </fo:block>
        </fo:table-cell>
	<fo:table-cell xsl:use-attribute-sets="myBorder">
	    	    <fo:block >
	    	    <xsl:value-of select="firstname_borrower"/><xsl:text>&#xA0;</xsl:text><xsl:value-of select="lastname_borrower"/>
	    	        </fo:block >
        </fo:table-cell>
	<fo:table-cell xsl:use-attribute-sets="myBorder">
	            <fo:block >
		    	<xsl:value-of select="booktitle"/>
	            </fo:block>
			<fo:block >
		    	<xsl:value-of select="bookauthor"/>
	            </fo:block>	            
        </fo:table-cell>
<fo:table-cell xsl:use-attribute-sets="myBorder" >
	<fo:block><xsl:variable name="dt" select="checkedout"/><xsl:value-of select="concat(
			      substring($dt, 9, 2),
			      '/',
	substring($dt, 6, 2))"/></fo:block>
</fo:table-cell>
<fo:table-cell xsl:use-attribute-sets="myBorder">
  <fo:block> </fo:block>
</fo:table-cell>
 </fo:table-row>
</xsl:template>

<xsl:template match="checkedoutcount">
	       <fo:block-container width="175mm">
	       <fo:block text-align="right" font-weight="bold"><xsl:value-of select="."/> Emprunté</fo:block>
</fo:block-container>
</xsl:template>




<xsl:template match="returnedlist">
<xsl:value-of select="firstname_borrower"/><xsl:text>&#xA0;</xsl:text><xsl:value-of select="lastname_borrower"/>: 
<xsl:value-of select="booktitle"/>(<xsl:value-of select="bookclientid"/>);<xsl:text>&#xA0;</xsl:text><xsl:text>&#xA0;</xsl:text>
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
      <fo:block font-weight="bold">Numéro de </fo:block>
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

</xsl:stylesheet>