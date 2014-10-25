package meg.biblio.catalog.web.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;








import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.db.dao.SubjectDao;

public class BookModel  implements Serializable {

	private static final long serialVersionUID = 1L;

	private BookDao book;


	private String authorname;
	private String illustratorname;
	private Long assignDetailId;

	private HashMap<Long, String> booktypedisps;

	private HashMap<Long, String> bookstatusdisps;

	private HashMap<Long, String> detailstatusdisps;
	
	// *** constructors ***//
	public BookModel(BookDao book) {
		super();
		this.book = book;
	}
	
	public BookModel() {
		super();
		this.book = new BookDao();
	}
	
	// *** getters and setters for book object ***//
	public BookDao getBook() {
		return book;
	}

	public void setBook(BookDao book) {
		this.book = book;
	}

	// *** getters and setters for web entry ***//
	public Long getAssignDetailId() {
		return assignDetailId;
	}

	public void setAssignDetailId(Long assignDetailId) {
		this.assignDetailId = assignDetailId;
	}

	
	public String getAuthorname() {
		return authorname;
	}

	public void setAuthorname(String aFname) {
		this.authorname = aFname;
	}

	public String getIllustratorname() {
		return illustratorname;
	}

	public void setIllustratorname(String aLname) {
		this.illustratorname = aLname;
	}



	// *** getters and setters for book object ***//
	public void setClientid(Long clientid) {
		this.book.setClientid(clientid);
	}

	public void setTitle(String title) {
		this.book.setTitle(title);
	}

	public void setAuthors(List<ArtistDao> authors) {
		this.book.setAuthors(authors);
	}

	public void setIllustrators(List<ArtistDao> illustrators) {
		this.book.setIllustrators(illustrators);
	}

	public void setSubjects(List<SubjectDao> subjects) {
		this.book.setSubjects(subjects);
	}

	public void setPublisherkey(PublisherDao publisherkey) {
		this.book.setPublisher(publisherkey);
	}

	public void setPublishyear(Long publishyear) {
		this.book.setPublishyear(publishyear);
	}

	public void setIsbn10(String isbn10) {
		this.book.setIsbn10(isbn10);
	}

	public void setIsbn13(String isbn13) {
		this.book.setIsbn13(isbn13);
	}

	public void setLanguage(String language) {
		this.book.setLanguage(language);
	}

	public void setType(Long type) {
		this.book.setType(type);
	}

	public void setDescription(String description) {
		this.book.setDescription(description);
	}

	public void setStatus(Long status) {
		this.book.setStatus(status);
	}

	public void setDetailstatus(Long detailstatus) {
		this.book.setDetailstatus(detailstatus);
	}

	public void setShelfclass(Long shelfclass) {
		this.book.setShelfclass(shelfclass);
	}

	public void setShelfclassverified(Boolean shelfclassverified) {
		this.book.setShelfclassverified(shelfclassverified);
	}

	public void setCreatedon(Date createdon) {
		this.book.setCreatedon(createdon);
	}

	public void setClientbookid(String clientbookid) {
		this.book.setClientbookid(clientbookid);
	}

	public Long getBookid() {
		return book.getId();
	}

	public Long getClientid() {
		return book.getClientid();
	}

	public String getTitle() {
		return book.getTitle();
	}

	public List<ArtistDao> getAuthors() {
		return book.getAuthors();
	}

	public List<ArtistDao> getIllustrators() {
		return book.getIllustrators();
	}

	public List<SubjectDao> getSubjects() {
		return book.getSubjects();
	}

	public PublisherDao getPublisher() {
		if (book.getPublisher()!=null) {
			return book.getPublisher();
		}
		return new PublisherDao();
	}

	public Long getPublishyear() {
		return book.getPublishyear();
	}

	public String getIsbn10() {
		return book.getIsbn10();
	}

	public String getIsbn13() {
		return book.getIsbn13();
	}

	public String getLanguage() {
		return book.getLanguage();
	}

	public Long getType() {
		return book.getType();
	}
	
	public String getBooktypeDisp() {
		if (book.getType()!=null && booktypedisps !=null &&booktypedisps.containsKey(book.getType())) {
			return booktypedisps.get(book.getType());
		}
		return book.getType()+"";
	}	

	public String getDescription() {
		return book.getDescription();
	}

	public Long getStatus() {
		return book.getStatus();
	}

	public String getStatusDisp() {
		if (book.getStatus()!=null && bookstatusdisps!=null && bookstatusdisps.containsKey(book.getStatus())) {
			return bookstatusdisps.get(book.getStatus());
		}
		return book.getStatus()+"";
	}
	
	public Long getDetailstatus() {
		return book.getDetailstatus();
	}

	public String getDetailstatusDisp() {
		if (book.getDetailstatus()!=null && detailstatusdisps!=null && detailstatusdisps.containsKey(book.getDetailstatus())) {
			return detailstatusdisps.get(book.getDetailstatus());
		}
		return book.getDetailstatus() + "";
	}
		
	public Long getShelfclass() {
		return book.getShelfclass();
	}

	public Boolean getShelfclassverified() {
		return book.getShelfclassverified();
	}

	public Date getCreatedon() {
		return book.getCreatedon();
	}

	public String getClientbookid() {
		return book.getClientbookid();
	}

	
	//***** convenience methods ****//


	public ArtistDao getMainAuthor() {
		if (getAuthors()!=null && getAuthors().size()>0) {
			ArtistDao author = getAuthors().get(0);
			return author;
		}
		return new ArtistDao();
	}
	
	public ArtistDao getMainIllustrator() {
		if (getIllustrators()!=null && getIllustrators().size()>0) {
			ArtistDao illus = getIllustrators().get(0);
			return illus;
		}
		return new ArtistDao();
	}

	public void addAuthorToBook(ArtistDao author) {
		if (author != null) {
			List<ArtistDao> authors = book.getAuthors();
			if (authors == null) {
				authors = new ArrayList<ArtistDao>();
			}
			authors.add(author);
			book.setAuthors(authors);
		}
	}

	public void addIllustratorToBook(ArtistDao illustrator) {
		if (illustrator != null) {
			List<ArtistDao> illustrators = book.getAuthors();
			if (illustrators == null) {
				illustrators = new ArrayList<ArtistDao>();
			}
			illustrators.add(illustrator);
			book.setIllustrators(illustrators);
		}
	}

	public void setDisplayInfo(HashMap<Long, String> booktypedisps,
			HashMap<Long, String> bookstatusdisps,
			HashMap<Long, String> detailstatusdisps) {
		this.booktypedisps = booktypedisps;
		this.bookstatusdisps = bookstatusdisps;
		this.detailstatusdisps = detailstatusdisps;
		
	}

}
