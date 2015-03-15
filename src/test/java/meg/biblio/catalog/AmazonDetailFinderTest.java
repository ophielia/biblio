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
public class AmazonDetailFinderTest {

	@Autowired
	CatalogService catalogService;

	@Autowired
	SearchService searchService;

	@Autowired
	ArtistRepository artistRepo;

	@Autowired
	AmazonDetailFinder amazonSearch;

	private String c_p_a;

	private String nc_p_a;

	private String c_p_i;

	private String nc_p_i;

	private ArtistDao c_p_i_obj;

	private ArtistDao nc_p_i_obj;

	private ArtistDao c_p_a_obj;

	private ArtistDao nc_p_a_obj;

	@Before
	public void setup() {
		c_p_a = "Complete I Author";
		nc_p_a = "N Author";
		c_p_i = "Complete I Illustrator";
		nc_p_i = "N Illustrator";

		c_p_i_obj = amazonSearch.textToArtistName("Complete I Illustrator");
		nc_p_i_obj = amazonSearch.textToArtistName("N Illustrator");
		c_p_a_obj = amazonSearch.textToArtistName("Complete I Author");
		nc_p_a_obj = amazonSearch.textToArtistName("N Author");

		c_p_i_obj = artistRepo.save(c_p_i_obj);
		nc_p_i_obj = artistRepo.save(nc_p_i_obj);
		c_p_a_obj = artistRepo.save(c_p_a_obj);
		nc_p_a_obj = artistRepo.save(nc_p_a_obj);
	}

	@Test
	public void testSearchLogic() throws Exception {
		BookDao book = new BookDao();
		book.getBookdetail().setTitle("coco tout nu");
		ArtistDao author = catalogService.textToArtistName("Monfreid");
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		authors.add(author);
		book.getBookdetail().setAuthors(authors);

		FinderObject findobj = new FinderObject(book.getBookdetail());

		// service call
		findobj = amazonSearch.searchLogic(findobj);
		BookDetailDao bookdetail = findobj.getBookdetail();

		// check call
		Assert.assertNotNull(findobj);
		Assert.assertFalse(findobj.getSearchStatus() == CatalogService.DetailStatus.NODETAIL);
		//Assert.assertEquals(new Long(3), findobj.get());
		
		// by isbn
		book = new BookDao();
		book.getBookdetail().setIsbn13("9782211011716");

		findobj = new FinderObject(book.getBookdetail());

		// service call
		findobj = amazonSearch.searchLogic(findobj);
		bookdetail = findobj.getBookdetail();
		Assert.assertNotNull(findobj);
		Assert.assertFalse(findobj.getSearchStatus() == CatalogService.DetailStatus.NODETAIL);
		Assert.assertNotNull(bookdetail.getTitle());
	}

