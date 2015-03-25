package meg.biblio.catalog.web;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.DetailSearchService;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.ClassificationDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.catalog.web.validator.BookModelValidator;
import meg.biblio.common.AppSettingService;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import flexjson.JSONSerializer;

@RequestMapping("/testrd")
@SessionAttributes("bookModel")
@Controller
public class RedirectController {

	

	@Autowired
	DetailSearchService detSearchService;
	
	@Autowired
	ClientService clientService;

	@Autowired
	CatalogService catalogService;

	@Autowired
	SelectKeyService keyService;

	@Autowired
	AppSettingService settingService;
	
	@Autowired
	BookModelValidator bookValidator;

	@RequestMapping(params = "form", produces = "text/html")
	public String addBookForm(Model uiModel) {
		BookModel bookModel = new BookModel();
		bookModel.setCreatenewid(new Boolean(true));
		bookModel.setStatus(CatalogService.Status.SHELVED);
		bookModel.setAssignedcode(null);
		uiModel.addAttribute("bookModel", bookModel);
		
		return "testrd/create";
	}
	
	@RequestMapping(value = "/findinfo", method = RequestMethod.POST, produces = "text/html")
	public String addBookFindInfo(BookModel bookModel,
			Model uiModel, BindingResult bindingResult,HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// fill lookups
		fillLookups(uiModel, httpServletRequest, principal, locale);
		String shortname = client.getShortname();
		uiModel.addAttribute("clientname",shortname);
		
		// validation - check isbn - OR - title 
		// if not generate new - no existing book for book code
		bookValidator.validateNewBookEntry(bookModel, bindingResult,client);
		if (bindingResult.hasErrors()) {
			// return to choosenewbook page
			return "testrd/create";
		}
		
		// get author and status
		String author = bookModel.getAuthorname();
		Long status = client.getDefaultStatus();
		
		// find information for book
		bookModel.setClientid(clientid);
		ArtistDao artist = catalogService.textToArtistName(author);
		if (artist!=null) {
			bookModel.setAuthorInBook(artist);
		}
		if (status!=null) {
			bookModel.setStatus(status);
		}else {
			bookModel.setStatus(CatalogService.Status.PROCESSING);
		}
		
		// want to find the details for this book ,but not save it yet...
		bookModel.setTrackchange(false);
		bookModel = detSearchService.fillInDetailsForBook(bookModel, client);
		//model = catalogService.createCatalogEntryFromBookModel(clientid, model,
			//	createclientbookid);
		// book-> BookModel -> uiModel
		BookDao book = bookModel.getBook();
		bookModel.setBook(book);
		bookModel.setTrackchange(true);
		uiModel.addAttribute("bookModel", bookModel);

		// fill lookups
		// return editbook view (unless multiresults)
		String returnview = "testrd/editbook";
		// determine if another search should be made or not
		long detstatus = book.getBookdetail().getDetailstatus().longValue();
		if (detstatus == CatalogService.DetailStatus.DETAILNOTFOUNDWISBN) {
			// should search again - steer this by setting attribute
			uiModel.addAttribute("searchagain",true);
		} else if (detstatus == CatalogService.DetailStatus.MULTIDETAILSFOUND) {
			// add found objects to model
			List<FoundDetailsDao> multidetails = bookModel.getFounddetails();
			returnview =  "testrd/choosedetails";
		}
		
		// check uses barcodes
		Boolean showbarcode = client.getUsesBarcodes()!=null && client.getUsesBarcodes();
		uiModel.addAttribute("showbarcodes",showbarcode);	
		
		// return view
		return returnview;

	}
	
	@RequestMapping(value = "/update/{id}", produces = "text/html")
	public String editBook(@PathVariable("id") Long id,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);

		// fill lookups
		fillLookups(uiModel, httpServletRequest, principal, locale);
		String shortname = client.getShortname();
		uiModel.addAttribute("clientname",shortname);
		
		// lookup book
		BookModel bookModel = null;
		if (id!=null) {
			bookModel = catalogService.loadBookModel(id);
		} else {
			bookModel = new BookModel();
		}
		bookModel.setTrackchange(true);
		uiModel.addAttribute("bookModel", bookModel);
		
