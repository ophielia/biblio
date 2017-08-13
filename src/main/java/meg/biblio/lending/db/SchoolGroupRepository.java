package meg.biblio.lending.db;

import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.TeacherDao;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolGroupRepository extends JpaRepository<SchoolGroupDao, Long> {

    @Query("select r from SchoolGroupDao as r where r.client = :client")
    List<SchoolGroupDao> findSchoolGroupsByClient(@Param("client") ClientDao client, Sort sort);

    @Query("select r from SchoolGroupDao as r where r.client = :client and r.teacherlist = :teacher")
    List<SchoolGroupDao> findSchoolGroupsByTeacher(@Param("client") ClientDao client, @Param("teacher") List<TeacherDao> teacher);

}
