package meg.biblio.lending.web.model;

import java.util.List;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.lending.db.dao.PersonDao;

public class BarcodeLendModel {

	private BookDao book;
	private PersonDao person;
	private String code;
	private List<LoanRecordDisplay> checkedoutforuser;
	private Boolean multicheckout;

	public BookDao getBook() {
		return book;
	}

	public void setBook(BookDao book) {
		this.book = book;
	}

	public PersonDao getPerson() {
		return person;
	}

	public void setPerson(PersonDao person) {
		this.person = person;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setCheckedoutForUser(List<LoanRecordDisplay> checkedoutforuser) {
		this.checkedoutforuser = checkedoutforuser;

	}

	public List<LoanRecordDisplay> getCheckedoutForUser() {
		return this.checkedoutforuser;

	}

	public boolean matchesPerson(PersonDao person2) {
		if (this.person==null || person2==null) return false;
		return (this.person.getId().longValue()==person2.getId().longValue()); 
		
	}



	public Boolean getMulticheckout() {
		return multicheckout;
	}

	public void setMulticheckout(Boolean multicheckout) {
		this.multicheckout = multicheckout;
	}
	
	

}
