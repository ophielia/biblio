package meg.biblio.common.db;
import java.util.List;

import meg.biblio.common.db.dao.RoleDao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;
import org.springframework.transaction.annotation.Transactional;

@RooJpaRepository(domainType = RoleDao.class)
public interface RoleRepository {

	@Query("select r from RoleDao as r where r.rolename = :rolename")
	@Transactional(readOnly = true)
	List<RoleDao> findRolesByName(@Param("rolename") String rolename);
}


