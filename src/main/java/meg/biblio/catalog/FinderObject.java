package meg.biblio.catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;

public class FinderObject {

	private BookDetailDao bookdetail;
	private List<String> allisbns;
	private HashMap<String, FoundDetailsDao> multiresultshash;
	private boolean isnew;
	private boolean isbnadded;
	private Long searchstatus = CatalogService.DetailStatus.NODETAIL;

	public FinderObject(BookDetailDao detail) {
		// determine if this is a firsttime search, or if an old search, if
		// an isbn has been added since the search was first made
		if (detail.getId() == null) {
			isnew = true;
			detail.setDetailstatus(CatalogService.DetailStatus.NODETAIL);
		} else {
			if (!detail.isSearchwisbn() && detail.hasIsbn()) {
				isbnadded = true;
			}
		}
		this.bookdetail = detail;
	}

	public BookDetailDao getBookdetail() {
		return bookdetail;
	}

	public void setBookdetail(BookDetailDao bookdetail) {
		this.bookdetail = bookdetail;
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
				// isbn10
				String key = fd.getIsbn10() != null ? fd.getIsbn10() : fd
						.getIsbn13();
				if (key != null) {
					if (!isbnInHash(fd.getIsbn10(), fd.getIsbn13())) {
						// add to hash
						multiresultshash.put(key, fd);
					}
				}

			}
		}
	}

	private boolean isbnInHash(String isbn10, String isbn13) {
		boolean inhash = false;
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

	public boolean isNew() {
		return isnew;
	}

	public boolean isIsbnadded() {
		return isbnadded;
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
			// set only if existing is NODETAIL
			if (searchstatus.longValue() == CatalogService.DetailStatus.NODETAIL) {
				searchstatus = newstatus;
			}
		} else if (newstatus.longValue() == CatalogService.DetailStatus.DETAILFOUND) {
			// always set
			searchstatus = newstatus;
		}

	}

	public Long getSearchStatus() {
		return searchstatus;
	}

}
