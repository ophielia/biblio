package meg.biblio.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import meg.biblio.catalog.db.ArtistRepository;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;

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


	@Autowired
	BookRepository bookRepo;
	
	@Before
	public void setup() {
		// put "Laura Ingalls Wilder" in db
		ArtistDao newart = new ArtistDao();
		newart.setFirstname("Laura");
		newart.setMiddlename("Ingalls");
		newart.setLastname("Wilder");
		newart = artistRepo.saveAndFlush(newart);
		
		// (sidestep - save in book)
		BookDao book = new BookDao();
		book.setClientid(new Long(1));
		book.getBookdetail().setTitle("Little House on the Prarie");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(newart);
		book.getBookdetail().setAuthors(authors);
		bookRepo.save(book);
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
	
	@Test
	public void testBasicSearchByCriteria() {
		// now - just testing that it doesn't explode
		BookSearchCriteria criteria = new BookSearchCriteria();
		criteria.setTitle("Coco");
		
		List<BookDao> foundbooks = searchService.findBooksForCriteria(criteria, 1L);
		Assert.assertNotNull(foundbooks);
	}
	
	@Test
	public void testAuthorSearchByCriteria() {
		// now - just testing that it doesn't explode
		BookSearchCriteria criteria = new BookSearchCriteria();
		criteria.setAuthor("Laura Ingalls Wilder");
		criteria.setClientid(new Long(1));
		
		List<BookDao> foundbooks = searchService.findBooksForCriteria(criteria, 1L);
		Assert.assertNotNull(foundbooks);
	}	
	
	@Test
	public void testKeywordWithCriteria() {
		// now - just testing that it doesn't explode
		BookSearchCriteria criteria = new BookSearchCriteria();
		criteria.setClientid(new Long(1));
		criteria.setKeyword("Coco");
		
		List<BookDao> foundbooks = searchService.findBooksForCriteria(criteria, 1L);
		Assert.assertNotNull(foundbooks);
		
		// now - just testing that it doesn't explode
		criteria = new BookSearchCriteria();
		criteria.setClientid(new Long(1));
		criteria.setKeyword("Coco l'éléphant");
		
		foundbooks = searchService.findBooksForCriteria(criteria, 1L);
		Assert.assertNotNull(foundbooks);		
	}		
	
	@Test
	public void testKeywordWithOtherCriteria() {
		// now - just testing that it doesn't explode
		BookSearchCriteria criteria = new BookSearchCriteria();
		criteria.setClientid(new Long(1));
		criteria.setKeyword("Coco l'éléphant");
		criteria.setAuthor("monfreid");
		
		List<BookDao> foundbooks = searchService.findBooksForCriteria(criteria, 1L);
		Assert.assertNotNull(foundbooks);
		
	}		
	
	@Test
	public void testPublisherWithCriteria() {
		// now - just testing that it doesn't explode
		BookSearchCriteria criteria = new BookSearchCriteria();
		criteria.setClientid(new Long(1));
		criteria.setPublisher("Ecole");
		
		List<BookDao> foundbooks = searchService.findBooksForCriteria(criteria, 1L);
		Assert.assertNotNull(foundbooks);
		
			
	}	
	
	@Test
	public void testAuthorSortNoAuthorSearch() {
		// search for books with coco in title - title sort, get count
		BookSearchCriteria criteria = new BookSearchCriteria();
		criteria.setClientid(new Long(1));
		criteria.setTitle("Coco");
		criteria.setOrderby(BookSearchCriteria.OrderBy.TITLE);
		List<BookDao> foundbooks = searchService.findBooksForCriteria(criteria, 1L);
		Assert.assertNotNull(foundbooks);
		int searchonecount = foundbooks.size();
		// search for books with coco in title - author sort, get count
		criteria = new BookSearchCriteria();
		criteria.setClientid(new Long(1));
		criteria.setTitle("Coco");
		criteria.setOrderby(BookSearchCriteria.OrderBy.AUTHOR);	
		foundbooks = searchService.findBooksForCriteria(criteria, 1L);
		Assert.assertNotNull(foundbooks);
		int searchtwocount = foundbooks.size();		
		// ensure that counts match
		Assert.assertEquals(searchonecount,searchtwocount);
		// search for books with monfried as author - author sort
		criteria = new BookSearchCriteria();
		criteria.setClientid(new Long(1));
		criteria.setAuthor("monfreid");
		criteria.setOrderby(BookSearchCriteria.OrderBy.AUTHOR);	
		foundbooks = searchService.findBooksForCriteria(criteria, 1L);
		Assert.assertNotNull(foundbooks);

	}
	
	@Test 
	public void testBugEmptyCriteria() {
		BookSearchCriteria criteria = new BookSearchCriteria();
		criteria.setClientid(1L);
		List<BookDao> foundbooks = searchService.findBooksForCriteria(criteria, 1L);
		Assert.assertNotNull(foundbooks);
		
	}
	
	@Test 
	public void testSortByBookIdCriteria() {
		BookSearchCriteria criteria = new BookSearchCriteria();
		criteria.setClientid(1L);
		criteria.setOrderby(BookSearchCriteria.OrderBy.BOOKID);
		List<BookDao> foundbooks = searchService.findBooksForCriteria(criteria, 1L);
		Assert.assertNotNull(foundbooks);
		
	}	
	
	@Test 
	public void testBreakoutMethod() {
		long breakoutfield = SearchService.Breakoutfield.STATUS;
		HashMap<Long,Long> results= searchService.breakoutByBookField(breakoutfield, 1L);
		Assert.assertNotNull(results);
		
	}	
	
	@Test 
	public void testCountMethod() {
		Long results= searchService.getBookCount(1L);
		Assert.assertNotNull(results);
		
	}		
	
	
	
	}