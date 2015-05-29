package meg.biblio.catalog;

import java.util.ArrayList;
import java.util.List;

import meg.biblio.catalog.db.BookDetailRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.common.ClientService;
import meg.biblio.common.db.dao.ClientDao;
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
public class InternalDetailFinderTest {

	@Autowired
	CatalogService catalogService;
	
	@Autowired
	ClientService clientService;	

	@Autowired
	BookMemberService bMemberService;

	@Autowired
	SearchService searchService;

	@Autowired
	BookDetailRepository bookDetRepo;

	@Autowired
	BaseDetailFinder internalSearch;

	Long isbnid;
	Long titleauthorid;

	@Before
	public void setup() {
		// add a bookdetail with isbn13 of "2222222222222"
		BookDetailDao bd = new BookDetailDao();
		bd.setTitle("-");
		bd.setIsbn13("2222222222222");

		// get id, and save in field
		bd = bookDetRepo.save(bd);
		isbnid = bd.getId();

		// add a bookdetail with title "James Duke is born", author
		// "Caroline Itoi"
		bd = new BookDetailDao();
		bd.setTitle("James Duke is born");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		ArtistDao author = bMemberService.textToArtistName("Caroline Itoi");
		authors.add(author);
		bd.setAuthors(authors);
		bd.setClientspecific(false);

		// get id, and save in field
		bd = bookDetRepo.save(bd);
		titleauthorid = bd.getId();

	}

	@Test
	public void testSearchLogic() throws Exception {
		Long clientkey = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientkey);
		BookDao book = new BookDao();
		book.getBookdetail().setTitle("James Duke is born");
		ArtistDao author = bMemberService.textToArtistName("Caroline Itoi");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		book.getBookdetail().setAuthors(authors);

		FinderObject findobj = new FinderObject(book.getBookdetail(),client);

		// service call
		findobj = internalSearch.findDetails(findobj, 210);
		BookDetailDao bookdetail = findobj.getBookdetail();

		// check call
		Assert.assertNotNull(findobj);
		Assert.assertFalse(findobj.getSearchStatus() == CatalogService.DetailStatus.NODETAIL);
		Assert.assertEquals(0L, findobj.getCurrentFinderLog()%11);
		Assert.assertNotNull(bookdetail);
	}
	
	@Test
	public void testSearchLogicIsbn() throws Exception {
		Long clientkey = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientkey);
		BookDao book = new BookDao();
		book.getBookdetail().setTitle("James Duke is born");
		book.getBookdetail().setIsbn13("2222222222222");

		FinderObject findobj = new FinderObject(book.getBookdetail(),client);

		// service call
		findobj = internalSearch.findDetails(findobj, 210);
		BookDetailDao bookdetail = findobj.getBookdetail();

		// check call
		Assert.assertNotNull(findobj);
		Assert.assertNotNull(bookdetail);
		Assert.assertFalse(findobj.getSearchStatus() == CatalogService.DetailStatus.NODETAIL);
		Assert.assertEquals(0L, findobj.getCurrentFinderLog()%11);
	}	

	
	@Test
	public void testISBNNotFoundWrite() throws Exception {
		Long clientkey = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientkey);
		BookDao book = new BookDao();
		book.getBookdetail().setIsbn13("1111111111111");
		book.getBookdetail().setTitle("coco tout nu");
		ArtistDao author = bMemberService.textToArtistName("Monfreid");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		book.getBookdetail().setAuthors(authors);

		FinderObject findobj = new FinderObject(book.getBookdetail(),client);

		// service call
		findobj = internalSearch.searchLogic(findobj);
		Assert.assertTrue(findobj.getSearchStatus() == CatalogService.DetailStatus.DETAILNOTFOUNDWISBN);
	}
	
	@Test
	public void testISBNNotFoundRead() throws Exception {
		Long clientkey = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientkey);
		BookDao book = new BookDao();
		BookDetailDao bd = book.getBookdetail();
		bd.setTitle("James Duke is born");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		ArtistDao author = bMemberService.textToArtistName("Caroline Itoi");
		authors.add(author);
		bd.setAuthors(authors);
		book.setBookdetail(bd);
		book.getBookdetail().setDetailstatus(CatalogService.DetailStatus.DETAILNOTFOUNDWISBN);

		FinderObject findobj = new FinderObject(book.getBookdetail(),client);

		// service call
		findobj = internalSearch.searchLogic(findobj);
		Assert.assertFalse(findobj.getSearchStatus() == CatalogService.DetailStatus.DETAILNOTFOUNDWISBN);
	}	
}