		return "testrd/editbook";
	}
	
	
	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
	public String addBookCreate(BookModel bookModel, Model uiModel,
				HttpServletRequest httpServletRequest, BindingResult bindingResult,Principal principal, Locale locale)  {

			ClientDao client = clientService.getCurrentClient(principal);
			Long clientid = client.getId();
			
			bookValidator.validateUpdateBook(bookModel,bindingResult);
			if (bindingResult.hasErrors()) {
				// fill lookups
				fillLookups(uiModel, httpServletRequest, principal, locale);
				String shortname = client.getShortname();
				uiModel.addAttribute("clientname",shortname);
				return "testrd/editbook";
			}
			
			// update book - if changed
			try {
				bookModel.setTrackchange(false);
				bookModel = catalogService.createCatalogEntryFromBookModel(clientid, bookModel);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			// return view - returns either to display book, or to assign code
			uiModel.addAttribute("bookModel",bookModel);


			// return target
			return "redirect:/testrd/display/"
					+ encodeUrlPathSegment(bookModel.getBookid().toString(), httpServletRequest);

		}

	
	@RequestMapping(value = "/display/{id}", produces = "text/html")
	public String displayBook(@PathVariable("id") Long id,Model uiModel,HttpServletRequest httpServletRequest, Principal principal,
			Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);
		fillLookups(uiModel, httpServletRequest,  principal,locale);
		String shortname = client.getShortname();
		uiModel.addAttribute("clientname",shortname);
		
		BookModel bmodel = catalogService.loadBookModel(id);
		uiModel.addAttribute("bookModel",bmodel);
		return "testrd/show";
	}

	String encodeUrlPathSegment(String pathSegment,
			HttpServletRequest httpServletRequest) {
		String enc = httpServletRequest.getCharacterEncoding();
		if (enc == null) {
			enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
		}
		try {
			pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
		} catch (UnsupportedEncodingException uee) {
		}
		return pathSegment;
	}

	@RequestMapping(value = "target", produces = "text/html")
	public String showTarget(Model uiModel,HttpServletRequest httpServletRequest, Principal principal,
			Locale locale) {
		fillLookups(uiModel, httpServletRequest,  principal,locale);
		return "testrd/rdtarget";
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = "text/html")
	public String update(@Valid ClientDao clientDao,
			BindingResult bindingResult, Model uiModel,
			HttpServletRequest httpServletRequest) {
		return "redirect:/client/"
				+ encodeUrlPathSegment(clientDao.getId().toString(),
						httpServletRequest);
	}

	// @ModelAttribute("classHash")
	private void fillLookups(Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal,
			Locale locale) {
		String lang = locale.getLanguage();
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();

		HashMap<Long, ClassificationDao> shelfclasses = catalogService
				.getShelfClassHash(clientkey, lang);
		uiModel.addAttribute("classHash", shelfclasses);

		// @ModelAttribute("classJson")
		List<ClassificationDao> classJson = catalogService.getShelfClassList(
				clientkey, lang);

		JSONSerializer serializer = new JSONSerializer();
		String json = serializer.exclude("*.class").serialize(classJson);
		uiModel.addAttribute("classJson", json);

		// @ModelAttribute("typeLkup")
		HashMap<Long, String> typeLkup = keyService.getDisplayHashForKey(
				CatalogService.booktypelkup, lang);
		uiModel.addAttribute("typeLkup", typeLkup);

		// @ModelAttribute("statusLkup")
		HashMap<Long, String> statusLkup = keyService.getDisplayHashForKey(
				CatalogService.bookstatuslkup, lang);
		uiModel.addAttribute("statusLkup", statusLkup);

		// @ModelAttribute("langLkup")
		HashMap<String, String> langLkup = keyService
				.getStringDisplayHashForKey(CatalogService.languagelkup, lang);
		uiModel.addAttribute("langLkup", langLkup);

		// @ModelAttribute("detailstatusLkup")
		HashMap<Long, String> detailstatusLkup = keyService
				.getDisplayHashForKey(CatalogService.detailstatuslkup, lang);
		uiModel.addAttribute("detailstatusLkup", detailstatusLkup);

		// @ModelAttribute("imagebasedir")
		String imagebasedir = settingService
				.getSettingAsString("biblio.imagebase");
		uiModel.addAttribute("imagebasedir", imagebasedir);
	}

}
