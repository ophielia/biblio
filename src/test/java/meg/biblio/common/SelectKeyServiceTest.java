package meg.biblio.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import meg.biblio.common.db.SelectKeyRepository;
import meg.biblio.common.db.dao.SelectKeyDao;
import meg.biblio.common.db.dao.SelectValueDao;

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
public class SelectKeyServiceTest {

	@Autowired
	SelectKeyService selectService;

	@Autowired
	SelectKeyRepository keyRepo;

	private String testkey = "testkey";

	@Before
	public void setup() {
		// create key for testing
		SelectKeyDao key = new SelectKeyDao();
		key.setLookup(testkey);
		// create values for key
		List<SelectValueDao> values = new ArrayList<SelectValueDao>();
		SelectValueDao newval = new SelectValueDao();
		newval.setActive(true);
		newval.setDisporder(new Long(0));
		newval.setDisplay("one");
		newval.setLanguagekey("en");
		newval.setValue("1");
		values.add(newval);
		newval = new SelectValueDao();
		newval.setActive(true);
		newval.setDisporder(new Long(1));
		newval.setDisplay("two");
		newval.setLanguagekey("en");
		newval.setValue("2");
		values.add(newval);
		newval = new SelectValueDao();
		newval.setActive(true);
		newval.setDisporder(new Long(2));
		newval.setDisplay("three");
		newval.setLanguagekey("en");
		newval.setValue("3");
		values.add(newval);
		// now, same values in french
		newval = new SelectValueDao();
		newval.setActive(true);
		newval.setDisporder(new Long(0));
		newval.setDisplay("un");
		newval.setLanguagekey("fr");
		newval.setValue("1");
		values.add(newval);
		newval = new SelectValueDao();
		newval.setActive(true);
		newval.setDisporder(new Long(1));
		newval.setDisplay("deux");
		newval.setLanguagekey("fr");
		newval.setValue("2");
		values.add(newval);
		newval = new SelectValueDao();
		newval.setActive(true);
		newval.setDisporder(new Long(2));
		newval.setDisplay("trois");
		newval.setLanguagekey("fr");
		newval.setValue("3");
		values.add(newval);
		// now - all values to the key
		key.setSelectvalues(values);

		// save the key
		keyRepo.saveAndFlush(key);
	}

	@Test
	public void testGetDisplayAsHash() {
		HashMap<Long, String> displays = selectService.getDisplayHashForKey(
				testkey, "en");

		// assert not null, and three members
		Assert.assertNotNull(displays);
		Assert.assertEquals(3, displays.size());
		String test = displays.get(new Long(1));
		Assert.assertEquals("one", test);
	}

}