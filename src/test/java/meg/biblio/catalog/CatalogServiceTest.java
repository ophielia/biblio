package meg.biblio.catalog;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.web.model.BookModel;

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
public class CatalogServiceTest {

	@Autowired
	CatalogService catalogService;

	@Test
	public void testCreateCatalogEntry() {
		BookDao book = new BookDao();
		book.setTitle("the quick brown fox");
		ArtistDao author = new ArtistDao();
		author.setFirstname("george");
		author.setLastname("willikins");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		ArtistDao illust = new ArtistDao();
		illust.setFirstname("george");
		illust.setLastname("willikins");
		List<ArtistDao> illustrators = new ArrayList<ArtistDao>();
		illustrators.add(author);
		book.setAuthors(authors);
		book.setIllustrators(illustrators);
		
		BookModel model = new BookModel(book);
		
		// service call
		BookModel result = catalogService.createCatalogEntryFromBookModel(1L, model);
		
		// check call
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getBookid());
		Assert.assertNotNull(result.getClientid());
		Assert.assertNotNull(result.getAuthors());
		Assert.assertNotNull(result.getIllustrators());
	}
	
	@Test
	public void testCreateCatalogEntryModelHelper() {
		BookDao book = new BookDao();
		book.setTitle("the quick brown fox");
		// create model
		BookModel model = new BookModel(book);
		// set names through model
		model.setAFname("first");
		model.setALname("last");
		model.setAMname("middle");
		model.setIFname("first");
		model.setILname("last");
		model.setIMname("middle");
		
		// process entries
		model.processAuthorEntry();
		model.processIllustratorEntry();
		
		// service call to create book
		BookModel result = catalogService.createCatalogEntryFromBookModel(1L, model);
		
		// check call
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getBookid());
		Assert.assertNotNull(result.getClientid());

		// check authors and illustrators
		Assert.assertNotNull(result.getAuthors());
		Assert.assertNotNull(result.getIllustrators());
	}	
	
	@Test
	public void testGetDetailsSingle() throws GeneralSecurityException, IOException {
		catalogService.fillInDetailsForSingleBook(1L);
		
	}

}