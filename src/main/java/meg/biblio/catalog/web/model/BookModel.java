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


	private String authorname;
	private String illustratorname;
	private String publishername;
	private Long assignDetailId;


	
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

	public String getPublishername() {
		return publishername;
	}

	public void setPublishername(String publishername) {
		this.publishername = publishername;
	}

	// *** getters and setters for book object ***//
	public void setBookid(Long bookid) {
		if (bookid!=null) this.book.setId(bookid);
	}

	
	public void setClientid(Long clientid) {
		if (clientid!=null) this.book.setClientid(clientid);
	}

	public void setTitle(String title) {
		if (title!=null) this.book.setTitle(title);
	}

	public void setAuthors(List<ArtistDao> authors) {
		if (authors!=null) this.book.setAuthors(authors);
	}

	public void setIllustrators(List<ArtistDao> illustrators) {
		if (illustrators!=null) this.book.setIllustrators(illustrators);
	}

	public void setSubjects(List<SubjectDao> subjects) {
		if (subjects!=null) this.book.setSubjects(subjects);
	}

	public void setPublisherkey(PublisherDao publisherkey) {
		if (publisherkey!=null) this.book.setPublisher(publisherkey);
	}

	public void setPublishyear(Long publishyear) {
		if (publishyear!=null) this.book.setPublishyear(publishyear);
	}

	public void setIsbn10(String isbn10) {
		if (isbn10 != null) {
			// remove non numeric characters
			String str = isbn10.replaceAll("[^\\d.X]", "");
			if (str.length() > 10) {
				this.book.setIsbn13(str);
			}
			this.book.setIsbn10(str);
		}
	}

	public void setIsbn13(String isbn13) {
		if (isbn13 != null) {
			// remove non numeric characters
			String str = isbn13.replaceAll("[^\\d.X]", "");
			if (str.length() > 10) {
				this.book.setIsbn13(str);
			}
			this.book.setIsbn10(str);
		}
	}

	public void setLanguage(String language) {
		if (language!=null) this.book.setLanguage(language);
	}

	public void setType(Long type) {
		if (type!=null) this.book.setType(type);
	}

	public void setDescription(String description) {
		if (description!=null) this.book.setDescription(description);
	}

	public void setStatus(Long status) {
		if (status!=null) this.book.setStatus(status);
	}

	public void setDetailstatus(Long detailstatus) {
		if (detailstatus!=null) this.book.setDetailstatus(detailstatus);
	}

	public void setShelfclass(Long shelfclass) {
		if (shelfclass!=null) this.book.setShelfclass(shelfclass);
	}

	public void setShelfclassverified(Boolean shelfclassverified) {
		if (shelfclassverified!=null) this.book.setShelfclassverified(shelfclassverified);
	}

	public void setCreatedon(Date createdon) {
		if (createdon!=null) this.book.setCreatedon(createdon);
	}

	public void setClientbookid(String clientbookid) {
		if (clientbookid!=null) this.book.setClientbookid(clientbookid);
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
	
	public void setAuthorInBook(ArtistDao author) {
		if (author != null) {
			List<ArtistDao> authors = new ArrayList<ArtistDao>();
			authors.add(author);
			book.setAuthors(authors);
		}
	}	

	public void addIllustratorToBook(ArtistDao illustrator) {
		if (illustrator != null) {
			List<ArtistDao> illustrators = book.getIllustrators();
			if (illustrators == null) {
				illustrators = new ArrayList<ArtistDao>();
			}
			illustrators.add(illustrator);
			book.setIllustrators(illustrators);
		}
	}
	
	public void setIllustratorInBook(ArtistDao illust) {
		if (illust != null) {
			List<ArtistDao> illusts = new ArrayList<ArtistDao>();
			illusts.add(illust);
			book.setAuthors(illusts);
		}
	}		
	public void setPublisher(String publishername) {
		PublisherDao publisher = new PublisherDao();
		publisher.setName(publishername);
		book.setPublisher(publisher);
	}



}
