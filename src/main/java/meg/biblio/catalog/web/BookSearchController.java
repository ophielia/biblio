package meg.biblio.catalog.web;

import java.security.Principal;
import java.util.ArrayList;
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
import meg.biblio.common.web.model.Pager;
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

@RequestMapping("/search")
@Controller
public class BookSearchController {

	private final String sessioncriteria = "sessioncriteriauser";

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

	public static final class PageDir {
		public static final String FIRST="first";
		public static final String PREV="prev";
		public static final String NEXT="next";
		public static final String LAST="last";
	}
	
	@RequestMapping(produces = "text/html")
	public String showList(
			@ModelAttribute("bookListModel") BookListModel model,
			Model uiModel, HttpServletRequest request, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
		BookSearchCriteria criteria = model.getCriteria();
		Pager pager = model.getPager();
		HttpSession session = request.getSession();
		session.setAttribute(sessioncriteria, criteria);
		// initialize pager if necessary
		pager = initPager(pager);
		// get total result count for criteria
		Long resultcount = searchService.getBookCountForCriteria(criteria, clientkey);
		pager.setResultcount(resultcount.intValue());
		// reset pager in model - new search, so resetting page
		pager.setCurrentpage(0);
		model.setPager(pager);
		// initialize grid columns
		model = initGridColumns(model);

		// search for results, and set in model
		List<BookDao> list = searchService.findBooksForCriteria(criteria,
				pager, clientkey);
		model.setBooks(list);

		return "book/resultlist";
	}

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
	public String searchCatalog(
			@ModelAttribute("bookListModel") BookListModel model,
			Model uiModel, HttpServletRequest request, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
		BookSearchCriteria criteria = model.getCriteria();
		Pager pager = model.getPager();
		HttpSession session = request.getSession();
		session.setAttribute(sessioncriteria, criteria);
		// initialize pager if necessary
		pager = initPager(pager);
		// get total result count for criteria
		Long resultcount = searchService.getBookCountForCriteria(criteria, clientkey);
		pager.setResultcount(resultcount.intValue());
		// reset pager in model - new search, so resetting page
		pager.setCurrentpage(0);
		model.setPager(pager);
		
		// search for results, and set in model
		List<BookDao> list = searchService.findBooksForCriteria(criteria,
				pager, clientkey);
		model.setBooks(list);

		return "book/resultlist";
	}

	@RequestMapping(method = RequestMethod.PUT, params = "sort", produces = "text/html")
	public String sortBooks(@RequestParam("sort") Long sorttype,
			@ModelAttribute("bookListModel") BookListModel model,
			Model uiModel, HttpServletRequest request, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
		BookSearchCriteria criteria = model.getCriteria();
		if (sorttype != null) {
			criteria.setOrderby(sorttype);
		}
		HttpSession session = request.getSession();
		session.setAttribute(sessioncriteria, criteria);
		// not doing anything with pager, because results haven't changed - only
		// ordering
		List<BookDao> list = searchService.findBooksForCriteria(criteria,
				null, clientkey);
		model.setBooks(list);

		return "book/resultlist";

	}
	
	@RequestMapping(params = "next",method = RequestMethod.PUT,  produces = "text/html")
	public String nextPage(
			@ModelAttribute("bookListModel") BookListModel model,
			Model uiModel, HttpServletRequest request, Principal principal) {
		return gotoPageResults(PageDir.NEXT, model, uiModel, request, principal);
	}
	
	@RequestMapping(params = "prev",method = RequestMethod.PUT,  produces = "text/html")
	public String prevPage(
			@ModelAttribute("bookListModel") BookListModel model,
			Model uiModel, HttpServletRequest request, Principal principal) {
		return gotoPageResults(PageDir.PREV, model, uiModel, request, principal);
	}
	
	@RequestMapping(params = "first",method = RequestMethod.PUT,  produces = "text/html")
	public String firstPage(
			@ModelAttribute("bookListModel") BookListModel model,
			Model uiModel, HttpServletRequest request, Principal principal) {
		return gotoPageResults(PageDir.FIRST, model, uiModel, request, principal);
	}
	
