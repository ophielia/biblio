package meg.biblio.catalog.db;
import java.util.List;

import meg.biblio.catalog.db.dao.ClassificationDao;

import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = ClassificationDao.class)
public interface ClassificationRepository {
	
	public List<ClassificationDao> findByClientidAndLanguage(Long clientid, String language);
	
	
}
