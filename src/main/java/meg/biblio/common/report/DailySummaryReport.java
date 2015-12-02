package meg.biblio.common.report;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "dailysummaryreport")
public class DailySummaryReport {

	

	private List<ClassSummaryReport> classsummarylist;
	
	public DailySummaryReport() {
		super();
	}

	public DailySummaryReport(List<ClassSummaryReport>  list) {
		super();
		this.classsummarylist = list;
	}
	
	@XmlElement(name="classreport")
	public List<ClassSummaryReport> getClasssummarylist() {
		return classsummarylist;
	}

	public void setClasssummarylist(List<ClassSummaryReport> classsummarylist) {
		this.classsummarylist = classsummarylist;
	}

	public boolean isPrintable() {
		boolean printable=false;
		if (classsummarylist!=null) {
			for (ClassSummaryReport csr:classsummarylist) {
				if ( !csr.isEmpty()) {
					printable=true;
					break;
				}
			}
		}
		return printable;
	}


}
