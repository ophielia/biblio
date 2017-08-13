package meg.biblio.common.db;

import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.db.dao.UserLoginDao;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LoginRepository extends JpaRepository<UserLoginDao, Long> {

    @Query("select r from UserLoginDao as r where lower(r.username) = :username")
    @Transactional(readOnly = true)
    List<UserLoginDao> findUsersByName(@Param("username") String username);


    @Query("select r from UserLoginDao as r, RoleDao as au where au.id = r.role and r.client = :client and au.rolename <> 'ROLE_SUPERADMIN'")
    List<UserLoginDao> findAllUsersByClient(@Param("client") ClientDao client, Sort sort);

    @Query("select r from UserLoginDao as r, RoleDao as au where au.id = r.role and r.client = :client and au.rolename <> 'ROLE_SUPERADMIN'")
    List<UserLoginDao> findUsersForClient(@Param("client") ClientDao client, Sort sort);


}

