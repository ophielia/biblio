package meg.biblio.catalog;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

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
import meg.biblio.search.BookSearchCriteria;
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
public class BookMemberServiceTest {

	@Autowired
	CatalogService catalogService;

	@Autowired
	BookMemberService bMemberService;
	
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
		ArtistDao artist = bMemberService.textToArtistName("Susan Cooper");
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
		pubtestbook.getBookdetail().setDescription("description");
		pubtestbook.getBookdetail().setClientspecific(false);

		// save book
		BookDao pubtest = catalogService.saveBook(pubtestbook);
		pubtestid = pubtest.getId();
	}

	@Test
	public void testTextToArtistName() {
		// text "Michael Vincent Marbboury"
		String text = "Michael Vincent Marbboury";
		ArtistDao name = bMemberService.textToArtistName(text);
		// check firstname
		Assert.assertEquals("Michael", name.getFirstname());
		// check middlename
		Assert.assertEquals("Vincent", name.getMiddlename());
		// check lastname
		Assert.assertEquals("Marbboury", name.getLastname());

		// text "Bill Moyers"
		text = "Bill Moyers";
		name = bMemberService.textToArtistName(text);
		// check firstname
		Assert.assertEquals("Bill", name.getFirstname());
		// check lastname
		Assert.assertEquals("Moyers", name.getLastname());

		// text "Sendak"
		text = "Sendak";
		name = bMemberService.textToArtistName(text);
		// check lastname
		Assert.assertEquals("Sendak", name.getLastname());

		// text "Martin, Kate"
		text = "Martin, Kate";
		name = bMemberService.textToArtistName(text);
		// check firstname
		Assert.assertEquals("Kate", name.getFirstname());
		// checkk lastname
		Assert.assertEquals("Martin", name.getLastname());

		// text null
		name = bMemberService.textToArtistName(null);
		// check null
		Assert.assertNull(name);

		// text "Martin, Jack Orion"
		text = "Martin, Jack Orion";
		name = bMemberService.textToArtistName(text);
		// check firstname
		Assert.assertEquals("Jack", name.getFirstname());
		// check middlename
		Assert.assertEquals("Orion", name.getMiddlename());
		// check lastname
		Assert.assertEquals("Martin", name.getLastname());
	}
}