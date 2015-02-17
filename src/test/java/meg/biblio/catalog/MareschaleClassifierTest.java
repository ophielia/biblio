package meg.biblio.catalog;

import java.util.ArrayList;
import java.util.List;

import meg.biblio.catalog.db.ArtistRepository;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.FoundWordsRepository;
import meg.biblio.catalog.db.IgnoredWordsRepository;
import meg.biblio.catalog.db.PublisherRepository;
import meg.biblio.catalog.db.SubjectRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
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
public class MareschaleClassifierTest {

	@Autowired
	CatalogService catalogService;

	@Autowired
	SearchService searchService;

	@Autowired
	ArtistRepository artistRepo;
	
	@Autowired
	FoundWordsRepository foundRepo;	
	
	@Autowired
	IgnoredWordsRepository ignoredRepo;		

	@Autowired
	BookRepository bookRepo;

	@Autowired
	PublisherRepository pubRepo;

	@Autowired
	SubjectRepository subjectRepo;
	

	@Autowired
	ClientService clientService;

	Long artistid;
	Long pubtestid;

	MareschaleClassifier classifier ;
	
	@Before
	public void setup() {
		classifier = new MareschaleClassifier();
	}

	@Test
	public void testClassifyBook() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		// create and save book
		// book - with statusid detail found, language fr, and book type unknown
		// - no author
		// should have shelfclass null
		BookDao book1 = new BookDao();
		book1.setClientbookid("1");
		book1.setClientid(1L);
		book1.getBookdetail().setTitle("Pride and Prejudice");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		book1.getBookdetail().setAuthors(authors);
		book1.getBookdetail().setDetailstatus(CatalogService.DetailStatus.DETAILFOUND);
		book1.getBookdetail().setLanguage("fr");
		book1.setType(CatalogService.BookType.UNKNOWN);

		// service call
		classifier.classifyBook(book1);
		
		// test
		Assert.assertNotNull(book1);
		Assert.assertNull(book1.getShelfclass());

		// create and save book
		// book - with statusid detail found, language fr, and book type unknown
		// - author "white"
		// should have shelfclass 29
		book1 = new BookDao();
		book1.setClientbookid("1");
		book1.setClientid(1L);
		book1.getBookdetail().setTitle("Pride and Prejudice");
		authors = new ArrayList<ArtistDao>();
		ArtistDao author = catalogService.textToArtistName("Ed White");
		authors.add(author);
		book1.getBookdetail().setAuthors(authors);
		book1.getBookdetail().setDetailstatus(CatalogService.DetailStatus.DETAILFOUND);
		book1.getBookdetail().setLanguage("fr");
		book1.setType(CatalogService.BookType.UNKNOWN);

		// service call
		classifier.classifyBook(book1);
		// test
		Assert.assertNotNull(book1);
		Assert.assertTrue(29L==book1.getShelfclass());
		
		// create and save book
		// book - with statusid detail found, language en, and book type unknown
		// - no author
		// should have shelfclass 5
		book1 = new BookDao();
		book1.setClientbookid("1");
		book1.setClientid(1L);
		book1.getBookdetail().setTitle("Pride and Prejudice");
		authors = new ArrayList<ArtistDao>();
		book1.getBookdetail().setAuthors(authors);
		book1.getBookdetail().setDetailstatus(CatalogService.DetailStatus.DETAILFOUND);
		book1.getBookdetail().setLanguage("en");
		book1.setType(CatalogService.BookType.UNKNOWN);

		// service call
		classifier.classifyBook(book1);
		// test
		Assert.assertNotNull(book1);
		Assert.assertTrue(5L==book1.getShelfclass());
		
		
		// create and save book
		// book - with statusid detail found, language fr, and book type
		// non-fiction - author "beta"
		// should have shelfclass null
		book1 = new BookDao();
		book1.setClientbookid("1");
		book1.setClientid(1L);
		book1.getBookdetail().setTitle("Pride and Prejudice");
		authors = new ArrayList<ArtistDao>();
		book1.getBookdetail().setAuthors(authors);
		book1.getBookdetail().setDetailstatus(CatalogService.DetailStatus.DETAILFOUND);
		book1.getBookdetail().setLanguage("fr");
		book1.setType(CatalogService.BookType.NONFICTION);

		// service call
		classifier.classifyBook(book1);
		// test
		Assert.assertNotNull(book1);
		Assert.assertNull(book1.getShelfclass());
		
		
		
	}

}