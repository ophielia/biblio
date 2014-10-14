package meg.biblio.catalog;

import java.util.List;

import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.SubjectDao;
import meg.biblio.catalog.web.model.BookModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class CatalogServiceImpl implements CatalogService {

	@Autowired
	BookRepository bookRepo;
	
	
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
			List<ArtistDao> illustrators = book.getAuthors();
			book.setIllustrators(illustrators);
			List<SubjectDao> subjects= book.getSubjects();
			book.setIllustrators(illustrators);			

			// set book in model
			BookModel model = new BookModel(book);
			
			// return model
			return model;
			
		}
		// return model
		return new BookModel();
	}

	
	
}
