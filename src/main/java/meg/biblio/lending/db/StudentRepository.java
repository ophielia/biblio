package meg.biblio.lending.db;
import java.util.List;

import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = StudentDao.class)
public interface StudentRepository {

	@Query("select r from StudentDao as r where r.client = :client and r.active = true and r.schoolgroup is null")
	List<StudentDao> findActiveUnassignedStudents(@Param("client") ClientDao client, Sort sort);

	@Query("select r from StudentDao as r where r.client = :client and r.schoolgroup = :schoolgroup and r.active = true")
	List<StudentDao> findActiveStudentsForClass(@Param("schoolgroup") SchoolGroupDao schoolgroup,
			@Param("client") ClientDao client, Sort sort);
	
	@Query("select r from StudentDao as r where r.client = :client and r.active = true")
	List<StudentDao> findActiveStudentsForClient(@Param("client") ClientDao client);	

}
