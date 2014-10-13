package meg.biblio.common.db;
import meg.biblio.common.db.dao.SelectValueDao;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = SelectValueDao.class)
public interface SelectValueRepository {
}
