package meg.biblio.inventory.db;
import java.util.List;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.inventory.db.dao.InventoryDao;
import meg.biblio.inventory.db.dao.InventoryHistoryDao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = InventoryHistoryDao.class)
public interface InventoryHistRepository {

	@Query("select r from InventoryHistoryDao as r where r.foundbook = true and r.inventory = :inventory") 
	List<InventoryHistoryDao> getRefoundBooksForInventory(@Param("inventory") InventoryDao inv);
	
	@Query("select r from InventoryHistoryDao as r where r.foundbook= false and r.inventory = :inventory") // shelved
	List<InventoryHistoryDao> getReconciledBooksForInventory(@Param("inventory") InventoryDao inv);
	
	@Query("select r from InventoryHistoryDao as r where  r.inventory = :inventory") // shelved
	List<InventoryHistoryDao> getHistoryForInventory(@Param("inventory") InventoryDao inv);

	@Query("select r from InventoryHistoryDao as r where r.inventory = :inventory and r.book = :book")
	List<InventoryHistoryDao> getFoundInInventory(@Param("inventory") InventoryDao inv,
			@Param("book") BookDao countedbook);
}
