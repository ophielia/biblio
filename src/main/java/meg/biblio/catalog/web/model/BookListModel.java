package meg.biblio.catalog.web.model;

import java.io.Serializable;
import java.util.List;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.search.BookSearchCriteria;

public class BookListModel implements Serializable {

	private static final long serialVersionUID = 1L;

	private BookSearchCriteria criteria;
	private List<BookDao> books;

	public BookListModel(BookSearchCriteria criteria) {
		super();
		this.criteria = criteria;
	}

	public BookSearchCriteria getCriteria() {
		return criteria;
	}

	public void setCriteria(BookSearchCriteria criteria) {
		this.criteria = criteria;
	}

	public List<BookDao> getBooks() {
		return books;
	}

	public void setBooks(List<BookDao> books) {
		this.books = books;
	}

	/** Setters on criteria object **/
	public String getKeyword() {
		return criteria.getKeyword();
	}

	public void setKeyword(String keyword) {
		criteria.setKeyword(keyword);
	}

	public String getAuthor() {
		return criteria.getAuthor();
	}

	public void setAuthor(String author) {
		 criteria.setAuthor( author);
	}

	public String getIllustrator() {
		return criteria.getIllustrator();
	}

	public void setIllustrator(String illustrator) {
		criteria.setIllustrator(illustrator);
	}

	public String getTitle() {
		return criteria.getTitle();
	}

	public void setTitle(String title) {
		criteria.setTitle(title);
	}

	public String getShelfclasskey() {
		return criteria.getShelfclasskey();
	}

	public void setShelfclasskey(String shelfclasskey) {
		criteria.setShelfclasskey(shelfclasskey);
	}

	public String getPublisher() {
		return criteria.getPublisher();
	}

	public void setPublisher(String publisherentry) {
		criteria.setPublisher(publisherentry);
	}

	public long getOrderby() {
		return criteria.getOrderby();
	}

	public void setOrderby(long orderby) {
		criteria.setOrderby(orderby);
	}

	public long getOrderbydir() {
		return criteria.getOrderbydir();
	}

	public void setOrderbydir(long orderbydir) {
		criteria.setOrderbydir(orderbydir);
	}

}
