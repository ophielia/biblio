package meg.biblio.catalog.db;
import java.util.List;

import meg.biblio.catalog.db.dao.BookDao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = FoundWordsDao.class)
public interface FoundWordsRepository {

	
	@Query("select r from FoundWordsDao as r where book = :book")
	List<FoundWordsDao> findWordsForBook(@Param("book") BookDao book);
}
