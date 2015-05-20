package meg.biblio.catalog.web.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.db.dao.SubjectDao;

public class BookModel  implements Serializable {

	private static final long serialVersionUID = 1L;

	private BookDao book;


	private String authorname;
	private String illustratorname;
	private String publishername;
	private Long assignDetailId;

	private String isbnentry;
	private Boolean createnewid;
	private String editMode;
	private String assignedcode;
	private Boolean showbarcodelinks;
	List<FoundDetailsDao> founddetails;
	
	private String authorentry;
	private String illustratorentry;
	private String subjectentry;

	private Long previousshelfcode;

	
	// *** constructors ***//
	public BookModel(BookDao book) {
		super();
		if (book.getBookdetail()==null) {
			BookDetailDao bookdetail = new BookDetailDao();
			book.setBookdetail(bookdetail);
		}
		this.book = book;
	}
	
	public BookModel() {
		super();
		BookDao book = new BookDao();
		BookDetailDao bookdetail = new BookDetailDao();
		book.setBookdetail(bookdetail);
		this.book = book;
	}
	
	// *** getters and setters for book object ***//
	public BookDao getBook() {
		return book;
	}

	public void setBook(BookDao book) {
		this.book = book;
	}


	public void setBookdetail(BookDetailDao bookdetail) {
		this.book.setBookdetail(bookdetail);
		
	}
	
	
	public List<FoundDetailsDao> getFounddetails() {
		return founddetails;
	}
	
	



	// *** getters and setters for web entry ***//
	public Long getAssignDetailId() {
		return assignDetailId;
	}

	public void setAssignDetailId(Long assignDetailId) {
		this.assignDetailId = assignDetailId;
	}

	
	public String getAuthorentry() {
		return authorentry;
	}

	public void setAuthorentry(String authorentry) {
		this.authorentry = authorentry;
	}

	public String getIllustratorentry() {
		return illustratorentry;
	}

	public void setIllustratorentry(String illustratorentry) {
		this.illustratorentry = illustratorentry;
	}

	public String getSubjectentry() {
		return subjectentry;
	}

	public void setSubjectentry(String subjectentry) {
		this.subjectentry = subjectentry;
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
		if (publishername==null) {
			BookDetailDao bdetail = this.book.getBookdetail();
			PublisherDao publisher = bdetail.getPublisher();
			if (publisher!=null) {
				return publisher.getName();
			}
		}
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
		if (title!=null) this.book.getBookdetail().setTitle(title);
	}

	public void setAuthors(List<ArtistDao> authors) {
		this.book.getBookdetail().setAuthors(authors);
	}

	public void setIllustrators(List<ArtistDao> illustrators) {
		this.book.getBookdetail().setIllustrators(illustrators);
	}

	public void setSubjects(List<SubjectDao> subjects) {
		this.book.getBookdetail().setSubjects(subjects);
	}

	public void setPublisherkey(PublisherDao publisherkey) {
		if (publisherkey!=null) this.book.getBookdetail().setPublisher(publisherkey);
	}

	public void setPublishyear(Long publishyear) {
		if (publishyear!=null) this.book.getBookdetail().setPublishyear(publishyear);
	}

	public void setIsbn10(String isbn10) {
		this.book.getBookdetail().setIsbn(isbn10);
	}

	public void setIsbn13(String isbn13) {
		this.book.getBookdetail().setIsbn(isbn13);
	}

	public void setLanguage(String language) {
		if (language!=null) this.book.getBookdetail().setLanguage(language);
	}

	public void setType(Long type) {
		if (type!=null) {
			// save type in book, for client
			this.book.setClientbooktype(type);
			if (type==CatalogService.BookType.FICTION ||
					type == CatalogService.BookType.NONFICTION) {
				// fiction or non-fiction - save through to the bookdetail
				this.book.getBookdetail().setListedtype(type);
			}
		}
	}

	public void setDescription(String description) {
		if (description!=null) this.book.getBookdetail().setDescription(description);
	}

	public void setStatus(Long status) {
		if (status!=null) this.book.setStatus(status);
	}

