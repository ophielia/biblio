package meg.biblio.lending.db;

import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.TeacherDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherRepository extends JpaRepository<TeacherDao, Long> {

    @Query("select r from TeacherDao as r where r.client = :client and r.active = true")
    List<TeacherDao> findActiveTeachersForClient(@Param("client") ClientDao client);

    @Query("select r from TeacherDao as r where r.client = :client and r.active = true and r.schoolgroup = :schoolgroup ")
    List<TeacherDao> findActiveTeachersForClientAndClass(@Param("client") ClientDao client, @Param("schoolgroup") SchoolGroupDao sgroup);

    @Query("select r from TeacherDao as r where r.client = :client and r.active = true and r.schoolgroup = :schoolgroup and r.barcodeid is null")
    List<TeacherDao> findActiveTeachersForClientAndClassWithoutBarcode(@Param("client") ClientDao client, @Param("schoolgroup") SchoolGroupDao sgroup);

    @Query("select r from TeacherDao as r where r.client = :client ")
    List<TeacherDao> findAllTeachersForClient(@Param("client") ClientDao client);

}
