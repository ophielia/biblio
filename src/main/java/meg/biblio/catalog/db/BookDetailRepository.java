package meg.biblio.catalog.db;
import meg.biblio.catalog.db.dao.BookDetailDao;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = BookDetailDao.class)
public interface BookDetailRepository {
}
