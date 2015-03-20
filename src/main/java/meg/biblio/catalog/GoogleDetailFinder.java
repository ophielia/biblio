package meg.biblio.catalog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import meg.biblio.catalog.db.PublisherRepository;
import meg.biblio.catalog.db.SubjectRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.db.dao.SubjectDao;
import meg.biblio.common.AppSettingService;
import meg.biblio.search.SearchService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.Books.Volumes.Get;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volume.VolumeInfo;
import com.google.api.services.books.model.Volume.VolumeInfo.IndustryIdentifiers;
import com.google.api.services.books.model.Volumes;

@Component
public class GoogleDetailFinder extends BaseDetailFinder {

	@Autowired
	AppSettingService settingService;

	@Autowired
	SearchService searchService;

	@Autowired
	PublisherRepository pubRepo;

	/* Get actual class name to be printed on */
	static Logger log = Logger.getLogger(GoogleDetailFinder.class.getName());

	Boolean lookupwithgoogle;
	String apikey;
	String appname;
	Long identifier = 2L;

	protected boolean isEnabled() throws Exception {
		if (lookupwithgoogle == null) {
			lookupwithgoogle = settingService
					.getSettingAsBoolean("biblio.google.turnedon");
		}
		return lookupwithgoogle;
	}

	protected Long getIdentifier() throws Exception {
		return identifier;
	}

	protected FinderObject searchLogic(FinderObject findobj) throws Exception {
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		if (apikey == null) {
			apikey = settingService.getSettingAsString("biblio.google.apikey");
		}
		if (appname == null) {
			appname = settingService.getSettingAsString("biblio.appname");
		}
		final Books books = new Books.Builder(
				GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, null)
				.setApplicationName(appname)
				.setGoogleClientRequestInitializer(
						new BooksRequestInitializer(apikey)).build();

		return searchLogic(findobj, books);
	}

	private FinderObject searchLogic(FinderObject findobj, Books books)
			throws Exception {
boolean isbnsearch = false;
		// find book
		BookDetailDao bookdetail = findobj.getBookdetail();

		if (bookdetail != null) {
			// set up query for title and author
			StringBuffer querybuild = new StringBuffer();

					Long currentstatus = findobj.getSearchStatus()!=null?findobj.getSearchStatus():0L;

		if (bookdetail.hasIsbn()&& currentstatus!=CatalogService.DetailStatus.DETAILNOTFOUNDWISBN) {String isbn = bookdetail.getIsbn10() != null ? bookdetail
						.getIsbn10() : bookdetail.getIsbn13();
				querybuild.append("isbn:");
				querybuild.append(isbn);
				isbnsearch=true;
			} else {
				querybuild.append("intitle:");
				String title = bookdetail.getTitle().toLowerCase();
				title = title.replace(" ", "+");
				querybuild.append(title);
				boolean remainingauthors = false;
				// try with author
				if (bookdetail.getAuthors() != null
						&& bookdetail.getAuthors().size() > 0) {
					ArtistDao author = bookdetail.getAuthors().get(0);
					String authname = author.getLastname();
					if (authname == null) {
						authname = author.getFirstname() != null ? author
								.getFirstname() : author.getMiddlename();
					}
					if (authname != null) {
						authname = authname.toLowerCase().replace(" ", "+");
						querybuild.append("+inauthor:").append(authname);
					}
					remainingauthors = bookdetail.getAuthors().size() > 1;
				} // if no authors available, try with illustrators
				else if (bookdetail.getIllustrators() != null
						&& bookdetail.getIllustrators().size() > 0) {
					ArtistDao author = bookdetail.getIllustrators().get(0);
					String authname = author.getLastname();
					if (authname == null) {
						authname = author.getFirstname() != null ? author
								.getFirstname() : author.getMiddlename();
					}
					if (authname != null) {
						authname = authname.toLowerCase().replace(" ", "+");
						querybuild.append("+inauthor:").append(authname);
					}
					remainingauthors |= bookdetail.getIllustrators().size() > 1;
				}
				// if no authors or illustrators available, try with publisher
				else if (bookdetail.getPublisher() != null) {
					PublisherDao publisher = bookdetail.getPublisher();
					String pubname = publisher.getName();
					if (pubname != null) {
						pubname = pubname.trim().toLowerCase();
						pubname = pubname.toLowerCase().replace(" ", "+");
						pubname = pubname + "+";
						// pubname added as general key
						querybuild.insert(0, pubname);
					}
				}

			}

			String query = querybuild.toString();

			// query google
			Volumes volumes = null;

			volumes = singleQueryGoogle(books, query);

			// process results
			List<FoundDetailsDao> details = null;
			if (volumes.getTotalItems() == 0 || volumes.getItems() == null) {
				// set detailstatus to not found in book
				Long searchstatus = isbnsearch?CatalogService.DetailStatus.DETAILNOTFOUNDWISBN:CatalogService.DetailStatus.DETAILNOTFOUND;
				findobj.setSearchStatus(searchstatus);	
			} else if (volumes.getTotalItems() == 1) {
				// one volume found - get details for this, fill in the book,
				// and save the book
				findobj.setSearchStatus(CatalogService.DetailStatus.DETAILFOUND);
				// do second query to get juicy description
				Volume founddetails = volumes.getItems().get(0);
				String volumeid = founddetails.getId();
				Get detailsrequest = books.volumes().get(volumeid);
				Volume completedetails = detailsrequest.execute();
				copyCompleteDetailsIntoBook(completedetails, bookdetail);
			} else {
				if (bookdetail.getDetailstatus() != CatalogService.DetailStatus.DETAILFOUND) {
					// multiple volumes found. save info for volumes, and set
					// detail status in book to multiple found
					details = copyDetailsIntoFoundRecords(volumes.getItems(),
							bookdetail);
					findobj.setMultiresults(details);
					findobj.setSearchStatus(CatalogService.DetailStatus.MULTIDETAILSFOUND);
				}
			}

			// set book in FinderObject
			findobj.setBookdetail(bookdetail);

		}
		return findobj;

	}

