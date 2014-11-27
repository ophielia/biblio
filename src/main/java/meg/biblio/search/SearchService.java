package meg.biblio.search;

import java.util.HashMap;
import java.util.List;

import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;


public interface SearchService {

	public static final class Breakoutfield {
		public static final long STATUS=1;
		public static final long DETAILSTATUS=2;
	}
	
	public ArtistDao findArtistMatchingName(ArtistDao tomatch);

	public List<Long> findBookIdByClientId(String clientbookid);
	
	public List<BookDao> findBooksWithoutDetails(int maxresults);

	List<BookDao> findBooksForCriteria(BookSearchCriteria criteria,
			Long clientid);

	HashMap<Long, Long> breakoutByBookField(long bookkey, Long clientid);

	Long getBookCount(Long clientid);

}