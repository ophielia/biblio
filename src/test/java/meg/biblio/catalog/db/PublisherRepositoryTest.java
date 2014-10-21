package meg.biblio.catalog.db;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import meg.biblio.catalog.db.ArtistRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.web.model.BookModel;

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
public class PublisherRepositoryTest {

	@Autowired
	PublisherRepository pubRepo;

		
	@Before
	public void setup() {
		PublisherDao pub = new PublisherDao();
		pub.setName("TestPub");
		pubRepo.save(pub);
	}
	
	@Test
	public void testFindPublisherByName() {
		List<PublisherDao> results = pubRepo.findPublisherByName("tteessddtt");
		Assert.assertNotNull(results);
		Assert.assertEquals(0,results.size());
		
		// now, for the one we know is there....
		results = pubRepo.findPublisherByName("testpub");
		Assert.assertNotNull(results);
		Assert.assertEquals(1,results.size());
		
	}
}