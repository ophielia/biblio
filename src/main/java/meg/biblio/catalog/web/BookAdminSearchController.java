package meg.biblio.catalog.web;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.ClassificationDao;
import meg.biblio.catalog.web.model.BookListModel;
import meg.biblio.common.AppSettingService;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.db.dao.SelectValueDao;
import meg.biblio.search.BookSearchCriteria;
import meg.biblio.search.SearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import flexjson.JSONSerializer;


@RequestMapping("/admin/search")
@Controller
public class BookAdminSearchController {

	private final String sessioncriteria="sessioncriteriaadmin";
	
	@Autowired
	SearchService searchService;
	
	@Autowired
	CatalogService catalogService;	
	
	@Autowired
	ClientService clientService;
	
    @Autowired
    SelectKeyService keyService;

    @Autowired
    AppSettingService settingService;
    
	@RequestMapping(produces = "text/html")
    public String showList(@ModelAttribute("bookListModel") BookListModel model,Model uiModel,HttpServletRequest request, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
		BookSearchCriteria criteria = model.getCriteria();
		HttpSession session = request.getSession();
		session.setAttribute(sessioncriteria,criteria);
		List<BookDao> list = searchService.findBooksForCriteria(criteria, clientkey);
		model.setBooks(list);
		return "book/admin/resultlist";
    }
	
	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
	public String searchCatalog(@ModelAttribute("bookListModel") BookListModel model,Model uiModel,HttpServletRequest request, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
		BookSearchCriteria criteria = model.getCriteria();
		HttpSession session = request.getSession();
		session.setAttribute(sessioncriteria,criteria);
		List<BookDao> list = searchService.findBooksForCriteria(criteria, clientkey);
		model.setBooks(list);

		return "book/admin/resultlist";
	}
	
	
	@RequestMapping(method = RequestMethod.PUT,params="sort" ,produces = "text/html")
	public String sortBooks(@RequestParam("sort") Long sorttype,@ModelAttribute("bookListModel") BookListModel model,
			Model uiModel,HttpServletRequest request, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
		BookSearchCriteria criteria = model.getCriteria();
		if (sorttype!=null) {
			// check if the sortby is the same
			if (sorttype.longValue()==criteria.getOrderby()) {
				// clicked on same header twice, change the order
				long neworderdir = criteria.getOrderbydir()==BookSearchCriteria.OrderByDir.ASC?BookSearchCriteria.OrderByDir.DESC:BookSearchCriteria.OrderByDir.ASC;
				criteria.setOrderbydir(neworderdir);
			}
			
			criteria.setOrderby(sorttype);	
		}
		HttpSession session = request.getSession();
		session.setAttribute(sessioncriteria,criteria);
		List<BookDao> list = searchService.findBooksForCriteria(criteria, clientkey);
		model.setBooks(list);

		return "book/admin/resultlist";
	
	}
	
	@RequestMapping(method = RequestMethod.PUT, params = "updateshelfclass",produces = "text/html")
	public String updateShelfClass(@ModelAttribute("bookListModel") BookListModel model,Model uiModel,HttpServletRequest request, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
		BookSearchCriteria criteria = model.getCriteria();
		HttpSession session = request.getSession();
		session.setAttribute(sessioncriteria,criteria);
		
		
		// get books to update
		List<Long> toupdate = model.getCheckedBookIds();
		
		// update books
		catalogService.assignShelfClassToBooks(model.getShelfclassUpdate(),toupdate);
		
		// retrieve and set list
		List<BookDao> list = searchService.findBooksForCriteria(criteria, clientkey);
		model.setBooks(list);		
		// return
		return "book/admin/resultlist";
	}	
	
