package meg.biblio.catalog;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import meg.biblio.catalog.db.ArtistRepository;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.FoundDetailsRepository;
import meg.biblio.catalog.db.PublisherRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.db.dao.SubjectDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.search.SearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@Transactional
public class CatalogServiceImpl implements CatalogService {

	@Autowired
	BookRepository bookRepo;

	@Autowired
	FoundDetailsRepository foundRepo;

	@Autowired
	ArtistRepository artistRepo;

	@Autowired
	PublisherRepository pubRepo;
	
	@Autowired
	SearchService searchService;
	
	@Value("${biblio.google.apikey}")
	private String apikey;

	@Value("${biblio.appname}")
	private String appname;

	public final class LocationStatus {
		public static final long CHECKEDOUT = 1;
		public static final long SHELVED = 2;
		public static final long LOSTBYLENDER = 3;
		public static final long REMOVEDFROMCIRC = 4;
		public static final long INVNOTFOUND = 5;
		public static final long PROCESSING = 6;
	}

	public static final class BookType {
		public static final long FICTION = 1;
		public static final long NONFICTION = 2;
		public static final long REFERENCE = 3;
		public static final long FOREIGNLANGUAGE = 4;
		public static final long UNKNOWN = 5;
	}

	public static final class DetailStatus {
		public static final long NODETAIL = 1;
		public static final long DETAILNOTFOUND = 2;
		public static final long MULTIDETAILSFOUND = 3;
		public static final long DETAILFOUND = 4;
	}
	
	private static final class NameMatchType {
		public static final long FIRSTINITIAL=1;
		public static final long LASTNAME=2;
	}

	/**
	 * Assumes validated BookModel. Saves a book to the database for the first
	 * time. Usually, only minimal entries are made here - details are filled in
	 * later. Book is entered with corresponding clientkey.
	 */
	@Override
	public BookModel createCatalogEntryFromBookModel(Long clientkey,
			BookModel model) {
		// get book
		BookDao book = model.getBook();

		// add clientkey
		book.setClientid(clientkey);

		// add default entries for status, detail status, type
		book.setStatus(LocationStatus.PROCESSING);
		book.setDetailstatus(DetailStatus.NODETAIL);
		book.setType(BookType.UNKNOWN);
		/*
		 * // save authors List<ArtistDao> authors = new ArrayList<ArtistDao>();
		 * for (ArtistDao author:book.getAuthors()) { ArtistDao newauthor =
		 * artistRepo.save(author); authors.add(newauthor); }
		 * book.setAuthors(authors);
		 */

		// persist book
		BookDao saved = bookRepo.save(book);
		Long bookid = saved.getId();

		// reload book in bookmodel
		BookModel result = loadBookModel(bookid);

		// return bookmodel
		return result;
	}

	@Override
	public BookModel loadBookModel(Long id) {
		// get book from repo
		BookDao book = bookRepo.findOne(id);

		if (book != null) {
			// get authors, subjects and illustrators
			List<ArtistDao> authors = book.getAuthors();
			book.setAuthors(authors);
			List<ArtistDao> illustrators = book.getIllustrators();
			book.setIllustrators(illustrators);
			List<SubjectDao> subjects = book.getSubjects();
			book.setSubjects(subjects);

			// set book in model
			BookModel model = new BookModel(book);

			// return model
			return model;

		}
		// return model
		return new BookModel();
	}

