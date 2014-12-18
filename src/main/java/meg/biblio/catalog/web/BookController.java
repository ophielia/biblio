package meg.biblio.catalog.web;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.CatalogServiceImpl;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.ClassificationDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.catalog.web.validator.BookModelValidator;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import flexjson.JSONSerializer;


@RequestMapping("/books")
@Controller
public class BookController {

	@Autowired
	CatalogService catalogService;
	
	@Autowired
	SelectKeyService keyService;
	
	@Autowired
	ClientService clientService;	
	
	@Autowired
	BookModelValidator bookValidator;
	

	
    @RequestMapping(params = "form",method = RequestMethod.GET, produces = "text/html")
    public String createBookEntryForm(Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
    	
    	// create empty book model
    	BookModel model = new BookModel();
    	// place in uiModel
    	uiModel.addAttribute("bookModel",model);
    	// return book/create
    	return "book/create";
    	
    }
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String createBookEntry(BookModel model,  Model uiModel,BindingResult bindingResult, HttpServletRequest httpServletRequest, Principal principal) {
    	ClientDao client = clientService.getCurrentClient(principal);
    	Long clientkey = client.getId();
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	bookValidator.validateSimpleEntry(model, bindingResult);

		if (bindingResult.hasErrors()) {
			uiModel.addAttribute("bookModel", model);
			return "book/create";
		}
        uiModel.asMap().clear();
        
        // process book entries (author, illustrator)
        ArtistDao author = catalogService.textToArtistName(model.getAuthorname());
        ArtistDao illustrator = catalogService.textToArtistName(model.getIllustratorname());
        model.addAuthorToBook(author);
        model.addIllustratorToBook(illustrator);
        
        // add book to catalog
        BookModel book = catalogService.createCatalogEntryFromBookModel(clientkey,model);
        Long bookid = book.getBookid();
        
        uiModel.addAttribute("bookModel", book);
        
        // go to show single page, unless multi details were found
        if (book.getDetailstatus().longValue() == CatalogServiceImpl.DetailStatus.MULTIDETAILSFOUND) {
        	// go to pickdetails page
        	return "redirect:/books/choosedetails/" + bookid;
        }
        return "redirect:/books/display/" + bookid;
    }    
    
    @RequestMapping(value="/display/{id}", method = RequestMethod.GET, produces = "text/html")
    public String showBook(@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();

    	BookModel model = new BookModel();
    	if (id!=null) {
    		model = catalogService.loadBookModel(id);	
    	} 
    	
    	uiModel.addAttribute("bookModel",model);

    	return "book/show";
    }   
    
    @RequestMapping(value="/edit/{id}", method = RequestMethod.GET, produces = "text/html")
    public String showEditBookForm(@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();

    	BookModel model = new BookModel();
    	if (id!=null) {
    		model = catalogService.loadBookModel(id);	
    	} 
    	
    	uiModel.addAttribute("bookModel",model);

    	return "book/edit";
    }    
    
    @RequestMapping(value="/edit/{id}", method = RequestMethod.POST, produces = "text/html")
    public String saveEditBook(@ModelAttribute("bookModel") BookModel bookModel,@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
    	ClientDao client = clientService.getCurrentClient(principal);
    	Long clientkey = client.getId();
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();

    	// only making a few changes. load the model from the database, and copy changes into database model (from passed model)
    	if (id!=null) {
    		BookModel model = catalogService.loadBookModel(id);	
    		model.setType(bookModel.getType());
    		model.setShelfclass(bookModel.getShelfclass());
    		model.setStatus(bookModel.getStatus());
    		model.setLanguage(bookModel.getLanguage());
    		BookModel book = catalogService.updateCatalogEntryFromBookModel(clientkey,model);
    		uiModel.addAttribute("bookModel", book);
    	} else {
    		uiModel.addAttribute("bookModel", bookModel);	
    	}

 
        return "book/show";
    }     
    
    @RequestMapping(value="/choosedetails/{id}", method = RequestMethod.GET, produces = "text/html")
    public String showBookDetails(@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();

    	BookModel model = new BookModel();
    	if (id!=null) {
        	// add found objects to model
        	List<FoundDetailsDao> multidetails = catalogService.getFoundDetailsForBook(id);
        	uiModel.addAttribute("foundDetails",multidetails);
    		
    		model = catalogService.loadBookModel(id);	
    	} 
    	
    	uiModel.addAttribute("bookModel",model);

    	return "book/choosedetails";
    }     
    
    @RequestMapping(value="/choosedetails", params={"detailid","bookid"} ,method = RequestMethod.POST, produces = "text/html")
    public String assignBookDetails(@RequestParam("detailid") Long detailid, @RequestParam("bookid") Long bookid,Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
    	// call catalog service
    	try {
			catalogService.assignDetailToBook(detailid, bookid);
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	// redirect to show book
    	return "redirect:/books/display/" + bookid;
    }      
 
    
    @ModelAttribute("classHash")
    public HashMap<Long,ClassificationDao> getClassificationInfo(HttpServletRequest httpServletRequest, Principal principal) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	ClientDao client = clientService.getCurrentClient(principal);
    	Long clientkey = client.getId();
    	
    	HashMap<Long,ClassificationDao> shelfclasses =catalogService.getShelfClassHash(clientkey,lang);
    			
    	return shelfclasses; 
    }
    
    @ModelAttribute("classJson")
    public String getClassificationInfoAsJson(HttpServletRequest httpServletRequest, Principal principal) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	ClientDao client = clientService.getCurrentClient(principal);
    	Long clientkey = client.getId();
    	
    	List<ClassificationDao> shelfclasses =catalogService.getShelfClassList(clientkey,lang);
    	
    	JSONSerializer serializer = new JSONSerializer();
		String json = serializer.exclude("*.class").serialize(shelfclasses);
		return json;
    }    
    
    
    @ModelAttribute("typeLkup")
    public HashMap<Long,String> getBookTypeLkup(HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	
    	HashMap<Long, String> booktypedisps = keyService.getDisplayHashForKey(
    			CatalogService.booktypelkup, lang);
    	return booktypedisps; 
    }

    @ModelAttribute("statusLkup")
    public HashMap<Long,String> getStatusLkup(HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	
    	HashMap<Long, String> booktypedisps = keyService
    			.getDisplayHashForKey(CatalogService.bookstatuslkup, lang);
    	return booktypedisps; 
    }   
    
    @ModelAttribute("langLkup")
    public HashMap<String,String> getLanguageLkup(HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	
    	HashMap<String, String> langdisps = keyService
    			.getStringDisplayHashForKey(CatalogService.languagelkup, lang);
    	return langdisps; 
    }   
        
    @ModelAttribute("detailstatusLkup")
    public HashMap<Long,String> getDetailStatusLkup(HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	
    	HashMap<Long, String> booktypedisps = keyService
    			.getDisplayHashForKey(CatalogService.detailstatuslkup, lang);
    	return booktypedisps; 
    }      
    
}
