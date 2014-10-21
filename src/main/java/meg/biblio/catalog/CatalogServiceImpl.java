package meg.biblio.catalog;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import meg.biblio.catalog.db.ArtistRepository;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.db.dao.SubjectDao;
import meg.biblio.catalog.web.model.BookModel;

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
	ArtistRepository artistRepo;
	

	@Value("${biblio.google.apikey}")
	private String apikey;
	
	
	@Value("${biblio.appname}")
	private String appname;
	
	public  final class LocationStatus {
		public static final long CHECKEDOUT=1;
		public static final long SHELVED=2;
		public static final long LOSTBYLENDER=3;
		public static final long REMOVEDFROMCIRC=4;
		public static final long INVNOTFOUND=5;
		public static  final long PROCESSING=6;
	}


	public static final class BookType {
		public static final long FICTION=1;
		public static final long NONFICTION=2;
		public static final long REFERENCE=3;
		public static final long FOREIGNLANGUAGE=4;
		public static final long UNKNOWN = 5;
	}

	public static final class DetailStatus {
		public static final long NODETAIL=1;
		public static final long DETAILNOTFOUND=2;
		public static final long MULTIDETAILSFOUND=3;
		public static final long DETAILFOUND=4;
	}

	/** 
	 * Assumes validated BookModel.  Saves a book to the database for the
	 * first time.  Usually, only minimal entries are made here - details are
	 * filled in later.  Book is entered with corresponding clientkey.
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
		// save authors
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		for (ArtistDao author:book.getAuthors()) {
			ArtistDao newauthor = artistRepo.save(author);
			authors.add(newauthor);
		}
		book.setAuthors(authors);
		*/
		
		// persist book
		BookDao saved = bookRepo.save(book);
		Long bookid = saved.getId();
		
		// reload book in bookmodel
		BookModel result= loadBookModel(bookid);
		
		// return bookmodel
		return result;
	}

	@Override
	public BookModel loadBookModel(Long id) {
		// get book from repo
		BookDao book = bookRepo.findOne(id);

		if (book!=null) {
			// get authors, subjects and illustrators
			List<ArtistDao> authors = book.getAuthors();
			book.setAuthors(authors);
			List<ArtistDao> illustrators = book.getIllustrators();
			book.setIllustrators(illustrators);
			List<SubjectDao> subjects= book.getSubjects();
			book.setSubjects(subjects);			

			// set book in model
			BookModel model = new BookModel(book);
			
			// return model
			return model;
			
		}
		// return model
		return new BookModel();
	}

	public void fillInDetailsForSingleBook(Long id) throws GeneralSecurityException, IOException {
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
			final Books books = new Books.Builder(
					GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
					null)
					.setApplicationName(appname)
					.setGoogleClientRequestInitializer(
							new BooksRequestInitializer(apikey)).build();
			com.google.api.services.books.Books.Volumes.List volumesList = books
					.volumes().list(query);
			// Execute the query.
			Volumes volumes = volumesList.execute();
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
				Get completedetails = books.volumes().get(volumeid);
				copyCompleteDetailsIntoBook(completedetails,book);
			} else {
				// multiple volumes found. save info for volumes, and set
				// detail status in book to multiple found
				List<FoundDetailsDao> details = copyDetailsIntoFoundRecords(volumes.getItems(),book);
				book.setDetailstatus(DetailStatus.MULTIDETAILSFOUND);

			}

			// save book
			
		}

