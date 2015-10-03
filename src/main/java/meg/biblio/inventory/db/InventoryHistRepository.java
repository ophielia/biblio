package meg.biblio.inventory.db;
import java.util.List;

import meg.biblio.inventory.db.dao.InventoryDao;
import meg.biblio.inventory.db.dao.InventoryHistoryDao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = InventoryHistoryDao.class)
public interface InventoryHistRepository {

	@Query("select r from InventoryHistoryDao as r where r.newstatus = 2 and r.inventory = :inventory") // shelved
	List<InventoryHistoryDao> getRefoundBooksForInventory(@Param("inventory") InventoryDao inv);
}
