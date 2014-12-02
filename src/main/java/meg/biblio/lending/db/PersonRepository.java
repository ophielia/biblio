package meg.biblio.lending.db;
import meg.biblio.lending.db.dao.PersonDao;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = PersonDao.class)
public interface PersonRepository {
}
