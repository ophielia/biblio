package meg.biblio.catalog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import meg.biblio.catalog.db.ArtistRepository;
import meg.biblio.catalog.db.BookDetailRepository;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.FoundWordsRepository;
import meg.biblio.catalog.db.IgnoredWordsRepository;
import meg.biblio.catalog.db.PublisherRepository;
import meg.biblio.catalog.db.SubjectRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.common.ClientService;
import meg.biblio.common.ImportManager;
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
public class ImportManagerTest {

	@Autowired
	CatalogService catalogService;

	@Autowired
	ImportManager importManager;


	@Autowired
	ClientService clientService;


	@Autowired
	BookDetailRepository bookDetailRepo;
	
	@Autowired
	BookRepository bookRepo;
		

	@Before
	public void setup() {
		// put bookdetail into db - with isbn "1111111111111", title "nonsense", author "no one"
		BookDetailDao bookdetail = new BookDetailDao();
		bookdetail.setTitle("nonsense");
		bookdetail.setIsbn13("11111111111");
		bookdetail.setIsbn10("1111111111");
		
		ArtistDao author = catalogService.textToArtistName("no one");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setDetailstatus(CatalogService.DetailStatus.DETAILFOUND);
		bookdetail.setFinderlog(6L);
		bookdetail.setClientspecific(false);
		
		bookDetailRepo.save(bookdetail);
	}

	@Test
	public void testImportBooks() throws IOException, Exception {
		Long clientid = clientService.getTestClientId();
		// import filestring with isbn "11111111111", clientbookid "1111111" 
		String importstring = "1111111\tnonsense\tno one\t\t\t\t\t1111111111111\r\n";
		// service call
		importManager.importBookList(clientid, importstring);
		// find book by clientbookid
		List<BookDao> books= bookRepo.findBookByClientAssignedId("1111111", clientid);
		BookDao book = books!=null && books.size()>0?books.get(0):null;

		// assert - bookmodel should have searchstatus of founddetail, finderlog includes offline
		Assert.assertNotNull(book);
		BookDetailDao bd = book.getBookdetail();
		Assert.assertNotNull(bd);
		Assert.assertTrue(CatalogService.DetailStatus.DETAILFOUND== bd.getDetailstatus());
		Assert.assertEquals(0L, bd.getFinderlog()%11);

		
		// import filestring with title "nonsense", author "no one"
		importstring = "2111111\tnonsense\tno one\t\t\t\t\t\r\n";
		// service call
		importManager.importBookList(clientid, importstring);
		// find book by clientbookid
		books= bookRepo.findBookByClientAssignedId("2111111", clientid);
		book = books!=null && books.size()>0?books.get(0):null;

		// assert - bookmodel should have searchstatus of founddetail, finderlog includes offline
		Assert.assertNotNull(book);
		bd = book.getBookdetail();
		Assert.assertNotNull(bd);
		Assert.assertTrue(CatalogService.DetailStatus.DETAILFOUND== bd.getDetailstatus());
		Assert.assertEquals(0L, bd.getFinderlog()%11);
	}

}