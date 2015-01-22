<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
	xmlns:barcode="org.krysalis.barcode4j.xalan.BarcodeExt" xmlns:common="http://exslt.org/common"
	xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="barcode common xalan">

	<xsl:attribute-set name="myBorder">
		<xsl:attribute name="border">solid 0.3mm black</xsl:attribute>
		<xsl:attribute name="padding">1pt</xsl:attribute>
	</xsl:attribute-set>

	<xsl:template match="/">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:layout-master-set>
				<fo:simple-page-master master-name="first"
					page-height="29.7cm" page-width="21.0cm" margin="1.5cm" margin-top="1cm">
					<fo:region-body />
				</fo:simple-page-master>
			</fo:layout-master-set>

			<fo:page-sequence master-reference="first">
				<fo:flow flow-name="xsl-region-body">
					<xsl:apply-templates />
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>

	<xsl:template match="overduereport">
		<fo:block>

				<!-- overdue books -->
					<!-- now, the notices themselves -->
					<xsl:apply-templates select="book" />
					

		</fo:block>
	</xsl:template>

	<xsl:template match="book">
		<fo:block page-break-inside="avoid" space-after="5mm">
			<fo:block text-align="right" margin="1mm">
				<xsl:value-of select="firstname_teacher" />
				's Class
			</fo:block>
			<fo:block margin-bottom="15mm" border-width="1mm" border-bottom-style="dotted" padding-bottom="15mm" space-after="5mm">
				<xsl:variable name="dt" select="checkedout" />
				<fo:block>Hello <xsl:value-of select="firstname_borrower" /><xsl:text>&#xA0;</xsl:text><xsl:value-of select="lastname_borrower" />! &#xA;</fo:block>
				<fo:block space-after="1cm" space-before="1cm">
				Your book,
				<fo:inline font-style="italic">
					<xsl:value-of select="booktitle" />
				</fo:inline>
				is really really late. You checked it out on
				<xsl:value-of select="concat(
                      substring($dt, 9, 2),
                      '/',
                      substring($dt, 6, 2),
                      '/',
                      substring($dt, 1, 4)
                      )" />
				. Please return it as soon as possible.
				</fo:block>

				<fo:block space-after="1cm">
					<fo:table table-layout="fixed">
						<fo:table-column column-width="140mm" />
						<fo:table-column column-width="40mm" />
						<fo:table-body>
							<fo:table-row>
								<fo:table-cell>
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

<xsl:template name="overduetitle">
Overdue Notices
</xsl:template>

	<xsl:template name="overduesummary">
		<fo:block text-align="left">
			Class of:
			<xsl:value-of select="firstname_teacher" />
			<xsl:text>&#xA0;</xsl:text>
			<xsl:value-of select="lastname_teacher" />
		</fo:block>
		<fo:block margin-bottom="5mm" border-width="1mm" white-space-collapse="true"
			border-bottom-style="dotted" padding-bottom="15mm">
			<xsl:for-each select="overduelist">
				<fo:block text-indent="10mm">
					<xsl:value-of select="firstname_borrower" />
					<xsl:text>&#xA0;</xsl:text>
					<xsl:value-of select="lastname_borrower" />
					-
					<xsl:text>&#xA0;</xsl:text>
					<xsl:value-of select="booktitle" />
				</fo:block>
			</xsl:for-each>
		</fo:block>
	</xsl:template>

</xsl:stylesheet>