	@RequestMapping(params = "last",method = RequestMethod.PUT,  produces = "text/html")
	public String lastPage(
			@ModelAttribute("bookListModel") BookListModel model,
			Model uiModel, HttpServletRequest request, Principal principal) {
		return gotoPageResults(PageDir.LAST, model, uiModel, request, principal);
	}	
	
	
	
	
	private String gotoPageResults(String pageparam,
		@ModelAttribute("bookListModel") BookListModel model,
		Model uiModel, HttpServletRequest request, Principal principal) {
	ClientDao client = clientService.getCurrentClient(principal);
	Long clientkey = client.getId();
	
	BookSearchCriteria criteria = model.getCriteria();
	Pager pager = model.getPager();
	
	// initialize pager if necessary
	pager = initPager(pager);
	
	// increment pager
	if (pageparam!=null) {
		pager.gotoPage(pageparam);
	}
	HttpSession session = request.getSession();
	session.setAttribute(sessioncriteria, criteria);
	
	// get total result count for criteria
	if (pager.getResultcount()<0) {
		Long resultcount = searchService.getBookCountForCriteria(criteria, clientkey);	
		pager.setResultcount(resultcount.intValue());
	}
	
	// reset pager in model - new search, so resetting page
	model.setPager(pager);
	
	// search for results, and set in model
	List<BookDao> list = searchService.findBooksForCriteria(criteria,
			pager, clientkey);
	model.setBooks(list);
	
	return "book/resultlist";
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.setAutoGrowCollectionLimit(100024);
	}

	private Pager initPager(Pager pager) {
		// determine if init is needed (no resultsperpage set)
		if (pager != null && pager.getResultsperpage() < 0) {
			// get results per page from appsetting
			Integer resultsperpage = settingService
					.getSettingAsInteger("biblio.display.bookresultsperpage");
			// set results per page
			pager.setResultsperpage(resultsperpage);
			// set current page to 0
			pager.setCurrentpage(0);
		}
		return pager;
	}
	
	private BookListModel initGridColumns(BookListModel model) {
		// determine if init is needed (no gridColumns set)
		if (model.getGridColumns()<0) {
			// get results per page from appsetting
			Integer gridcols = settingService
					.getSettingAsInteger("biblio.display.bookresultgridcol");
			// set results per page
			model.setGridColumns(gridcols);
		}
		return model;
	}

	private BookSearchCriteria getDefaultCriteria(Long clientkey) {
		BookSearchCriteria criteria = new BookSearchCriteria();
		criteria.setClientid(clientkey);
		List<Long> statuslist = new ArrayList<Long>();
		statuslist.add(CatalogService.Status.INREPAIR);
		statuslist.add(CatalogService.Status.INVNOTFOUND);
		statuslist.add(CatalogService.Status.LOSTBYBORROWER);
		statuslist.add(CatalogService.Status.REMOVEDFROMCIRC);
		criteria.setStatuslist(statuslist);
		criteria.setInstatuslist(false);
		return criteria;
	}

	@ModelAttribute("bookListModel")
	public BookListModel populateBookList(HttpServletRequest request,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();

		HttpSession session = request.getSession();
		BookSearchCriteria criteria = (BookSearchCriteria) session
				.getAttribute(sessioncriteria);
		if (criteria == null) {
			criteria = getDefaultCriteria(clientkey);
			session.setAttribute(sessioncriteria, criteria);
		}
		BookListModel model = new BookListModel(criteria);

		return model;
	}

	@ModelAttribute("sortlist")
	public List<SelectValueDao> referenceSortkeys(
			HttpServletRequest httpServletRequest, Locale locale) {
		String lang = locale.getLanguage();

		List<SelectValueDao> sortlist = keyService.getSelectValuesForKey(
				BookSearchCriteria.usersortlkup, lang);
		return sortlist;
	}

	@ModelAttribute("statusLkup")
	public HashMap<Long, String> getStatusLkup(
			HttpServletRequest httpServletRequest, Locale locale) {
		String lang = locale.getLanguage();

		HashMap<Long, String> booktypedisps = keyService.getDisplayHashForKey(
				CatalogService.bookstatuslkup, lang);
		return booktypedisps;
	}

	@ModelAttribute("classHash")
	public HashMap<Long, ClassificationDao> getClassificationInfo(
			HttpServletRequest httpServletRequest, Principal principal,
			Locale locale) {
		String lang = locale.getLanguage();
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();

		HashMap<Long, ClassificationDao> shelfclasses = catalogService
				.getShelfClassHash(clientkey, lang);

		return shelfclasses;
	}

	@ModelAttribute("imagebasedir")
	public String getImageBaseSetting(HttpServletRequest httpServletRequest) {
		String imagebase = settingService
				.getSettingAsString("biblio.imagebase");
		return imagebase;
	}

}
