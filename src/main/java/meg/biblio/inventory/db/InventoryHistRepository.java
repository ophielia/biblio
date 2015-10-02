package meg.biblio.inventory.db;
import meg.biblio.inventory.db.dao.InventoryHistoryDao;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = InventoryHistoryDao.class)
public interface InventoryHistRepository {
}
