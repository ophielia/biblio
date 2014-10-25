package meg.biblio.catalog;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.db.dao.SubjectDao;
import meg.biblio.catalog.web.model.BookModel;

public interface CatalogService {

	BookModel createCatalogEntryFromBookModel(Long clientkey, BookModel model);

	BookModel loadBookModel(Long id);
	
	public void setDisplayInfoForLanguage(String lang,BookModel model);

	void fillInDetailsForSingleBook(Long id) throws GeneralSecurityException, IOException;
	
	List<BookDao> getAllBooks();
	
	public ArtistDao textToArtistName(String text);

	public List<FoundDetailsDao> getFoundDetailsForBook(Long id);
	
	public 	void assignDetailToBook(Long detailid, Long bookid) throws GeneralSecurityException, IOException;




	
}
