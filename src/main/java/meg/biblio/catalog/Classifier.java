package meg.biblio.catalog;

import meg.biblio.catalog.db.dao.BookDao;

public interface Classifier {

	BookDao classifyBook(BookDao book);

}
