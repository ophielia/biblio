package meg.biblio.lending.web.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.lending.db.dao.LoanHistoryDao;
import meg.biblio.lending.db.dao.PersonDao;

@XmlRootElement(name = "LoanRecord")
public class LoanHistoryDisplay {

	private LoanHistoryDao loanhistory;
	private PersonDao person;
	private BookDao book;
	private Long classid;


	public LoanHistoryDisplay(LoanHistoryDao loanhistory, PersonDao person,
			BookDao book,Long classid) {
		super();
		this.loanhistory = loanhistory;
		this.person = person;
		this.book = book;
		this.classid = classid;
	}

	public LoanHistoryDisplay() {
		super();
	}
	@XmlTransient
	public Long getLoanrecordid() {
		return this.loanhistory.getId();
	}

	@XmlTransient
	public Long getBorrowerid() {
		return this.person.getId();
	}

	@XmlTransient
	public Long getClientid() {
		return this.person.getClient().getId();
	}

	@XmlTransient
	public Long getBookid() {
		return this.book.getId();
	}
	
	@XmlElement
	public String getBookclientid() {
		return this.book.getClientbookid();
	}	

	@XmlTransient
	public Long getClasssid() {
		return classid;
	}

	@XmlElement(name = "firstname_borrower")
	public String getBorrowerfn() {
		return this.person.getFirstname();
	}

	@XmlElement(name = "lastname_borrower")
	public String getBorrowerln() {
		return this.person.getLastname();
	}

	@XmlElement
	public String getBooktitle() {
		return this.book.getBookdetail().getTitle();
	}

	@XmlElement
	public String getAuthor() {
		return this.book.getBookdetail().getAuthorsAsString();
	}


	@XmlTransient
	public Long getShelfclass() {
		return this.book.getClientshelfcode();
	}

	@XmlElement
	public Date getCheckedout() {
		return this.loanhistory.getCheckedout();
	}

	@XmlElement
	public Date getReturned() {
		return this.loanhistory.getReturned();
	}	

	@XmlElement
	public Date getDuedate() {
		return this.loanhistory.getDuedate();
	}

	@XmlTransient
	public Boolean getIsoverdue() {
		Date returned = getReturned();
		return returned.after(getDuedate());
	}

}
