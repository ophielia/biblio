package meg.biblio.catalog.db;
import meg.biblio.catalog.db.dao.FoundDetailsDao;

import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = FoundDetailsDao.class)
public interface FoundDetailsRepository {
	
}
