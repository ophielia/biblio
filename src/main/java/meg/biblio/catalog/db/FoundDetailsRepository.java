package meg.biblio.catalog.db;

import meg.biblio.catalog.db.dao.FoundDetailsDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoundDetailsRepository extends JpaRepository<FoundDetailsDao, Long> {

    @Query("select r from FoundDetailsDao as r where bookdetailid = :id")
    List<FoundDetailsDao> findDetailsForBook(@Param("id") Long id);

}
