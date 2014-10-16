package meg.biblio.catalog;

import java.util.List;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.web.model.BookModel;

public interface CatalogService {

	BookModel createCatalogEntryFromBookModel(Long clientkey, BookModel model);

	BookModel loadBookModel(Long id);

	List<BookDao> getAllBooks();

}
