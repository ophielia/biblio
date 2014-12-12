package meg.biblio.lending.web.model;

import java.util.Date;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.lending.db.dao.LoanRecordDao;
import meg.biblio.lending.db.dao.PersonDao;

public class LoanRecordDisplay {

	private LoanRecordDao loanrecord;
	private PersonDao person;
	private BookDao book;
	private Long classid;


	public LoanRecordDisplay(LoanRecordDao loanrecord, PersonDao person,
			BookDao book,Long classid) {
		super();
		this.loanrecord = loanrecord;
		this.person = person;
		this.book = book;
		this.classid = classid;
	}


	public Long getLoanrecordid() {
		return this.loanrecord.getId();
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


	public Long getClassid() {
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

	public String getBookclientid() {
		return this.book.getClientbookid();
	}


	public String getAuthor() {
		return this.book.getAuthorsAsString();
	}



	public Long getShelfclass() {
		return this.book.getShelfclass();
	}


	public Date getCheckedout() {
		return this.loanrecord.getCheckoutdate();
	}


	public Date getDuedate() {
		return this.loanrecord.getDuedate();
	}


	public Boolean getIsoverdue() {
		Date today = new Date();
		return today.after(getDuedate());
	}

}