	@Override
	public List<FinderObject> findDetailsForList(List<FinderObject> objects,
			long clientcomplete, Integer batchsearchmax) throws Exception {
		// check enabled
		if (isEnabled()) {
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
			final Books books = new Books.Builder(
					GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
					null)
					.setApplicationName(appname)
					.setGoogleClientRequestInitializer(
							new BooksRequestInitializer(apikey)).build();

			// go through list
			for (FinderObject findobj : objects) {
				// check eligibility for object (eligible and not complete)
				if (isEligible(findobj)
						&& !resultsComplete(findobj, clientcomplete)) {
					// do search
					findobj = searchLogic(findobj, books);
					// log, process search
					findobj.logFinderRun(getIdentifier());
				}
			} // end list loop
		}
		// pass to next in chain, or return
		if (getNext() != null) {
			objects = getNext().findDetailsForList(objects, clientcomplete,
					batchsearchmax);
		}

		return objects;
	}

	private void copyCompleteDetailsIntoBook(Volume volume,
			BookDetailDao bookdetail) {
		VolumeInfo info = volume.getVolumeInfo();

		bookdetail.setTitle(info.getTitle());
		PublisherDao publisher = findPublisherForName(info.getPublisher());
		bookdetail.setDescription(info.getDescription());
		bookdetail.setPublisher(publisher);
		if (info.getPublishedDate() != null) {
			String publishyearstr = info.getPublishedDate();
			// handle full date
			if (publishyearstr.contains("-")) {
				// chop off after dash
				publishyearstr = publishyearstr.substring(0,
						publishyearstr.indexOf("-"));
				bookdetail.setPublishyear(new Long(publishyearstr));
			} else if (publishyearstr.contains("?")) {
				// do nothing - vague year
			} else {
				bookdetail.setPublishyear(new Long(publishyearstr));
			}

		}
		bookdetail.setLanguage(info.getLanguage());

		insertAuthorsIntoBookDetail(info.getAuthors(), bookdetail);

		List<IndustryIdentifiers> isbn = info.getIndustryIdentifiers();
		ListIterator<IndustryIdentifiers> iter = isbn.listIterator();
		while (iter.hasNext()) {
			IndustryIdentifiers ident = iter.next();
			String type = ident.getType() != null ? ident.getType() : "";
			if (type.endsWith("_10")) {
				bookdetail.setIsbn10(ident.getIdentifier());
			} else if (type.endsWith("_13")) {
				bookdetail.setIsbn13(ident.getIdentifier());
			}
		}
		// file categories into subjects - if containing fiction, assign type
		List<String> categories = info.getCategories();
		if (categories != null) {
			List<String> subjects = new ArrayList<String>();
			for (String category : categories) {
				// clean it up
				if (category.toLowerCase().contains("nonfiction")) {
					bookdetail
							.setListedtype(CatalogService.BookType.NONFICTION);
				} else if (category.toLowerCase().contains("fiction")) {
					bookdetail.setListedtype(CatalogService.BookType.FICTION);

				}
				
				subjects.add(category);
			}
			if (subjects.size()>0) {
				insertSubjectsIntoBookDetail(subjects, bookdetail);
			}
		}
		
		// add image link
		String imagelink = info.getImageLinks() != null ? info.getImageLinks()
				.getThumbnail() : null;
		bookdetail.setImagelink(imagelink);

	}

