package meg.biblio.inventory.db;
import java.util.List;

import meg.biblio.inventory.db.dao.InventoryDao;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = InventoryDao.class)
public interface InventoryRepository {
	
	@Query("select r from InventoryDao as r where r.enddate is null and r.clientid = :clientid")
	InventoryDao getCurrentInventoryForClient(@Param("clientid") Long clientid);
	
	@Query("select r from InventoryDao as r where r.clientid = :clientid")
	List<InventoryDao> getInventoryListForClient(@Param("clientid") Long clientid, Sort sort);
	
}
