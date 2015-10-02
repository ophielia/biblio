package meg.biblio.inventory;

import java.util.Date;

import meg.biblio.common.ClientService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.inventory.db.InventoryRepository;
import meg.biblio.inventory.db.dao.InventoryDao;
import meg.tools.DateUtils;

import org.junit.Assert;
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
	
	@Test
	public void testMarkerMethod() {

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
}