	@RequestMapping(method = RequestMethod.PUT, params = "updatestatus",produces = "text/html")
	public String updateStatus(@ModelAttribute("bookListModel") BookListModel model,Model uiModel,HttpServletRequest request, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
		BookSearchCriteria criteria = model.getCriteria();
		HttpSession session = request.getSession();
		session.setAttribute(sessioncriteria,criteria);
		
		
		// get books to update
		List<Long> toupdate = model.getCheckedBookIds();
		
		// update books
		catalogService.assignStatusToBooks(model.getStatusUpdate(),toupdate);
		
		// retrieve and set list
		List<BookDao> list = searchService.findBooksForCriteria(criteria, clientkey);
		model.setBooks(list);		
		// return
		return "book/admin/resultlist";
	}		
	
	
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
	    binder.setAutoGrowCollectionLimit(100024);
	}
	
	private BookSearchCriteria getDefaultCriteria(Long clientkey) {
		BookSearchCriteria criteria = new BookSearchCriteria();
		criteria.setClientid(clientkey);
		return criteria;
	}

	@ModelAttribute("bookListModel")
	public BookListModel populateBookList(HttpServletRequest request, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
		
		HttpSession session = request.getSession();
		BookSearchCriteria criteria = (BookSearchCriteria) session.getAttribute(sessioncriteria);
		if (criteria==null) {
			criteria = getDefaultCriteria(clientkey);
			session.setAttribute(sessioncriteria,criteria);
		}
		BookListModel model = new BookListModel(criteria);
		
		return model;
	}
	
	@ModelAttribute("sortlist")
    public List<SelectValueDao> referenceSortkeys(HttpServletRequest httpServletRequest,Locale locale) {
    	String lang = locale.getLanguage();
    	
    	List<SelectValueDao> sortlist = keyService.getSelectValuesForKey(
    			BookSearchCriteria.usersortlkup, lang);
    	return sortlist; 
    }	

    @ModelAttribute("statusLkup")
    public HashMap<Long,String> getStatusLkup(HttpServletRequest httpServletRequest,Locale locale) {
    	String lang = locale.getLanguage();
    	
    	HashMap<Long, String> booktypedisps = keyService
    			.getDisplayHashForKey(CatalogService.bookstatuslkup, lang);
    	return booktypedisps; 
    } 
  
    @ModelAttribute("detailstatusLkup")
    public HashMap<Long,String> getDetailstatusLkup(HttpServletRequest httpServletRequest,Locale locale) {
    	String lang = locale.getLanguage();
    	
    	HashMap<Long, String> booktypedisps = keyService
    			.getDisplayHashForKey(CatalogService.detailstatuslkup, lang);
    	return booktypedisps; 
    } 
      
    @ModelAttribute("booktypeLkup")
    public HashMap<Long,String> getBooktyeLkup(HttpServletRequest httpServletRequest,Locale locale) {
    	String lang = locale.getLanguage();
    	
    	HashMap<Long, String> booktypedisps = keyService
    			.getDisplayHashForKey(CatalogService.booktypelkup, lang);
    	return booktypedisps; 
    } 
          
    
    @ModelAttribute("classHash")
    public HashMap<Long,ClassificationDao> getClassificationInfo(HttpServletRequest httpServletRequest,Principal principal,Locale locale) {
    	String lang = locale.getLanguage();
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
    	
    	HashMap<Long,ClassificationDao> shelfclasses =catalogService.getShelfClassHash(clientkey,lang);
    			
    	return shelfclasses; 
    }    
    
    @ModelAttribute("classList")
    public List<ClassificationDao> getClassificationInfoAsList(HttpServletRequest httpServletRequest,Principal principal,Locale locale) {
    	String lang = locale.getLanguage();
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
    	
    	List<ClassificationDao> shelfclasses =catalogService.getShelfClassList(clientkey,lang);
    			
    	return shelfclasses; 
    }    
    
    @ModelAttribute("imagebasedir")
    public String getImageBaseSetting(HttpServletRequest httpServletRequest) {
    	String imagebase = settingService.getSettingAsString("biblio.imagebase");
    	return imagebase; 
    }     
    
    @ModelAttribute("classJson")
    public String getClassificationInfoAsJson(HttpServletRequest httpServletRequest,Principal principal,Locale locale) {
    	String lang = locale.getLanguage();
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
    	
    	List<ClassificationDao> shelfclasses =catalogService.getShelfClassList(clientkey,lang);
    	ClassificationDao selectdao = new ClassificationDao();
    	selectdao.setId(0L);	
    	selectdao.setTextdisplay("Select");
    	selectdao.setKey(0L);
    	shelfclasses.add(selectdao);
    	JSONSerializer serializer = new JSONSerializer();
		String json = serializer.exclude("*.class").serialize(shelfclasses);
		return json;
    }       

 }
