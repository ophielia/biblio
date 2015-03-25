package meg.biblio.catalog.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import flexjson.JSONSerializer;

@RequestMapping("/books")
@SessionAttributes("bookModel")
@Controller
public class BookController {

	@Autowired
	CatalogService catalogService;

	@Autowired
	DetailSearchService detSearchService;
	
	@Autowired
	SelectKeyService keyService;

	@Autowired
	ClientService clientService;

	@Autowired
	BookModelValidator bookValidator;
	
    @Autowired
    AppSettingService settingService;	
    
	
	public static final class EditMode {
		public static String title = "T";
		public static String isbn="I";
		public static String editbook="E";
	}
	

	
	@RequestMapping(params = "form", method = RequestMethod.GET, produces = "text/html")
	public String showNewBookPage(BookModel bookModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);

		// clear model, place in uiModel
		Long classification = bookModel.getShelfcode();
		bookModel = new BookModel();
		bookModel.setShelfcode(classification);
		bookModel.setCreatenewid(new Boolean(true));
		bookModel.setStatus(CatalogService.Status.SHELVED);
		bookModel.setAssignedcode(null);
		uiModel.addAttribute("bookModel", bookModel);

		String shortname = client.getShortname();
		uiModel.addAttribute("clientname",shortname);
		