	private PublisherDao findPublisherForName(String text) {
		if (text != null) {
			// clean up text
			text = text.trim();
			// query db
			List<PublisherDao> foundlist = pubRepo.findPublisherByName(text
					.toLowerCase());
			if (foundlist != null && foundlist.size() > 0) {
				return foundlist.get(0);
			} else {
				// if nothing found, make new PublisherDao
				PublisherDao pub = new PublisherDao();
				pub.setName(text);
				return pub;
			}
		}
		return null;
	}

	private Volumes singleQueryGoogle(Books books, String query)
			throws IOException {
		// do search
		com.google.api.services.books.Books.Volumes.List volumesList = books
				.volumes().list(query);
		// Execute the query.
		Volumes volumes = volumesList.execute();
		return volumes;
	}

	private List<FoundDetailsDao> copyDetailsIntoFoundRecords(
			List<Volume> items, BookDetailDao bookdetail) {
		Integer maxdetails = settingService
				.getSettingAsInteger("biblio.google.maxdetails");

		List<FoundDetailsDao> details = new ArrayList<FoundDetailsDao>();
		int processedcnt = 0;
		for (Volume found : items) {
			FoundDetailsDao detail = new FoundDetailsDao();
			VolumeInfo info = found.getVolumeInfo();

			if (info == null) {
				continue;
			}
			if (processedcnt >= maxdetails.intValue()) {
				break;
			}
			detail.setBookdetailid(bookdetail.getId());
			detail.setSearchserviceid(found.getId());
			detail.setTitle(info.getTitle());
			detail.setPublisher(info.getPublisher());
			if (info.getPublishedDate() != null) {
				String publishyearstr = info.getPublishedDate();
				// handle full date
				if (publishyearstr.contains("-")) {
					// chop off after dash
					publishyearstr = publishyearstr.substring(0,
							publishyearstr.indexOf("-"));
					detail.setPublishyear(new Long(publishyearstr));
				} else if (publishyearstr.contains("?")) {
					// do nothing - vague year
				} else {
					detail.setPublishyear(new Long(publishyearstr));
				}

			}
			detail.setLanguage(info.getLanguage());
			detail.setDescription(info.getDescription());
			String imagelink = info.getImageLinks() != null ? info
					.getImageLinks().getThumbnail() : null;
			detail.setImagelink(imagelink);
			StringBuilder authors = new StringBuilder();
			if (info.getAuthors() != null) {
				for (String author : info.getAuthors()) {
					authors.append(author).append(",");
				}
			}
			if (authors.length() > 1) {
				authors.setLength(authors.length() - 1);
			}
			detail.setAuthors(authors.toString());
			List<IndustryIdentifiers> isbn = info.getIndustryIdentifiers();
			if (isbn != null) {
				ListIterator<IndustryIdentifiers> iter = isbn.listIterator();
				while (iter.hasNext()) {
					IndustryIdentifiers ident = iter.next();
					String type = ident.getType() != null ? ident.getType()
							: "";
					if (type.endsWith("_10")) {
						detail.setIsbn10(ident.getIdentifier());
					} else if (type.endsWith("_13")) {
						detail.setIsbn13(ident.getIdentifier());
					}
				}
			}
			details.add(detail);
			processedcnt++;
		}

		return details;
	}



}
