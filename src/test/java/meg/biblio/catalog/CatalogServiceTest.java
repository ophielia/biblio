package meg.biblio.catalog;

import java.util.ArrayList;
import java.util.List;

import meg.biblio.catalog.db.ArtistRepository;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.PublisherRepository;
import meg.biblio.catalog.db.SubjectRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.web.model.BookModel;
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
public class CatalogServiceTest {

	@Autowired
	CatalogService catalogService;
	
	@Autowired
	SearchService searchService;	

	@Autowired
	ArtistRepository artistRepo;

	@Autowired
	BookRepository bookRepo;
	
	@Autowired
	PublisherRepository pubRepo;	
	
	@Autowired
	SubjectRepository subjectRepo;		
	
	Long artistid;
	Long pubtestid;
	
	@Before
	public void setup() {
		// make artist susan cooper
		ArtistDao artist = catalogService.textToArtistName("Susan Cooper");
		artist = artistRepo.save(artist);
		artistid = artist.getId();
		
		// setup book with publisher
		// create publisher
		PublisherDao pub = new PublisherDao();
		pub.setName("publisher");
		// create book
		BookDao pubtestbook = new BookDao();
		pubtestbook.setTitle("testTitle");
		// put publisher in book
		pubtestbook.setPublisher(pub);
		// save book
		BookDao pubtest = bookRepo.saveAndFlush(pubtestbook);
		pubtestid = pubtest.getId();
	}
	
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
		Assert.assertEquals(1, result.getAuthors().size());
		ArtistDao resultart = result.getAuthors().get(0);
		Assert.assertEquals("willikins",resultart.getLastname());
		Assert.assertNotNull(result.getIllustrators());
		
		// test with existing author
		book = new BookDao();
		book.setTitle("the dark is rising");
		author = new ArtistDao();
		author.setFirstname("susan");
		author.setLastname("cooper");
		authors = new ArrayList<ArtistDao>();
		authors.add(author);
		book.setAuthors(authors);
		
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
		Assert.assertEquals(artistid,artist.getId());
	}

	@Test
	public void testPublisherBug() {

		// retreive book
		BookDao book = bookRepo.findOne(pubtestid);
		
		
		// check that publisher is not null
		Assert.assertNotNull(book.getPublisher());
	}
	
	@Test
	public void testFillDetailsFromEntry() {
		BookDao book = new BookDao();
		book.setTitle("coco tout nu");
		ArtistDao author = catalogService.textToArtistName("Monfreid");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		book.setAuthors(authors);

		BookModel model = new BookModel(book);

		// service call
		BookModel result = catalogService.createCatalogEntryFromBookModel(1L, model);

		// check call
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getBookid());
		Assert.assertNotNull(result.getClientid());
		Assert.assertNotNull(result.getAuthors());
		// now - check details - should not be no detail
		Assert.assertTrue(CatalogServiceImpl.DetailStatus.NODETAIL!= result.getDetailstatus().longValue());
	}
	
 	
  @Test
  public void testCreateFromList() {
	  //createCatalogEntriesFromList(Long clientkey,List<BookModel> toimport) 
	  // make three books add to list
	  BookDao book1 = new BookDao();
	  book1.setClientbookid("1A");
	  book1.setTitle("Pride and Prejudice");
	  BookDao book2 = new BookDao();
	  book2.setClientbookid("2A");
	  book2.setTitle("Sense and Sensibility");
	  List<ArtistDao> authors = new ArrayList<ArtistDao>();
	  ArtistDao author = catalogService.textToArtistName("Jane Austen");
	  authors.add(author);
	  BookDao book3 = new BookDao();
	  book3.setClientbookid("3A");
	  book3.setTitle("The Very Hungry Catepillar");
	  authors = new ArrayList<ArtistDao>();
	  author = catalogService.textToArtistName("Eric Carle");
	  book3.setAuthors(authors);
	  
	  // to model, and in list
	  List<BookModel> toimport = new ArrayList<BookModel>();
	  toimport.add(new BookModel(book1));
	  toimport.add(new BookModel(book2));
	  toimport.add(new BookModel(book3));
	  
	  // service call
	  catalogService.createCatalogEntriesFromList(new Long(1), toimport);

	  // find by book id
	  List<BookDao> found = searchService.findBookByClientId("1A");
	  // Assert not null
	  Assert.assertNotNull(found);
	  Assert.assertTrue(1==found.size());
	  BookDao result = found.get(0);
	  Assert.assertEquals("Pride and Prejudice", result.getTitle());
	  
	  
  }
