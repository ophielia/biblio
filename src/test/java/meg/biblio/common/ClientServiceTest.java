package meg.biblio.common;

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
public class ClientServiceTest {

	@Autowired
	ClientService clientService;

	@Test
	public void testGetCurrentClientKeyDefault() {
		Long result = clientService.getCurrentClientKey(null);
		Assert.assertNotNull(result);
		Assert.assertEquals(1L, result.longValue());
	}


}