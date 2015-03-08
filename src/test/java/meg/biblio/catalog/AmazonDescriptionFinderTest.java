package meg.biblio.catalog;

import java.util.ArrayList;
import java.util.List;

import meg.biblio.catalog.db.ArtistRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.search.SearchService;

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
public class AmazonDescriptionFinderTest {

	@Autowired
	CatalogService catalogService;

	@Autowired
	SearchService searchService;

	@Autowired
	ArtistRepository artistRepo;

	@Autowired
	AmazonDetailFinder firstAmazonSearch;

	@Autowired
	AmazonDescriptionFinder descriptionSearch;

	@Before
	public void setup() {

	}

	@Test
	public void testSearchLogic() throws Exception {
		BookDao book = new BookDao();
		book.getBookdetail().setIsbn10("2211221092");

		FinderObject findobj = new FinderObject(book.getBookdetail());

		// service call to first amazon search (we need something which doesn't have a description)
		findobj = firstAmazonSearch.searchLogic(findobj);
		BookDetailDao bookdetail = findobj.getBookdetail();

		// check call
		Assert.assertNotNull(findobj);
		Assert.assertFalse(findobj.getSearchStatus() == CatalogService.DetailStatus.NODETAIL);
		Assert.assertTrue(bookdetail.getDescription()==null || bookdetail.getDescription().trim().length()==0);
		Assert.assertTrue(bookdetail.getTitle()!=null && bookdetail.getTitle().trim().length()>0);
		
		// ok - nothing found - let's put it through the description finder
		findobj = descriptionSearch.findDetails(findobj, 210);
		
		// check call - run should be logged (this should be eligible)
		// and description shouldn't be null
		Assert.assertNotNull(findobj);
		Assert.assertTrue(findobj.getCurrentFinderLog() % descriptionSearch.getIdentifier() ==0);
		Assert.assertNotNull(findobj.getBookdetail().getDescription());
		Assert.assertTrue(findobj.getBookdetail().getDescription().length()>0);
		
	
	
	}



}