package meg.biblio.catalog.web;


import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.ArtistDaoDataOnDemand;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDaoDataOnDemand;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.catalog.web.validator.BookModelValidator;
import meg.biblio.common.ClientService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("classpath*:/META-INF/spring/applicationContext*.xml")
public class BookControllerTest {

	@Mock
	CatalogService bookService;
	
	@Mock
	ClientService clientService;	

	@Autowired
	BookModelValidator bookValidator;
	
	
    @InjectMocks
    BookController controllerUnderTest;
    private MockMvc mockMvc;

    @Before
    public void setup() {
    	// this must be called for the @Mock annotations above to be processed
        // and for the mock service to be injected into the controller under
        // test.
        MockitoAnnotations.initMocks(this);

    	this.mockMvc = MockMvcBuilders.standaloneSetup(controllerUnderTest)
    			.build();
    }



    @Test
    public void getCreateForm() throws Exception {

    	when(bookService.getAllBooks()).thenReturn(new ArrayList<BookDao>());

        this.mockMvc.perform(get("/books?create")
        		.accept(MediaType.TEXT_HTML)
        		.param("form","form")
        		.header("content-type", "application/x-www-form-urlencoded"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/create"));


    }

    @Test
    public void createEntry() throws Exception {
    	BookDaoDataOnDemand cdod = new BookDaoDataOnDemand();
    	ArtistDaoDataOnDemand adod = new ArtistDaoDataOnDemand();
    	BookDao book = cdod.getNewTransientBookDao(0);
    	ArtistDao artist = adod.getNewTransientArtistDao(0);
    	book.setId(2222L);
    	List<ArtistDao> authors = new ArrayList<ArtistDao>();
    	authors.add(artist);
    	book.setAuthors(authors);
    	BookModel returnmodel = new BookModel(book);

    	when(bookService.getAllBooks()).thenReturn(new ArrayList<BookDao>());
    	when(bookService.createCatalogEntryFromBookModel(any(Long.class),any(BookModel.class))).thenReturn(returnmodel);
    	when(clientService.getCurrentClientKey(null)).thenReturn(1L);

        this.mockMvc.perform(post("/books")
        		.accept(MediaType.TEXT_HTML)
        		.param("title","title")
        		.param("aFname","first")
        		.param("aMname","middle")
        		.param("aLname","last")
        		.header("content-type", "application/x-www-form-urlencoded"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/books/display/2222"));

    }


    @Test
    public void getDisplayModel() throws Exception {
    	BookDaoDataOnDemand cdod = new BookDaoDataOnDemand();
    	ArtistDaoDataOnDemand adod = new ArtistDaoDataOnDemand();
    	BookDao book = cdod.getNewTransientBookDao(0);
    	ArtistDao artist = adod.getNewTransientArtistDao(0);
    	book.setId(2222L);
    	List<ArtistDao> authors = new ArrayList<ArtistDao>();
    	authors.add(artist);
    	book.setAuthors(authors);
    	BookModel returnmodel = new BookModel(book);
    	
    	when(bookService.getAllBooks()).thenReturn(new ArrayList<BookDao>());
    	when(bookService.loadBookModel(any(Long.class))).thenReturn(returnmodel);
    	
        this.mockMvc.perform(get("/books/display/{id}",22222L)
        		.accept(MediaType.TEXT_HTML)
        		.param("form","form")
        		.header("content-type", "application/x-www-form-urlencoded"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/show"));


    }    
}