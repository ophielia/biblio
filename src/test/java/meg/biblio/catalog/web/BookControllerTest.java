package meg.biblio.catalog.web;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.ArtistDaoDataOnDemand;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDaoDataOnDemand;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.catalog.web.validator.BookModelValidator;
import meg.biblio.common.AppSettingService;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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
	ClientService mockClientService;

	@Mock
	BookModelValidator bookValidator;
	
	@Autowired
	ClientService clientService;	
	
	@Mock
	SelectKeyService keyService;
	
    @Mock
	AppSettingService settingService;

	@InjectMocks
	BookController controllerUnderTest;
	private MockMvc mockMvc;

    @Autowired
    @Qualifier("myUserDetailsService")
    protected UserDetailsService loginService;	

    public static class MockSecurityContext implements SecurityContext {
		
	    private static final long serialVersionUID = -1386535243513362694L;
	
	    private Authentication authentication;
	
	    public MockSecurityContext(Authentication authentication) {
	        this.authentication = authentication;
	    }
	
	    @Override
	    public Authentication getAuthentication() {
	        return this.authentication;
	    }
	
	    @Override
	    public void setAuthentication(Authentication authentication) {
	        this.authentication = authentication;
	    }
	}

	@Resource
	private FilterChainProxy springSecurityFilterChain;
	
	
	@Before
	public void setup() {
		// this must be called for the @Mock annotations above to be processed
		// and for the mock service to be injected into the controller under
		// test.
		MockitoAnnotations.initMocks(this);

		this.mockMvc = MockMvcBuilders.standaloneSetup(controllerUnderTest)
				.build();
	}

	
    protected UsernamePasswordAuthenticationToken getPrincipal(String username) {

        
        UserDetails user = this.loginService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                        user, 
                        user.getPassword());

        return authentication;
    }  	
	
	@Test
	public void getCreateForm() throws Exception {

		Long testclientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(testclientid);
		
		UsernamePasswordAuthenticationToken principal = 
                this.getPrincipal("test");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                new MockSecurityContext(principal));
		
		when(mockClientService.getCurrentClient(any(Principal.class)))
		.thenReturn(client);
		
		
		
		
		

		this.mockMvc
				.perform(
						get("/books?create")
							.session(session)
								.accept(MediaType.TEXT_HTML)
								.param("form", "form")
								.header("content-type",
										"application/x-www-form-urlencoded"))
				.andExpect(status().isOk())
				.andExpect(view().name("book/create"));

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
		book.getBookdetail().setAuthors(authors);
		BookModel returnmodel = new BookModel(book);
		Long testclientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(testclientid);
		
		UsernamePasswordAuthenticationToken principal = 
                this.getPrincipal("test");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                new MockSecurityContext(principal));
		
		when(bookService.loadBookModel(any(Long.class)))
				.thenReturn(returnmodel);
		when(mockClientService.getCurrentClient(any(Principal.class)))
		.thenReturn(client);

		
		this.mockMvc
				.perform(
						get("/books/display/{id}", 22222L)
						.session(session)		
						.accept(MediaType.TEXT_HTML)
								.param("form", "form")
								.header("content-type",
										"application/x-www-form-urlencoded"))
				.andExpect(status().isOk()).andExpect(view().name("book/show"));

	}
}