package meg.biblio.catalog.db;
import meg.biblio.catalog.db.dao.ClassificationDao;

import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = ClassificationDao.class)
public interface ClassificationRepository {
}
