package meg.biblio.lending.db;
import java.util.List;

import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.db.dao.UserLoginDao;
import meg.biblio.lending.db.dao.SchoolGroupDao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = SchoolGroupDao.class)
public interface SchoolGroupRepository {
	
	@Query("select r from SchoolGroupDao as r where r.client = :client")
	List<SchoolGroupDao> findSchoolGroupByClient(@Param("client") ClientDao client);	
}
