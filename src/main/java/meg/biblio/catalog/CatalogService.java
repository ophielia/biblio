package meg.biblio.catalog;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;

import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.ClassificationDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.web.model.BookModel;

public interface CatalogService {

	String booktypelkup = "booktype";
	String bookstatuslkup = "bookstatus";
	String detailstatuslkup = "detailstatus";

	BookModel createCatalogEntryFromBookModel(Long clientkey, BookModel model);

	BookModel loadBookModel(Long id);

	List<BookDao> getAllBooks();

	public ArtistDao textToArtistName(String text);

	public List<FoundDetailsDao> getFoundDetailsForBook(Long id);
	public void classifyBook(Long bookid) throws ClassNotFoundException, InstantiationException, IllegalAccessException;
	public void assignDetailToBook(Long detailid, Long bookid)
			throws GeneralSecurityException, IOException;

	void createCatalogEntriesFromList(Long clientkey, List<BookModel> toimport);

	public PublisherDao findPublisherForName(String text);

	public void fillInDetailsForList(List<BookModel> searchobjects)
			throws GeneralSecurityException, IOException;

	public final static  class LocationStatus {
		public static final long CHECKEDOUT = 1;
		public static final long SHELVED = 2;
		public static final long LOSTBYLENDER = 3;
		public static final long REMOVEDFROMCIRC = 4;
		public static final long INVNOTFOUND = 5;
		public static final long PROCESSING = 6;
	}

	public final static  class BookType {
		public static final long FICTION = 1;
		public static final long NONFICTION = 2;
		public static final long REFERENCE = 3;
		public static final long FOREIGNLANGUAGE = 4;
		public static final long UNKNOWN = 5;
	}

	public final static  class DetailStatus {
		public static final long NODETAIL = 1;
		public static final long DETAILNOTFOUND = 2;
		public static final long MULTIDETAILSFOUND = 3;
		public static final long DETAILFOUND = 4;
	}

	HashMap<Long, ClassificationDao> getShelfClassHash(Long clientkey,
			String lang);



}
