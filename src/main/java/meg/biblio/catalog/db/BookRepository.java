package meg.biblio.catalog.db;
import java.util.List;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.db.dao.SchoolGroupDao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = BookDao.class)
public interface BookRepository {
	
	@Query("select r from BookDao as r where r.clientid = :client and r.clientbookid = :clientbookid")
	List<BookDao> findBookByClientAssignedId(@Param("clientbookid") String clientbookid,@Param("client") Long clientid);

	@Query("select r from BookDao as r where r.barcodeid = :code")
	BookDao findBookByBarcode(@Param("code") String code);
}