	public void fillInDetailsForSingleBook(Long id)
			throws GeneralSecurityException, IOException {
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

		// find book
		BookDao book = bookRepo.findOne(id);

		if (book != null) {
			// set up query for title and author
			StringBuffer querybuild = new StringBuffer();
			querybuild.append("intitle:");
			String title = book.getTitle().toLowerCase();
			title = title.replace(" ", "+");
			querybuild.append(title);
			if (book.getAuthors() != null && book.getAuthors().size() > 0) {
				ArtistDao author = book.getAuthors().get(0);
				String authname = author.getLastname();
				if (authname == null) {
					authname = author.getFirstname() != null ? author
							.getFirstname() : author.getMiddlename();
				}
				if (authname != null) {
					authname = authname.toLowerCase().replace(" ", "+");
					querybuild.append("+inauthor:").append(authname);
				}
			}
			String query = querybuild.toString();

			// query google
			Volumes volumes = null;
			final Books books = new Books.Builder(
					GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
					null)
					.setApplicationName(appname)
					.setGoogleClientRequestInitializer(
							new BooksRequestInitializer(apikey)).build();

			volumes = singleQueryGoogle(books, query);

			// can run query with other params if nothing is found - with
			// publisher, or with illustrator

			// process results
			List<FoundDetailsDao> details = null;
			if (volumes.getTotalItems() == 0 || volumes.getItems() == null) {
				// set detailstatus to not found in book
				book.setDetailstatus(DetailStatus.DETAILNOTFOUND);
			} else if (volumes.getTotalItems() == 1) {
				// one volume found - get details for this, fill in the book,
				// and save the book
				book.setDetailstatus(DetailStatus.DETAILFOUND);
				// do second query to get juicy description
				Volume founddetails = volumes.getItems().get(0);
				String volumeid = founddetails.getId();
				Get detailsrequest = books.volumes().get(volumeid);
				Volume completedetails = detailsrequest.execute();
				copyCompleteDetailsIntoBook(completedetails, book);
			} else {
				// multiple volumes found. save info for volumes, and set
				// detail status in book to multiple found
				details = copyDetailsIntoFoundRecords(volumes.getItems(), book);
				book.setDetailstatus(DetailStatus.MULTIDETAILSFOUND);

			}

			// save book
			bookRepo.save(book);

			// save founddetails if available
			foundRepo.save(details);

		}

	}

