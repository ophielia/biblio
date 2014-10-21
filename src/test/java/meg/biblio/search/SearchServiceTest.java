package meg.biblio.search;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import meg.biblio.catalog.db.ArtistRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
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
public class SearchServiceTest {

	@Autowired
	SearchService searchService;

	@Autowired
	ArtistRepository artistRepo;

	
	@Before
	public void setup() {
		// put "Laura Ingalls Wilder" in db
		ArtistDao newart = new ArtistDao();
		newart.setFirstname("Laura");
		newart.setMiddlename("Ingalls");
		newart.setLastname("Wilder");
		artistRepo.saveAndFlush(newart);
		// put "John Smith" in db
		newart = new ArtistDao();
		newart.setFirstname("John");
		newart.setLastname("Smith");
		artistRepo.saveAndFlush(newart);
		// put "Michael" (lastname) in db
		newart = new ArtistDao();
		newart.setLastname("Michael");
		artistRepo.saveAndFlush(newart);		
	}
	
	@Test
	public void testFindArtistMatchingName() {
		ArtistDao testname = new ArtistDao();
		// test "Laura Ingalls Wilder" - match in db
		testname.setFirstname("LAUra");
		testname.setMiddlename("Ingalls");
		testname.setLastname("WILDER");
		ArtistDao result = searchService.findArtistMatchingName(testname);
		Assert.assertNotNull(result);
		Assert.assertEquals(testname.getLastname(), testname.getLastname());
		
		
		// test "Laura Wilder"
		testname = new ArtistDao();
		testname.setFirstname("Laura");
		testname.setLastname("Wilder");		
		result = searchService.findArtistMatchingName(testname);
		Assert.assertNull(result);
		
		
		// test "John Jacob Smith" - "John Smith" in db
		testname = new ArtistDao();
		testname.setFirstname("John");
		testname.setMiddlename("Jacob");
		testname.setLastname("Smith");			
		result = searchService.findArtistMatchingName(testname);
		Assert.assertNull(result);

		// test "John Smith" - db as above
		testname = new ArtistDao();
		testname.setFirstname("John");
		testname.setLastname("Smith");				
		result = searchService.findArtistMatchingName(testname);
		Assert.assertNotNull(result);
		Assert.assertEquals(testname.getLastname(),result.getLastname());
		
		// test "George Michael" - "Michael" in db
		testname = new ArtistDao();
		testname.setFirstname("George");
		testname.setLastname("Michael");	
		result = searchService.findArtistMatchingName(testname);
		Assert.assertNull(result);
		
		// test "Michael" - db as above
		testname = new ArtistDao();
		testname.setLastname("Michael");	
		result = searchService.findArtistMatchingName(testname);
		Assert.assertNotNull(result);
		Assert.assertEquals(testname.getLastname(),result.getLastname());		
	}

}