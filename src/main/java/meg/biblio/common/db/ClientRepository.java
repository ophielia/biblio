package meg.biblio.common.db;
import meg.biblio.common.db.dao.ClientDao;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = ClientDao.class)
public interface ClientRepository {
}