	public void setDetailstatus(Long detailstatus) {
		if (detailstatus!=null) this.book.getBookdetail().setDetailstatus(detailstatus);
	}

	public void setShelfcode(Long shelfclass) {
		if (shelfclass!=null) this.book.setClientshelfcode(shelfclass);
	}

	public void setShelfclass(String shelfclass) {
		if (shelfclass!=null) this.book.setClientshelfclass(shelfclass);
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
		return book.getBookdetail().getTitle();
	}

	public List<ArtistDao> getAuthors() {
		return book.getBookdetail().getAuthors();
	}

	public List<ArtistDao> getIllustrators() {
		return book.getBookdetail().getIllustrators();
	}

	public List<SubjectDao> getSubjects() {
		return book.getBookdetail().getSubjects();
	}

	public PublisherDao getPublisher() {
		if (book.getBookdetail().getPublisher()!=null) {
			return book.getBookdetail().getPublisher();
		}
		return new PublisherDao();
	}

	public Long getPublishyear() {
		return book.getBookdetail().getPublishyear();
	}

	public String getIsbn10() {
		return book.getBookdetail().getIsbn10();
	}

	public String getIsbn13() {
		return book.getBookdetail().getIsbn13();
	}

	public String getLanguage() {
		return book.getBookdetail().getLanguage();
	}

	public Long getType() {
		// check for type in book first (client specific)
		if (book.getClientbooktype()!=null) return book.getClientbooktype();
		// and then in bookdetail
		return book.getBookdetail().getListedtype();
	}

	public String getDescription() {
		return book.getBookdetail().getDescription();
	}

	public Long getStatus() {
		return book.getStatus();
	}


	
	public Long getDetailstatus() {
		return book.getBookdetail().getDetailstatus();
	}

	public String getShelfclass() {
		// returns client shelfclass first, 
		// otherwise, bookdetail shelfclass
		if (book.getClientshelfclass()!=null) {
			return book.getClientshelfclass();
		}
		if (book.getBookdetail()!=null) {
			return book.getBookdetail().getShelfclass();
		}
		return null;
	}

	public Long getShelfcode() {
		return book.getClientshelfcode();
	}

	public Date getCreatedon() {
		return book.getCreatedon();
	}

	public String getClientbookid() {
		return book.getClientbookid();
	}

	
	//***** convenience methods ****//
	public boolean hasIsbn() {
		return this.book.getBookdetail().hasIsbn();
	}
	
	public String getAuthorsforJS() {
		if (getAuthors()!=null && getAuthors().size()>0) {
			StringBuffer jstext = new StringBuffer();
			for (ArtistDao artist:getAuthors()) {
				 if (artist.getDisplayName()==null) {
					 continue;
				 }
				 jstext.append(artist.getDisplayName()).append(";");
			}
			jstext.setLength(jstext.length()-1);
			return jstext.toString();
		}
		return "";
	}
	
	public String getAllAuthorsDisplay() {
		if (getAuthors()!=null && getAuthors().size()>0) {
			StringBuffer jstext = new StringBuffer();
			for (ArtistDao artist:getAuthors()) {
				 if (artist.getDisplayName()==null) {
					 continue;
				 }
				 jstext.append(artist.getDisplayName()).append(", ");
			}
			jstext.setLength(jstext.length()-2);
			return jstext.toString();
		}
		return "";
	}	

	public String getIllustratorsforJS() {
		if (getIllustrators()!=null && getIllustrators().size()>0) {
			StringBuffer jstext = new StringBuffer();
			for (ArtistDao artist:getIllustrators()) {
				 if (artist.getDisplayName()==null) {
					 continue;
				 }
				 jstext.append(artist.getDisplayName()).append(";");
			}
			jstext.setLength(jstext.length()-1);
			return jstext.toString();
		}
		return "";
	}
	
	public String getAllIllustratorsDisplay() {
		if (getIllustrators()!=null && getIllustrators().size()>0) {
			StringBuffer jstext = new StringBuffer();
			for (ArtistDao artist:getIllustrators()) {
				 if (artist.getDisplayName()==null) {
					 continue;
				 }
				 jstext.append(artist.getDisplayName()).append(", ");
			}
			jstext.setLength(jstext.length()-2);
			return jstext.toString();
		}
		return "";
	}
	
