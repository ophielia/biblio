package meg.biblio.catalog;

import java.util.ArrayList;
import java.util.List;

import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
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
public class BNFCatalogFinderTest {

	@Autowired
	CatalogService catalogService;

	
	@Autowired
	ClientService clientService;	
	@Autowired
	SearchService searchService;

	

	@Autowired
	BookMemberService bMemberService;
	
	@Autowired
	BNFCatalogFinder bnfSearch;

	
	List<FinderObject> searchfor;
	@Before
	public void setup() {
		Long clientkey = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientkey);
		searchfor= new ArrayList<FinderObject>();
		BookDetailDao bookdetail = new BookDetailDao();
		bookdetail.setTitle("Jour de lessive");
		ArtistDao author = bMemberService.textToArtistName("Frédéric Stehr");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setIsbn13("9782211206464");
		FinderObject findobj = new FinderObject(bookdetail,client);
		searchfor.add(findobj);
		
		bookdetail = new BookDetailDao();
		bookdetail.setTitle("Petit Pierrot : Décrocher la Lune");
		author = bMemberService.textToArtistName("Alberto Varanda");
		authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setIsbn13("9782302007482");
		 findobj = new FinderObject(bookdetail,client);
		searchfor.add(findobj);
		
		bookdetail = new BookDetailDao();
		bookdetail.setTitle("Et pourquoi ?");
		author = bMemberService.textToArtistName("Michel Zeveren");
		authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setIsbn13("9782211093293");
		 findobj = new FinderObject(bookdetail);
		searchfor.add(findobj);
		
		bookdetail = new BookDetailDao();
		bookdetail.setTitle("C'est plus drôle avec Mimi");
		author = bMemberService.textToArtistName("Lucy Cousins");
		authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setIsbn13("9782226156488");
		 findobj = new FinderObject(bookdetail);
		searchfor.add(findobj);
		
		bookdetail = new BookDetailDao();
		bookdetail.setTitle("Eloïse");
		author = bMemberService.textToArtistName("Kay Thompson");
		authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setIsbn13("9782070561797");
		 findobj = new FinderObject(bookdetail);
		searchfor.add(findobj);
		
		
		bookdetail = new BookDetailDao();
		bookdetail.setTitle("Le Premier Grand Voyage du Père Noël");
		author = bMemberService.textToArtistName("Moe Price");
		authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setIsbn13("9782867268915");
		 findobj = new FinderObject(bookdetail);
		searchfor.add(findobj);
		
		
		bookdetail = new BookDetailDao();
		bookdetail.setTitle("Jour de lessive");
		author = bMemberService.textToArtistName("Frédéric Stehr");
		authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setIsbn13("9782211206464");
		 findobj = new FinderObject(bookdetail);
		searchfor.add(findobj);
		
		bookdetail = new BookDetailDao();
		bookdetail.setTitle("Superlapin");
		author = bMemberService.textToArtistName("Stephanie Blake");
		authors = new ArrayList<ArtistDao>();
		authors.add(author);
		bookdetail.setAuthors(authors);
		bookdetail.setIsbn13("9782211089685");
		 findobj = new FinderObject(bookdetail);
		searchfor.add(findobj);
		
	}


	@Test
	public void testSearchLogic() throws Exception {
		Long clientkey = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientkey);
		BookDao book = new BookDao();
		book.getBookdetail().setTitle("Jour de lessive");
		ArtistDao author = bMemberService.textToArtistName("Frédéric Stehr");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		book.getBookdetail().setAuthors(authors);
		book.getBookdetail().setIsbn13("9782211206464");

		FinderObject findobj = new FinderObject(book.getBookdetail(),client);
		

		// service call
		findobj = bnfSearch.findDetails(findobj,210);
		
		// check call
		Assert.assertNotNull(findobj);
		Assert.assertFalse(findobj.getSearchStatus() == CatalogService.DetailStatus.NODETAIL);
		Assert.assertNotNull(findobj.getBookdetail().getDescription());
		Assert.assertEquals(new Long(7), findobj.getCurrentFinderLog());
	}
	
	@Test
	public void testAssignDetails() throws Exception {
		Long clientkey = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientkey);
		// first get some multi details
		BookDao book = new BookDao();
		book.getBookdetail().setTitle("Jour de lessive");
		ArtistDao author = bMemberService.textToArtistName("Frédéric Stehr");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		book.getBookdetail().setAuthors(authors);
		FinderObject findobj = new FinderObject(book.getBookdetail(),client);
		// service call - to get multidetails
		findobj = bnfSearch.findDetails(findobj, 210);
		// get the first of the multidetails
		List<FoundDetailsDao> detailslist = findobj.getMultiresults();
		if (detailslist != null && detailslist.size() > 0) {
			FoundDetailsDao fd = detailslist.get(0);
			String titlecompare = fd.getTitle();
			// service call
			findobj = bnfSearch.assignDetailToBook(findobj, fd);
			BookDetailDao bdetail = findobj.getBookdetail();
			// ensure - finder is logged in findobj, detailstatus in
			// findobj is found, titles match
			Long finderlog = findobj.getCurrentFinderLog();
			Assert.assertTrue(finderlog % 7 == 0);
			Assert.assertEquals(titlecompare, bdetail.getTitle());
			Assert.assertEquals(findobj.getSearchStatus().longValue(),
					CatalogService.DetailStatus.DETAILFOUND);
		} else {
			// test fail - no multidetails found
			Assert.assertEquals(1L, 2L);
		}

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

		bookdetail = bMemberService.insertSubjectsIntoBookDetail(subjects, bookdetail);
		
		// check - should have subjects not null, with size 3
		Assert.assertNotNull(bookdetail.getSubjects());
		Assert.assertEquals(3L,bookdetail.getSubjects().size());
		
		// add another array of subjects - blue and pink
		subjects = new ArrayList<String>();
		subjects.add("pink");
		subjects.add("blue");

		bookdetail = bMemberService.insertSubjectsIntoBookDetail(subjects, bookdetail);
		// should have subjects not null, with size of 4
		Assert.assertNotNull(bookdetail.getSubjects());
		Assert.assertEquals(4L,bookdetail.getSubjects().size());
	}
	
	@Test
	public void testNormalizeArtistName() throws Exception {
		String totest = "Martin, Margaret";
		// service call
		String result = bMemberService.normalizeArtistName(totest);
		
		// check call
		Assert.assertNotNull(result);
		Assert.assertEquals("Margaret Martin",result);
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
		findobj = bnfSearch.searchLogic(findobj);
		Assert.assertTrue(findobj.getSearchStatus() == CatalogService.DetailStatus.DETAILNOTFOUNDWISBN);
	}
	
	@Test
	public void testISBNNotFoundRead() throws Exception {
		Long clientkey = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientkey);
		BookDao book = new BookDao();
		book.getBookdetail().setTitle("Jour de lessive");
		ArtistDao author = bMemberService.textToArtistName("Frédéric Stehr");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		book.getBookdetail().setAuthors(authors);
		book.getBookdetail().setDetailstatus(CatalogService.DetailStatus.DETAILNOTFOUNDWISBN);

		FinderObject findobj = new FinderObject(book.getBookdetail(),client);

		// service call
		findobj = bnfSearch.searchLogic(findobj);
		Assert.assertFalse(findobj.getSearchStatus() == CatalogService.DetailStatus.DETAILNOTFOUNDWISBN);
	}	

}