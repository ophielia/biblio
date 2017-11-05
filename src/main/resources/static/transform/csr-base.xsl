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
	
	
	
	       <!-- begin table of checkedout books -->
	       <!-- first the checkedout count -->
		<xsl:apply-templates select="checkedoutcount"/>	       
	       <!-- then the table -->
	       <fo:table>
	       <fo:table-column column-width="15mm"/>
			<fo:table-column column-width="44mm"/>
			<fo:table-column column-width="65mm"/>
			<fo:table-column column-width="25mm"/>
			<fo:table-column column-width="25mm"/>
	       <!-- call template for table headers (keep the text out of here...)-->
		<xsl:call-template name="tableheaders" />
	                   <fo:table-body>
	        <xsl:if test="checkedoutcount = 0">
	        	<!-- apply templates here for checkedoutlist -->
	       		<xsl:call-template name="nonecheckedout"/>
	        </xsl:if>
			 <xsl:if test="checkedoutcount > 0">
				       <!-- apply templates here for checkedoutlist -->
				       <xsl:apply-templates select="checkedoutlist"/>
			 </xsl:if>
	       </fo:table-body>
               </fo:table>
	            
	       <!-- after the table, list of returns -->
	        <xsl:call-template name="returnedbooks" />
	        
	        <!-- apply templates here for returns -->
		    <fo:block font-size="9pt" page-break-after="always"><xsl:apply-templates select="returnedlist"/></fo:block>

		<!-- overdue books -->
<!-- removing ...  -->
		
            
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


<xsl:template match="returnedlist">
<xsl:value-of select="firstname_borrower"/><xsl:text>&#xA0;</xsl:text><xsl:value-of select="lastname_borrower"/>: 
<xsl:value-of select="booktitle"/>(<xsl:value-of select="bookclientid"/>);<xsl:text>&#xA0;</xsl:text><xsl:text>&#xA0;</xsl:text>
</xsl:template>


</xsl:stylesheet>