	public ArtistDao textToArtistName(String text) {
		ArtistDao name = new ArtistDao();
		if (text != null) {
			if (text.contains(",")) {
				// break text by comma
				String[] tokens = text.trim().split(",");
				List<String> tknlist = arrayToList(tokens);
				// first member goes to last name
				String lastname = tknlist.remove(0);
				name.setLastname(lastname);
				if (tknlist.size() > 0) {
					// break remaining by space
					String remaining = tknlist.get(0);
					tokens = remaining.trim().split(" ");
					tknlist = arrayToList(tokens);
					// first member goes to first name
					String firstname = tknlist.remove(0);
					name.setFirstname(firstname);
					// any remaining members go to middle name
					if (tknlist.size() > 0) {
						String middlename = tknlist.remove(0);
						name.setMiddlename(middlename);
					}
				}
			} else {
				// break name into list
				String[] tokens = text.trim().split(" ");
				List<String> tknlist = arrayToList(tokens);
				// last member of list is last name
				String lastname = tknlist.remove(tknlist.size() - 1);
				name.setLastname(lastname);
				// if members remaining, first member is firstname
				if (tknlist.size() > 0) {
					String firstname = tknlist.remove(0);
					name.setFirstname(firstname);
					if (tknlist.size() > 0) {
						// all remaining go to middlename
						String middlename = tknlist.remove(0);
						name.setMiddlename(middlename);
					}
				}
			}
			// return name
			return name;
		}
		// return name
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

	private void copyCompleteDetailsIntoBook(Volume volume, BookDao book) {
		VolumeInfo info = volume.getVolumeInfo();

		book.setTitle(info.getTitle());
		PublisherDao publisher = findPublisherForName(info.getPublisher());
		book.setDescription(info.getDescription());
		book.setPublisher(publisher);
		Long publishyear = info.getPublishedDate() != null ? new Long(
				info.getPublishedDate()) : null;
		book.setPublishyear(publishyear);
		book.setLanguage(info.getLanguage());

		copyAuthorsIntoBook(book, info.getAuthors());

		List<IndustryIdentifiers> isbn = info.getIndustryIdentifiers();
		ListIterator<IndustryIdentifiers> iter = isbn.listIterator();
		while (iter.hasNext()) {
			IndustryIdentifiers ident = iter.next();
			String type = ident.getType() != null ? ident.getType() : "";
			if (type.endsWith(" 10")) {
				book.setIsbn10(ident.getIdentifier());
			} else if (type.endsWith(" 13")) {
				book.setIsbn13(ident.getIdentifier());
			}
		}
		// file categories into subjects - if containing fiction, assign type
		List<String> categories = info.getCategories();
		List<SubjectDao> subjects = new ArrayList<SubjectDao>();
		for (String category : categories) {
			// clean it up
			if (category.toLowerCase().contains("nonfiction")) {
				book.setType(BookType.NONFICTION);
				SubjectDao subject = findSubjectForString(category);
				subjects.add(subject);
			} else if (category.toLowerCase().contains("fiction")) {
				book.setType(BookType.FICTION);
				SubjectDao subject = findSubjectForString(category);
				subjects.add(subject);
			} else {
				SubjectDao subject = new SubjectDao();
				subject.setListing(category);
				subjects.add(subject);
			}
		}

	}

	private List<FoundDetailsDao> copyDetailsIntoFoundRecords(
			List<Volume> items, BookDao book) {
		List<FoundDetailsDao> details = new ArrayList<FoundDetailsDao>();
		for (Volume found : items) {
			FoundDetailsDao detail = new FoundDetailsDao();
			VolumeInfo info = found.getVolumeInfo();
			detail.setBookid(book.getId());
			detail.setTitle(info.getTitle());
			detail.setPublisher(info.getPublisher());
			Long publishyear = info.getPublishedDate() != null ? new Long(
					info.getPublishedDate()) : null;
			detail.setPublishyear(publishyear);
			detail.setLanguage(info.getLanguage());
			detail.setDescription(info.getDescription());
			String imagelink = info.getImageLinks() != null ? info
					.getImageLinks().getThumbnail() : null;
			detail.setImagelink(imagelink);
			StringBuilder authors = new StringBuilder();
			for (String author : info.getAuthors()) {
				authors.append(author).append(",");
			}
			if (authors.length() > 1) {
				authors.setLength(authors.length() - 1);
			}
			detail.setAuthors(authors.toString());
			List<IndustryIdentifiers> isbn = info.getIndustryIdentifiers();
			ListIterator<IndustryIdentifiers> iter = isbn.listIterator();
			while (iter.hasNext()) {
				IndustryIdentifiers ident = iter.next();
				String type = ident.getType() != null ? ident.getType() : "";
				if (type.endsWith(" 10")) {
					detail.setIsbn10(ident.getIdentifier());
				} else if (type.endsWith(" 13")) {
					detail.setIsbn13(ident.getIdentifier());
				}
			}
			details.add(detail);
		}
	
		return details;
	}

	public BookDao copyAuthorsIntoBook(BookDao book, List<String> foundauthors) {
		if (foundauthors != null && book != null) {
			HashMap<Long, ArtistDao> bookauthors = new HashMap<Long, ArtistDao>();
			HashMap<Long, ArtistDao> bookillustrators = new HashMap<Long, ArtistDao>();
			if (book.getAuthors() != null) {
				for (ArtistDao author : book.getAuthors()) {
					bookauthors.put(author.getId(), author);
				}
			}
			if (book.getIllustrators() != null) {
				for (ArtistDao illust : book.getIllustrators()) {
					bookillustrators.put(illust.getId(), illust);
				}
			}
			List<ArtistDao> newauthors = new ArrayList<ArtistDao>();
			List<ArtistDao> newillustrators = new ArrayList<ArtistDao>();
	
			// go through all found authors
			for (String found : foundauthors) {
				boolean matchfound = false;
				Long bookartistid = null;
				boolean fromillus = false;
	
				// get name (ArtistDao) for authortext
				ArtistDao foundauthor = textToArtistName(found);
				// go through book authors to find match (by last name)
				if (book.getAuthors() != null) {
					for (ArtistDao bookauth : book.getAuthors()) {
						// check if lastnames match
						if (namesMatch(NameMatchType.LASTNAME, bookauth,
								foundauthor)) {
							// if last names match, check that first initials
							// match
							if (namesMatch(NameMatchType.FIRSTINITIAL,
									bookauth, foundauthor)) {
								// first initial and lastname equals enough to
								// match
								matchfound = true;
								bookartistid = bookauth.getId();
								fromillus = false;
							} else if (!bookauth.hasFirstname()
									|| !foundauthor.hasFirstname()) {
								// either book or found doesn't have first name
								// - will still
								// match
								matchfound = true;
								bookartistid = bookauth.getId();
								fromillus = false;
							}
						}
					}
				}
				if (book.getIllustrators() != null) {
					if (!matchfound) {
						for (ArtistDao bookauth : book.getIllustrators()) {
							// check if lastnames match
							if (namesMatch(NameMatchType.LASTNAME, bookauth,
									foundauthor)) {
								// if last names match, check that first
								// initials match
								if (namesMatch(NameMatchType.FIRSTINITIAL,
										bookauth, foundauthor)) {
									// first initial and lastname equals enough
									// to match
									matchfound = true;
									bookartistid = bookauth.getId();
									fromillus = true;
								} else if (!bookauth.hasFirstname()
										|| !foundauthor.hasFirstname()) {
									// either book or found doesn't have first
									// name - will still
									// match
									matchfound = true;
									bookartistid = bookauth.getId();
									fromillus = true;
								}
							}
						}
					} 
				}
	
					// if matchfound - determine if more complete
					if (matchfound) {
						
						List<ArtistDao> targetlist = fromillus ? newillustrators
								: newauthors;
						HashMap<Long, ArtistDao> targethash = fromillus ? bookillustrators
								: bookauthors;
						ArtistDao existauth = targethash.get(bookartistid); 
						boolean existmorecomplete = (existauth.hasFirstname() && !foundauthor
								.hasFirstname());
						existmorecomplete |= (existauth.hasMiddlename() && !foundauthor
								.hasMiddlename());
						existmorecomplete |= (existauth.hasFirstname()
								&& !foundauthor.hasFirstname() && foundauthor.getFirstname().length() < existauth
								.getFirstname().length());
	
						// if more complete, set in new list (author or
						// illustrator)
						if (!existmorecomplete) {
							// get new found name from db
							ArtistDao dbfound = searchService
									.findArtistMatchingName(foundauthor);
							// if found in db, set db artist in list
							if (dbfound != null) {
								targetlist.add(dbfound);
							} else {
								// if not found in db, set new foundauth in list
								targetlist.add(foundauthor);
							}
						} else {
							// match made, but book (existing) more complete -
							// discard found
							// value. Add existingauth to targetlist
							targetlist.add(existauth);
	
						}
	
						// remove old bookvalue from hash
						targethash.remove(bookartistid);
					} else {
						// if no match found
						// add found to author list as is
						newauthors.add(foundauthor);
					}
			}  // end loop through foundauthors
	
			// add any remaining non-matched authors or illustrators to list
			if (bookauthors.size()>0) {
				for (Long id:bookauthors.keySet()) {
					ArtistDao oldadd = bookauthors.get(id);
					newauthors.add(oldadd);
				}
			}
			if (bookillustrators.size()>0) {
				for (Long id:bookillustrators.keySet()) {
					ArtistDao oldadd = bookillustrators.get(id);
					newillustrators.add(oldadd);
				}
			}
			// if authorlist has members, set in book
			if (newauthors.size() > 0) {
				book.setAuthors(newauthors);
			}
			// if illustratorlist has members, set in book
			if (newillustrators.size() > 0) {
				book.setIllustrators(newillustrators);
			}
		}
		return book;
	}

	private SubjectDao findSubjectForString(String text) {
		// TODO Auto-generated method stub
		return null;
	}





	public PublisherDao findPublisherForName(String text) {
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

	private boolean namesMatch(long matchtype, ArtistDao bookauth,
			ArtistDao foundauthor) {
		String bookval=null;
		String compareval = null;
		
		if (matchtype==NameMatchType.LASTNAME) {
			bookval=bookauth.getLastname()!=null?bookauth.getLastname():"";
			compareval=foundauthor.getLastname()!=null?foundauthor.getLastname():"";
		} else if (matchtype==NameMatchType.FIRSTINITIAL) {
			bookval=bookauth.getFirstname()!=null?bookauth.getFirstname():"";
			compareval=foundauthor.getFirstname()!=null?foundauthor.getFirstname():"";
		}
		bookval=bookval.trim().toLowerCase();
		compareval=compareval.trim().toLowerCase();
		
		// make comparison
		if (matchtype==NameMatchType.LASTNAME) {
			return (bookval.equals(compareval));
		} else if (matchtype==NameMatchType.FIRSTINITIAL) {
			bookval=bookval.length()>0?bookval.substring(0,1):"";
			compareval=compareval.length()>0?compareval.substring(0,1):"";
			return (bookval.equals(compareval));
		}
		return false;
	}



	private List<String> arrayToList(String[] tokens) {
		List<String> list = new ArrayList<String>();
		if (tokens != null) {
			for (int i = 0; i < tokens.length; i++) {
				list.add(tokens[i]);
			}
		}
		return list;
	}

	/**
	 * Convenience method for testing/devpt. Final will be done through
	 * SearchService.
	 */
	@Override
	public List<BookDao> getAllBooks() {
		return bookRepo.findAll();
	}

}
	
	
	/*
	 * 
	 * 	public BookDao copyAuthorsIntoBook(BookDao book, List<String> foundauthors) {
		if (foundauthors != null && book != null) {
			HashMap<Long,ArtistDao> bookauthors = new HashMap<Long, ArtistDao>();
			HashMap<Long,ArtistDao> bookillustrators = new HashMap<Long, ArtistDao>();
			if (book.getAuthors()!=null) {
				for (ArtistDao author:book.getAuthors()) {
					bookauthors.put(author.getId(), author);
				}
			}
			if (book.getIllustrators()!=null) {
				for (ArtistDao illust:book.getIllustrators()) {
					bookillustrators.put(illust.getId(), illust);
				}
			}
			List<ArtistDao> newauthors = new ArrayList<ArtistDao>();
			List<ArtistDao> newillustrators = new ArrayList<ArtistDao>();
			
			// go through all found authors
			for (String found : foundauthors) {
				boolean matchfound = false;

				// get name (ArtistDao) for authortext
				ArtistDao foundauthor = textToName(found);

				// go through book authors to find match (by last name)
				if (book.getAuthors() != null) {
					for (ArtistDao existauth : book.getAuthors()) {
						if (existauth.hasLastname()
								&& foundauthor.hasLastname()) {
							if (existauth
									.getLastname()
									.trim()
									.toLowerCase()
									.equals(foundauthor.getLastname().trim()
											.toLowerCase())) {
								// ensure that first initials of first match
								if (existauth.hasFirstname()
										&& foundauthor.hasFirstname()) {
									if (existauth
											.getFirstname()
											.trim().toLowerCase().startsWith(foundauthor.getFirstname().toLowerCase().substring(0,2))) {
										matchfound = true;
									}
								} else {
									matchfound = true;
								}
								// determine if foundauthor info is more
								// complete
								boolean foundmorecomplete = (!existauth
										.hasFirstname() && foundauthor
										.hasFirstname());
								foundmorecomplete |= (!existauth
										.hasMiddlename() && foundauthor
										.hasMiddlename());
								foundmorecomplete |= (existauth.hasFirstname()
										&& foundauthor.hasFirstname() && existauth
										.getFirstname().length() < foundauthor
										.getFirstname().length());

								// if foundauthor is more complete, find in db
								if (foundmorecomplete) {

									ArtistDao match = searchService.findArtistMatchingName(foundauthor);
									// if found, replace in list
									if (match != null) {
										newauthors.add(match);
									} else {
										// if not found, replace copy
										// foundauthor info into current author
										newauthors.add(foundauthor);
									}
								}
							}
						}
					}
				}
				// end book authors

				// if not match, go through illustrators
				if (!matchfound) {
					for (ArtistDao existillus : book.getIllustrators()) {
						if (existillus.hasLastname()
								&& foundauthor.hasLastname()) {
							if (existillus
									.getLastname()
									.trim()
									.toLowerCase()
									.equals(foundauthor.getLastname().trim()
											.toLowerCase())) {
								// ensure that first initials of first match
								if (existillus.hasFirstname()
										&& foundauthor.hasFirstname()) {
									if (existillus
											.getFirstname()
											.trim()
											.substring(0, 2)
											.equals(existillus.getFirstname()
													.trim().substring(0, 2))) {
										matchfound = true;
									}
								} else {
									matchfound = true;
								}
								// determine if foundauthor info is more
								// complete
								boolean foundmorecomplete = (!existillus
										.hasFirstname() && foundauthor
										.hasFirstname());
								foundmorecomplete |= (!existillus
										.hasMiddlename() && foundauthor
										.hasMiddlename());
								foundmorecomplete |= (existillus.hasFirstname()
										&& foundauthor.hasFirstname() && existillus
										.getFirstname().length() < foundauthor
										.getFirstname().length());

								// if foundauthor is more complete, find in db
								if (foundmorecomplete) {

									ArtistDao match = searchService.findArtistMatchingName(foundauthor);
									// if found, replace in list
									if (match != null) {
										newillustrators.add(match);
									} else {
										// if not found, replace copy
										// foundauthor info into current author
										newillustrators.add(foundauthor);
									}
								}
							}
						}
					}
				}
				// end illustrators
				// if no match found, add to authors
				if (!matchfound) {
					newauthors.add(foundauthor);
				}
				

			
			}			// end go through all foundauthors

			// if authorlist has members, set in book
			if (newauthors.size()>0) {
				book.setAuthors(newauthors);
			}
			// if illustratorlist has members, set in book
			if (newillustrators.size()>0) {
				book.setIllustrators(newauthors);
			}
		}
		return book;
	}
*/


