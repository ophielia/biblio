package meg.biblio.catalog.db;
import java.util.List;

import meg.biblio.catalog.db.dao.PublisherDao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = PublisherDao.class)
public interface PublisherRepository {
	
	@Query("select r from PublisherDao as r where lower(trim(r.name)) = :pubname")
	List<PublisherDao> findPublisherByName(@Param("pubname") String pubname);
}
