package meg.biblio.catalog;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.CatalogServiceImpl;
import meg.biblio.catalog.db.ArtistRepository;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.FoundWordsDao;
import meg.biblio.catalog.db.FoundWordsRepository;
import meg.biblio.catalog.db.IgnoredWordsDao;
import meg.biblio.catalog.db.IgnoredWordsRepository;
import meg.biblio.catalog.db.PublisherRepository;
import meg.biblio.catalog.db.SubjectRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.common.ClientService;
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
public class BNFCatalogFinderTest {

	@Autowired
	CatalogService catalogService;

	@Autowired
	SearchService searchService;

	@Autowired
	BNFCatalogFinder bnfSearch;

	@Before
	public void setup() {
	}


	@Test
	public void testSearchLogic() throws Exception {
		BookDao book = new BookDao();
		book.getBookdetail().setIsbn13("9782286008567");
		ArtistDao author = catalogService.textToArtistName("Monfreid");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		book.getBookdetail().setAuthors(authors);

		FinderObject findobj = new FinderObject(book.getBookdetail());
		

		// service call
		findobj = bnfSearch.findDetails(findobj,210);
		BookDetailDao bookdetail = findobj.getBookdetail();
		
		// check call
		Assert.assertNotNull(findobj);
		Assert.assertFalse(findobj.getSearchStatus() == CatalogService.DetailStatus.NODETAIL);
		Assert.assertEquals(new Long(7), findobj.getCurrentFinderLog());
	}
	
	
	@Test
	public void testRemoveTags() throws Exception {
		String totest = "<b> Auteur(s) :  </b> &#160;<A HREF=\"/servlet/autorite?ID=11892201&idNoeud=1.1&host=catalogue\" TARGET=\"_self\" >Blake, St&#233;phanie (1968-....)</A>";

		// service call
		String result = bnfSearch.removeTags(totest);
		
		// check call
		Assert.assertNotNull(result);
	}



}