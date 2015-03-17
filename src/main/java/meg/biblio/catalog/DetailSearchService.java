package meg.biblio.catalog;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.common.db.dao.ClientDao;

public interface DetailSearchService {

	public final static  class CompletionTargets {
		public static final long TITLE = 2;
		public static final long AUTHOR= 3;
		public static final long IMAGE = 5;
		public static final long DESCRIPTION = 7;
	}

	public BookModel fillInDetailsForBook(BookModel model,ClientDao client);
	
	public List<BookModel> fillInDetailsForBookList(List<BookModel> models, ClientDao client); 
	
	public List<FoundDetailsDao> getFoundDetailsForBook(Long bookid);

	public void assignDetailToBook(Long detailid, Long bookid)
			throws GeneralSecurityException, IOException;

	List<BookModel> doOfflineSearchForBookList(List<BookModel> models,
			ClientDao client);

	
}
