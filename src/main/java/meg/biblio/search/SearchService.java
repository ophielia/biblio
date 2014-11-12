package meg.biblio.search;

import java.util.List;

import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;


public interface SearchService {

	public ArtistDao findArtistMatchingName(ArtistDao tomatch);

	public List<Long> findBookIdByClientId(String clientbookid);
	
	public List<BookDao> findBooksWithoutDetails(int maxresults);	
}