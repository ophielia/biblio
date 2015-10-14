package meg.biblio.inventory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.ClientService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.inventory.db.InventoryHistRepository;
import meg.biblio.inventory.db.InventoryRepository;
import meg.biblio.inventory.db.dao.InvStackDisplay;
import meg.biblio.inventory.db.dao.InventoryDao;
import meg.biblio.inventory.db.dao.InventoryHistoryDao;
import meg.biblio.search.BookSearchCriteria;
import meg.biblio.search.SearchService;
import meg.tools.DateUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class InventoryServiceTest {

	@Autowired
	InventoryService invService;

	@Autowired
	ClientService clientService;

	@Autowired
	SearchService searchService;
	
	@Autowired
	InventoryRepository invRepo;
	
	@Autowired
	InventoryHistRepository invHistRepo;
	
	@Autowired
	BookRepository bookRepo;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	Long shelvedid;
	Long lostid;
	Long countedid;
	
	
	@Before
	public void setup() {
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
// create two books - one with status shelved, and one with status not found in inventory
		BookDao shelved = new BookDao();
		shelved.setClientid(clientid);
		shelved.getBookdetail().setTitle("Tidings of comfort");
		shelved.setStatus(CatalogService.Status.SHELVED);
		shelved = bookRepo.save(shelved);
		shelvedid=shelved.getId();
		
		BookDao lost = new BookDao();
		lost.setClientid(clientid);
		lost.getBookdetail().setTitle("Super Smash");
		lost.setStatus(CatalogService.Status.LOSTBYBORROWER);
		lost = bookRepo.save(lost);
		lostid=lost.getId();
		
		BookDao counted = new BookDao();
		counted.setClientid(clientid);
		counted.getBookdetail().setTitle("A book to be counted");
		counted.setStatus(CatalogService.Status.SHELVED);
		counted = bookRepo.save(lost);
		countedid=counted.getId();		
	}

	/**
	 * tests creation of an inventory object. After creation, an inventory
	 * object should have a start date, completed as null, and a number counted
	 * more than one.
	 */
	@Test
	public void testBeginInventory() {
		// get test client
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);

		// make call
		InventoryDao inv = invService.beginInventory(client);

		// test that the inventory is created
		Assert.assertNotNull(inv);
		// test that start date is today
		Date invstartdate = inv.getStartdate();
		Assert.assertTrue(DateUtils.isToday(invstartdate));

		// test that end date is null;
		Assert.assertNull(inv.getEnddate());

		// test that completed is false
		Assert.assertNull(inv.getCompleted());

		// test that tocount is more than one
		Assert.assertTrue(inv.getTobecounted().intValue() > 1);

		// test second call - should fail, because only one inventory
		// can be active at a time.
		inv = invService.beginInventory(client);

		// should be null
		Assert.assertNull(inv);
		
		// test that books are marked as tocount
		BookSearchCriteria criteria = new BookSearchCriteria();
		List<Long> excludedstatus = new ArrayList<Long>();
		excludedstatus.add(CatalogService.Status.INVNOTFOUND);
		excludedstatus.add(CatalogService.Status.LOSTBYBORROWER);
		excludedstatus.add(CatalogService.Status.REMOVEDFROMCIRC);
		criteria.setStatuslist(excludedstatus);
		criteria.setInstatuslist(false);
		List<BookDao> found = searchService.findBooksForCriteria(criteria, null, clientid);
		// Assert that all these books have tocount as true
		boolean alltocount=true;
		for (BookDao book:found) {
			if (book.getTocount()==null || !book.getTocount() ) {
				alltocount=false;
				break;
			}
		}
		Assert.assertTrue(alltocount);
	}

	/**
	 * After cancelInventory is run when there is an inventory in progress, the
	 * inventory object should have an end date of non null, and a completed
	 * field of false.
	 */
	@Test
	public void testCancelInventory() {
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		// if no inventory is currently running, one should be started
		InventoryDao current = invService.getCurrentInventory(client);
		if (current==null) {
			current = invService.beginInventory(client);
		}
		// save the id on currentinv
		Long invid = current.getId();
		// service call
		invService.cancelCurrentInventory(client);
		// retrieve currentinv
		InventoryDao check =invRepo.findOne(invid);
		// assert not null
		Assert.assertNotNull(check);
		// assert enddate not null
		Assert.assertNotNull(check.getEnddate());
		// assert completed false
		Assert.assertFalse(check.getCompleted());
	}
	
	/**
	 * finish inventory should only run if the inventory is complete.  It should
	 * mark the inventoryobject as complete and add an enddate.  It should also
	 * clear all inventory info in BookDao
	 */
	 @Test
	public void testFinishInventory() {
			// get client and userid
			Long clientid = clientService.getTestClientId();
			ClientDao client = clientService.getClientForKey(clientid);
			Long dummyuserid = 9999L;
			// start a new inventory
		InventoryDao current = invService.getCurrentInventory(client);
		if (current!=null) {
			invService.cancelCurrentInventory(client);
		} 
		current = invService.beginInventory(client);
		Long invid = current.getId();
		
		// count all books
		BookSearchCriteria criteria = new BookSearchCriteria();
		List<Long> excludedstatus = new ArrayList<Long>();
		excludedstatus.add(CatalogService.Status.INVNOTFOUND);
		excludedstatus.add(CatalogService.Status.LOSTBYBORROWER);
		excludedstatus.add(CatalogService.Status.REMOVEDFROMCIRC);
		criteria.setStatuslist(excludedstatus);
		criteria.setInstatuslist(false);
		List<BookDao> found = searchService.findBooksForCriteria(criteria, null, clientid);
		int countcount = found.size();
		// Assert that all these books have tocount as true
		for (BookDao book:found) {
				invService.countBook(book, dummyuserid, client, true);
		}
		
		// service call
		InventoryDao test = invService.finishInventory(client);
		
		// assert end date not null
		Assert.assertNotNull(test);
		Assert.assertNotNull(test.getEnddate());
		// assert complete is true
		Assert.assertTrue(test.getCompleted());
		// assert counted is equal to what it should be
		Assert.assertEquals(new Integer(countcount),test.getTotalcounted());
		// get all books
		found = searchService.findBooksForCriteria(criteria, null, clientid);
		// Assert that all these books have tocount  null, countstatus  null
		boolean allcleared=true;
		for (BookDao book:found) {
				if ((book.getTocount()!=null&&book.getTocount()) || book.getCountstatus()!=null) {
					allcleared=false;
					break;
				} 
		}
		Assert.assertTrue(allcleared);
	}
	/**
	 * Tests that inventory is deemed incomplete if books remain to be counted,
	 * and complete if no books remain to be counted.
	 */
	@Test
	public void testInventoryComplete() {
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		// first testing not complete
		// cancel any current and start a new inventory
		InventoryDao current = invService.getCurrentInventory(client);
		if (current!=null) {
			invService.cancelCurrentInventory(client);
			current = invService.beginInventory(client);
		}
		// a new inventory should not be complete
		boolean test = invService.getInventoryIsComplete(client);
		
		// should be false
		Assert.assertFalse(test);
		
		// should test the complete as well
		// MM TODO
	}
	
	
	
	@Test
	public void testInventoryList() {
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		// first testing not complete
		// cancel any current and start a new inventory
		InventoryDao current = invService.getCurrentInventory(client);
		if (current!=null) {
			invService.cancelCurrentInventory(client);
			current = invService.beginInventory(client);
		}
		invService.cancelCurrentInventory(client);
		current = invService.beginInventory(client);
		invService.cancelCurrentInventory(client);
		current = invService.beginInventory(client);
		
		List<InventoryDao> testlist = invService.getInventoryList(client);
		
		// shouldn't be null
		Assert.assertNotNull(testlist);
		// size should be greater than 1
		Assert.assertTrue(testlist.size()>=1);
		
		// should test the complete as well
		// MM TODO
	}
	

	/** to test this method, we need to count one book with the status of shelved,
	 * and ensure that the book has a count status of counted afterwards.  We also
	 * need to count one book that has the status of not found in inventory, and ensure
	 * that that book has a counterid, and also a InvHistoryDao object.
	 */
	@Test
	public void testCountBook() {
		// get client and userid
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Long dummyuserid = 9999L;
		
		// start new inventory
		InventoryDao current = invService.getCurrentInventory(client);
		if (current!=null) {
			invService.cancelCurrentInventory(client);
		} 
		current = invService.beginInventory(client);
		
		// get shelved book
		BookDao book = bookRepo.findOne(shelvedid);
		// count book - service call
		invService.countBook(book,dummyuserid,client, true);
		// reretrieve shelved book - ensure that
		BookDao test = bookRepo.findOne(shelvedid);
		// book not null
		Assert.assertNotNull(test);
		// counted status = InventoryService.CountStatus.COUNTED
		Assert.assertEquals(test.getCountstatus(),new Long(InventoryService.CountStatus.COUNTED));
		// userid = dummy userid
		Assert.assertEquals(test.getUserid(),dummyuserid);
		
		
		// next test - lost book
		book = bookRepo.findOne(lostid);
		// count book - service call
		invService.countBook(book,dummyuserid,client, true);
		// reretrieve lost book - ensure that
		test = bookRepo.findOne(lostid);
		// book not null
		Assert.assertNotNull(test);
		// counted status null
		Assert.assertNull(test.getCountstatus());
		// InvHistoryDao object exists for book
		List<InventoryHistoryDao> refound = invHistRepo.getRefoundBooksForInventory(current);
		boolean found=false;
		for (InventoryHistoryDao rec:refound) {
			if (rec.getBook()!=null) {
				if (rec.getBook().getId().longValue()==lostid.longValue()) {
					found=true;
					break;
				}
			}
		}
		Assert.assertTrue(found);
		// userid = dummy userid
		Assert.assertEquals(test.getUserid(),dummyuserid);
		
		// test no count if no inventory
		invService.cancelCurrentInventory(client);
		book = bookRepo.findOne(shelvedid);
		entityManager.refresh(book);
		// count book - service call
		invService.countBook(book,dummyuserid,client, true);
		// reretrieve shelved book - ensure that
		test = bookRepo.findOne(shelvedid);
		// book not null
		Assert.assertNotNull(test);
		// counted status = null
		Assert.assertNull(test.getCountstatus());
		// userid = dummy userid
		Assert.assertNull(test.getUserid());
		
	}
	
	/** to test this method, we need to reconcile a book with the status of shelved
	 * and ensure that the book has a count status of counted, and a status of notfoundininventory
	 * afterwards.  We also need to make sure that a book is not reconciled, when no inventory
	 * is in progress.
	 */
	@Test
	public void testReconcileBook() {
		// get client and userid
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		
		// start new inventory
		InventoryDao current = invService.getCurrentInventory(client);
		if (current!=null) {
			invService.cancelCurrentInventory(client);
		} 
		current = invService.beginInventory(client);
		
		// get shelved book - and set status to shelved.
		BookDao book = bookRepo.findOne(shelvedid);
		book.setStatus(CatalogService.Status.SHELVED);
		bookRepo.save(book);
		
		// count book - service call
		invService.reconcileBook(client,shelvedid,CatalogService.Status.INVNOTFOUND, null);  

		// reretrieve shelved book - ensure that
		BookDao test = bookRepo.findOne(shelvedid);
		// book not null
		Assert.assertNotNull(test);
		// status is notfoundininventory
		Assert.assertEquals( new Long(CatalogService.Status.INVNOTFOUND),test.getStatus());
		// inventoryHistory exists for book
		List<InventoryHistoryDao> refound = invHistRepo.getReconciledBooksForInventory(current);
		boolean found=false;
		for (InventoryHistoryDao rec:refound) {
			if (rec.getBook()!=null) {
				if (rec.getBook().getId().longValue()==shelvedid.longValue()) {
					found=true;
					break;
				}
			}
		}
		Assert.assertTrue(found);
		// countstatus is reconciled
		Assert.assertEquals(new Long(InventoryService.CountStatus.RECONCILED),test.getCountstatus());
		
		
		// test no reconcile if no inventory
		invService.cancelCurrentInventory(client);
		book = bookRepo.findOne(shelvedid);
		entityManager.refresh(book);
		// count book - service call
		invService.reconcileBook(client,shelvedid,CatalogService.Status.INVNOTFOUND, null);
		// reretrieve shelved book - ensure that
		test = bookRepo.findOne(shelvedid);
		// book not null
		Assert.assertNotNull(test);
		// counted status = null
		Assert.assertNull(test.getCountstatus());
		// userid = dummy userid
		Assert.assertNull(test.getUserid());
		
	}	
	
	@Test
	public void testReconcileBookList() {
		// get client and userid
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Long updatestatus = new Long(CatalogService.Status.INVNOTFOUND);
		// start new inventory
		InventoryDao current = invService.getCurrentInventory(client);
		if (current!=null) {
			invService.cancelCurrentInventory(client);
		} 
		current = invService.beginInventory(client);
		
		// make list of bookids
		List<Long> bookids = new ArrayList<Long>();
		bookids.add(shelvedid);
		bookids.add(lostid);
		
		// service call
		invService.reconcileBookList(client, bookids, updatestatus);
		
		// ensure that both ids have a status of INVNOTFOUND, and an entry in InvHistoryDao
		// make hash of reconciled books
		List<Long> reconciledlist = new ArrayList<Long>();
		List<InventoryHistoryDao> refound = invHistRepo.getReconciledBooksForInventory(current);
		for (InventoryHistoryDao hist:refound) {
			reconciledlist.add(hist.getBook().getId());
		}
		
		// test updated books
		for (Long testid:bookids) {
			BookDao test = bookRepo.findOne(testid);
			// assert status INVNOTFOUND
			Assert.assertEquals(updatestatus,test.getStatus());
			// assert countstatus reconciled
			Assert.assertEquals(new Long(InventoryService.CountStatus.RECONCILED), test.getCountstatus());
			// assert existance in reconciledlist
			Assert.assertTrue(reconciledlist.contains(testid));
		}
	}

	
	@Test
	public void testBlowUp() {
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		List<InvStackDisplay> dips = invService.getStackForUser(9999L,client);
		long testing=111;
	}
	
	/**
	 * Test getStackForUser.  After counting two books for a new inventory, 
	 * the getStackForUser call should return a list of two books, each
	 * with the correct bookid.
	 */
	// MM TODO  - add to database test
	public void testGetStackForUser() {
		// get client and userid
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Long dummyuserid = 9999L;
		
		// start new inventory
		InventoryDao current = invService.getCurrentInventory(client);
		if (current!=null) {
			invService.cancelCurrentInventory(client);
		} 
		current = invService.beginInventory(client);
		
		// count shelved
		BookDao book = bookRepo.findOne(shelvedid);
		invService.countBook(book, dummyuserid, client, true);
		entityManager.refresh(book);
		book = bookRepo.findOne(lostid);
		invService.countBook(book, dummyuserid, client, true);	
		
		// service call
		List<InvStackDisplay> dips = invService.getStackForUser(dummyuserid,client);
		
		// list should have two members
		Assert.assertNotNull(dips);
		Assert.assertTrue(dips.size()==2);
		// put list into hash by id
		HashMap<Long,InvStackDisplay> hash=new HashMap<Long,InvStackDisplay>();
		for (InvStackDisplay di:dips) {
			hash.put(di.getBookid(),di);
		}
		// test shelved - should have counteddate set, countstatus set, and counterid set
		InvStackDisplay test = hash.get(shelvedid);
		Assert.assertNotNull(test);
		Assert.assertEquals(test.getUserid(), dummyuserid);
		Assert.assertNotNull(test.getCounteddate());
		Assert.assertNotNull(test.getCountstatus());
		Assert.assertEquals(new Long(InventoryService.CountStatus.COUNTED),test.getCountstatus());
		// test lost - should have counterid set
		test = hash.get(lostid);
		Assert.assertNotNull(test);
		Assert.assertEquals(test.getUserid(), dummyuserid);
		Assert.assertNull(test.getCounteddate());
		Assert.assertNull(test.getCountstatus());

	}


	/** test clearStackForUser.  Count two books for a new inventory, clear the stack, 
	 * and then call getStackForUser.  Returned list should be null. 
	 */
	@Test
	public void testClearStackForUser() {
		// get client and userid
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Long dummyuserid = 9999L;
		
		// start new inventory
		InventoryDao current = invService.getCurrentInventory(client);
		if (current!=null) {
			invService.cancelCurrentInventory(client);
		} 
		current = invService.beginInventory(client);
		
		// count shelved
		BookDao book = bookRepo.findOne(shelvedid);
		invService.countBook(book, dummyuserid, client, true);
		book = bookRepo.findOne(lostid);
		invService.countBook(book, dummyuserid, client, true);	

		
		// service call
		invService.clearStackForUser(dummyuserid,client);
		
		List<InvStackDisplay> dips = invService.getStackForUser(dummyuserid,client);
		// Assert that list is null
		Assert.assertNotNull(dips);
		Assert.assertTrue(dips.size()==0);
	}
	
	
	/**
	 * test uncounted books. count counted book, don't count shelved.  getUncountedBooks
	 * should include shelvedid.
	 */
	// MM TODO  - add to database test
	public void testGetUncountedBooks() {
		// get client and userid
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Long dummyuserid = 9999L;
		
		// start new inventory
		InventoryDao current = invService.getCurrentInventory(client);
		if (current!=null) {
			invService.cancelCurrentInventory(client);
		} 
		current = invService.beginInventory(client);
		
		// count counted
		BookDao book = bookRepo.findOne(countedid);
		entityManager.refresh(book);
		invService.countBook(book, dummyuserid, client, true);
		book = bookRepo.findOne(lostid);
		invService.countBook(book, dummyuserid, client, true);	

		// service call
		List<InvStackDisplay> uncounted = invService.getUncountedBooks(client);
		
		// put uncounted into hash
		HashMap<Long,InvStackDisplay> hash=new HashMap<Long,InvStackDisplay>();
		for (InvStackDisplay di:uncounted) {
			hash.put(di.getBookid(),di);
		}
		// assert that shelvedid is amount the hash
		Assert.assertTrue(hash.containsKey(shelvedid));
	}
}