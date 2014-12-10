package meg.biblio.lending.db;
import java.util.List;

import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.db.dao.TeacherDao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = TeacherDao.class)
public interface TeacherRepository {
	
	@Query("select r from TeacherDao as r where r.client = :client and r.active = true")
	List<TeacherDao> findActiveTeachersForClient(@Param("client") ClientDao client);		
}
