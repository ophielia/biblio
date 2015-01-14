package meg.biblio.lending.db;
import java.util.List;

import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.TeacherDao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = TeacherDao.class)
public interface TeacherRepository {
	
	@Query("select r from TeacherDao as r where r.client = :client and r.active = true")
	List<TeacherDao> findActiveTeachersForClient(@Param("client") ClientDao client);	
	
	@Query("select r from TeacherDao as r where r.client = :client and r.active = true and r.schoolgroup = :schoolgroup ")
	List<TeacherDao> findActiveTeachersForClientAndClass(@Param("client") ClientDao client,@Param("schoolgroup") SchoolGroupDao sgroup);
	
	@Query("select r from TeacherDao as r where r.client = :client and r.active = true and r.schoolgroup = :schoolgroup and r.barcodeid is null")
	List<TeacherDao> findActiveTeachersForClientAndClassWithoutBarcode(@Param("client") ClientDao client,@Param("schoolgroup") SchoolGroupDao sgroup);

	@Query("select r from TeacherDao as r where r.client = :client ")
	List<TeacherDao> findAllTeachersForClient(@Param("client") ClientDao client);

}