	@Test
	public void testInsertAuthors() {
		BookDetailDao bd = new BookDetailDao();
		List<String> artists = new ArrayList<String>();
		// Empty book details - no authors. Adding list of two new authors
		artists.add("new writer");
		// service call
		bd = amazonSearch.insertAuthorsIntoBookDetail(artists, bd);
		// should have one writer in the author slot with lastname writer, and
		// first name new
		List<ArtistDao> resultauth = bd.getAuthors();
		Assert.assertNotNull(resultauth);
		Assert.assertEquals(1L, resultauth.size());
		ArtistDao test = resultauth.get(0);
		Assert.assertEquals("new", test.getFirstname());
		Assert.assertEquals("writer", test.getLastname());

		// test with new author already in db (Complete I Author)
		artists.clear();
		bd = new BookDetailDao();

		artists.add(c_p_a);
		// service call
		bd = amazonSearch.insertAuthorsIntoBookDetail(artists, bd);
		// should have one writer in the author slot with lastname Author, and
		// first name Complete
		resultauth = bd.getAuthors();
		Assert.assertNotNull(resultauth);
		Assert.assertEquals(1L, resultauth.size());
		test = resultauth.get(0);
		Assert.assertEquals("Complete", test.getFirstname());
		Assert.assertEquals("Author", test.getLastname());
		Assert.assertNotNull(test.getId());

		// test with new author already in db (Complete I Author),
		// and new illustrator not
		artists.clear();
		bd = new BookDetailDao();

		artists.add(c_p_a);
		artists.add("Random Illustrator");
		// service call
		amazonSearch.insertAuthorsIntoBookDetail(artists, bd);
		// should have one writer in the author slot with lastname Author, and
		// first name Complete
		resultauth = bd.getAuthors();
		Assert.assertNotNull(resultauth);
		Assert.assertEquals(1L, resultauth.size());
		test = resultauth.get(0);
		Assert.assertEquals("Complete", test.getFirstname());
		Assert.assertEquals("Author", test.getLastname());
		Assert.assertNotNull(test.getId());
		List<ArtistDao> resultillus = bd.getIllustrators();
		Assert.assertNotNull(resultillus);
		Assert.assertEquals(1L, resultillus.size());
		test = resultillus.get(0);
		Assert.assertEquals("Random", test.getFirstname());
		Assert.assertEquals("Illustrator", test.getLastname());
		Assert.assertNull(test.getId());

		// Non matching bd
		artists.clear();
		bd = new BookDetailDao();
		List<ArtistDao> bdauth = new ArrayList<ArtistDao>();
		List<ArtistDao> bdillus = new ArrayList<ArtistDao>();
		bdauth.add(c_p_a_obj);
		bdauth.add(nc_p_a_obj);
		bd.setAuthors(bdauth);
		bdillus.add(c_p_a_obj);
		bdillus.add(nc_p_a_obj);
		bd.setIllustrators(bdillus);

		artists.add("Random Author");
		artists.add("Random Illustrator");
		// service call
		bd = amazonSearch.insertAuthorsIntoBookDetail(artists, bd);
		// should have three illustrators and three authors
		resultauth = bd.getAuthors();
		Assert.assertNotNull(resultauth);
		Assert.assertEquals(3L, resultauth.size());
		resultillus = bd.getIllustrators();
		Assert.assertNotNull(resultillus);
		Assert.assertEquals(3L, resultillus.size());

		// Matching bd
		artists.clear();
		bd = new BookDetailDao();
		bdauth = new ArrayList<ArtistDao>();
		bdillus = new ArrayList<ArtistDao>();
		bdauth.add(c_p_a_obj);
		bdauth.add(nc_p_a_obj);
		bd.setAuthors(bdauth);
		bdillus.add(c_p_i_obj);
		bdillus.add(nc_p_i_obj);
		bd.setIllustrators(bdillus);

		artists.add(c_p_a);
		artists.add(c_p_i);
		// service call
		bd = amazonSearch.insertAuthorsIntoBookDetail(artists, bd);
		// should have three illustrators and three authors
		resultauth = bd.getAuthors();
		Assert.assertNotNull(resultauth);
		Assert.assertEquals(2L, resultauth.size());
		test = resultauth.get(0);
		Assert.assertEquals("Complete", test.getFirstname());
		Assert.assertEquals("Author", test.getLastname());
		resultillus = bd.getIllustrators();
		Assert.assertNotNull(resultillus);
		Assert.assertEquals(2L, resultillus.size());
		test = resultillus.get(0);
		Assert.assertEquals("Complete", test.getFirstname());
		Assert.assertEquals("Illustrator", test.getLastname());

		// More complete new
		artists.clear();
		bd = new BookDetailDao();
		bdauth = new ArrayList<ArtistDao>();
		bdillus = new ArrayList<ArtistDao>();
		bdauth.add(nc_p_a_obj);
		bd.setAuthors(bdauth);
		bdillus.add(nc_p_i_obj);
		bd.setIllustrators(bdillus);

		artists.add("Nonpersisted Author");
		artists.add("Nonpersisted Illustrator");
		// service call
		bd = amazonSearch.insertAuthorsIntoBookDetail(artists, bd);
		// should have three illustrators and three authors
		resultauth = bd.getAuthors();
		Assert.assertNotNull(resultauth);
		Assert.assertEquals(1L, resultauth.size());
		test = resultauth.get(0);
		Assert.assertEquals("Nonpersisted", test.getFirstname());
		Assert.assertEquals("Author", test.getLastname());
		Assert.assertNull(test.getId());
		resultillus = bd.getIllustrators();
		Assert.assertNotNull(resultillus);
		Assert.assertEquals(1L, resultillus.size());
		test = resultillus.get(0);
		Assert.assertEquals("Nonpersisted", test.getFirstname());
		Assert.assertEquals("Illustrator", test.getLastname());
		Assert.assertNull(test.getId());

		// More complete in bd
		artists.clear();
		bd = new BookDetailDao();
		bdauth = new ArrayList<ArtistDao>();
		bdillus = new ArrayList<ArtistDao>();
		bdauth.add(amazonSearch.textToArtistName("Nonpersisted Author"));
		bd.setAuthors(bdauth);
		bdillus.add(amazonSearch.textToArtistName("Nonpersisted Illustrator"));
		bd.setIllustrators(bdillus);

		artists.add(nc_p_a);
		artists.add(nc_p_i);
		// service call
		bd = amazonSearch.insertAuthorsIntoBookDetail(artists, bd);
		// should have three illustrators and three authors
		resultauth = bd.getAuthors();
		Assert.assertNotNull(resultauth);
		Assert.assertEquals(1L, resultauth.size());
		test = resultauth.get(0);
		Assert.assertEquals("Nonpersisted", test.getFirstname());
		Assert.assertEquals("Author", test.getLastname());
		Assert.assertNull(test.getId());
		resultillus = bd.getIllustrators();
		Assert.assertNotNull(resultillus);
		Assert.assertEquals(1L, resultillus.size());
		test = resultillus.get(0);
		Assert.assertEquals("Nonpersisted", test.getFirstname());
		Assert.assertEquals("Illustrator", test.getLastname());
		Assert.assertNull(test.getId());

		// Author in bd illustrator
		artists.clear();
		bd = new BookDetailDao();
		bdauth = new ArrayList<ArtistDao>();
		bdillus = new ArrayList<ArtistDao>();
		bdauth.add(amazonSearch.textToArtistName("Random Author"));
		bd.setAuthors(bdauth);
		bdillus.add(c_p_a_obj);
		bd.setIllustrators(bdillus);

		artists.add(c_p_a_obj.getDisplayName());
		// service call
		bd = amazonSearch.insertAuthorsIntoBookDetail(artists, bd);
		// should have 1 illustrator, 1 author
		resultauth = bd.getAuthors();
		Assert.assertNotNull(resultauth);
		Assert.assertEquals(1L, resultauth.size());
		test = resultauth.get(0);
		Assert.assertEquals("Random", test.getFirstname());
		Assert.assertEquals("Author", test.getLastname());
		Assert.assertNull(test.getId());
		resultillus = bd.getIllustrators();
		Assert.assertNotNull(resultillus);
		Assert.assertEquals(1L, resultillus.size());
		test = resultillus.get(0);
		Assert.assertEquals(c_p_a_obj.getFirstname(), test.getFirstname());
		Assert.assertEquals(c_p_a_obj.getLastname(), test.getLastname());
		Assert.assertEquals(c_p_a_obj.getId(), test.getId());
		Assert.assertNotNull(test.getId());

	}

}