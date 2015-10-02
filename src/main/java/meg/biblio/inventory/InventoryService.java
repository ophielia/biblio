package meg.biblio.inventory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.catalog.db.dao.ClassificationDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.inventory.db.dao.InventoryDao;

public interface InventoryService {

	InventoryDao beginInventory(ClientDao client);

	void cancelCurrentInventory(ClientDao client);

	InventoryDao getCurrentInventory(ClientDao client);

	
}
