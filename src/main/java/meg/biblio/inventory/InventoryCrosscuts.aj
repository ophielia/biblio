package meg.biblio.inventory;

import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.ClientService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.inventory.db.dao.InventoryDao;
import meg.biblio.lending.db.LoanRecordRepository;
import meg.biblio.lending.db.dao.LoanRecordDao;

import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;

public aspect InventoryCrosscuts {

	@Autowired
	ClientService clientService;

	@Autowired
	InventoryService invService;

	@Autowired
	LoanRecordRepository lrRepo;
	

	@Autowired
	BookRepository bookRepo;	
	
    @Before("execution(* meg.biblio.lending.LendingService.returnBook(..)) && args(loanrecordid,clientid)")
    public void beforeReturnByLoanRecordId(Long loanrecordid,Long clientid) {
    	// get client
    	ClientDao client = clientService.getClientForKey(clientid);
    	
    	// determine if inventory is in progress
    	InventoryDao current = invService.getCurrentInventory(client);
    	
    	// if so, count book
    	if (current!=null && loanrecordid!=null) {
    		LoanRecordDao lr = lrRepo.findOne(loanrecordid);
    		BookDao book = lr.getBook();
    		if (book!=null) {
    			invService.countBook(book, null, client, false);
    		}
    	}
    }
    
    @Before("execution(* meg.biblio.lending.LendingService.returnBookByBookid(..)) && args(bookid,clientid)")
    public void beforeReturnByBookId(Long bookid,Long clientid) {
    	// get client
    	ClientDao client = clientService.getClientForKey(clientid);
    	
    	// determine if inventory is in progress
    	InventoryDao current = invService.getCurrentInventory(client);
    	
    	// if so, count book
    	if (current!=null && bookid!=null) {
    		BookDao book = bookRepo.findOne(bookid);
    		if (book!=null) {
    			invService.countBook(book, null, client, false);
    		}
    	}
    }    
    
    //@AfterReturning(
    //pointcut="com.xyz.myapp.SystemArchitecture.dataAccessOperation()",
    //returning="retVal")
}
