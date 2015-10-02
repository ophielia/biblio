package meg.biblio.common.report;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import meg.biblio.lending.db.dao.LoanRecordDisplay;

@XmlRootElement(name = "overduereport")
public class OverdueBookReport {

	private List<LoanRecordDisplay> booklist;
	private String clientname;
	private Date rundate;

	public OverdueBookReport() {
		super();
	}

	@XmlElement
	public Date getRundate() {
		return rundate;
	}

	public void setRundate(Date date) {
		this.rundate = date;
	}

	@XmlElement
	public String getClientname() {
		return clientname;
	}

	public void setClientname(String name) {
		this.clientname = name;
	}

	@XmlElement(name="book")
	public List<LoanRecordDisplay> getBooklist() {
		return booklist;
	}

	public void setBooklist(List<LoanRecordDisplay> overdue) {
		this.booklist = overdue;
	}

}
