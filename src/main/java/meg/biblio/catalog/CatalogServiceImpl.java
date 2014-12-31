package meg.biblio.catalog;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import meg.biblio.catalog.db.ArtistRepository;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.ClassificationRepository;
import meg.biblio.catalog.db.FoundDetailsRepository;
import meg.biblio.catalog.db.FoundWordsDao;
import meg.biblio.catalog.db.FoundWordsRepository;
import meg.biblio.catalog.db.IgnoredWordsDao;
import meg.biblio.catalog.db.IgnoredWordsRepository;
import meg.biblio.catalog.db.PublisherRepository;
import meg.biblio.catalog.db.SubjectRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.ClassificationDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.db.dao.SubjectDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.common.AppSettingService;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.search.SearchService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
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
@EnableScheduling
public class CatalogServiceImpl implements CatalogService {

	private final static class NameMatchType {
		public static final long FIRSTINITIAL = 1;
		public static final long LASTNAME = 2;
	}

	
	  /* Get actual class name to be printed on */
	  static Logger log = Logger.getLogger(
			  CatalogServiceImpl.class.getName());
	  
	@Autowired
	SelectKeyService keyService;

	@Autowired
	SearchService searchService;

	@Autowired
	ClientService clientService;

	@Autowired
	BookRepository bookRepo;

	@Autowired
	FoundDetailsRepository foundRepo;
	
	@Autowired
	FoundWordsRepository indexRepo;	

	
	
	@Autowired
	IgnoredWordsRepository ignoredRepo;	
	
	@Autowired
	ArtistRepository artistRepo;

	@Autowired
	PublisherRepository pubRepo;

	@Autowired
	SubjectRepository subjectRepo;

	@Autowired
	ClassificationRepository classRepo;

	@Autowired
	AppSettingService settingService;

