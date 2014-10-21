package meg.biblio.catalog;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.web.model.BookModel;

public interface CatalogService {

	BookModel createCatalogEntryFromBookModel(Long clientkey, BookModel model);

	BookModel loadBookModel(Long id);

	void fillInDetailsForSingleBook(Long id) throws GeneralSecurityException, IOException;
	
	List<BookDao> getAllBooks();

}
