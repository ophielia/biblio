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
	
    @Autowired
    AppSettingService settingService;	

	@RequestMapping(params = "form", method = RequestMethod.GET, produces = "text/html")
	public String createBookEntryForm(Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {

		// create empty book model
		BookModel model = new BookModel();
		// place in uiModel
		uiModel.addAttribute("bookModel", model);
		// return book/create
		return "book/create";

	}

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
	public String createBookEntry(BookModel model, Model uiModel,
			BindingResult bindingResult, HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
		bookValidator.validateSimpleEntry(model, bindingResult);

		if (bindingResult.hasErrors()) {
			uiModel.addAttribute("bookModel", model);
			return "book/create";
		}
		uiModel.asMap().clear();

		// process book entries (author, illustrator)
		ArtistDao author = catalogService.textToArtistName(model
				.getAuthorname());
		ArtistDao illustrator = catalogService.textToArtistName(model
				.getIllustratorname());
		String publisher = model.getPublishername();
		model.addAuthorToBook(author);
		model.addIllustratorToBook(illustrator);
		model.setPublisher(publisher);

		// add book to catalog
		BookModel book = catalogService.createCatalogEntryFromBookModel(
				clientkey, model);
		Long bookid = book.getBookid();

		uiModel.addAttribute("bookModel", book);

		// go to show single page, unless multi details were found
		if (book.getDetailstatus().longValue() == CatalogServiceImpl.DetailStatus.MULTIDETAILSFOUND) {
			// go to pickdetails page
			return "redirect:/books/choosedetails/" + bookid;
		} else if (book.getDetailstatus().longValue() == CatalogServiceImpl.DetailStatus.DETAILNOTFOUND) {
			// go to pickdetails page
			return "redirect:/books/editfind/" + bookid;
		}
		return "redirect:/books/display/" + bookid;
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

	@RequestMapping(value = "/edit/{id}", method = RequestMethod.GET, produces = "text/html")
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
			model.setShelfclass(bookModel.getShelfclass());
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

	@RequestMapping(value = "/edit/{id}", method = RequestMethod.POST, produces = "text/html")
	public String saveEditBook(
			@ModelAttribute("bookModel") BookModel bookModel,
			@PathVariable("id") Long id, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();

		// only making a few changes. load the model from the database, and copy
		// changes into database model (from passed model)
		if (id != null) {
			BookModel model = catalogService.loadBookModel(id);
			model.setType(bookModel.getType());
			model.setShelfclass(bookModel.getShelfclass());
			model.setStatus(bookModel.getStatus());
			model.setLanguage(bookModel.getLanguage());
			BookModel book;
			try {
				book = catalogService.updateCatalogEntryFromBookModel(
						clientkey, model, false);
				uiModel.addAttribute("bookModel", book);
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		uiModel.addAttribute("bookModel", bookModel);
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
			catalogService.assignDetailToBook(detailid, bookid);
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
			dbmodel = catalogService.addToFoundDetails(clientkey, dbmodel);
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
			HttpServletRequest httpServletRequest, Principal principal) {
		Locale locale = httpServletRequest.getLocale();
		String lang = locale.getLanguage();
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();

		HashMap<Long, ClassificationDao> shelfclasses = catalogService
				.getShelfClassHash(clientkey, lang);

		return shelfclasses;
	}

	@ModelAttribute("classJson")
	public String getClassificationInfoAsJson(
			HttpServletRequest httpServletRequest, Principal principal) {
		Locale locale = httpServletRequest.getLocale();
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
			HttpServletRequest httpServletRequest) {
		Locale locale = httpServletRequest.getLocale();
		String lang = locale.getLanguage();

		HashMap<Long, String> booktypedisps = keyService.getDisplayHashForKey(
				CatalogService.booktypelkup, lang);
		return booktypedisps;
	}

	@ModelAttribute("statusLkup")
	public HashMap<Long, String> getStatusLkup(
			HttpServletRequest httpServletRequest) {
		Locale locale = httpServletRequest.getLocale();
		String lang = locale.getLanguage();

		HashMap<Long, String> booktypedisps = keyService.getDisplayHashForKey(
				CatalogService.bookstatuslkup, lang);
		return booktypedisps;
	}

	@ModelAttribute("langLkup")
	public HashMap<String, String> getLanguageLkup(
			HttpServletRequest httpServletRequest) {
		Locale locale = httpServletRequest.getLocale();
		String lang = locale.getLanguage();

		HashMap<String, String> langdisps = keyService
				.getStringDisplayHashForKey(CatalogService.languagelkup, lang);
		return langdisps;
	}

	@ModelAttribute("detailstatusLkup")
	public HashMap<Long, String> getDetailStatusLkup(
			HttpServletRequest httpServletRequest) {
		Locale locale = httpServletRequest.getLocale();
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
