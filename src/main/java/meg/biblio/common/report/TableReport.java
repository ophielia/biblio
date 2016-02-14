package meg.biblio.common.report;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tablereport")
public class TableReport {

	List<TableValue> reportvalues;
	List<String> colheaders;
	String title;
	Integer columncount;
	int valcount;
	
	public TableReport(String title) {
		this.title = title;
		this.valcount=1;
		this.reportvalues = new ArrayList<TableValue>();
		this.colheaders = new ArrayList<String>();
	}

	public TableReport() {
		super();
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
	}
	
	@XmlElement
	public String getTitle() {
		return this.title;
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


	


}
