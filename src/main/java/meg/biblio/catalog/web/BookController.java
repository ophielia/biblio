package meg.biblio.catalog.web;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import meg.biblio.catalog.BookMemberService;
import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.DetailSearchService;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.ClassificationDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.db.dao.SubjectDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.catalog.web.validator.BookModelValidator;
import meg.biblio.common.AppSettingService;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.ClassManagementService;
import meg.biblio.lending.LendingService;
import meg.biblio.lending.db.dao.LoanRecordDisplay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import flexjson.JSONSerializer;

@RequestMapping("/books")
@SessionAttributes("bookModel")
@Controller
public class BookController {

	@Autowired
	BookMemberService bMemberService;

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
	LendingService lendingService;	

	@Autowired
	BookModelValidator bookValidator;

	@RequestMapping(params = "form", produces = "text/html")
	public String addBookForm(Model uiModel,
			@RequestParam(value = "shelfcode", required = false) Long shelfcode,
			@RequestParam(value = "booktype", required = false) Long booktype,
			@RequestParam(value = "createnewid", required = false) Boolean newid) {
		if (newid==null) {
			newid = new Boolean(true);
		}
		
		BookModel bookModel = new BookModel();
		bookModel.setCreatenewid(newid);
		bookModel.setStatus(CatalogService.Status.SHELVED);
		bookModel.setAssignedcode(null);
		
		if (shelfcode!=null) {
			bookModel.setPreviousShelfcode(shelfcode);
		}
		if (booktype!=null) {
			bookModel.setPreviousBookType(booktype);
		}		
		
		uiModel.addAttribute("bookModel", bookModel);

		return "book/create";
	}

