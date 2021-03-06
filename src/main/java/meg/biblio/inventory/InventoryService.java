package meg.biblio.inventory;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.report.TableReport;
import meg.biblio.inventory.db.dao.InvStackDisplay;
import meg.biblio.inventory.db.dao.InventoryDao;
import meg.biblio.inventory.db.dao.InventoryHistoryDao;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;

public interface InventoryService {


    public static final class CountStatus {
        public static final long COUNTED = 2;
        public static final long RECONCILED = 3;
    }

    public static final class HistoryType {
        public static final long ADDED = 1;
        public static final long RECONCILED = 2;
    }

    public static final String reconcilestatuslkup = "reconcilestatus";

    InventoryDao beginInventory(ClientDao client);

    void cancelCurrentInventory(ClientDao client);

    InventoryDao getCurrentInventory(ClientDao client);

    boolean getInventoryIsComplete(ClientDao client);

    InventoryStatus getInventoryStatus(InventoryDao inv, ClientDao client);

    List<InventoryDao> getInventoryList(ClientDao client);

    void countBook(BookDao book, Long userid, ClientDao client, Boolean saveinstack);

    void reconcileBook(ClientDao client, Long bookid, Long updatestatus, String note);

    void reconcileBookList(ClientDao client, List<Long> bookidlist,
                           Long updatestatus);

    List<InvStackDisplay> getStackForUser(Long userid, ClientDao client);

    void clearStackForUser(Long userid, ClientDao client);

    List<InvStackDisplay> getUncountedBooks(ClientDao client);

    InventoryDao finishInventory(ClientDao client);

    List<InventoryDao> getPreviousInventories(ClientDao client);

    InventoryDao getInventoryById(Long invid);

    List<InventoryHistoryDao> getDetailForInventory(InventoryDao inventory, long detailtype);

    InventoryStatus getLastCompleted(ClientDao client);

    TableReport getToReconcileReport(ClientDao client, Locale locale,
                                     MessageSource messageSource);


}
