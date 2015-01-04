package meg.biblio.lending.web.model;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.lending.db.dao.PersonDao;

public class BarcodeLendModel {

	private BookDao book;
	private PersonDao person;
	private String code;
	
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


}
