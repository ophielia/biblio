package meg.biblio.lending.web;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.ClassificationDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.common.AppSettingService;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.web.model.AssignCodeModel;
import meg.biblio.lending.web.validator.AssignModelValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import flexjson.JSONSerializer;

@RequestMapping("/assignbarcode")
@SessionAttributes("assignCodeModel")
@Controller
public class AssignBarcodeController {

	@Autowired
	ClientService clientService;

	@Autowired
	CatalogService catalogService;

	@Autowired
	SelectKeyService keyService;

	@Autowired
	AppSettingService settingService;

	@Autowired
	AssignModelValidator assignValidator;

	
	public static final class EditMode {
		public static String title = "T";
		public static String isbn="I";
		public static String editbook="E";
	}
	
	@RequestMapping(value = "/enterbook", method = RequestMethod.GET, produces = "text/html")
	public String showChooseBookPage(AssignCodeModel assignCodeModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);

		// clear model, place in uiModel
		Long classification = assignCodeModel.getShelfcode();
		assignCodeModel = new AssignCodeModel();
		assignCodeModel.setShelfclass(classification);
		assignCodeModel.setStatus(CatalogService.Status.SHELVED);
		assignCodeModel.setCreatenewid(new Boolean(true));
		uiModel.addAttribute("assignCodeModel", assignCodeModel);

		String shortname = client.getShortname();
		uiModel.addAttribute("clientname",shortname);
		