/*
		
		
		// Set up Books client.

	    // Set query string and filter only Google eBooks.
	    System.out.println("Query: [" + query + "]");
	    List volumesList = books.volumes().list(query);
	    volumesList.setFilter("ebooks");


	    // Output results.
	    for (Volume volume : volumes.getItems()) {
	      Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();
	      Volume.SaleInfo saleInfo = volume.getSaleInfo();
	      System.out.println("==========");
	      // Title.
	      System.out.println("Title: " + volumeInfo.getTitle());
	      // Author(s).
	      java.util.List<String> authors = volumeInfo.getAuthors();
	      if (authors != null && !authors.isEmpty()) {
	        System.out.print("Author(s): ");
	        for (int i = 0; i < authors.size(); ++i) {
	          System.out.print(authors.get(i));
	          if (i < authors.size() - 1) {
	            System.out.print(", ");
	          }
	        }
	        System.out.println();
	      }
	      // Description (if any).
	      if (volumeInfo.getDescription() != null && volumeInfo.getDescription().length() > 0) {
	        System.out.println("Description: " + volumeInfo.getDescription());
	      }
	      // Ratings (if any).
	      if (volumeInfo.getRatingsCount() != null && volumeInfo.getRatingsCount() > 0) {
	        int fullRating = (int) Math.round(volumeInfo.getAverageRating().doubleValue());
	        System.out.print("User Rating: ");
	        for (int i = 0; i < fullRating; ++i) {
	          System.out.print("*");
	        }
	        System.out.println(" (" + volumeInfo.getRatingsCount() + " rating(s))");
	      }
	      // Price (if any).
	      if (saleInfo != null && "FOR_SALE".equals(saleInfo.getSaleability())) {
	        double save = saleInfo.getListPrice().getAmount() - saleInfo.getRetailPrice().getAmount();
	        if (save > 0.0) {
	          System.out.print("List: " + CURRENCY_FORMATTER.format(saleInfo.getListPrice().getAmount())
	              + "  ");
	        }
	        System.out.print("Google eBooks Price: "
	            + CURRENCY_FORMATTER.format(saleInfo.getRetailPrice().getAmount()));
	        if (save > 0.0) {
	          System.out.print("  You Save: " + CURRENCY_FORMATTER.format(save) + " ("
	              + PERCENT_FORMATTER.format(save / saleInfo.getListPrice().getAmount()) + ")");
	        }
	        System.out.println();
	      }
	      // Access status.
	      String accessViewStatus = volume.getAccessInfo().getAccessViewStatus();
	      String message = "Additional information about this book is available from Google eBooks at:";
	      if ("FULL_PUBLIC_DOMAIN".equals(accessViewStatus)) {
	        message = "This public domain book is available for free from Google eBooks at:";
	      } else if ("SAMPLE".equals(accessViewStatus)) {
	        message = "A preview of this book is available from Google eBooks at:";
	      }
	      System.out.println(message);
	      // Link to Google eBooks.
	      System.out.println(volumeInfo.getInfoLink());
	    }
	    System.out.println("==========");
	    System.out.println(
	        volumes.getTotalItems() + " total results at http://books.google.com/ebooks?q="
	        + URLEncoder.encode(query, "UTF-8"));
		
		
	*/	
	} 
	
	private void copyCompleteDetailsIntoBook(Get completedetails, BookDao book) {
		// TODO Auto-generated method stub
		
	}

	private List<FoundDetailsDao> copyDetailsIntoFoundRecords(
			List<Volume> items, BookDao book) {
		List<FoundDetailsDao> details = new ArrayList<FoundDetailsDao>();
		for (Volume found:items) {
			FoundDetailsDao detail = new FoundDetailsDao();
			VolumeInfo info = found.getVolumeInfo();
			detail.setBookid(book.getId());
			detail.setTitle(info.getTitle());
			detail.setPublisher(info.getPublisher());
			Long publishyear = info.getPublishedDate()!=null?new Long(info.getPublishedDate()):null;
			detail.setPublishyear(publishyear);
			detail.setLanguage(info.getLanguage());
			StringBuilder authors=new StringBuilder();
			for (String author:info.getAuthors()) {
				authors.append(author).append(",");
			}
			if (authors.length()>1) {
				authors.setLength(authors.length()-1);
			}
			detail.setAuthors(authors.toString());

// puzzle with industry identifiers
		}
		
		return details;
	}

	/**
	 * Convenience method for testing/devpt.  Final will be done through SearchService.
	 */
	@Override
	public List<BookDao> getAllBooks() {
		return bookRepo.findAll();
	}

	
	
}
