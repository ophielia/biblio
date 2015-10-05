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
import meg.biblio.inventory.db.dao.InvStackDisplay;
import meg.biblio.inventory.db.dao.InventoryDao;

public interface InventoryService {


	public static final class CountStatus {
		public static final long COUNTED = 2;
		public static final long RECONCILED = 3;
	}

	InventoryDao beginInventory(ClientDao client);

	void cancelCurrentInventory(ClientDao client);

	InventoryDao getCurrentInventory(ClientDao client);

	boolean getInventoryIsComplete(ClientDao client);

	InventoryStatus getInventoryStatus(InventoryDao inv, ClientDao client);

	List<InventoryDao> getInventoryList(ClientDao client);

	void countBook(BookDao book, Long userid, ClientDao client);

	void reconcileBook(ClientDao client, Long bookid, Long updatestatus);

	void reconcileBookList(ClientDao client, List<Long> bookidlist,
			Long updatestatus);

	List<InvStackDisplay> getStackForUser(Long userid, ClientDao client);

	void clearStackForUser(Long userid, ClientDao client);

	List<InvStackDisplay> getUncountedBooks(ClientDao client);

	InventoryDao finishInventory(ClientDao client);

	
}
