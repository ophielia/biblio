package meg.biblio.catalog.db;

import meg.biblio.catalog.db.dao.SubjectDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<SubjectDao, Long> {

    @Query("select r from SubjectDao as r where lower(trim(r.listing)) = :text")
    List<SubjectDao> findSubjectByText(@Param("text") String text);

}
