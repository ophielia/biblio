package meg.biblio.lending.web.model;

import java.util.Date;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.lending.db.dao.LoanHistoryDao;
import meg.biblio.lending.db.dao.PersonDao;

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


	public Long getLoanrecordid() {
		return this.loanhistory.getId();
	}


	public Long getBorrowerid() {
		return this.person.getId();
	}


	public Long getClientid() {
		return this.person.getClient().getId();
	}


	public Long getBookid() {
		return this.book.getId();
	}


	public Long getClasssid() {
		return classid;
	}


	public String getBorrowerfn() {
		return this.person.getFirstname();
	}


	public String getBorrowerln() {
		return this.person.getLastname();
	}


	public String getBooktitle() {
		return this.book.getTitle();
	}


	public String getAuthor() {
		return this.book.getAuthorsAsString();
	}



	public Long getShelfclass() {
		return this.book.getShelfclass();
	}


	public Date getCheckedout() {
		return this.loanhistory.getCheckedout();
	}
	
	public Date getReturned() {
		return this.loanhistory.getReturned();
	}	


	public Date getDuedate() {
		return this.loanhistory.getDuedate();
	}


	public Boolean getIsoverdue() {
		Date returned = getReturned();
		return returned.after(getDuedate());
	}

}
