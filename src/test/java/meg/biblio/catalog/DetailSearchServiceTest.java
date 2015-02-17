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
public class DetailSearchServiceTest {

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

	@Before
	public void setup() {
		// make artist susan cooper
		ArtistDao artist = catalogService.textToArtistName("Susan Cooper");
		artist = artistRepo.save(artist);
		artistid = artist.getId();

		// add "eating" to ignored words list
		IgnoredWordsDao ignored = new IgnoredWordsDao();
		ignored.setWord("eating");
		ignoredRepo.saveAndFlush(ignored);		
		
		// setup book with publisher
		// create publisher
		PublisherDao pub = new PublisherDao();
		pub.setName("publisher");
		// create book
		BookDao pubtestbook = new BookDao();
		pubtestbook.getBookdetail().setTitle("testTitle");
		// put publisher in book
		pubtestbook.getBookdetail().setPublisher(pub);
		// save book
		BookDao pubtest = bookRepo.saveAndFlush(pubtestbook);
		pubtestid = pubtest.getId();
	}

	@Test
	public void testCreateCatalogEntry() {
		BookDao book = new BookDao();
		book.getBookdetail().setTitle("the quick brown fox");
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
		book.getBookdetail().setAuthors(authors);
		book.getBookdetail().setIllustrators(illustrators);

		BookModel model = new BookModel(book);

		// service call
		BookModel result = catalogService.createCatalogEntryFromBookModel(1L,
				model);

		// check call
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getBookid());
		Assert.assertNotNull(result.getClientid());
		Assert.assertNotNull(result.getAuthors());
		Assert.assertEquals(1, result.getAuthors().size());
		ArtistDao resultart = result.getAuthors().get(0);
		Assert.assertEquals("willikins", resultart.getLastname());
		Assert.assertNotNull(result.getIllustrators());

		// test with existing author
		book = new BookDao();
		book.getBookdetail().setTitle("the dark is rising");
		author = new ArtistDao();
		author.setFirstname("susan");
		author.setLastname("cooper");
		authors = new ArrayList<ArtistDao>();
		authors.add(author);
		book.getBookdetail().setAuthors(authors);

		model = new BookModel(book);

		// service call
		result = catalogService.createCatalogEntryFromBookModel(1L, model);

