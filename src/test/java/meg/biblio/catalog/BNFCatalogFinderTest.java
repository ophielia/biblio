package meg.biblio.catalog;

import java.util.ArrayList;
import java.util.List;

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
public class BNFCatalogFinderTest {

	@Autowired
	CatalogService catalogService;

	@Autowired
	SearchService searchService;

	@Autowired
	BNFCatalogFinder bnfSearch;

	
	List<FinderObject> searchfor;
	@Before
	public void setup() {
		searchfor= new ArrayList<FinderObject>();
		BookDetailDao bookdetail = new BookDetailDao();
		bookdetail.setTitle("Jour de lessive");
		ArtistDao author = catalogService.textToArtistName("Frédéric Stehr");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setIsbn13("9782211206464");
		FinderObject findobj = new FinderObject(bookdetail);
		searchfor.add(findobj);
		
		bookdetail = new BookDetailDao();
		bookdetail.setTitle("Petit Pierrot : Décrocher la Lune");
		author = catalogService.textToArtistName("Alberto Varanda");
		authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setIsbn13("9782302007482");
		 findobj = new FinderObject(bookdetail);
		searchfor.add(findobj);
		
		bookdetail = new BookDetailDao();
		bookdetail.setTitle("Et pourquoi ?");
		author = catalogService.textToArtistName("Michel Zeveren");
		authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setIsbn13("9782211093293");
		 findobj = new FinderObject(bookdetail);
		searchfor.add(findobj);
		
		bookdetail = new BookDetailDao();
		bookdetail.setTitle("C'est plus drôle avec Mimi");
		author = catalogService.textToArtistName("Lucy Cousins");
		authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setIsbn13("9782226156488");
		 findobj = new FinderObject(bookdetail);
		searchfor.add(findobj);
		
		bookdetail = new BookDetailDao();
		bookdetail.setTitle("Eloïse");
		author = catalogService.textToArtistName("Kay Thompson");
		authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setIsbn13("9782070561797");
		 findobj = new FinderObject(bookdetail);
		searchfor.add(findobj);
		
		
		bookdetail = new BookDetailDao();
		bookdetail.setTitle("Le Premier Grand Voyage du Père Noël");
		author = catalogService.textToArtistName("Moe Price");
		authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setIsbn13("9782867268915");
		 findobj = new FinderObject(bookdetail);
		searchfor.add(findobj);
		
		
		bookdetail = new BookDetailDao();
		bookdetail.setTitle("Jour de lessive");
		author = catalogService.textToArtistName("Frédéric Stehr");
		authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setIsbn13("9782211206464");
		 findobj = new FinderObject(bookdetail);
		searchfor.add(findobj);
		
		bookdetail = new BookDetailDao();
		bookdetail.setTitle("Superlapin");
		author = catalogService.textToArtistName("Stephanie Blake");
		authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setIsbn13("9782211089685");
		 findobj = new FinderObject(bookdetail);
		searchfor.add(findobj);
		
	}


	@Test
	public void testSearchLogic() throws Exception {
		BookDao book = new BookDao();
		book.getBookdetail().setTitle("Jour de lessive");
		ArtistDao author = catalogService.textToArtistName("Frédéric Stehr");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		book.getBookdetail().setAuthors(authors);
		book.getBookdetail().setIsbn13("9782211206464");

		FinderObject findobj = new FinderObject(book.getBookdetail());
		

		// service call
		findobj = bnfSearch.findDetails(findobj,210);
		
		// check call
		Assert.assertNotNull(findobj);
		Assert.assertFalse(findobj.getSearchStatus() == CatalogService.DetailStatus.NODETAIL);
		Assert.assertNotNull(findobj.getBookdetail().getDescription());
		Assert.assertEquals(new Long(7), findobj.getCurrentFinderLog());
	}
	

	
	@Test
	public void testFindForList() throws Exception  {
		// use search for list
		bnfSearch.findDetailsForList(searchfor, 210, 10);
		
		// ensure that searchfor is not null, that all results have 7 in
		// the finder log, that detailfound is true for at least one
		Assert.assertNotNull(searchfor);
		boolean detailfound = false;
		for (FinderObject findobj:searchfor) {
			Long finderlog=findobj.getCurrentFinderLog();
			Assert.assertTrue(finderlog%7==0);
			detailfound |= findobj.getSearchStatus()!=null && findobj.getSearchStatus()==CatalogService.DetailStatus.DETAILFOUND;
		}
		Assert.assertTrue(detailfound);
	}
	
	
	@Test
	public void testInsertSubjects() {
		//insertSubjectsIntoBookDetail(List<String> subjectstrings, BookDetailDao bookdetail) 
		// make bookdetail
		BookDao book = new BookDao();
		BookDetailDao bookdetail = book.getBookdetail();
		
		// add subjects red blue green through call
		List<String> subjects = new ArrayList<String>();
		subjects.add("red");
		subjects.add("green");
		subjects.add("blue");

		bookdetail = bnfSearch.insertSubjectsIntoBookDetail(subjects, bookdetail);
		
		// check - should have subjects not null, with size 3
		Assert.assertNotNull(bookdetail.getSubjects());
		Assert.assertEquals(3L,bookdetail.getSubjects().size());
		
		// add another array of subjects - blue and pink
		subjects = new ArrayList<String>();
		subjects.add("pink");
		subjects.add("blue");

		bookdetail = bnfSearch.insertSubjectsIntoBookDetail(subjects, bookdetail);
		// should have subjects not null, with size of 4
		Assert.assertNotNull(bookdetail.getSubjects());
		Assert.assertEquals(4L,bookdetail.getSubjects().size());
	}
	
	@Test
	public void testNormalizeArtistName() throws Exception {
		String totest = "Martin, Margaret";
		// service call
		String result = bnfSearch.normalizeArtistName(totest);
		
		// check call
		Assert.assertNotNull(result);
		Assert.assertEquals("Margaret Martin",result);
	}	

	@Test
	public void testISBNNotFoundWrite() throws Exception {
		BookDao book = new BookDao();
		book.getBookdetail().setIsbn13("1111111111111");
		book.getBookdetail().setTitle("coco tout nu");
		ArtistDao author = catalogService.textToArtistName("Monfreid");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		book.getBookdetail().setAuthors(authors);

		FinderObject findobj = new FinderObject(book.getBookdetail());

		// service call
		findobj = bnfSearch.searchLogic(findobj);
		Assert.assertTrue(findobj.getSearchStatus() == CatalogService.DetailStatus.DETAILNOTFOUNDWISBN);
	}

}