/*
	@Test
	public void testCopyDetailsAuthor() {
		BookDao testbook = new BookDao();
		ArtistDao artist = new ArtistDao();
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		List<ArtistDao> illustrators = new ArrayList<ArtistDao>();
		List<String> foundarray = new ArrayList<String>();
		// test book, author "L Wilder" - Found array - Laura Wilder
		artist = new ArtistDao();
		artist.setFirstname("L");
		artist.setLastname("Wilder");
		authors.add(artist);
		testbook.setAuthors(authors);
		foundarray.add("Laura Wilder");
		// service call
		BookDao resultbook = catalogService.copyAuthorsIntoBook(testbook, foundarray);
		// should have author without id, and name Laura Wilder
		Assert.assertNotNull(resultbook);
		Assert.assertNotNull(resultbook.getAuthors());
		ArtistDao testauth = resultbook.getAuthors().get(0);
		Assert.assertNull(testauth.getId());
		Assert.assertEquals("Laura", testauth.getFirstname());
		Assert.assertEquals("Wilder", testauth.getLastname());


		// test book, illustrator "L Wilder" - Found array - Laura Wilder, Laura Wilder in db
		ArtistDao dbartist = new ArtistDao();
		dbartist.setFirstname("Laura");
		dbartist.setLastname("Wilder");
		dbartist = artistRepo.save(dbartist);
		Long dbid = dbartist.getId();
		testbook = new BookDao();
		artist = new ArtistDao();
		authors = new ArrayList<ArtistDao>();
		illustrators = new ArrayList<ArtistDao>();
		foundarray = new ArrayList<String>();
		// test book, author "L Wilder" - Found array - Laura Wilder
		artist = new ArtistDao();
		artist.setFirstname("L");
		artist.setLastname("Wilder");
		artist.setId(11L);
		illustrators.add(artist);
		testbook.setIllustrators(illustrators);
		foundarray.add("Laura Wilder");
		// service call
		resultbook = catalogService.copyAuthorsIntoBook(testbook, foundarray);
		// should have author with id, id should be artist "Laura Wilder"
		Assert.assertNotNull(resultbook);
		Assert.assertNull(resultbook.getAuthors());
		Assert.assertNotNull(resultbook.getIllustrators());
		testauth = resultbook.getIllustrators().get(0);
		Assert.assertNotNull(testauth.getId());
		Assert.assertEquals(dbid,testauth.getId());
		Assert.assertEquals("Laura", testauth.getFirstname());
		Assert.assertEquals("Wilder", testauth.getLastname());


		// test book, illustrator "Garth Williams" - Found array - Laura Wilder,Garth Williams -  nothing in db
		testbook = new BookDao();
		artist = new ArtistDao();
		authors = new ArrayList<ArtistDao>();
		illustrators = new ArrayList<ArtistDao>();
		foundarray = new ArrayList<String>();
		artist = new ArtistDao();
		artist.setFirstname("Garth");
		artist.setLastname("Williams");
		illustrators.add(artist);
		testbook.setIllustrators(illustrators);
		foundarray.add("Laura Wilder");
		foundarray.add("Garth Williams");
		// service call
		resultbook = catalogService.copyAuthorsIntoBook(testbook, foundarray);
		// should have illust without id, and name "Garth Williams"
		Assert.assertNotNull(resultbook);
		Assert.assertNotNull(resultbook.getAuthors());
		Assert.assertNotNull(resultbook.getIllustrators());
		testauth = resultbook.getIllustrators().get(0);
		Assert.assertNull(testauth.getId());
		Assert.assertEquals("Garth", testauth.getFirstname());
		Assert.assertEquals("Williams", testauth.getLastname());
		

		// test book, illustrator "Garth Williams" - Found array - Prince Humperdink,Laura Wilder,Garth Williams -  nothing in db
		testbook = new BookDao();
		artist = new ArtistDao();
		authors = new ArrayList<ArtistDao>();
		illustrators = new ArrayList<ArtistDao>();
		foundarray = new ArrayList<String>();
		artist = new ArtistDao();
		artist.setFirstname("Garth");
		artist.setLastname("Williams");
		illustrators.add(artist);
		testbook.setIllustrators(illustrators);
		foundarray.add("Laura Wilder");
		foundarray.add("Garth Williams");
		foundarray.add("Prince Humperdink");
		// service call
		resultbook = catalogService.copyAuthorsIntoBook(testbook, foundarray);
		// should have illust without id, and name "Garth Williams"
		Assert.assertNotNull(resultbook);
		Assert.assertNotNull(resultbook.getAuthors());
		Assert.assertNotNull(resultbook.getIllustrators());
		testauth = resultbook.getIllustrators().get(0);
		Assert.assertNull(testauth.getId());
		Assert.assertEquals("Garth", testauth.getFirstname());
		Assert.assertEquals("Williams", testauth.getLastname());
	
		// test book, illustrator "Hilary Knight" - Found array - Kay Thompson,Hilary Knight -  Hilary Knight in db
		dbartist = new ArtistDao();
		dbartist.setFirstname("Hilary");
		dbartist.setLastname("Knight");
		dbartist = artistRepo.save(dbartist);
		dbid = dbartist.getId();
		testbook = new BookDao();
		artist = new ArtistDao();
		authors = new ArrayList<ArtistDao>();
		illustrators = new ArrayList<ArtistDao>();
		foundarray = new ArrayList<String>();
		artist = new ArtistDao();
		artist.setFirstname("Hilary");
		artist.setLastname("Knight");
		illustrators.add(artist);
		testbook.setIllustrators(illustrators);
		foundarray.add("Laura Wilder");
		foundarray.add("Hilary Knight");
		// service call
		resultbook = catalogService.copyAuthorsIntoBook(testbook, foundarray);
		// should have illustrator with id, id should be artist "Hilary Knight"
		Assert.assertNotNull(resultbook);
		Assert.assertNotNull(resultbook.getAuthors());
		Assert.assertNotNull(resultbook.getIllustrators());
		testauth = resultbook.getIllustrators().get(0);
		Assert.assertNotNull(testauth.getId());
		Assert.assertEquals(dbid,testauth.getId());
		Assert.assertEquals("Hilary", testauth.getFirstname());
		Assert.assertEquals("Knight", testauth.getLastname());		// service call

	}
*/
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

	
	/*
	@Test
	public void testFindPublisherByName() {
		// find publisher "newJonestest"
		PublisherDao testpub = catalogService.findPublisherForName("newJonestest");
		// should be new - no id
		Assert.assertNotNull(testpub);
		Assert.assertNull(testpub.getId());
		// save publisher
		PublisherDao resultpub = pubRepo.saveAndFlush(testpub);
		Long id = resultpub.getId();
		
		// find publisher "newJonestest"
		testpub = catalogService.findPublisherForName("newJonestest");
		// now, should have same id as previously
		Assert.assertNotNull(testpub);
		Assert.assertNotNull(testpub.getId());
		Assert.assertEquals(id,testpub.getId());
		
		// find with null
		testpub = catalogService.findPublisherForName(null);
		// should be null
		Assert.assertNull(testpub);
		
	}*/
	
	/*	@Test 
	public void testFindDetailsSingleBook() throws GeneralSecurityException, IOException {
		// currently basically doing a blow up test, with peeking
		// full test requires moving Google Search into it's own service (possible)
		// and setting up Mock for this search.  Not ready to do that now.
		
		// create book
		BookDao book = new BookDao();
		book.setTitle("Les trois brigands");
		ArtistDao author = catalogService.textToArtistName("Toni Ungerer");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		book.setAuthors(authors);
		book = bookRepo.save(book);
		
		// service call
		catalogService.fillInDetailsForSingleBook(book.getId());
		BookModel model = catalogService.loadBookModel(book.getId());
		
		// Assertions
		Assert.assertNotNull(model);
		Assert.assertNotNull(model.getDescription());
		Assert.assertEquals(CatalogServiceImpl.DetailStatus.DETAILFOUND, model.getDetailstatus().longValue());
		Assert.assertNotNull(model.getPublishyear());
	}
	

	@Test 
	public void testFindDetailsSingleBookFoundDetails() throws GeneralSecurityException, IOException {
		// create book
		BookDao book = new BookDao();
		book.setTitle("corps HumAin");
		PublisherDao publisher = catalogService.findPublisherForName("Fleurus");
		book.setPublisher(publisher);
		book = bookRepo.save(book);
		
		// service call
		catalogService.fillInDetailsForSingleBook(book.getId());
		BookModel model = catalogService.loadBookModel(book.getId());
		List<FoundDetailsDao> found = catalogService.getFoundDetailsForBook(book.getId());
		
		// Assertions
		Assert.assertNotNull(model);
		Assert.assertNull(model.getDescription());
		Assert.assertNotNull(found);
		Assert.assertEquals(5, found.size());
		Assert.assertEquals(CatalogServiceImpl.DetailStatus.MULTIDETAILSFOUND, model.getDetailstatus().longValue());
	}
	*/
	
/*
	@Test
	public void testFindSubjectByString() {
		// find publisher "newJonestest"
		SubjectDao testpub = catalogService.findSubjectForString("newJonestest");
		// should be new - no id
		Assert.assertNotNull(testpub);
		Assert.assertNull(testpub.getId());
		// save publisher
		SubjectDao resultpub = subjectRepo.saveAndFlush(testpub);
		Long id = resultpub.getId();
		
		// find publisher "newJonestest"
		testpub = catalogService.findSubjectForString("newJonestest");
		// now, should have same id as previously
		Assert.assertNotNull(testpub);
		Assert.assertNotNull(testpub.getId());
		Assert.assertEquals(id,testpub.getId());
		
		// find with null
		testpub = catalogService.findSubjectForString(null);
		// should be null
		Assert.assertNull(testpub);
		
	}
	*/
}