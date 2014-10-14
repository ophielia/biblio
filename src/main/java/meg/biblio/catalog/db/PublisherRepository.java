package meg.biblio.catalog.db;
import meg.biblio.catalog.db.dao.PublisherDao;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = PublisherDao.class)
public interface PublisherRepository {
}