		// check call
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getBookid());
		Assert.assertNotNull(result.getClientid());
		Assert.assertNotNull(result.getAuthors());
		Assert.assertNull(result.getIllustrators());
		// test authors
		ArtistDao artist = result.getAuthors().get(0);
		Assert.assertEquals(artistid, artist.getId());
	}

	@Test
	public void testIndexing() {
		// make book with description "i am eating a hat. I am Eating a Hat";
		BookDao book = bookRepo.findOne(pubtestid);
		book.getBookdetail().setDescription("i am eating a hat. I am Eating a Hat");
		

		
		// save book (calls indexing)
		catalogService.saveBook(book);
		
		// get foundwords for book
		List<FoundWordsDao> foundw = foundRepo.findWordsForBookDetail( book.getBookdetail());
		
		// should have 2 i, 2 eating, 2 hat
		Assert.assertNotNull(foundw);
		Assert.assertTrue(foundw.size()>0);
		boolean eatingfound=false;
		for (FoundWordsDao found:foundw) {
			if (found.getWord().equals("i")) {
				Assert.assertNotNull(found.getCountintext());
				Assert.assertTrue(found.getCountintext().intValue()==2);
			}
			if (found.getWord().equals("eating")) {
				eatingfound=true;
			}
			if (found.getWord().equals("hat")) {
				Assert.assertNotNull(found.getCountintext());
				Assert.assertTrue(found.getCountintext().intValue()==2);
			}
		}
		// Assert that eating wasn't found(an ignored word)
		Assert.assertFalse(eatingfound);
		
		// do a second time - and ensure that results are the same
		book.getBookdetail().setDescription("i am eating a hat! I am Eating a Hat");
		// save book (calls indexing)
		catalogService.saveBook(book);
		
		// get foundwords for book
		foundw = foundRepo.findWordsForBookDetail( book.getBookdetail());
		
		// should have 2 i, 2 eating, 2 hat
		Assert.assertNotNull(foundw);
		Assert.assertTrue(foundw.size()>0);
		eatingfound=false;
		for (FoundWordsDao found:foundw) {
			if (found.getWord().equals("i")) {
				Assert.assertNotNull(found.getCountintext());
				Assert.assertTrue(found.getCountintext().intValue()==2);
			}
			if (found.getWord().equals("eating")) {
				eatingfound=true;
			}
			if (found.getWord().equals("hat")) {
				Assert.assertNotNull(found.getCountintext());
				Assert.assertTrue(found.getCountintext().intValue()==2);
			}
		}
		// Assert that eating wasn't found(an ignored word)
		Assert.assertFalse(eatingfound);		
	}
	
	@Test
	public void testPublisherBug() {

		// retreive book
		BookDao book = bookRepo.findOne(pubtestid);

		// check that publisher is not null
		Assert.assertNotNull(book.getBookdetail().getPublisher());
	}
	
	@Test
	public void testAssignCodeToBook() {
		Long clientid = clientService.getTestClientId();
		BookDao book = new BookDao();
		book.getBookdetail().setTitle("les trois brigands");
		ArtistDao author = catalogService.textToArtistName("Ungerer");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		book.getBookdetail().setAuthors(authors);

		BookModel model = new BookModel(book);
		model = catalogService.createCatalogEntryFromBookModel(clientid,
				model);
		
		// bookid, code
		Long bookid = model.getBookid();
		String code = "B1000001000";
		
		// service call
		catalogService.assignCodeToBook(code, bookid);
		
		model = catalogService.loadBookModel(bookid);
		Assert.assertEquals(code,model.getBook().getBarcodeid());
		
	}

	@Test
	public void testFillDetailsFromEntry() {
		BookDao book = new BookDao();
		book.getBookdetail().setTitle("coco tout nu");
		ArtistDao author = catalogService.textToArtistName("Monfreid");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		book.getBookdetail().setAuthors(authors);

		BookModel model = new BookModel(book);

		// service call
		BookModel result = catalogService.createCatalogEntryFromBookModel(1L,
				model);

		// check call
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getBookid());
		Assert.assertNotNull(result.getClientid());
		Assert.assertNotNull(result.getAuthors());

	}



	@Test
	public void testCreateFromList() {
		// createCatalogEntriesFromList(Long clientkey,List<BookModel> toimport)
		// make three books add to list
		BookDao book1 = new BookDao();
		book1.setClientbookid("1A");
		book1.getBookdetail().setTitle("Pride and Prejudice");
		BookDao book2 = new BookDao();
		book2.setClientbookid("2A");
		book2.getBookdetail().setTitle("Sense and Sensibility");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		ArtistDao author = catalogService.textToArtistName("Jane Austen");
		authors.add(author);
		BookDao book3 = new BookDao();
		book3.setClientbookid("3A");
		book3.getBookdetail().setTitle("The Very Hungry Catepillar");
		authors = new ArrayList<ArtistDao>();
		author = catalogService.textToArtistName("Eric Carle");
		book3.getBookdetail().setAuthors(authors);

		// to model, and in list
		List<BookModel> toimport = new ArrayList<BookModel>();
		toimport.add(new BookModel(book1));
		toimport.add(new BookModel(book2));
		toimport.add(new BookModel(book3));

		// service call
		catalogService.createCatalogEntriesFromList(new Long(1), toimport);

		// find by book id
		List<Long> found = searchService.findBookIdByClientId("1A");

		// Assert not null
		Assert.assertNotNull(found);
		Assert.assertTrue(1 == found.size());
		BookDao result = bookRepo.findOne(found.get(0));
		Assert.assertEquals("Pride and Prejudice", result.getBookdetail().getTitle());

	}

	
	@Test
	public void testTextToArtistName() {
		// text "Michael Vincent Marbboury"
		String text = "Michael Vincent Marbboury";
		ArtistDao name = catalogService.textToArtistName(text);
		// check firstname
		Assert.assertEquals("Michael", name.getFirstname());
		// check middlename
		Assert.assertEquals("Vincent", name.getMiddlename());
		// check lastname
		Assert.assertEquals("Marbboury", name.getLastname());

		// text "Bill Moyers"
		text = "Bill Moyers";
		name = catalogService.textToArtistName(text);
		// check firstname
		Assert.assertEquals("Bill", name.getFirstname());
		// check lastname
		Assert.assertEquals("Moyers", name.getLastname());

		// text "Sendak"
		text = "Sendak";
		name = catalogService.textToArtistName(text);
		// check lastname
		Assert.assertEquals("Sendak", name.getLastname());

		// text "Martin, Kate"
		text = "Martin, Kate";
		name = catalogService.textToArtistName(text);
		// check firstname
		Assert.assertEquals("Kate", name.getFirstname());
		// checkk lastname
		Assert.assertEquals("Martin", name.getLastname());

		// text null
		name = catalogService.textToArtistName(null);
		// check null
		Assert.assertNull(name);

		// text "Martin, Jack Orion"
		text = "Martin, Jack Orion";
		name = catalogService.textToArtistName(text);
		// check firstname
		Assert.assertEquals("Jack", name.getFirstname());
		// check middlename
		Assert.assertEquals("Orion", name.getMiddlename());
		// check lastname
		Assert.assertEquals("Martin", name.getLastname());
	}

	@Test
	public void testUpdateBook() throws GeneralSecurityException, IOException {
		// load testpubid
		BookModel model = catalogService.loadBookModel(pubtestid);
		
		// change classification, booktype, status, and language
		model.setShelfclass(5L);
		model.setType(CatalogService.BookType.FOREIGNLANGUAGE);
		model.setLanguage("EN");
		model.setStatus(CatalogService.Status.CHECKEDOUT);

		// server call
		model = catalogService.updateCatalogEntryFromBookModel(1L, model,false);
		
		// ensure that changes are shown
		Assert.assertNotNull(model);
		Assert.assertEquals(new Long(CatalogService.BookType.FOREIGNLANGUAGE), model.getType());
		Assert.assertEquals(new Long(CatalogService.Status.CHECKEDOUT), model.getStatus());
		Assert.assertEquals("EN", model.getLanguage());
		Assert.assertEquals(new Long(5), model.getShelfclass());
		
	}

}