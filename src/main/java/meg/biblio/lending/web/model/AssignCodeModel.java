package meg.biblio.lending.web.model;

import meg.biblio.catalog.db.dao.BookDao;

public class AssignCodeModel {

	private Long bookid;
	private String newbooknr;
	private String title;
	private String author;
	private String existbooknr;
	private Boolean createnewid;
	private String isbnentry;
	private Long shelfclass;
	private String assignedcode;
private Long status;
private String editmode;	

	private BookDao book;
	
	public BookDao getBook() {
		return book;
	}

	public void setBook(BookDao book) {
		this.book = book;
		if (this.shelfclass==null) {
			this.shelfclass=this.book.getShelfclass();
		}
	}



	public Long getStatus() {
		return status;
	}

	public void setStatus(Long status) {
		this.status = status;
	}

	public Long getBookid() {
		return bookid;
	}

	public void setBookid(Long bookid) {
		this.bookid = bookid;
	}

	public String getNewbooknr() {
		return newbooknr;
	}

	public void setNewbooknr(String newbooknr) {
		this.newbooknr = newbooknr;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getExistbooknr() {
		return existbooknr;
	}

	public void setExistbooknr(String existcbookid) {
		this.existbooknr = existcbookid;
	}

	public Boolean getCreatenewid() {
		return this.createnewid;
	}

	public void setCreatenewid(Boolean createnewid) {
		this.createnewid = createnewid;
	}

	public String getIsbnentry() {
		return isbnentry;
	}

	public void setIsbnentry(String isbnentry) {
		this.isbnentry = isbnentry;
	}

	public Long getShelfclass() {
		return shelfclass;
	}

	public void setShelfclass(Long shelfclass) {
		this.shelfclass = shelfclass;
	}

	public String getAssignedcode() {
		return assignedcode;
	}

	public void setAssignedcode(String assignedcode) {
		this.assignedcode = assignedcode;
	}

	public void setEditMode(String editprefix) {
		this.editmode = editprefix;
	}

	public String getEditMode() {
		return this.editmode;
	}
	
	
	
	
}