	@RequestMapping(value = "/findinfo", method = RequestMethod.POST, produces = "text/html")
	public String addBookFindInfo(BookModel bookModel, Model uiModel,
			BindingResult bindingResult, HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {
		return findInfo(bookModel,uiModel,bindingResult,httpServletRequest,principal,locale, true);
	}

	@RequestMapping(params = "searchagain", method = RequestMethod.POST, produces = "text/html")
	public String searchAgain(BookModel bookModel, Model uiModel,
			BindingResult bindingResult, HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {

		// deal with authors, illustrators, and subjects
		List<String> authors = parseEntryIntoStringlist(bookModel
				.getAuthorentry());
		List<String> illustrators = parseEntryIntoStringlist(bookModel
				.getIllustratorentry());
		List<String> subjects = parseEntryIntoStringlist(bookModel
				.getSubjectentry());
		String pubname = bookModel.getPublishername();

		List<ArtistDao> authorlist = bMemberService
				.stringListToArtists(authors);
		List<ArtistDao> illustratorlist = bMemberService
				.stringListToArtists(illustrators);
		List<SubjectDao> subjectlist = bMemberService
				.stringListToSubjects(subjects);
		PublisherDao publisher = bMemberService.findPublisherForName(pubname);

		bookModel.setAuthors(authorlist);
		bookModel.setIllustrators(illustratorlist);
		bookModel.setSubjects(subjectlist);
		bookModel.getBook().getBookdetail().setPublisher(publisher);
		
		
		return findInfo(bookModel,uiModel,bindingResult,httpServletRequest,principal,locale,false);
	}

	private String findInfo(BookModel bookModel, Model uiModel,
			BindingResult bindingResult, HttpServletRequest httpServletRequest,
			Principal principal, Locale locale, boolean firstsearch) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// fill lookups
		fillLookups(uiModel, httpServletRequest, principal, locale);
		String shortname = client.getShortname();
		uiModel.addAttribute("clientname", shortname);

		// validation - check isbn - OR - title
		// if not generate new - no existing book for book code
		bookValidator.validateNewBookEntry(bookModel, bindingResult, client);
		if (bindingResult.hasErrors()) {
			if (firstsearch) {
				FieldError bookexists =bindingResult.getFieldError("clientbookid");
				if (bookexists!=null) {
					// find the book id, make a link, and put into model
					BookDao existing = catalogService.findBookByClientBookId(bookModel.getClientbookid(), client);
					if (existing !=null) {
						Long existid = existing.getId();
						String link = "/books/display/" + existid;
						uiModel.addAttribute("numbertaken",true);
						uiModel.addAttribute("displaylink",link);
					}
				}
				return "book/create";
			} else {
				// return to editbook page
				uiModel.addAttribute("searchagain", true);
				return "book/editbook";
				
			}
		}

		// get author and status
		String author = bookModel.getAuthorname();
		String publishername = bookModel.getPublishername();
		Long status = client.getDefaultStatus();

		// find information for book
		bookModel.setClientid(clientid);
		ArtistDao artist = bMemberService.textToArtistName(author);
		if (artist != null) {
			bookModel.setAuthorInBook(artist);
		}
		PublisherDao publisher = bMemberService.findPublisherForName(publishername);
		if (publisher!=null) {
			bookModel.getBook().getBookdetail().setPublisher(publisher);
		}
		if (status != null) {
			bookModel.setStatus(status);
		} else {
			bookModel.setStatus(CatalogService.Status.PROCESSING);
		}

		// want to find the details for this book ,but not save it yet...
		bookModel.setTrackchange(false);
		BookModel returnModel = detSearchService.fillInDetailsForBook(bookModel, client);

		// book-> BookModel -> uiModel
		//BookDao book = bookModel.getBook();
		//bookModel.setBook(book);
		returnModel.setTrackchange(true);
		uiModel.addAttribute("bookModel", returnModel);

		// fill lookups
		// return editbook view (unless multiresults)
		String returnview = "book/editbook";
		// determine if another search should be made or not
		long detstatus = returnModel.getBook().getBookdetail().getDetailstatus().longValue();
		if (detstatus == CatalogService.DetailStatus.DETAILNOTFOUNDWISBN) {
			// should search again - steer this by setting attribute
			uiModel.addAttribute("searchagain", true);
			bookModel.setTrackchange(false);
		} else if (detstatus == CatalogService.DetailStatus.MULTIDETAILSFOUND) {
			// add found objects to model
			List<FoundDetailsDao> multidetails = bookModel.getFounddetails();
			uiModel.addAttribute("noisbn", !bookModel.getBook().getBookdetail().hasIsbn());
			returnview = "book/choosedetails";
		} else {
			// add previous shelf code if desired
			if (bookModel.getPreviousShelfcode()!=null) {
				bookModel.setShelfcode(bookModel.getPreviousShelfcode());
			}
			if (bookModel.getPreviousBookType()!=null) {
				bookModel.setType(bookModel.getPreviousBookType());
			}			
		}

		// add addbookcontext as true to model
		uiModel.addAttribute("addbookcontext", true);

		// return view
		return returnview;

	}

	@RequestMapping(params = "saveandadd", method = RequestMethod.POST, produces = "text/html")
	public String addBookCreateAndAdd(
			@RequestParam(value = "saveandadd", required = false) String addanother,
			BookModel bookModel, Model uiModel,
			HttpServletRequest httpServletRequest, BindingResult bindingResult,
			Principal principal, Locale locale) {

		return createBook(bookModel, uiModel, httpServletRequest,
				bindingResult, principal, locale, true);
	}


	@RequestMapping(value = "/createedit",method = RequestMethod.GET, produces = "text/html")
	public String notAmongFoundDetails(
			BookModel bookModel, Model uiModel,
			HttpServletRequest httpServletRequest, BindingResult bindingResult,
			Principal principal, Locale locale) {
		// multi-details found shouldn't be saved to db.  
		// manually change status to "no details found"
		bookModel.setDetailstatus(CatalogService.DetailStatus.NODETAIL);
		return "book/editbook";
	}

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
	public String addBookCreate(
			@RequestParam(value = "saveandadd", required = false) String addanother,
			BookModel bookModel, Model uiModel,
			HttpServletRequest httpServletRequest, BindingResult bindingResult,
			Principal principal, Locale locale) {

		return createBook(bookModel, uiModel, httpServletRequest,
				bindingResult, principal, locale, false);
	}

	public String createBook(BookModel bookModel, Model uiModel,
			HttpServletRequest httpServletRequest, BindingResult bindingResult,
			Principal principal, Locale locale, Boolean returntoadd) {

		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();
		String shortname = client.getShortname();
		Long detailstatus = bookModel.getDetailstatus();
		Boolean createnewid = bookModel.getCreatenewid();
		

		
		bookValidator.validateUpdateBook(bookModel, bindingResult);
		if (bindingResult.hasErrors()) {
			// fill lookups
			fillLookups(uiModel, httpServletRequest, principal, locale);
			uiModel.addAttribute("clientname", shortname);
			return "book/editbook";
		}
		if (detailstatus == CatalogService.DetailStatus.DETAILNOTFOUNDWISBN) {
			uiModel.addAttribute("searchagain", "true");
			uiModel.addAttribute("clientname", shortname);
			return "book/editbook";
		}

		// deal with authors, illustrators, and subjects
		List<String> authors = parseEntryIntoStringlist(bookModel
				.getAuthorentry());
		List<String> illustrators = parseEntryIntoStringlist(bookModel
				.getIllustratorentry());
		List<String> subjects = parseEntryIntoStringlist(bookModel
				.getSubjectentry());

		List<ArtistDao> authorlist = bMemberService
				.stringListToArtists(authors);
		List<ArtistDao> illustratorlist = bMemberService
				.stringListToArtists(illustrators);
		List<SubjectDao> subjectlist = bMemberService
				.stringListToSubjects(subjects);

		bookModel.setAuthors(authorlist);
		bookModel.setIllustrators(illustratorlist);
		bookModel.setSubjects(subjectlist);

		// update book - if changed
		try {
			bookModel.setTrackchange(false);
			bookModel = catalogService.createCatalogEntryFromBookModel(
					clientid, bookModel);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// return target
		if (!returntoadd) {
			uiModel.addAttribute("bookModel", bookModel);
			return "redirect:/books/display/"
					+ bookModel.getBookid().toString();
		} else {
			// get current shelf and generate code info (createnewid)
			Long shelfcode = bookModel.getShelfcode();
			Long booktype = bookModel.getType();
			String returnparams = "?form&createnewid=" + createnewid;
			if (shelfcode!=null) {
				returnparams += "&shelfcode=" + shelfcode;
			}
			if (booktype!=null) {
				returnparams += "&booktype=" + booktype;
			}		
			
			return "redirect:/books" + returnparams;
		}

	}

	@RequestMapping(value = "/choosedetails", params = { "detailidx" }, method = RequestMethod.POST, produces = "text/html")
	public String assignBookDetails(@RequestParam("detailidx") Long detailidx,
			BookModel bookModel, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal,Locale locale) {

		ClientDao client = clientService.getCurrentClient(principal);
		List<FoundDetailsDao> fdetails = bookModel.getFounddetails();
		if (fdetails!=null && fdetails.size()>detailidx) {
FoundDetailsDao fd = fdetails.get(detailidx.intValue());
			// call catalog service
		try {
			bookModel = detSearchService.assignDetailToBook(bookModel, fd,client);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// show book
		uiModel.addAttribute("bookModel", bookModel);
		// fill lookups
		fillLookups(uiModel, httpServletRequest, principal, locale);
		String shortname = client.getShortname();
		uiModel.addAttribute("clientname", shortname);

		return "book/editbook";
		}
		return "book/choosedetails";
	}


	@RequestMapping(value = "/update/{id}", produces = "text/html")
	public String editBook(@PathVariable("id") Long id, Model uiModel,
			HttpServletRequest httpServletRequest,@RequestParam(value = "from", required = false) String frommarker, Principal principal,
			Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);

		// fill lookups
		fillLookups(uiModel, httpServletRequest, principal, locale);
		String shortname = client.getShortname();
		uiModel.addAttribute("clientname", shortname);

		// lookup book
		BookModel bookModel = null;
		if (id != null) {
			bookModel = catalogService.loadBookModel(id);
		} else {
			bookModel = new BookModel();
		}
		bookModel.setTrackchange(true);
		uiModel.addAttribute("bookModel", bookModel);
		if (frommarker!=null) {
			uiModel.addAttribute("from",frommarker);
		}
		return "book/editbook";
	}

	@RequestMapping(value = "/update/{id}", method = RequestMethod.POST, produces = "text/html")
	public String updateBook(@PathVariable("id") Long id, BookModel bookModel,
			BindingResult bindingResult, Model uiModel,@RequestParam(value = "from", required = false) String frommarker,
			HttpServletRequest httpServletRequest, Principal principal,
			Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		bookValidator.validateUpdateBook(bookModel, bindingResult);
		if (bindingResult.hasErrors()) {
			// fill lookups
			fillLookups(uiModel, httpServletRequest, principal, locale);
			String shortname = client.getShortname();
			uiModel.addAttribute("clientname", shortname);
			return "book/editbook";
		}

		// deal with authors, illustrators, and subjects
		List<String> authors = parseEntryIntoStringlist(bookModel
				.getAuthorentry());
		List<String> illustrators = parseEntryIntoStringlist(bookModel
				.getIllustratorentry());
		List<String> subjects = parseEntryIntoStringlist(bookModel
				.getSubjectentry());

		List<ArtistDao> authorlist = bMemberService
				.stringListToArtists(authors);
		List<ArtistDao> illustratorlist = bMemberService
				.stringListToArtists(illustrators);
		List<SubjectDao> subjectlist = bMemberService
				.stringListToSubjects(subjects);

		bookModel.setAuthors(authorlist);
		bookModel.setIllustrators(illustratorlist);
		bookModel.setSubjects(subjectlist);

		// update book - if changed
		try {
			bookModel.setTrackchange(false);
			bookModel = catalogService.updateCatalogEntryFromBookModel(
					clientid, bookModel, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// return view - returns either to display book, or to assign code
		uiModel.addAttribute("bookModel", bookModel);

		if (frommarker!=null && frommarker.equals("inv") ) {
			 uiModel.asMap().clear();
			 return "redirect:/inventory/count";
		}
		
		// return target
		return "redirect:/books/display/" + bookModel.getBookid().toString();

	}

	@RequestMapping(value = "/display/{id}", produces = "text/html")
	public String displayBook(@PathVariable("id") Long id, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal,
			Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);
		fillLookups(uiModel, httpServletRequest, principal, locale);
		String shortname = client.getShortname();
		uiModel.addAttribute("clientname", shortname);

		BookModel bmodel = catalogService.loadBookModel(id);
		// get checkout count
		Integer count = lendingService.getCheckoutCountForBook(id, client.getId());
		bmodel.setCheckoutcount(count);
		
		uiModel.addAttribute("bookModel", bmodel);
		return "book/show";
	}
	
	@RequestMapping(value = "/displayhistory/{id}", produces = "text/html")
	public String displayBookLendingHistory(@PathVariable("id") Long id, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal,
			Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);
		fillLookups(uiModel, httpServletRequest, principal, locale);
		String shortname = client.getShortname();
		uiModel.addAttribute("clientname", shortname);

		BookModel bmodel = catalogService.loadBookModel(id);
		// get checkout history
		List<LoanRecordDisplay> history = lendingService.getLendingHistoryForBook(id, client.getId());
		// set checkout history in model
		bmodel.setLendingHistory(history);
		// get section lookup for display
		HashMap<Long,String> sectionsel = getSectionSelect( httpServletRequest, locale);
		uiModel.addAttribute("sectionsel", sectionsel);

		uiModel.addAttribute("bookModel", bmodel);
		return "book/showhistory";
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

	private List<String> parseEntryIntoStringlist(String authorentry) {
		if (authorentry != null && authorentry.trim().length() > 0) {
			String[] splitresult = authorentry.split(";");
			List<String> result = new ArrayList<String>();
			for (int i = 0; i < splitresult.length; i++) {
				String value = splitresult[i];
				if (value.trim().length() > 0) {
					result.add(value);
				}
			}

			return result;
		}
		return null;
	}


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
	
    private HashMap<Long,String> getSectionSelect(HttpServletRequest httpServletRequest,Locale locale) {
    	String lang = locale.getLanguage();

    	HashMap<Long, String> booktypedisps = keyService
    			.getDisplayHashForKey(ClassManagementService.sectionSelect, lang);
    	return booktypedisps;
    }



}