	/**
	 * Assumes validated BookModel. Saves a book to the database for the first
	 * time. Usually, only minimal entries are made here - details are filled in
	 * later. Book is entered with corresponding clientkey.
	 */
	@Override
	public BookModel createCatalogEntryFromBookModel(Long clientkey,
			BookModel model, Boolean createclientbookid) {
		BookDao book = createBookFromBookModel(clientkey, model, createclientbookid);
		Long bookid = book.getId();
		Boolean lookupwithgoogle = settingService.getSettingAsBoolean("biblio.google.turnedon");

		// fill in details with google call
		if (lookupwithgoogle) {
			try {
				fillInDetailsForSingleBook(bookid);
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// call automatic classification of book
		try {
			classifyBook(bookid);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// reload book in bookmodel
		BookModel result = loadBookModel(bookid);

		// return bookmodel
		return result;
	}
	
	@Override
	public BookModel createCatalogEntryFromBookModel(Long clientkey,
			BookModel model) {
		return createCatalogEntryFromBookModel(clientkey, model, false);
	}

	@Override
	public void createCatalogEntriesFromList(Long clientkey,
			List<BookModel> toimport) {
		List<BookModel> createdobjects = new ArrayList<BookModel>();
		Boolean lookupwithgoogle = settingService.getSettingAsBoolean("biblio.google.turnedon");
		
		// go through list of BookModels, persisting them
		for (BookModel imported : toimport) {

			BookDao saved = createBookFromBookModel(clientkey, imported, false);
			Long bookid = saved.getId();

			// reload book in bookmodel
			BookModel result = loadBookModel(bookid);

			// add info to lists
			createdobjects.add(result);
		}

		// get info on set number of books
		if (lookupwithgoogle) {
			try {
				fillInDetailsForList(createdobjects);
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

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


	public void fillInDetailsForList(List<BookModel> searchobjects)
			throws GeneralSecurityException, IOException {
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		String apikey = settingService.getSettingAsString("biblio.google.apikey");
		String appname = settingService.getSettingAsString("biblio.appname");
		Integer batchsearchmax = settingService.getSettingAsInteger("biblio.google.batchsearchmax");
		
		// set up query google
		Volumes volumes = null;
		final Books books = new Books.Builder(
				GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, null)
				.setApplicationName(appname)
				.setGoogleClientRequestInitializer(
						new BooksRequestInitializer(apikey)).build();

		if (searchobjects != null) {
			int searchsize = searchobjects.size() > batchsearchmax ? batchsearchmax
					: searchobjects.size();

			for (int i = 0; i < searchsize; i++) {
				BookModel tofillin = searchobjects.get(i);
				// get book
				BookDao book = tofillin.getBook();

				if (book != null) {
					// set up query for title and author
					StringBuffer querybuild = new StringBuffer();
					querybuild.append("intitle:");
					String title = book.getTitle().toLowerCase();
					title = title.replace(" ", "+");
					querybuild.append(title);
					boolean remainingauthors = false;
					// try with author first
					if (book.getAuthors() != null
							&& book.getAuthors().size() > 0) {
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
						remainingauthors = book.getAuthors().size() > 1;
					} // if no authors available, try with illustrators
					else if (book.getIllustrators() != null
							&& book.getIllustrators().size() > 0) {
						ArtistDao author = book.getIllustrators().get(0);
						String authname = author.getLastname();
						if (authname == null) {
							authname = author.getFirstname() != null ? author
									.getFirstname() : author.getMiddlename();
						}
						if (authname != null) {
							authname = authname.toLowerCase().replace(" ", "+");
							querybuild.append("+inauthor:").append(authname);
						}
						remainingauthors |= book.getIllustrators().size() > 1;
					}
					// if no authors or illustrators available, try with
					// publisher
					else if (book.getPublisher() != null) {
						PublisherDao publisher = book.getPublisher();
						String pubname = publisher.getName();
						if (pubname != null) {
							pubname = pubname.trim().toLowerCase();
							pubname = pubname.toLowerCase().replace(" ", "+");
							pubname = pubname + "+";
							// pubname added as general key
							querybuild.insert(0, pubname);
						}
					}

					String query = querybuild.toString();

					volumes = singleQueryGoogle(books, query);

					// process results
					List<FoundDetailsDao> details = null;
					if (volumes.getTotalItems() == 0
							|| volumes.getItems() == null) {
						// set detailstatus to not found in book
						book.setDetailstatus(DetailStatus.DETAILNOTFOUND);
					} else if (volumes.getTotalItems() == 1) {
						// one volume found - get details for this, fill in the
						// book,
						// and save the book
						book.setDetailstatus(DetailStatus.DETAILFOUND);
						// do second query to get juicy description
						Volume founddetails = volumes.getItems().get(0);
						String volumeid = founddetails.getId();
						Get detailsrequest = books.volumes().get(volumeid);
						Volume completedetails = detailsrequest.execute();
						copyCompleteDetailsIntoBook(completedetails, book);
					} else {
						// multiple volumes found. save info for volumes, and
						// set
						// detail status in book to multiple found
						details = copyDetailsIntoFoundRecords(
								volumes.getItems(), book);
						book.setDetailstatus(DetailStatus.MULTIDETAILSFOUND);
					}

					// save book
					book = saveBook(book);

					// call automatic classification of book
					try {
						classifyBook(book.getId());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// save founddetails if available
					if (details != null) {
						foundRepo.save(details);
					}
				}

			} // end loop through list
		} // end if created objects not null
	}


	public BookDao saveBook(BookDao book) {
		boolean bookchange = book.getTextchange();

		
		BookDao saved = bookRepo.save(book);
		if (bookchange) {
			indexBooktext(saved);
			saved.setTextchange(false);
		}

		return saved;
	}


	private void indexBooktext(BookDao saved) {
		log.debug("indexing for bookid:" + saved.getId() + "; description:" + saved.getDescription());
		List<FoundWordsDao> todelete = indexRepo.findWordsForBook(saved);
		indexRepo.delete(todelete);
		
		// make counting hash
		HashMap<String,Integer> wordcounts = new HashMap<String,Integer>();
		List<FoundWordsDao> foundwords = new ArrayList<FoundWordsDao>();

		// mash up all text fields together - title, description, subjects, authors, illustrators
		StringBuffer charles = new StringBuffer(saved.getTitle());
		if (saved.getDescription()!=null) {charles.append(" ").append(saved.getDescription());}
		if (saved.getSubjects()!=null) {
			for (SubjectDao subject:saved.getSubjects()) {
				charles.append(" ").append(subject.getListing());	
			}
		}
		if (saved.getAuthors()!=null) {
			for (ArtistDao author:saved.getAuthors()) {
				charles.append(" ").append(author.getDisplayName());	
			}
		}
		if (saved.getIllustrators()!=null) {
			for (ArtistDao author:saved.getIllustrators()) {
				charles.append(" ").append(author.getDisplayName());	
			}
		}		

		// split all text into words
		String mashup = charles.toString();
		// remove punctuation
		String cleanmashup = mashup.replaceAll("[^a-zA-Z'éèàùâêîôûëïç ]", " ");
		// split by space
		String[] words = cleanmashup.split(" ");
		
		// go through all words, counting each
		for (int i=0;i<words.length;i++) {
			String word = words[i];
			word = word.toLowerCase().trim();
			if (word.length()>0) {
				// count this word....
				if (wordcounts.containsKey(word)) {
					// add to count
					Integer count = wordcounts.get(word);
					count++;
					wordcounts.put(word, count);
				} else {
					// make new key
					wordcounts.put(word, new Integer(1));
				}
			}
		}
		
		// double all words with apostrophes
		HashMap<String,Integer> noapostrophes = new HashMap<String,Integer>();
		for (String word:wordcounts.keySet()) {
			// go through all keys in wordcounts
			// if word contains apostrophe
			if (word.contains("'")) {
				// get count
				Integer count = wordcounts.get(word);
		
				// replace apostrophe with space
				String noapos = word.replaceAll("'", " ");
				// split on space
				String[] noaposparts = noapos.split(" ");
				// add each part to repeat hash with same count
				for (int i=0;i<noaposparts.length;i++) {
					String newword = noaposparts[i].trim();
					noapostrophes.put(newword, count);
				}
			}
		}
		// add all repeat hash values and keys to wordcount
		for (String word:noapostrophes.keySet()) {
			Integer count = noapostrophes.get(word); 
			wordcounts.put(word, count);
		}
		
		
		
		// get ignoredwords 
		List<String> toignore = new ArrayList<String>();
		List<IgnoredWordsDao> ignoredlist = ignoredRepo.findAll();
		for (IgnoredWordsDao ignore:ignoredlist) {
			toignore.add(ignore.getWord());
		}
		
		// now, save counted words - book, word, countintext
		for (String word:wordcounts.keySet()) {
			if (word.length() > 1) {
				if (!toignore.contains(word)) {
					Integer count = wordcounts.get(word);
					FoundWordsDao wordcount = new FoundWordsDao();
					wordcount.setBook(saved);
					wordcount.setCountintext(count);
					wordcount.setWord(word);
					foundwords.add(wordcount);
				}
			}
		}
		// persist
		if (foundwords.size()>0) {
			
			indexRepo.save(foundwords);
		}

	}

	public void assignDetailToBook(Long detailid, Long bookid)
			throws GeneralSecurityException, IOException {
		String apikey = settingService.getSettingAsString("biblio.google.apikey");
		String appname = settingService.getSettingAsString("biblio.appname");
		// dummy check - nothing null
		if (detailid != null && bookid != null) {
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
			// get detail
			FoundDetailsDao detail = foundRepo.findOne(detailid);

			if (detail != null) {
				// dummy check - bookid = passed bookid
				if (detail.getBookid().longValue() == bookid.longValue()) {
					BookDao book = bookRepo.findOne(bookid);

					// get searchserviceid
					String searchid = detail.getSearchserviceid();

					// query for volumeinfo
					final Books books = new Books.Builder(
							GoogleNetHttpTransport.newTrustedTransport(),
							jsonFactory, null)
							.setApplicationName(appname)
							.setGoogleClientRequestInitializer(
									new BooksRequestInitializer(apikey))
							.build();

					Get detailsrequest = books.volumes().get(searchid);
					Volume completedetails = detailsrequest.execute();

					// copyDetailsIntoBook
					copyCompleteDetailsIntoBook(completedetails, book);

					// update book detailstatus
					book.setDetailstatus(DetailStatus.DETAILFOUND);

					// save book
					bookRepo.saveAndFlush(book);

					// classify book
					try {
						classifyBook(book.getId());
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// delete found records
					List<FoundDetailsDao> bookdetails = foundRepo
							.findDetailsForBook(bookid);
					foundRepo.delete(bookdetails);
				}
			}
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

	public List<FoundDetailsDao> getFoundDetailsForBook(Long id) {
		if (id != null) {
			// query db for founddetails
			List<FoundDetailsDao> details = foundRepo.findDetailsForBook(id);
			// return founddetails
			return details;
		}
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

	@Override
	public HashMap<Long, ClassificationDao> getShelfClassHash(Long clientkey,
			String lang) {
		HashMap<Long, ClassificationDao> resulthash=new HashMap<Long, ClassificationDao>();
		List<ClassificationDao> shelfclasses =classRepo.findByClientidAndLanguage(clientkey, lang);
		if (shelfclasses!=null) {
			for (ClassificationDao shelfclass:shelfclasses) {
				resulthash.put(shelfclass.getKey(), shelfclass);
			}
		}
		return resulthash;
	}

	@Override
	public List<ClassificationDao> getShelfClassList(Long clientkey,
			String lang) {
		List<ClassificationDao> shelfclasses =classRepo.findByClientidAndLanguage(clientkey, lang);
		return shelfclasses;
	}

	/**
	 * Convenience method for testing/devpt. Final will be done through
	 * SearchService.
	 */
	@Override
	public List<BookDao> getAllBooks() {
		return bookRepo.findAll();
	}

	public void classifyBook(Long bookid) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		BookDao book = bookRepo.findOne(bookid);
		if (book != null) {
			if (book.getDetailstatus().equals(CatalogService.DetailStatus.DETAILFOUND)) {
				if (book.getClientid() != null) {
					Long clientkey = book.getClientid();
					Classifier classifier = clientService
							.getClassifierForClient(clientkey);
					book = classifier.classifyBook(book);
					if (book.getShelfclass() != null) {
						// save book if classification assigned.
						saveBook(book);
					}
				}
			}
		}
	}

	private BookDao createBookFromBookModel(Long clientkey, BookModel model, Boolean createid) {
		// get book
		BookDao book = model.getBook();

		// add clientkey
		book.setClientid(clientkey);

		// make new clientbookid if needed
		if (createid) {
			// get max bookid
			Long maxbookid = clientService.getAndIncrementLastBookNr(clientkey);
			// set max bookid in book
			book.setClientbookid(maxbookid.toString());
		}
		
		
		// add default entries for status, detail status, type
		book.setStatus(Status.PROCESSING);
		book.setDetailstatus(DetailStatus.NODETAIL);
		book.setType(BookType.UNKNOWN);
		book.setCreatedon(new Date());

		// handle authors and illustrators by
		// retrieving any existing artists from db
		if (book.getAuthors() != null) {
			List<ArtistDao> newauthors = new ArrayList<ArtistDao>();
			for (ArtistDao author : book.getAuthors()) {
				if (author.getId() != null) {
					// we have an existing author here - add it to the list
					newauthors.add(author);
				} else {
					// new author - check for match in db
					ArtistDao dbfound = searchService
							.findArtistMatchingName(author);
					if (dbfound != null) {
						newauthors.add(dbfound);
					} else {
						newauthors.add(author);
					}
				}
			}
			book.setAuthors(newauthors);
		}
		if (book.getIllustrators() != null) {
			List<ArtistDao> newillustrators = new ArrayList<ArtistDao>();
			for (ArtistDao illustrator : book.getIllustrators()) {
				if (illustrator.getId() != null) {
					// we have an existing illustrator here - add it to the list
					newillustrators.add(illustrator);
				} else {
					// new illustrator - check for match in db
					ArtistDao dbfound = searchService
							.findArtistMatchingName(illustrator);
					if (dbfound != null) {
						newillustrators.add(dbfound);
					} else {
						newillustrators.add(illustrator);
					}
				}
			}
			book.setIllustrators(newillustrators);
		}
		if (book.getPublisher() != null) {
			PublisherDao pub = findPublisherForName(book.getPublisher()
					.getName());
			book.setPublisher(pub);
		}
		
		if (book.hasIsbn()&& (book.getTitle()==null || book.getTitle().trim().length()==0)) {
			book.setTitle("--");
		}

		// persist book
		BookDao saved = saveBook(book);
		return saved;

	}

	private void fillInDetailsForSingleBook(Long id, boolean forceintofounddetails)
			throws GeneralSecurityException, IOException {
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		String apikey = settingService.getSettingAsString("biblio.google.apikey");
		String appname = settingService.getSettingAsString("biblio.appname");
		
		// find book
		BookDao book = bookRepo.findOne(id);

		if (book != null) {
			// set up query for title and author
			StringBuffer querybuild = new StringBuffer();
			
			if (book.hasIsbn()) {
				String isbn = book.getIsbn10()!=null?book.getIsbn10():book.getIsbn13();
				querybuild.append("isbn:");
				querybuild.append(isbn);
			} else {
				querybuild.append("intitle:");
				String title = book.getTitle().toLowerCase();
				title = title.replace(" ", "+");
				querybuild.append(title);
				boolean remainingauthors = false;
				// try with author 
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
					remainingauthors = book.getAuthors().size() > 1;
				} // if no authors available, try with illustrators
				else if (book.getIllustrators() != null
						&& book.getIllustrators().size() > 0) {
					ArtistDao author = book.getIllustrators().get(0);
					String authname = author.getLastname();
					if (authname == null) {
						authname = author.getFirstname() != null ? author
								.getFirstname() : author.getMiddlename();
					}
					if (authname != null) {
						authname = authname.toLowerCase().replace(" ", "+");
						querybuild.append("+inauthor:").append(authname);
					}
					remainingauthors |= book.getIllustrators().size() > 1;
				}
				// if no authors or illustrators available, try with publisher
				else if (book.getPublisher() != null) {
					PublisherDao publisher = book.getPublisher();
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
			} else if (volumes.getTotalItems() == 1 && !forceintofounddetails) {
				// one volume found - get details for this, fill in the book,
				// and save the book
				book.setDetailstatus(DetailStatus.DETAILFOUND);
				// do second query to get juicy description
				Volume founddetails = volumes.getItems().get(0);
				String volumeid = founddetails.getId();
				Get detailsrequest = books.volumes().get(volumeid);
				Volume completedetails = detailsrequest.execute();
				copyCompleteDetailsIntoBook(completedetails, book);
			} else if (volumes.getTotalItems() == 1 && forceintofounddetails) {
				// one volume found - but should be saved into found details
				details = copyDetailsIntoFoundRecords(volumes.getItems(), book);
				book.setDetailstatus(DetailStatus.ISBNFOUND);
			} else {
				// multiple volumes found. save info for volumes, and set
				// detail status in book to multiple found
				details = copyDetailsIntoFoundRecords(volumes.getItems(), book);
				book.setDetailstatus(DetailStatus.MULTIDETAILSFOUND);

			}

			// save book
			saveBook(book);

			// save founddetails if available
			foundRepo.save(details);
		}

	}

	private void fillInDetailsForSingleBook(Long id) throws GeneralSecurityException, IOException {
		fillInDetailsForSingleBook(id,false);
		
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
		if (info.getPublishedDate() != null) {
			String publishyearstr = info.getPublishedDate();
			// handle full date
			if (publishyearstr.contains("-")) {
				// chop off after dash
				publishyearstr = publishyearstr.substring(0,
						publishyearstr.indexOf("-"));
				book.setPublishyear(new Long(publishyearstr));
			} else if (publishyearstr.contains("?")) {
				// do nothing - vague year
			} else {
				book.setPublishyear(new Long(publishyearstr));
			}

		}
		book.setLanguage(info.getLanguage());

		copyAuthorsIntoBook(book, info.getAuthors());

		List<IndustryIdentifiers> isbn = info.getIndustryIdentifiers();
		ListIterator<IndustryIdentifiers> iter = isbn.listIterator();
		while (iter.hasNext()) {
			IndustryIdentifiers ident = iter.next();
			String type = ident.getType() != null ? ident.getType() : "";
			if (type.endsWith("_10")) {
				book.setIsbn10(ident.getIdentifier());
			} else if (type.endsWith("_13")) {
				book.setIsbn13(ident.getIdentifier());
			}
		}
		// file categories into subjects - if containing fiction, assign type
		List<String> categories = info.getCategories();
		if (categories != null) {
			List<SubjectDao> subjects = new ArrayList<SubjectDao>();
			for (String category : categories) {
				// clean it up
				if (category.toLowerCase().contains("nonfiction")) {
					book.setType(BookType.NONFICTION);
				} else if (category.toLowerCase().contains("fiction")) {
					book.setType(BookType.FICTION);

				}
				SubjectDao subject = findSubjectForString(category);
				subjects.add(subject);
			}
		}

	}

	private List<FoundDetailsDao> copyDetailsIntoFoundRecords(
			List<Volume> items, BookDao book) {
		Integer maxdetails = settingService.getSettingAsInteger("biblio.google.maxdetails");
		
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
			detail.setBookid(book.getId());
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

	private BookDao copyAuthorsIntoBook(BookDao book, List<String> foundauthors) {
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
							&& !foundauthor.hasFirstname() && foundauthor
							.getFirstname().length() < existauth.getFirstname()
							.length());

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
			} // end loop through foundauthors

			// add any remaining non-matched authors or illustrators to list
			if (bookauthors.size() > 0) {
				for (Long id : bookauthors.keySet()) {
					ArtistDao oldadd = bookauthors.get(id);
					newauthors.add(oldadd);
				}
			}
			if (bookillustrators.size() > 0) {
				for (Long id : bookillustrators.keySet()) {
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
		if (text != null) {
			// clean up text
			text = text.trim();
			// query db
			List<SubjectDao> foundlist = subjectRepo.findSubjectByText(text
					.toLowerCase());
			if (foundlist != null && foundlist.size() > 0) {
				return foundlist.get(0);
			} else {
				// if nothing found, make new PublisherDao
				SubjectDao pub = new SubjectDao();
				pub.setListing(text);
				return pub;
			}
		}
		return null;
	}

	private boolean namesMatch(long matchtype, ArtistDao bookauth,
			ArtistDao foundauthor) {
		String bookval = null;
		String compareval = null;

		if (matchtype == NameMatchType.LASTNAME) {
			bookval = bookauth.getLastname() != null ? bookauth.getLastname()
					: "";
			compareval = foundauthor.getLastname() != null ? foundauthor
					.getLastname() : "";
		} else if (matchtype == NameMatchType.FIRSTINITIAL) {
			bookval = bookauth.getFirstname() != null ? bookauth.getFirstname()
					: "";
			compareval = foundauthor.getFirstname() != null ? foundauthor
					.getFirstname() : "";
		}
		bookval = bookval.trim().toLowerCase();
		compareval = compareval.trim().toLowerCase();

		// make comparison
		if (matchtype == NameMatchType.LASTNAME) {
			return (bookval.equals(compareval));
		} else if (matchtype == NameMatchType.FIRSTINITIAL) {
			bookval = bookval.length() > 0 ? bookval.substring(0, 1) : "";
			compareval = compareval.length() > 0 ? compareval.substring(0, 1)
					: "";
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

	@Scheduled(fixedRate = 60000)
	private void scheduledFillInDetails() {
		Integer batchsearchmax = settingService.getSettingAsInteger("biblio.google.batchsearchmax");
		Boolean progressivefillenabled = settingService.getSettingAsBoolean("biblio.progressivefill.turnedon");
		if (progressivefillenabled) {

			// get list of books without details - max batchsearchmax
			List<BookDao> nodetails = searchService
					.findBooksWithoutDetails(batchsearchmax);

			// put books in book model
			if (nodetails != null) {
				List<BookModel> adddetails = new ArrayList<BookModel>();
				for (BookDao book : nodetails) {
					adddetails.add(new BookModel(book));
				}
				// service call to fill in details
				try {
					fillInDetailsForList(adddetails);
				} catch (GeneralSecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// end

		}
	}
	
	@Override
	public void assignShelfClassToBooks(Long shelfclassUpdate,
			List<Long> toupdate) {
		List<BookDao> books = bookRepo.findAll(toupdate);
		List<BookDao> tosave = new ArrayList<BookDao>();
		if (books!=null) {
			for (BookDao book:books) {
				book.setShelfclass(shelfclassUpdate);
				tosave.add(book);
			}
		}
		if (tosave.size()>0) {
			bookRepo.save(tosave);
		}
		
	}
	

	@Override
	public void assignStatusToBooks(Long statusUpdate, List<Long> toupdate) {
		List<BookDao> books = bookRepo.findAll(toupdate);
		List<BookDao> tosave = new ArrayList<BookDao>();
		if (books!=null) {
			for (BookDao book:books) {
				book.setStatus(statusUpdate);
				tosave.add(book);
			}
		}
		if (tosave.size()>0) {
			bookRepo.save(tosave);
		}
		
	}	
	
	
	
	
	
	
	
	
	
	private boolean reindex=false;
	@Scheduled(fixedRate = 60000)
	private void reindexBooks() {
		if (reindex) {

			// get list of books without details - max 
			List<BookDao> toreindex = getAllBooks();
			for (BookDao book:toreindex) {
				book.setTextchange(true);
				saveBook(book);
			}
			reindex=false;
		}
	}

	@Override
	public BookModel updateCatalogEntryFromBookModel(Long clientkey,
			BookModel model, Boolean fillindetails) throws GeneralSecurityException, IOException {
		// get book
		BookDao book = model.getBook();
		book.setClientid(clientkey);
		
		// if book is not null, save it
		if (book!=null) {
			book = saveBook(book);
		}
		
		if (fillindetails) {
			fillInDetailsForSingleBook(book.getId());
		}
		BookModel toreturn = loadBookModel(book.getId());
		return toreturn;
	}

	@Override
	public BookModel addToFoundDetails(Long clientkey, BookModel dbmodel) {
		Long id = dbmodel.getBookid();
		if (id != null && dbmodel.getBook() != null) {
			// save isbn in book
			bookRepo.save(dbmodel.getBook());
			// get found details for book
			List<FoundDetailsDao> origdetails = getFoundDetailsForBook(id);
			// do book search - copy into found details
			try {
				fillInDetailsForSingleBook(id, true);
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// load book model
			dbmodel = loadBookModel(id);

			// if results found (and copied into found details), delete other
			// found details
			if (dbmodel.getDetailstatus().longValue() == CatalogService.DetailStatus.ISBNFOUND) {
				foundRepo.delete(origdetails);
				BookDao book = dbmodel.getBook();
				book.setDetailstatus(CatalogService.DetailStatus.MULTIDETAILSFOUND);
				bookRepo.save(book);
				dbmodel.setBook(book);
			}
		}
		// return model
		return dbmodel;
	}

	@Override
	public BookDao findBookByClientBookId(String bookid, ClientDao client) {
		// call repository
		List<BookDao> books = bookRepo.findBookByClientAssignedId(bookid.trim(), client.getId());
		// return results
		if (books!=null && books.size()>0) {
			return books.get(0);
		}
		return null;
	}

	@Override
	public BookDao updateBookStatus(Long bookid, long status) {
		// get book
		BookDao book = bookRepo.findOne(bookid);
		
		// set status
		book.setStatus(new Long(status));
		
		// save book
		book = bookRepo.save(book);
		
		// return book
		return book;
	}

	@Override
	public void assignCodeToBook(String code, Long bookid) {
		if (bookid != null && code != null) {
			// get book
			BookDao book = bookRepo.findOne(bookid);
			if (book != null) {
				// put code in slot
				book.setBarcodeid(code);
				// save book
				saveBook(book);
			}
		}
	}



}
