package meg.biblio.common.db;
import java.util.List;

import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.db.dao.UserLoginDao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;
import org.springframework.transaction.annotation.Transactional;

@RooJpaRepository(domainType = UserLoginDao.class)
public interface LoginRepository {

	@Query("select r from UserLoginDao as r where lower(r.username) = :username")
	@Transactional(readOnly=true)
	List<UserLoginDao> findUsersByName(@Param("username") String username);
	

	@Query("select r from UserLoginDao as r where r.client = :client")
	List<UserLoginDao> findUsersByClient(@Param("client") ClientDao client);	
}
