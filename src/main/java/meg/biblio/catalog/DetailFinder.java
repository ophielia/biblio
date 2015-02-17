package meg.biblio.catalog;

import java.util.List;

import meg.biblio.catalog.web.model.BookModel;

public interface DetailFinder {

	FinderObject findDetails(FinderObject findobj, long clientcomplete) throws Exception;

	List<BookModel> findDetailsForList(List<BookModel> models,
			long clientcomplete, Integer batchsearchmax);

	
}
