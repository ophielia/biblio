package meg.biblio.catalog;

import meg.biblio.catalog.db.dao.FoundDetailsDao;

import java.util.List;

public interface DetailFinder {

    FinderObject findDetails(FinderObject findobj, long clientcomplete) throws Exception;

    List<FinderObject> findDetailsForList(List<FinderObject> forsearch,
                                          long clientcomplete, Integer batchsearchmax) throws Exception;


    public FinderObject assignDetailToBook(FinderObject findobj, FoundDetailsDao fd) throws Exception;


}
