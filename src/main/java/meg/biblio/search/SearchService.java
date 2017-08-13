package meg.biblio.search;

import meg.biblio.catalog.BookIdentifier;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.web.model.Pager;

import java.util.HashMap;
import java.util.List;


public interface SearchService {

    public static final class Breakoutfield {
        public static final long STATUS = 1;
        public static final long DETAILSTATUS = 2;
        public static final long COUNTSTATUS = 3;
        public static final long CLIENTCATEGORY = 4;
    }

    public ArtistDao findArtistMatchingName(ArtistDao tomatch);

    public List<Long> findBookIdByClientId(String clientbookid);

    public List<BookDao> findBooksWithoutDetails(int maxresults, ClientDao client);

    List<BookDao> findBooksForCriteria(BookSearchCriteria criteria,
                                       Pager pager, Long clientid);

    Long getBookCountForCriteria(BookSearchCriteria criteria,
                                 Long clientid);

    HashMap<Long, Long> breakoutByBookField(long bookkey, Long clientid);

    Long getBookCount(Long clientid);

    public BookDetailDao findBooksForIdentifier(BookIdentifier bi);


}