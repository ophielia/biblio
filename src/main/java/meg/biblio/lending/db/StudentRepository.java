package meg.biblio.lending.db;
import meg.biblio.lending.db.dao.StudentDao;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = StudentDao.class)
public interface StudentRepository {
}
