package meg.biblio.catalog.web;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.CatalogServiceImpl;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.ClassificationDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.catalog.web.validator.BookModelValidator;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.search.SearchService;

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


@RequestMapping("/dashboard")
@Controller
public class DashboardController {

	@Autowired
	SearchService searchService;

	@Autowired
	SelectKeyService keyService;

	@Autowired
	ClientService clientService;



    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    public String showDashboard(Model uiModel, HttpServletRequest httpServletRequest) {
    	// get client key
    	ClientDao client = clientService.getCurrentClient(httpServletRequest);
    	// get total book count
    	Long bookcount = searchService.getBookCount(client.getId());
    	// get status breakout
    	HashMap<Long,Long> statusbkout = searchService.breakoutByBookField(SearchService.Breakoutfield.STATUS, client.getId());
    	// put together in model
    	uiModel.addAttribute("client",client);
    	uiModel.addAttribute("bookcount",bookcount);
    	uiModel.addAttribute("statusbkout",statusbkout);
    	
    	return "dashboard/show";
    }


    @ModelAttribute("statusLkup")
    public HashMap<Long,String> getStatusLkup(HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();

    	HashMap<Long, String> booktypedisps = keyService
    			.getDisplayHashForKey(CatalogService.bookstatuslkup, lang);
    	return booktypedisps;
    }


}
