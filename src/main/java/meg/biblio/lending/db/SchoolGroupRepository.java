package meg.biblio.lending.db;
import java.util.List;

import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.TeacherDao;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = SchoolGroupDao.class)
public interface SchoolGroupRepository {
	
	@Query("select r from SchoolGroupDao as r where r.client = :client")
	List<SchoolGroupDao> findSchoolGroupsByClient(@Param("client") ClientDao client, Sort sort);

	@Query("select r from SchoolGroupDao as r where r.client = :client and r.teacherlist = :teacher")
	List<SchoolGroupDao> findSchoolGroupsByTeacher(@Param("client") ClientDao client, @Param("teacher") List<TeacherDao> teacher);

}
