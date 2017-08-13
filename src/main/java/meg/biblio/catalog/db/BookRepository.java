package meg.biblio.catalog.db;

import meg.biblio.catalog.db.dao.BookDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<BookDao, Long> {

    @Query("select r from BookDao as r where r.clientid = :client and r.clientbookid = :clientbookid")
    List<BookDao> findBookByClientAssignedId(@Param("clientbookid") String clientbookid, @Param("client") Long clientid);

    @Query("select r from BookDao as r where r.barcodeid = :code")
    BookDao findBookByBarcode(@Param("code") String code);
}
