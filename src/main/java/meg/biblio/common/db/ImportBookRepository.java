package meg.biblio.common.db;
import meg.biblio.common.db.dao.ImportBookDao;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = ImportBookDao.class)
public interface ImportBookRepository {
}