		// return choosebook page
		return "barcode/chooseexistbook";
	}
	
	@RequestMapping(value = "/enternewbook", method = RequestMethod.GET, produces = "text/html")
	public String showNewBookPage(AssignCodeModel assignCodeModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);

		// clear model, place in uiModel
		Long classification = assignCodeModel.getShelfcode();
		assignCodeModel = new AssignCodeModel();
		assignCodeModel.setShelfclass(classification);
		assignCodeModel.setCreatenewid(true);
		assignCodeModel.setStatus(CatalogService.Status.SHELVED);
		uiModel.addAttribute("assignCodeModel", assignCodeModel);

		String shortname = client.getShortname();
		uiModel.addAttribute("clientname",shortname);
		
		// return choosebook page
		return "barcode/choosenewbook";
	}	

	// assign code for existing book
	@RequestMapping(value = "/editbook", params = "existing", method = RequestMethod.POST, produces = "text/html")
	public String assignCodeForExistingBook(AssignCodeModel assignCodeModel,
			Model uiModel, BindingResult bindingResult,HttpServletRequest httpServletRequest,
			Principal principal,Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// get booknr
		String booknr = assignCodeModel.getExistbooknr();

		// load bookid
		BookDao book = catalogService.findBookByClientBookId(booknr, client);
		assignCodeModel.setBook(book);
		
		// validate - book already assigned code, book not found
		assignValidator.validateExistingBookEntry(assignCodeModel, book,bindingResult,client);
		if (bindingResult.hasErrors()) {
			// return to choosenewbook page
			return "barcode/choosenewbook";
		}
		
		// book-> AssignCodeModel -> uiModel
		uiModel.addAttribute("assignCodeModel", assignCodeModel);

		// choose return view (isbnedit if no details, otherwise editbook)
		assignCodeModel.setEditMode(EditMode.editbook);
		String returnview = "barcode/editbook";
		if (book.getBookdetail().getDetailstatus().longValue() != CatalogService.DetailStatus.DETAILFOUND) {
			if (book.getBookdetail().getTitle()!=null && book.getBookdetail().getTitle().equals(CatalogService.titledefault)) {
				assignCodeModel.setTitle("");
				assignCodeModel.setEditMode(EditMode.title);
				uiModel.addAttribute("assignCodeModel",assignCodeModel);
				returnview = "barcode/titleedit";
			} else {
				assignCodeModel.setEditMode(EditMode.isbn);
				returnview = "barcode/isbnedit";	
			}
		}


		// add lookups / displays for view
		putDisplayInfoInModel(uiModel, httpServletRequest, client,locale);
		// return view
		return returnview;
	}

	// assign code for existing book
	@RequestMapping(value = "/editbook", params = "booknr", method = RequestMethod.GET, produces = "text/html")
	public String assignCodeForExistingBookGet(@RequestParam("booknr") String booknr,AssignCodeModel assignCodeModel,
			Model uiModel, BindingResult bindingResult,HttpServletRequest httpServletRequest,
			Principal principal,Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// load bookid
		BookDao book = catalogService.findBookByClientBookId(booknr, client);
		assignCodeModel.setBook(book);
		
		// validate - book already assigned code, book not found
		assignValidator.validateExistingBookEntry(assignCodeModel, book,bindingResult,client);
		if (bindingResult.hasErrors()) {
			// return to choosenewbook page
			return "barcode/choosenewbook";
		}
		
		// book-> AssignCodeModel -> uiModel
		uiModel.addAttribute("assignCodeModel", assignCodeModel);

		// choose return view (isbnedit if no details, otherwise editbook)
		assignCodeModel.setEditMode(EditMode.editbook);
		String returnview = "barcode/editbook";
		if (book.getBookdetail().getDetailstatus().longValue() != CatalogService.DetailStatus.DETAILFOUND) {
			if (book.getBookdetail().getTitle()!=null && book.getBookdetail().getTitle().equals(CatalogService.titledefault)) {
				assignCodeModel.setTitle("");
				assignCodeModel.setEditMode(EditMode.title);
				uiModel.addAttribute("assignCodeModel",assignCodeModel);
				returnview = "barcode/titleedit";
			} else {
				assignCodeModel.setEditMode(EditMode.isbn);
				returnview = "barcode/isbnedit";	
			}
		}


		// add lookups / displays for view
		putDisplayInfoInModel(uiModel, httpServletRequest, client,locale);
		// return view
		return returnview;
	}
	
	// assign code for new (to catalog) book
	@RequestMapping(value = "/editbook", params = "newbook", method = RequestMethod.POST, produces = "text/html")
	public String createNewBook(AssignCodeModel assignCodeModel,
			Model uiModel, BindingResult bindingResult,HttpServletRequest httpServletRequest,
			Principal principal,Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// validation - check isbn - OR - title 
		// if not generate new - no existing book for book code
		assignValidator.validateNewBookEntry(assignCodeModel, bindingResult,client);
		if (bindingResult.hasErrors()) {
			String shortname = client.getShortname();
			uiModel.addAttribute("clientname",shortname);
			if (bindingResult.hasFieldErrors("newbooknr")) {
				// this book is already found, so add clue that link should be 
				// shown to assign code to existingbook
				uiModel.addAttribute("showlinkexist",true);
			}
			// return to choosenewbook page
			return "barcode/choosenewbook";
		}
		
		// gatherinfo -cBookid (or generateok), isbn, title, author
		String cbooknr = assignCodeModel.getNewbooknr();
		String isbn = assignCodeModel.getIsbnentry();
		String title = assignCodeModel.getTitle();
		String author = assignCodeModel.getAuthor();
		Boolean createclientbookid = assignCodeModel.getCreatenewid();

		// create book in catalog
		BookModel model = new BookModel();
		if (cbooknr!=null) model.setClientbookid(cbooknr);
		if (isbn!=null) model.setIsbn10(isbn);
		if (title!=null) model.setTitle(title);
		ArtistDao artist = catalogService.textToArtistName(author);
		if (artist!=null) {
			model.setAuthorInBook(artist);
		}
//		model = catalogService.createCatalogEntryFromBookModel(clientid, model,
	//			createclientbookid);
		// book-> AssignCodeModel -> uiModel
		BookDao book = model.getBook();
		assignCodeModel.setBook(book);
		uiModel.addAttribute("assignCodeModel", assignCodeModel);

		// choose return view (isbnedit if no details, otherwise editbook)
		assignCodeModel.setEditMode(EditMode.editbook);
		String returnview = "barcode/editbook";
		if (book.getBookdetail().getDetailstatus().longValue() != CatalogService.DetailStatus.DETAILFOUND) {
			if (book.getBookdetail().getTitle()!=null && book.getBookdetail().getTitle().equals(CatalogService.titledefault)) {
				assignCodeModel.setTitle("");
				assignCodeModel.setEditMode(EditMode.title);
				uiModel.addAttribute("assignCodeModel",assignCodeModel);
				returnview = "barcode/titleedit";
			} else {
				assignCodeModel.setEditMode(EditMode.isbn);
				returnview = "barcode/isbnedit";	
			}
		}

		// add lookups / displays for view
		String shortname = client.getShortname();
		uiModel.addAttribute("clientname",shortname);
		putDisplayInfoInModel(uiModel, httpServletRequest, client,locale);
		// return view
		return returnview;

	}

	// persist any changes to book (new classification, addition of isbn)
	@RequestMapping(value = "/updatebook", method = RequestMethod.POST, produces = "text/html")
	public String updateBook(AssignCodeModel assignCodeModel, Model uiModel,
			HttpServletRequest httpServletRequest, BindingResult bindingResult,Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		assignValidator.validateUpdateBook(assignCodeModel,bindingResult);
		if (bindingResult.hasErrors()) {
			String returnview = "barcode/editbook";
			if (assignCodeModel.getEditMode().equals(EditMode.title)) {
					returnview = "barcode/titleedit";
				} else if (assignCodeModel.getEditMode().equals(EditMode.title)) {

					returnview = "barcode/isbnedit";	
			}

			return returnview;
		}
		
		// update book - gather classification, isbn
		Long classification=assignCodeModel.getShelfcode();
		String isbn = assignCodeModel.getIsbnentry();
		Long status = assignCodeModel.getStatus();
		String title =assignCodeModel.getTitle();
		String author = assignCodeModel.getAuthor();
		
		// put book into model
		BookModel model = catalogService.loadBookModel(assignCodeModel.getBook().getId());
		if (classification!=null) model.setShelfcode(classification);
		if (status!=null) model.setStatus(status);
		if (isbn!=null) model.setIsbn10(isbn);
		if (title!=null) model.setTitle(title);
		ArtistDao artist = catalogService.textToArtistName(author);
		if (artist!=null) {
			model.setAuthorInBook(artist);
		}
		
		// update book - if changed
		// fill in details if not detailfound, and isbn exists
		boolean fillindetails = model.hasIsbn() && !(model.getDetailstatus().longValue()==CatalogService.DetailStatus.DETAILFOUND);
		try {
			model = catalogService.updateCatalogEntryFromBookModel(clientid, model, fillindetails);
			assignCodeModel.setBook(model.getBook());
			uiModel.addAttribute("assignCodeModel",assignCodeModel);
			return "barcode/assigncode";
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		// return assign code page
		uiModel.addAttribute("assignCodeModel",assignCodeModel);
		return "barcode/assigncode";
	}

	// persist any changes to book (new classification, addition of isbn)
	@RequestMapping(value = "/assign", method = RequestMethod.POST, produces = "text/html")
	public String assignCodeToBook(AssignCodeModel assignCodeModel,
			Model uiModel, BindingResult bindingResult,HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// gather info - code, bookid
		Long bookid = assignCodeModel.getBook().getId();
		String code = assignCodeModel.getAssignedcode();

		// validation - has this barcode already been assigned??
		assignValidator.validateAssignCodeToBook(code,bindingResult);
		if (bindingResult.hasErrors()) {
			assignCodeModel.setAssignedcode(null);
			uiModel.addAttribute("assignCodeModel",assignCodeModel);
			return "barcode/assigncode";
		}
		
		// service call - assignCodeToBook
		catalogService.assignCodeToBook(code, bookid);
		// load book model
		BookModel model = catalogService.loadBookModel(bookid);
		uiModel.addAttribute("assignCodeModel",assignCodeModel);
		// return to success page
		return "barcode/assignsuccess";
	}

	private void putDisplayInfoInModel(Model uiModel,
			HttpServletRequest httpServletRequest, ClientDao client,Locale locale) {
		String lang = locale.getLanguage();
		Long clientkey = client.getId();

		HashMap<Long, ClassificationDao> shelfclasses = catalogService
				.getShelfClassHash(clientkey, lang);
		List<ClassificationDao> shelfclasslist = catalogService
				.getShelfClassList(clientkey, lang);
		
		uiModel.addAttribute("classis", shelfclasses);
		JSONSerializer serializer = new JSONSerializer();
		String json = serializer.exclude("*.class").serialize(shelfclasslist);
		uiModel.addAttribute("classJson", json);
		HashMap<Long, String> booktypedisps = keyService.getDisplayHashForKey(
				CatalogService.booktypelkup, lang);
		uiModel.addAttribute("typeLkup", booktypedisps);
		booktypedisps = keyService.getDisplayHashForKey(
				CatalogService.bookstatuslkup, lang);
		uiModel.addAttribute("statusLkup", booktypedisps);
		HashMap<String, String> langdisps = keyService
				.getStringDisplayHashForKey(CatalogService.languagelkup, lang);
		uiModel.addAttribute("langLkup", langdisps);
		booktypedisps = keyService.getDisplayHashForKey(
				CatalogService.detailstatuslkup, lang);
		uiModel.addAttribute("detailstatusLkup", booktypedisps);
		String imagebase = settingService
				.getSettingAsString("biblio.imagebase");
		uiModel.addAttribute("imagebasedir", imagebase);
	}

}
