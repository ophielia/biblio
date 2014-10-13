package meg.biblio.catalog.db;
import meg.biblio.catalog.db.dao.SubjectDao;

import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = SubjectDao.class)
public interface SubjectRepository {
	
}
