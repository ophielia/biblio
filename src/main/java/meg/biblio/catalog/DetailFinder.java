package meg.biblio.catalog;

import java.util.List;

import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.web.model.BookModel;

public interface DetailFinder {

	FinderObject findDetails(FinderObject findobj, long clientcomplete) throws Exception;

	List<FinderObject> findDetailsForList(List<FinderObject> forsearch,
			long clientcomplete, Integer batchsearchmax)  throws Exception;

	

	public FinderObject assignDetailToBook(FinderObject findobj, FoundDetailsDao fd) throws Exception;

	
}