	public String getSubjectsforJS() {
		if (getSubjects()!=null && getSubjects().size()>0) {
			StringBuffer jstext = new StringBuffer();
			for (SubjectDao subject:getSubjects()) {
				 if (subject.getListing()==null) {
					 continue;
				 }
				 jstext.append(subject.getListing().trim()).append(";");
			}
			jstext.setLength(jstext.length()-1);
			return jstext.toString();
		}
		return "";
	}
	
	public String getAllSubjectsDisplay() {
		if (getSubjects()!=null && getSubjects().size()>0) {
			StringBuffer jstext = new StringBuffer();
			for (SubjectDao subject:getSubjects()) {
				 if (subject.getListing()==null) {
					 continue;
				 }
				 jstext.append(subject.getListing()).append(", ");
			}
			jstext.setLength(jstext.length()-2);
			return jstext.toString();
		}
		return "";
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

	public void addAuthorToBook(ArtistDao author) {
		if (author != null) {
			List<ArtistDao> authors = book.getBookdetail().getAuthors();
			if (authors == null) {
				authors = new ArrayList<ArtistDao>();
			}
			authors.add(author);
			book.getBookdetail().setAuthors(authors);
		}
	}
	
	public void setAuthorInBook(ArtistDao author) {
		if (author != null) {
			// only set if different
			if (!getMainAuthor().getDisplayName().equals(author.getDisplayName())) {
				List<ArtistDao> authors = new ArrayList<ArtistDao>();
				authors.add(author);
				book.getBookdetail().setAuthors(authors);
			}
		}
	}	

	public void addIllustratorToBook(ArtistDao illustrator) {
		if (illustrator != null) {
			List<ArtistDao> illustrators = book.getBookdetail().getIllustrators();
			if (illustrators == null) {
				illustrators = new ArrayList<ArtistDao>();
			}
			illustrators.add(illustrator);
			book.getBookdetail().setIllustrators(illustrators);
		}
	}
	
	public void setIllustratorInBook(ArtistDao illust) {
		if (!getMainIllustrator().getDisplayName().equals(illust.getDisplayName())) {
			List<ArtistDao> illusts = new ArrayList<ArtistDao>();
			illusts.add(illust);
			book.getBookdetail().setIllustrators(illusts);
		}
	}		

	
	public void setPublisher(String publishername) {
		PublisherDao publisher = new PublisherDao();
		publisher.setName(publishername);
		book.getBookdetail().setPublisher(publisher);
	}

	public String getIsbnentry() {
		return isbnentry;
	}

	public void setIsbnentry(String isbnentry) {
		this.isbnentry = isbnentry;
	}

	public Boolean getCreatenewid() {
		return createnewid!=null?createnewid:new Boolean(false);
	}

	public void setCreatenewid(Boolean createnewid) {
		this.createnewid = createnewid;
	}

	public String getEditMode() {
		return editMode!=null?editMode:"";
	}

	public void setEditMode(String editMode) {
		this.editMode = editMode;
	}

	public String getAssignedcode() {
		return assignedcode;
	}

	public void setAssignedcode(String assignedcode) {
		this.assignedcode = assignedcode;
	}
	
	public void setTrackchange(Boolean trackchange) {
		if (this.book!=null && this.book.getBookdetail()!=null) {
			this.book.getBookdetail().setTrackchange(trackchange);
		}
	}

	public Boolean getShowbarcodelinks() {
		return showbarcodelinks;
	}

	public void setShowbarcodelinks(Boolean showbarcodelinks) {
		this.showbarcodelinks = showbarcodelinks;
	}

	public void setFounddetails(List<FoundDetailsDao> founddetails) {
		this.founddetails = founddetails;
		
	}

	public void setBarcode(String barcodeid) {
		this.book.setBarcodeid(barcodeid);
	}

	public void setPreviousShelfcode(Long shelfcode) {
		previousshelfcode = shelfcode;
	}
	
	public Long getPreviousShelfcode() {
		return previousshelfcode;
	}





}
