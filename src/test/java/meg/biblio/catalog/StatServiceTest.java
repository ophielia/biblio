package meg.biblio.catalog;

import java.util.List;
import java.util.Locale;

import org.junit.Assert;

import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.common.ClientService;
import meg.biblio.common.db.dao.ClientDao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class StatServiceTest {

	@Autowired
	StatService statService;

	@Autowired
	ClientService clientService;

	@Test
	public void testStatusBreakout() {
		// get test client, test locale
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Locale loc = Locale.FRANCE;

		StatBreakout sb = statService.runBreakoutStatByType(client, loc,
				StatService.StatType.STATUSBKOUT);

		// Assert not null
		Assert.assertNotNull(sb);
		// Assert has message, type
		Assert.assertNotNull(sb.getLabel());
		Assert.assertNotNull(sb.getStattype());
		// Assert at least one value
		List<BasicStat> values = sb.getBkoutValues();
		Assert.assertNotNull(values);
		Assert.assertTrue(values.size() > 0);
	}
	
	@Test
	public void testCategoryBreakout() {
		// get test client, test locale
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Locale loc = Locale.FRANCE;

		StatBreakout sb = statService.runBreakoutStatByType(client, loc,
				StatService.StatType.CATEGORYBKOUT);

		// Assert not null
		Assert.assertNotNull(sb);
		// Assert has message, type
		Assert.assertNotNull(sb.getLabel());
		Assert.assertNotNull(sb.getStattype());
		// Assert at least one value
		List<BasicStat> values = sb.getBkoutValues();
		Assert.assertNotNull(values);
		Assert.assertTrue(values.size() > 0);
	}	
	
	@Test
	public void testPopularBreakout_GL() {
		// get test client, test locale
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Locale loc = Locale.FRANCE;

		StatBreakout sb = statService.runBreakoutStatByType(client, loc,
				StatService.StatType.POPULARBKOUT_GL);

		// Assert not null
		Assert.assertNotNull(sb);
		// Assert has message, type
		Assert.assertNotNull(sb.getLabel());
		Assert.assertNotNull(sb.getStattype());
		// Assert at least one value
		List<BasicStat> values = sb.getBkoutValues();
		Assert.assertNotNull(values);
		Assert.assertTrue(values.size() > 0);
	}	
	
	@Test
	public void testPopularBreakout_YR() {
		// get test client, test locale
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Locale loc = Locale.FRANCE;

		StatBreakout sb = statService.runBreakoutStatByType(client, loc,
				StatService.StatType.POPULARBKOUT_YR);

		// Assert not null
		Assert.assertNotNull(sb);
		// Assert has message, type
		Assert.assertNotNull(sb.getLabel());
		Assert.assertNotNull(sb.getStattype());
		// Assert at least one value
		List<BasicStat> values = sb.getBkoutValues();
		Assert.assertNotNull(values);
		Assert.assertTrue(values.size() > 0);
	}	
	
	
	@Test
	public void testCOCategoryBreakout_Global() {
		// get test client, test locale
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Locale loc = Locale.FRANCE;

		StatBreakout sb = statService.runBreakoutStatByType(client, loc,
				StatService.StatType.COCATEGORYBKOUT_GL);

		// Assert not null
		Assert.assertNotNull(sb);
		// Assert has message, type
		Assert.assertNotNull(sb.getLabel());
		Assert.assertNotNull(sb.getStattype());
		// Assert at least one value
		List<BasicStat> values = sb.getBkoutValues();
		Assert.assertNotNull(values);
		Assert.assertTrue(values.size() > 0);
	}	
	
	@Test
	public void testCOCategoryBreakout_Yearly() {
		// get test client, test locale
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Locale loc = Locale.FRANCE;

		StatBreakout sb = statService.runBreakoutStatByType(client, loc,
				StatService.StatType.COCATEGORYBKOUT_YR);

		// Assert not null
		Assert.assertNotNull(sb);
		// Assert has message, type
		Assert.assertNotNull(sb.getLabel());
		Assert.assertNotNull(sb.getStattype());
		// Assert at least one value
		List<BasicStat> values = sb.getBkoutValues();
		Assert.assertNotNull(values);
		Assert.assertTrue(values.size() > 0);
	}	
	
	@Test
	public void testBasicCatalog() {
		// get test client, test locale
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Locale loc = Locale.FRANCE;

		BasicStat sb = statService.runBasicStatByType(client, loc, StatService.StatType.CATALOGCOUNT);

		// Assert not null
		Assert.assertNotNull(sb);
		// Assert has message, type
		Assert.assertNotNull(sb.getLabel());
		Assert.assertNotNull(sb.getStattype());
		// Assert has value
		Assert.assertNotNull(sb.getValue());
	}	
	
	@Test
	public void testBasicInventory() {
		// get test client, test locale
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Locale loc = Locale.FRANCE;

		BasicStat sb = statService.runBasicStatByType(client, loc, StatService.StatType.INVENTORY);

		// Assert not null
		Assert.assertNotNull(sb);
		// Assert has message, type
		Assert.assertNotNull(sb.getLabel());
		Assert.assertNotNull(sb.getStattype());
		// Assert has value
		Assert.assertNotNull(sb.getValue());
	}	
	
	@Test
	public void testCheckedOutCatalog() {
		// get test client, test locale
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Locale loc = Locale.FRANCE;

		BasicStat sb = statService.runBasicStatByType(client, loc, StatService.StatType.CHECKEDOUTCOUNT);

		// Assert not null
		Assert.assertNotNull(sb);
		// Assert has message, type
		Assert.assertNotNull(sb.getLabel());
		Assert.assertNotNull(sb.getStattype());
		// Assert has value
		Assert.assertNotNull(sb.getValue());
	}	
	
	@Test
	public void testBorrowerCount() {
		// get test client, test locale
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Locale loc = Locale.FRANCE;

		BasicStat sb = statService.runBasicStatByType(client, loc, StatService.StatType.BORROWERCOUNT);

		// Assert not null
		Assert.assertNotNull(sb);
		// Assert has message, type
		Assert.assertNotNull(sb.getLabel());
		Assert.assertNotNull(sb.getStattype());
		// Assert has value
		Assert.assertNotNull(sb.getValue());
	}	
	
	@Test
	public void testMostPopularGlobal() {
		// get test client, test locale
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Locale loc = Locale.FRANCE;

		BasicStat sb = statService.runBasicStatByType(client, loc, StatService.StatType.MOSTPOPULAR_GL);

		// Assert not null
		Assert.assertNotNull(sb);
		// Assert has message, type
		Assert.assertNotNull(sb.getLabel());
		Assert.assertNotNull(sb.getStattype());
		// Assert has value
		Assert.assertNotNull(sb.getValue());
	}	
	
	@Test
	public void testMostPopularYearly() {
		// get test client, test locale
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Locale loc = Locale.FRANCE;

		BasicStat sb = statService.runBasicStatByType(client, loc, StatService.StatType.MOSTPOPULAR_YR);

		// Assert not null
		Assert.assertNotNull(sb);
		// Assert has message, type
		Assert.assertNotNull(sb.getLabel());
		Assert.assertNotNull(sb.getStattype());
		// Assert has value
		Assert.assertNotNull(sb.getValue());
	}	
}