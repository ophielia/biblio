package meg.biblio.common.report;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import meg.biblio.lending.LendingSearchCriteria;
import meg.biblio.lending.db.dao.LoanRecordDisplay;

@XmlRootElement(name = "lendingreport")
public class LendingReport {

	

	private List<ClassSummaryReport> classsummarylist;
	
	private List<LoanRecordDisplay> loanrecords;
	private LendingSearchCriteria criteria;
	
	public LendingReport() {
		super();
	}

	public LendingReport(List<LoanRecordDisplay>  loanrecords) {
		super();
		this.loanrecords = loanrecords;
	}
	
	@XmlElement(name="loanrecords")
	public List<LoanRecordDisplay> getRecordsList() {
		return loanrecords;
	}

	public void setRecordsList(List<LoanRecordDisplay> loanrecords) {
		this.loanrecords = loanrecords;
	}

	
}
