package meg.biblio.lending.db;
import meg.biblio.lending.db.dao.TeacherDao;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = TeacherDao.class)
public interface TeacherRepository {
}
