package meg.biblio.catalog.db;

import meg.biblio.catalog.db.dao.PublisherDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublisherRepository extends JpaRepository<PublisherDao, Long> {

    @Query("select r from PublisherDao as r where lower(trim(r.name)) = :pubname")
    List<PublisherDao> findPublisherByName(@Param("pubname") String pubname);
}
