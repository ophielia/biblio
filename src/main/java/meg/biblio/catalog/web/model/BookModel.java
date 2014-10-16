package meg.biblio.catalog.web.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;




import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.db.dao.SubjectDao;

public class BookModel  implements Serializable {

	private static final long serialVersionUID = 1L;

	private BookDao book;

	private String aFname;
	private String aLname;
	private String aMname;

	private String iFname;
	private String iLname;
	private String iMname;

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
	public String getAFname() {
		return aFname;
	}

	public void setAFname(String aFname) {
		this.aFname = aFname;
	}

	public String getALname() {
		return aLname;
	}

	public void setALname(String aLname) {
		this.aLname = aLname;
	}

	public String getAMname() {
		return aMname;
	}

	public void setAMname(String aMname) {
		this.aMname = aMname;
	}

	public String getIFname() {
		return iFname;
	}

	public void setIFname(String iFname) {
		this.iFname = iFname;
	}

	public String getILname() {
		return iLname;
	}

	public void setILname(String iLname) {
		this.iLname = iLname;
	}

	public String getIMname() {
		return iMname;
	}

	public void setIMname(String iMname) {
		this.iMname = iMname;
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

	public void setLanguage(Long language) {
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
		return book.getPublisher();
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

	public Long getLanguage() {
		return book.getLanguage();
	}

	public Long getType() {
		return book.getType();
	}

	public String getDescription() {
		return book.getDescription();
	}

	public Long getStatus() {
		return book.getStatus();
	}

	public Long getDetailstatus() {
		return book.getDetailstatus();
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
	public void processAuthorEntry() {
		// create ArtistDao
		ArtistDao artist = new ArtistDao();
		
		if (aFname!=null) {
			artist.setFirstname(aFname);
		}
		if (aLname!=null) {
			artist.setLastname(aLname);
		}
		if (aMname!=null) {
			artist.setMiddlename(aMname);
		}
		
		// add to list
		List<ArtistDao> authors = getAuthors();
		if (authors==null) {
			authors = new ArrayList<ArtistDao>();
		}
		authors.add(artist);
		setAuthors(authors);
		
	}

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
	
	public void processIllustratorEntry() {
		// create ArtistDao
		ArtistDao artist = new ArtistDao();
		
		if (iFname!=null) {
			artist.setFirstname(iFname);
		}
		if (iLname!=null) {
			artist.setLastname(iLname);
		}
		if (iMname!=null) {
			artist.setMiddlename(iMname);
		}
		
		// add to list
		List<ArtistDao> illustrators = getIllustrators();
		if (illustrators==null) {
			illustrators = new ArrayList<ArtistDao>();
		}
		illustrators.add(artist);
		setIllustrators(illustrators);
		
	}

}
