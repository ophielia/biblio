package meg.biblio.catalog.web;

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
import meg.biblio.catalog.web.validator.BookModelValidator;
import meg.biblio.common.AppSettingService;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import flexjson.JSONSerializer;

@RequestMapping("/newbook")
@SessionAttributes("bookModel")
@Controller
public class NewBookController {

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
		Locale locale = httpServletRequest.getLocale();

		// clear model, place in uiModel
		Long classification = bookModel.getShelfclass();
		bookModel = new BookModel();
		bookModel.setShelfclass(classification);
		bookModel.setCreatenewid(new Boolean(true));
		bookModel.setStatus(CatalogService.Status.SHELVED);

		uiModel.addAttribute("bookModel", bookModel);

		String shortname = client.getShortname();
		uiModel.addAttribute("clientname",shortname);
		


		// return choosebook page
		return "newbook/create";
	}	



	
	// assign code for new (to catalog) book
	@RequestMapping(value = "/editbook", params = "newbook", method = RequestMethod.POST, produces = "text/html")
	public String createNewBook(BookModel bookModel,
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
			return "newbook/create";
		}
		
		// gatherinfo -cBookid (or generateok), isbn, title, author
		String cbooknr = bookModel.getClientbookid();
		String isbn = bookModel.getIsbnentry();
		String title = bookModel.getTitle();
		String author = bookModel.getAuthorname();
		Boolean createclientbookid = bookModel.getCreatenewid();

		// create book in catalog
		BookModel model = new BookModel();
		if (cbooknr!=null) model.setClientbookid(cbooknr);
		if (isbn!=null) model.setIsbn10(isbn);
		if (title!=null) model.setTitle(title);
		ArtistDao artist = catalogService.textToArtistName(author);
		if (artist!=null) {
			model.setAuthorInBook(artist);
		}
		model = catalogService.createCatalogEntryFromBookModel(clientid, model,
				createclientbookid);
		// book-> BookModel -> uiModel
		BookDao book = model.getBook();
		bookModel.setBook(book);
		uiModel.addAttribute("bookModel", bookModel);

		// choose return view (isbnedit if no details, otherwise editbook)
		bookModel.setEditMode(EditMode.editbook);
		String returnview = "newbook/editbook";
		if (book.getDetailstatus().longValue() != CatalogService.DetailStatus.DETAILFOUND) {
			if (book.getTitle()!=null && book.getTitle().equals(CatalogService.titledefault)) {
				bookModel.setTitle("");
				bookModel.setEditMode(EditMode.title);
				uiModel.addAttribute("bookModel",bookModel);
				returnview = "newbook/titleedit";
			} else {
				bookModel.setEditMode(EditMode.isbn);
				returnview = "newbook/isbnedit";	
			}
		}

		// add lookups / displays for view
		String shortname = client.getShortname();
		uiModel.addAttribute("clientname",shortname);
		putDisplayInfoInModel(uiModel, httpServletRequest, client);
		
		// check uses barcodes
		Boolean showbarcode = client.getUsesBarcodes()!=null && client.getUsesBarcodes();
		uiModel.addAttribute("usesbarcodes",showbarcode);	
		// return view
		return returnview;

	}

	// persist any changes to book (new classification, addition of isbn)
	@RequestMapping(value = "/updatebook", method = RequestMethod.POST, produces = "text/html")
	public String updateBook(BookModel bookModel, Model uiModel,
			HttpServletRequest httpServletRequest, BindingResult bindingResult,Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// update book - gather classification, isbn
		Long classification=bookModel.getShelfclass();
		String isbn = bookModel.getIsbnentry();
		Long status = bookModel.getStatus();
		String title =bookModel.getTitle();
		String author = bookModel.getAuthorname();
		
		// put book into model
		BookModel model = catalogService.loadBookModel(bookModel.getBook().getId());
		if (classification!=null) model.setShelfclass(classification);
		if (status!=null) model.setStatus(status);
		if (isbn!=null) model.setIsbn10(isbn);
		if (title!=null) model.setTitle(title);
		ArtistDao artist = catalogService.textToArtistName(author);
		if (artist!=null) {
			model.setAuthorInBook(artist);
		}
		// now, put this model updated book into the (session) book model
		bookModel.setBook(model.getBook());
		
		bookValidator.validateUpdateBook(bookModel,bindingResult);
		if (bindingResult.hasErrors()) {
			String returnview = "newbook/editbook";
			if (bookModel.getEditMode().equals(EditMode.title)) {
					returnview = "newbook/titleedit";
				} else if (bookModel.getEditMode().equals(EditMode.title)) {
					returnview = "newbook/isbnedit";	
			}

			return returnview;
		}
		
		// update book - if changed
		// fill in details if not detailfound, and isbn exists
		boolean fillindetails = model.getBook().hasIsbn() && !(model.getDetailstatus().longValue()==CatalogService.DetailStatus.DETAILFOUND);
		try {
			model = catalogService.updateCatalogEntryFromBookModel(clientid, model, fillindetails);
			bookModel.setBook(model.getBook());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		// return view - returns either to display book, or to assign code
		uiModel.addAttribute("bookModel",bookModel);
		Boolean showbarcode = client.getUsesBarcodes()!=null && client.getUsesBarcodes();
		if (showbarcode) {
			// return assign code page
			return "newbook/assigncode";
		} else {
			// redirect to display book page
			String redirect = "/books/display/" + bookModel.getBookid();
			return "redirect:" + redirect;
			
		}
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
			bookModel.setAssignedcode(null);
			uiModel.addAttribute("bookModel",bookModel);
			return "newbook/isbnedit";
		}
		
		// service call - assignCodeToBook
		catalogService.assignCodeToBook(code, bookid);
		// load book model
		BookModel model = catalogService.loadBookModel(bookid);
		uiModel.addAttribute("bookModel",bookModel);
		// redirect to display book page
		String redirect = "/books/display/" + bookModel.getBookid();
		return "redirect:" + redirect;
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
