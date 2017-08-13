package meg.biblio.common.db;

import meg.biblio.common.db.dao.RoleDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<RoleDao, Long> {

    @Query("select r from RoleDao as r where r.rolename = :rolename")
    @Transactional(readOnly = true)
    List<RoleDao> findRolesByName(@Param("rolename") String rolename);
}


