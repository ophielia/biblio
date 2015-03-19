package meg.biblio.catalog;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;

import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.catalog.db.dao.ClassificationDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.common.db.dao.ClientDao;

public interface CatalogService {

	String booktypelkup = "booktype";
	String bookstatuslkup = "bookstatus";
	String detailstatuslkup = "detailstatus";
	String languagelkup = "language";
	String titledefault = "--";

	public final static  class Status {
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
		public static final long DETAILNOTFOUNDWISBN = 5;
	}



	BookModel createCatalogEntryFromBookModel(Long clientkey, BookModel model,Boolean createclientbookid);
	
	BookModel createCatalogEntryFromBookModel(Long clientkey, BookModel model);

	BookModel loadBookModel(Long id);



	public ArtistDao textToArtistName(String text);

	public List<FoundDetailsDao> getFoundDetailsForBook(Long id);

	List<BookModel> createCatalogEntriesFromList(Long clientkey, List<BookModel> toimport);



	public BookDao saveBook(BookDao book);
	
	HashMap<Long, ClassificationDao> getShelfClassHash(Long clientkey,
			String lang);

	List<ClassificationDao> getShelfClassList(Long clientkey, String lang);

	void assignShelfClassToBooks(Long shelfclassUpdate, List<Long> toupdate);

	void assignStatusToBooks(Long statusupdate, List<Long> toupdate);

	BookModel updateCatalogEntryFromBookModel(Long clientkey, BookModel model, Boolean fillindetails) throws GeneralSecurityException, IOException;

	 BookDao findBookByClientBookId(String bookid, ClientDao client) ;

	BookDao updateBookStatus(Long id, long checkedout);


	void assignCodeToBook(String code, Long bookid);

	BookDao findBookByBarcode(String code);

	BookDetailDao saveBookDetail(BookDetailDao newdetail);



}
