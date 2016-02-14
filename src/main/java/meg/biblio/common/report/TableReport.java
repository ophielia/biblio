package meg.biblio.common.report;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tablereport")
public class TableReport {

	List<TableValue> reportvalues;
	List<String> colheaders;
	List<String> colsizes;
	String fontsize;
	String title;
	Integer columncount;
	int valcount;
	
	public TableReport(String title) {
		this.title = title;
		setDefaults();
	}

	public TableReport() {
		super();
		setDefaults();
	}

	private void setDefaults() {
		this.valcount=1;
		this.reportvalues = new ArrayList<TableValue>();
		this.colheaders = new ArrayList<String>();
		this.colsizes = new ArrayList<String>();
		this.fontsize="12pt";
	}
	
	public void addValue(String value) {
		// make ReportValue
		TableValue rv = new TableValue();
		rv.value=value;
		rv.position = valcount;
		// add to list
		reportvalues.add(rv);
		// increment position
		valcount++;
	}
	
	public void addColHeader(String header) {
		colheaders.add(header);
		colsizes.add("");
	}
	
	public void addColHeader(String header, String size) {
		colheaders.add(header);
		colsizes.add(size);
	}
	
	@XmlElement
	public String getTitle() {
		return this.title;
	}
	
	@XmlElement(name="fontsize")
	public String getFontsize() {
		return this.fontsize;
	}
	
	

	public void setFontsize(String fontsize) {
		this.fontsize = fontsize;
	}

	@XmlElement(name="tablevalue")
	public List<TableValue> getTableValue() {
		return reportvalues;
	}

	@XmlElement
	public Integer getColumncount() {
		return colheaders.size();
	}

	@XmlElement
	public List<String> getColheader() {
		return colheaders;
	}
	
	@XmlElement
	public List<String> getColsizes() {
		return colsizes;
	}	


	


}
