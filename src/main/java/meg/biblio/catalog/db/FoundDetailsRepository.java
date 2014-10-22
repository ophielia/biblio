package meg.biblio.catalog.db;
import java.util.List;

import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.db.dao.PublisherDao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = FoundDetailsDao.class)
public interface FoundDetailsRepository {

	@Query("select r from FoundDetailsDao as r where bookid = :id")
	List<FoundDetailsDao> findDetailsForBook(@Param("id") Long id);
	
}
