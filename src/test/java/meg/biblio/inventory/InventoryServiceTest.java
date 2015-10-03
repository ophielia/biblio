package meg.biblio.inventory;

import java.util.Date;
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
import meg.biblio.inventory.db.dao.InventoryDao;
import meg.biblio.inventory.db.dao.InventoryHistoryDao;
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
	InventoryRepository invRepo;
	
	@Autowired
	InventoryHistRepository invHistRepo;
	
	@Autowired
	BookRepository bookRepo;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	Long shelvedid;
	Long lostid;
	
	
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
		invService.countBook(book,dummyuserid,client);
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
		invService.countBook(book,dummyuserid,client);
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
		invService.countBook(book,dummyuserid,client);
		// reretrieve shelved book - ensure that
		test = bookRepo.findOne(shelvedid);
		// book not null
		Assert.assertNotNull(test);
		// counted status = null
		Assert.assertNull(test.getCountstatus());
		// userid = dummy userid
		Assert.assertNull(test.getUserid());
		
	}
}