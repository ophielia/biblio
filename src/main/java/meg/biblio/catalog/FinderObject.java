package meg.biblio.catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.db.dao.PublisherDao;

public class FinderObject {

	private BookDetailDao bookdetail;
	private List<String> allisbns;
	private HashMap<String, FoundDetailsDao> multiresultshash;
	private boolean isnew;
	private Long searchstatus = CatalogService.DetailStatus.NODETAIL;
	private Long previousfinders = 1L;
	private List<Long> findersrun;
	private Long tempident;

	public FinderObject(BookDetailDao detail) {
		// determine if this is a firsttime search, or if an old search, if
		// an isbn has been added since the search was first made
		if (detail.getId() == null) {
			isnew = true;
			detail.setDetailstatus(CatalogService.DetailStatus.NODETAIL);
		} else {
			setPreviousfinderCode(detail.getFinderlog());
		}
		this.bookdetail = detail;
	}

	public BookDetailDao getBookdetail() {
		return bookdetail;
	}

	public void setBookdetail(BookDetailDao bookdetail) {
		this.bookdetail = bookdetail;
		if (getSearchStatus().longValue() == CatalogService.DetailStatus.MULTIDETAILSFOUND) {
			// also add bookdetail to found details, if isbn is filled in
			if (bookdetail.hasIsbn()) {
				addToMultiresults(bookdetail);
			}
		}
	}

	public void addToMultiresults(BookDetailDao bd) {
		if (bd != null) {
			if (bd.hasIsbn()) {
				// copy bookdetails into founddetail
				FoundDetailsDao fd = new FoundDetailsDao();
				String title = bd.getTitle();
				String imagelink = bd.getImagelink();
				String isbn10 = bd.getIsbn10();
				String isbn13 = bd.getIsbn13();
				Long publishyear = bd.getPublishyear();
				String language = bd.getLanguage();
				String description = bd.getDescription();
				PublisherDao publisher = bd.getPublisher();
				List<ArtistDao> authorlist = bd.getAuthors();
				List<ArtistDao> illustratorlist = bd.getIllustrators();

				fd.setTitle(title);
				fd.setImagelink(imagelink);
				fd.setIsbn10(isbn10);
				fd.setIsbn13(isbn13);
				fd.setPublishyear(publishyear);
				fd.setLanguage(language);
				fd.setDescription(description);
				fd.setPublisher(publisher.getName());

				if (authorlist != null) {
					StringBuilder authors = new StringBuilder();
					for (ArtistDao author : authorlist) {
						authors.append(author.getDisplayName()).append(",");
					}

					if (authors.length() > 1) {
						authors.setLength(authors.length() - 1);
					}
					fd.setAuthors(authors.toString());
				}

				if (illustratorlist != null) {

					StringBuilder illus = new StringBuilder();
					for (ArtistDao ill : illustratorlist) {
						illus.append(ill.getDisplayName()).append(",");
					}

					if (illus.length() > 1) {
						illus.setLength(illus.length() - 1);
					}
					fd.setIllustrators(illus.toString());
				}

			}
		}

	}

	public void addToMultiresults(FoundDetailsDao fd) {
		// isbn10
		String key = fd.getIsbn10() != null ? fd.getIsbn10() : fd.getIsbn13();
		if (key != null) {
			if (!isbnInHash(fd.getIsbn10(), fd.getIsbn13())) {
				// add to hash
				multiresultshash.put(key, fd);
			}
		}

	}

	public List<FoundDetailsDao> getMultiresults() {
		// return list of all values from multiresults hash
		List<FoundDetailsDao> returnlist = new ArrayList<FoundDetailsDao>();
		returnlist.addAll(multiresultshash.values());
		return returnlist;
	}

	public void setMultiresults(List<FoundDetailsDao> multiresults) {
		// set the found results in hash. Only add if doesn't exist already. No
		// repeats!

		// initialize the hash, if not yet created
		if (multiresultshash == null) {
			multiresultshash = new HashMap<String, FoundDetailsDao>();
		}

		if (multiresults != null) {
			for (FoundDetailsDao fd : multiresults) {
				addToMultiresults(fd);
			}
		}
	}

	public boolean isNew() {
		return isnew;
	}


	public void setSearchStatus(Long newstatus) {
		if (searchstatus.longValue() == newstatus.longValue()) {
			return;
		}
		if (newstatus.longValue() == CatalogService.DetailStatus.DETAILNOTFOUND) {
			// set only if existing is NODETAIL
			if (searchstatus.longValue() == CatalogService.DetailStatus.NODETAIL) {
				searchstatus = newstatus;
			}
		} else if (newstatus.longValue() == CatalogService.DetailStatus.MULTIDETAILSFOUND) {
			searchstatus = newstatus;
			if (searchstatus.longValue() == CatalogService.DetailStatus.DETAILFOUND) {
				// copy current bookdetail into multiresults
				addToMultiresults(bookdetail);
			}	
		} else{
			searchstatus = newstatus;
		}

	}

	public Long getSearchStatus() {
		return searchstatus;
	}

	public Long getPreviousfinderCode() {
		return previousfinders;
	}

	public void setPreviousfinderCode(Long previousfinders) {
		this.previousfinders = previousfinders;
	}

	public Long getCurrentFinderLog() {
		// this is made up of the previous finders, plus any findersrun
		long code = getPreviousfinderCode();
		if (findersrun != null) {
			for (Long finderident : findersrun) {
				long test = finderident.longValue();
				if (code % test != 0) {
					code = code * test;
				}
			}
		}
		return new Long(code);
	}

	public void logFinderRun(Long identifier) {
		if (findersrun == null) {
			findersrun = new ArrayList<Long>();
		}

		if (!findersrun.contains(identifier)) {
			findersrun.add(identifier);
		}
	}

	private boolean isbnInHash(String isbn10, String isbn13) {
		boolean inhash = false;
		if (allisbns==null) {
			 allisbns=new ArrayList<String>();
		}
		if (isbn10 != null) {
			if (allisbns.contains(isbn10)) {
				inhash = true;
			} else {
				allisbns.add(isbn10);
			}

		}
		if (isbn13 != null) {
			if (allisbns.contains(isbn13)) {
				inhash = true;
			} else {
				allisbns.add(isbn13);
			}

		}
		return inhash;
	}

	public void setTempIdent(Long tempident) {
		this.tempident = tempident;
		
	}

	public Long getTempIdent() {
		return tempident;
	}
	
	

}