		// return choosebook page
		return "book/create";
	}	



	
	// assign code for new (to catalog) book
	@RequestMapping(value = "/newbook", method = RequestMethod.POST, produces = "text/html")
	public String findInfo(BookModel bookModel,
			Model uiModel, BindingResult bindingResult,HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// validation - check isbn - OR - title 
		// if not generate new - no existing book for book code
		bookValidator.validateNewBookEntry(bookModel, bindingResult,client);
		if (bindingResult.hasErrors()) {
			String shortname = client.getShortname();
			uiModel.addAttribute("clientname",shortname);
			if (bindingResult.hasFieldErrors("newbooknr")) {
				// this book is already found, so add clue that link should be 
				// shown to assign code to existingbook
				uiModel.addAttribute("showlinkexist",true);
			}
			// return to choosenewbook page
			return "book/create";
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

		// return editbook view (unless multiresults)
		bookModel.setEditMode(EditMode.editbook);
		String returnview = "book/editbook";
		// determine if another search should be made or not
		long detstatus = book.getBookdetail().getDetailstatus().longValue();
		if (detstatus == CatalogService.DetailStatus.DETAILNOTFOUNDWISBN) {
			// should search again - steer this by setting attribute
			uiModel.addAttribute("searchagain",true);
		} else if (detstatus == CatalogService.DetailStatus.MULTIDETAILSFOUND) {
			// add found objects to model
			List<FoundDetailsDao> multidetails = bookModel.getFounddetails();
			uiModel.addAttribute("foundDetails", multidetails);

			returnview =  "book/choosedetails";
		}
		
		// add lookups / displays for view
		String shortname = client.getShortname();
		uiModel.addAttribute("clientname",shortname);
		
		// check uses barcodes
		Boolean showbarcode = client.getUsesBarcodes()!=null && client.getUsesBarcodes();
		uiModel.addAttribute("showbarcodes",showbarcode);	
		// return view
		return returnview;

	}

	// persist any changes to book (new classification, addition of isbn)
	@RequestMapping(value = "/updatebook", method = RequestMethod.POST, produces = "text/html")
	public String updateBook(BookModel bookModel, Model uiModel,
			HttpServletRequest httpServletRequest, BindingResult bindingResult,Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();
		bookModel.setTrackchange(false);
		bookValidator.validateUpdateBook(bookModel,bindingResult);
		if (bindingResult.hasErrors()) {
			String returnview = "book/editbook";

			// MM return returnview;
		}
		
		// update book - if changed
		try {
			bookModel = catalogService.createCatalogEntryFromBookModel(clientid, bookModel);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		// return view - returns either to display book, or to assign code
		uiModel.addAttribute("bookModel",bookModel);
		String shortname = client.getShortname();
		uiModel.addAttribute("clientname",shortname);
		Boolean showbarcode = client.getUsesBarcodes()!=null && client.getUsesBarcodes();
		uiModel.addAttribute("showbarcodes",showbarcode);
		// redirect to display book page

		return "redirect:/books/display/" + encodeUrlPathSegment(bookModel.getBookid().toString(), httpServletRequest);
		//return new RedirectView("/books/display/" + bookModel.getBookid(), true);

		
/*
 * 
 * 		if (showbarcode) {
			// return assign code page
			return "book/assigncode";
		} else {
			// redirect to display book page
			String redirect = "/books/display/" + bookModel.getBookid();
			return "redirect:" + redirect;
			
		}

 */
	}
	// persist any changes to book (new classification, addition of isbn)
	@RequestMapping(value = "/assign", method = RequestMethod.POST, produces = "text/html")
	public String assignCodeToBook(BookModel bookModel,
			Model uiModel, BindingResult bindingResult,HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// gather info - code, bookid
		Long bookid = bookModel.getBook().getId();
		String code = bookModel.getAssignedcode();

		// validation - has this barcode already been assigned??
		bookValidator.validateAssignCodeToBook(code,bindingResult);
		if (bindingResult.hasErrors()) {
			BookModel model = catalogService.loadBookModel(bookModel.getBook().getId());
			bookModel.setAssignedcode(null);
			uiModel.addAttribute("bookModel",bookModel);
			return "book/assigncode";
		}
		
		// service call - assignCodeToBook
		catalogService.assignCodeToBook(code, bookid);
		// load book model
		BookModel model = catalogService.loadBookModel(bookid);
		uiModel.addAttribute("bookModel",model);
		// go to success page
		return "book/assignsuccess";
	}
  
	@RequestMapping(value = "/display/{id}", method = RequestMethod.GET, produces = "text/html")
	public String showBook(@PathVariable("id") Long id, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {

		BookModel model = new BookModel();
		if (id != null) {
			model = catalogService.loadBookModel(id);
		}

		uiModel.addAttribute("bookModel", model);

		return "book/show";
	}

	
    private String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        } catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }
	@RequestMapping(value = "/editall/{id}", method = RequestMethod.GET, produces = "text/html")
	public String showEditBookForm(@PathVariable("id") Long id, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
	
		BookModel model = new BookModel();
		if (id != null) {
			model = catalogService.loadBookModel(id);
		}
	
		uiModel.addAttribute("bookModel", model);
	
		String returnview="book/edit";
		if (model.getDetailstatus().longValue()== CatalogService.DetailStatus.DETAILNOTFOUND) {
			returnview = "book/editall";
		}
		return returnview;
	}	
	
	@RequestMapping(value = "/editall/{id}", method = RequestMethod.POST, produces = "text/html")
	public String saveEditAll(
			@ModelAttribute("bookModel") BookModel bookModel,
			@PathVariable("id") Long id, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();

		// only making a few changes. load the model from the database, and copy
		// changes into database model (from passed model)
		if (id != null) {

			ArtistDao author = catalogService.textToArtistName(bookModel
					.getAuthorname());
			ArtistDao illustrator = catalogService.textToArtistName(bookModel
					.getIllustratorname());
			String publisher = bookModel.getPublishername();
			String isbn = bookModel.getIsbn10();

			BookModel model = catalogService.loadBookModel(id);
			if (isbn != null)
				model.setIsbn10(isbn);
			if (publisher != null)
				model.setPublisher(publisher);
			if (author != null)
				model.setAuthorInBook(author);
			if (illustrator != null)
				model.setIllustratorInBook(author);
			model.setType(bookModel.getType());
			model.setShelfcode(bookModel.getShelfcode());
			model.setStatus(bookModel.getStatus());
			model.setLanguage(bookModel.getLanguage());

			BookModel book;
			try {
				book = catalogService.updateCatalogEntryFromBookModel(
						clientkey, model, true);
				uiModel.addAttribute("bookModel", book);
			} catch (GeneralSecurityException | IOException e) {
				e.printStackTrace();
			}
		}
		return "book/show";
	}

	@RequestMapping(value = "/choosedetails/{id}", method = RequestMethod.GET, produces = "text/html")
	public String showBookDetails(@PathVariable("id") Long id, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {

		BookModel model = new BookModel();
		if (id != null) {
			// add found objects to model
			List<FoundDetailsDao> multidetails = catalogService
					.getFoundDetailsForBook(id);
			uiModel.addAttribute("foundDetails", multidetails);

			model = catalogService.loadBookModel(id);
		}

		uiModel.addAttribute("bookModel", model);

		return "book/choosedetails";
	}

	@RequestMapping(value = "/choosedetails", params = { "detailid", "bookid" }, method = RequestMethod.POST, produces = "text/html")
	public String assignBookDetails(@RequestParam("detailid") Long detailid,
			@RequestParam("bookid") Long bookid, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		// call catalog service
		try {
			detSearchService.assignDetailToBook(detailid, bookid);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// show book
		BookModel model = catalogService.loadBookModel(bookid);
		uiModel.addAttribute("bookModel", model);

		return "book/show";
	}

	@RequestMapping(value = "/choosedetails", params = "isbn", method = RequestMethod.POST, produces = "text/html")
	public String assignBookISBN(BookModel bookModel, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
		Long id = bookModel.getBookid();
		// add isbn to book model
		String isbn = bookModel.getIsbn10();

		if (isbn != null && isbn.length() > 0) {
			BookModel dbmodel = catalogService.loadBookModel(id);
			dbmodel.setIsbn10(isbn);
			// update book , search for details
			dbmodel = detSearchService.fillInDetailsForBook(dbmodel, client);
			uiModel.addAttribute("bookModel", dbmodel);

			List<FoundDetailsDao> multidetails = catalogService
					.getFoundDetailsForBook(id);
			uiModel.addAttribute("foundDetails", multidetails);

			uiModel.addAttribute("bookModel", dbmodel);
			return "book/choosedetails";
		}
		bookModel = catalogService.loadBookModel(id);
		List<FoundDetailsDao> multidetails = catalogService
				.getFoundDetailsForBook(id);
		uiModel.addAttribute("foundDetails", multidetails);

		uiModel.addAttribute("bookModel", bookModel);
		return "book/choosedetails";
	}

	@ModelAttribute("classHash")
	public HashMap<Long, ClassificationDao> getClassificationInfo(
			HttpServletRequest httpServletRequest, Principal principal,Locale locale) {
		String lang = locale.getLanguage();
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();

		HashMap<Long, ClassificationDao> shelfclasses = catalogService
				.getShelfClassHash(clientkey, lang);

		return shelfclasses;
	}
	
	

	@ModelAttribute("classJson")
	public String getClassificationInfoAsJson(
			HttpServletRequest httpServletRequest, Principal principal,Locale locale) {
		String lang = locale.getLanguage();
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();

		List<ClassificationDao> shelfclasses = catalogService
				.getShelfClassList(clientkey, lang);

		JSONSerializer serializer = new JSONSerializer();
		String json = serializer.exclude("*.class").serialize(shelfclasses);
		return json;
	}

	@ModelAttribute("typeLkup")
	public HashMap<Long, String> getBookTypeLkup(
			HttpServletRequest httpServletRequest,Locale locale) {
		String lang = locale.getLanguage();

		HashMap<Long, String> booktypedisps = keyService.getDisplayHashForKey(
				CatalogService.booktypelkup, lang);
		return booktypedisps;
	}

	@ModelAttribute("statusLkup")
	public HashMap<Long, String> getStatusLkup(
			HttpServletRequest httpServletRequest,Locale locale) {
		String lang = locale.getLanguage();

		HashMap<Long, String> booktypedisps = keyService.getDisplayHashForKey(
				CatalogService.bookstatuslkup, lang);
		return booktypedisps;
	}

	@ModelAttribute("langLkup")
	public HashMap<String, String> getLanguageLkup(
			HttpServletRequest httpServletRequest,Locale locale) {
		String lang = locale.getLanguage();

		HashMap<String, String> langdisps = keyService
				.getStringDisplayHashForKey(CatalogService.languagelkup, lang);
		return langdisps;
	}

	@ModelAttribute("detailstatusLkup")
	public HashMap<Long, String> getDetailStatusLkup(
			HttpServletRequest httpServletRequest,Locale locale) {
		String lang = locale.getLanguage();

		HashMap<Long, String> booktypedisps = keyService.getDisplayHashForKey(
				CatalogService.detailstatuslkup, lang);
		return booktypedisps;
	}
	
    
    @ModelAttribute("imagebasedir")
    public String getImageBaseSetting(HttpServletRequest httpServletRequest) {
    	String imagebase = settingService.getSettingAsString("biblio.imagebase");
    	return imagebase; 
    }  

}
