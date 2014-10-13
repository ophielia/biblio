package meg.biblio.common.db;
import meg.biblio.common.db.dao.SelectKeyDao;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = SelectKeyDao.class)
public interface SelectKeyRepository {
}
