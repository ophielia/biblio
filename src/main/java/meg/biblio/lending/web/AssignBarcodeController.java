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

	@RequestMapping(value = "/enterbook", method = RequestMethod.GET, produces = "text/html")
	public String showChooseBookPage(AssignCodeModel assignCodeModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();
		Locale locale = httpServletRequest.getLocale();
		String lang = locale.getLanguage();

		// clear model, place in uiModel
		Long classification = assignCodeModel.getShelfclass();
		assignCodeModel = new AssignCodeModel();
		assignCodeModel.setShelfclass(classification);
		assignCodeModel.setCreatenewid(true);
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
		Long clientid = client.getId();
		Locale locale = httpServletRequest.getLocale();
		String lang = locale.getLanguage();

		// clear model, place in uiModel
		Long classification = assignCodeModel.getShelfclass();
		assignCodeModel = new AssignCodeModel();
		assignCodeModel.setShelfclass(classification);
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
			Principal principal) {
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
		String returnview = "barcode/editbook";
		if (book.getDetailstatus().longValue() != CatalogService.DetailStatus.DETAILFOUND) {
			returnview = "barcode/isbnedit";
		}

		// add lookups / displays for view
		putDisplayInfoInModel(uiModel, httpServletRequest, client);
		// return view
		return returnview;
	}

	// assign code for new (to catalog) book
	@RequestMapping(value = "/editbook", params = "newbook", method = RequestMethod.POST, produces = "text/html")
	public String createNewBook(AssignCodeModel assignCodeModel,
			Model uiModel, BindingResult bindingResult,HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// validation - check isbn - OR - title 
		// if not generate new - no existing book for book code
		assignValidator.validateNewBookEntry(assignCodeModel, bindingResult,client);
		if (bindingResult.hasErrors()) {
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
		model.setClientbookid(cbooknr);
		model.setIsbn10(isbn);
		model.setTitle(title);
		ArtistDao artist = catalogService.textToArtistName(author);
		if (artist!=null) {
			model.setAuthorInBook(artist);
		}
		model = catalogService.createCatalogEntryFromBookModel(clientid, model,
				createclientbookid);
		// book-> AssignCodeModel -> uiModel
		BookDao book = model.getBook();
		assignCodeModel.setBook(book);
		uiModel.addAttribute("assignCodeModel", assignCodeModel);

		// choose return view (isbnedit if no details, otherwise editbook)
		String returnview = "barcode/editbook";
		if (book.getDetailstatus().longValue() != CatalogService.DetailStatus.DETAILFOUND) {
			returnview = "barcode/isbnedit";
		}

		// add lookups / displays for view
		putDisplayInfoInModel(uiModel, httpServletRequest, client);
		// return view
		return returnview;

	}

	// persist any changes to book (new classification, addition of isbn)
	@RequestMapping(value = "/updatebook", method = RequestMethod.POST, produces = "text/html")
	public String updateBook(AssignCodeModel assignCodeModel, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// update book - gather classification, isbn
		Long classification=assignCodeModel.getShelfclass();
		String isbn = assignCodeModel.getIsbnentry();
		Long status = assignCodeModel.getStatus();

		// put book into model
		BookModel model = catalogService.loadBookModel(assignCodeModel.getBook().getId());
		if (isbn!=null) model.setIsbn10(isbn);
		if (classification!=null) model.setShelfclass(classification);
		if (status!=null) model.setStatus(status);
		
		// update book - if changed
		// fill in details if not detailfound, and isbn exists
		boolean fillindetails = model.getBook().hasIsbn() && !(model.getDetailstatus().longValue()==CatalogService.DetailStatus.DETAILFOUND);
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
			HttpServletRequest httpServletRequest, ClientDao client) {
		Locale locale = httpServletRequest.getLocale();
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