package meg.biblio.lending.web;

import java.security.Principal;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.AppSettingService;
import meg.biblio.common.BarcodeService;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.ClassManagementService;
import meg.biblio.lending.LendingService;
import meg.biblio.lending.db.PersonRepository;
import meg.biblio.lending.db.dao.LoanHistoryDao;
import meg.biblio.lending.db.dao.PersonDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.db.dao.TeacherDao;
import meg.biblio.lending.web.model.BarcodeLendModel;
import meg.biblio.lending.web.model.LoanRecordDisplay;
import meg.biblio.lending.web.validator.BarcodeLendValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

@RequestMapping("/barcode/checkout")
@SessionAttributes("barcodeLendModel")
@Controller
public class BarcodeLendingController {

	@Autowired
	ClientService clientService;

	@Autowired
	SelectKeyService selectService;	
	
	@Autowired
	BookRepository bookRepo;

	@Autowired
	PersonRepository personRepo;

	@Autowired
	LendingService lendingService;

	@Autowired
	SelectKeyService keyService;

	@Autowired
	AppSettingService settingService;

	@Autowired
	BarcodeLendValidator barcodeLendValidator;

	@RequestMapping(method = RequestMethod.GET, produces = "text/html")
	public String showMainCheckoutPage(BarcodeLendModel barcodeLendModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal) {

		// return choosebook page
		return "barcode/maincheckout";
	}

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
	public String processBarcode(BarcodeLendModel barcodeLendModel,
			Model uiModel, BindingResult bindingErrors,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);

		// get code
		String code = barcodeLendModel.getCode();
		// person or book
		if (isPerson(code)) {
			return setPersonInModel(code, barcodeLendModel, uiModel,
					bindingErrors, client);
		} else if (isBook(code)) {
			return processBook(code, barcodeLendModel, uiModel, bindingErrors,
					client);
		}
		barcodeLendModel.setCode(null);
		uiModel.addAttribute("barcodeLendModel", barcodeLendModel);
		bindingErrors.reject("error_codeunrecognized", null,
				"Code not recognized.");
		return "barcode/maincheckout";

	}

	@RequestMapping(value = "clearuser", method = RequestMethod.GET, produces = "text/html")
	public String clearUser(BarcodeLendModel barcodeLendModel, Model uiModel,
			BindingResult bindingErrors, HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);

		clearUser(barcodeLendModel, uiModel);
		return "redirect:/barcode/checkout";

	}

	private String processBook(String code, BarcodeLendModel barcodeLendModel,
			Model uiModel, BindingResult bindingErrors, ClientDao client) {
		// get book for code
		BookDao book = bookRepo.findBookByBarcode(code);

		if (book != null) {

			// is checkout ? (do we have a person in model??)
			boolean ischeckout = barcodeLendModel.getPerson() != null;

			// validate (book not found - ischeckout && book already checkedout
			// -
			// error... or !ischeckout, and book not checkedout)
			barcodeLendValidator.validateBook(book, ischeckout, bindingErrors);
			if (bindingErrors.hasErrors()) {
				barcodeLendModel.setCode(null);
				if (barcodeLendModel.getMulticheckout()) {
					return "barcode/multicheckout";
				} else {
					clearUser(barcodeLendModel, uiModel);
					return "barcode/maincheckout";
					
				}
			}

			// if checkout - checkout book for user, and return checkout success
			// page
			String firstname = barcodeLendModel.getPerson() != null ? barcodeLendModel
					.getPerson().getFulldisplayname() : "";
			String booktitle = book.getTitle();
			String bookauthor = book.getAuthorsAsString();
			String bookimg = book.getImagelink();
			if (ischeckout) {
				Long borrowerid = barcodeLendModel.getPerson().getId();
				// checkout book
				lendingService.checkoutBook(book.getId(), borrowerid,
						client.getId());

				// ---- put name, book title in uiModel
				uiModel.addAttribute("personname", firstname);
				uiModel.addAttribute("booktitle", booktitle);
				uiModel.addAttribute("bookauthor", bookauthor);
				uiModel.addAttribute("bookimagelink", bookimg);
				// delete from LendingModel
				barcodeLendModel.setCode(null);

				// determine multi-checkout
				List<LoanRecordDisplay> checkedoutforuser = lendingService
						.getCheckedOutBooksForUser(borrowerid, client.getId());
				int borrowerlimit = lendingService.getLendLimitForBorrower(
						borrowerid, client.getId());
				barcodeLendModel.setCheckedoutForUser(checkedoutforuser);
				
				if (checkedoutforuser != null
						&& checkedoutforuser.size() < borrowerlimit) {
					barcodeLendModel.setMulticheckout(true);
					uiModel.addAttribute("barcodeLendModel", barcodeLendModel);
					
					return "barcode/multicheckout";
				} else {
					clearUser(barcodeLendModel, uiModel);
					// return success
					return "barcode/checkoutsuccess";
				}

			} else {
				// if return - return book and return success page
				// return book
				LoanHistoryDao lh = lendingService.returnBookByBookid(
						book.getId(), client.getId());

				firstname = lh.getBorrower().getFirstname();
				// ---- put name, book title in uiModel
				uiModel.addAttribute("personname", firstname);
				uiModel.addAttribute("booktitle", booktitle);
				uiModel.addAttribute("bookauthor", bookauthor);
				uiModel.addAttribute("bookimagelink", bookimg);
				// delete from LendingModel
				barcodeLendModel.setCode(null);
				clearUser(barcodeLendModel, uiModel);
				// return success
				return "barcode/returnsuccess";
			}
		} else {
			if (bindingErrors.hasErrors()) {
				barcodeLendModel.setCode(null);
				if (barcodeLendModel.getMulticheckout()) {
					return "barcode/multicheckout";
				} else {
					clearUser(barcodeLendModel, uiModel);
					return "barcode/maincheckout";
					
				}
			}
			
			
			// book not found
			barcodeLendModel.setCode(null);
			uiModel.addAttribute("barcodeLendModel", barcodeLendModel);
			bindingErrors.reject("error_nobookforcode");
			if (barcodeLendModel.getMulticheckout()) {
				return "barcode/multicheckout";
			} else {
				return "barcode/maincheckout";
			}
		}
	}

	private String setPersonInModel(String code,
			BarcodeLendModel barcodeLendModel, Model uiModel,
			BindingResult bindingErrors, ClientDao client) {
		// get person for code
		PersonDao person = personRepo.findPersonByBarcode(code);
		if (person != null) {
			// ending checkout?
			if (barcodeLendModel.matchesPerson(person)) {
				barcodeLendModel.setCode(null);
				clearUser(barcodeLendModel,uiModel);
				return "barcode/maincheckout";
			}
			
			// validate (found??person already reached limit of checkout books?
			barcodeLendValidator.validatePerson(person, client, bindingErrors);
			if (bindingErrors.hasErrors()) {
				barcodeLendModel.setCode(null);
				uiModel.addAttribute("barcodeLendModel", barcodeLendModel);
				return "barcode/maincheckout";
			}
			// put person in model
			barcodeLendModel.setPerson(person);
			barcodeLendModel.setCode(null);
			uiModel.addAttribute("barcodeLendModel", barcodeLendModel);
			// return person page
			return "barcode/personcheckout";
		} else {
			// person not found
			bindingErrors.reject("error_nopersonforcode");
			barcodeLendModel.setCode(null);
			uiModel.addAttribute("barcodeLendModel", barcodeLendModel);
			return "barcode/maincheckout";
		}

	}

	@RequestMapping(value = "/verify", method = RequestMethod.GET, produces = "text/html")
	public String showVerifyPage(BarcodeLendModel barcodeLendModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal) {

				return "barcode/verify";
	}

	@RequestMapping(value = "/verify", method = RequestMethod.POST, produces = "text/html")
	public String verifyCode(BarcodeLendModel barcodeLendModel, Model uiModel,BindingResult bindingErrors,
			HttpServletRequest httpServletRequest, Principal principal,Locale locale) {
		String lang = locale.getLanguage();
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();
		// get code
				String code = barcodeLendModel.getCode();
				// person or book
				if (isPerson(code)) {
					// get person for code
					PersonDao person = personRepo.findPersonByBarcode(code);
					// put person firstname, lastname, section and/or teacher in model
					if (person!=null) {
						String name = person.getFulldisplayname();
						Boolean isteacher = (person instanceof TeacherDao);
						String sectiondisp = "";
						if (!isteacher) {
							StudentDao student = (StudentDao) person;
							String sectionkey = String.valueOf(student.getSectionkey());
							sectiondisp = selectService.getDisplayForKeyValue(ClassManagementService.sectionLkup,sectionkey,lang);
						}
						uiModel.addAttribute("personname",name);
						uiModel.addAttribute("isteacher",isteacher);
						uiModel.addAttribute("section",sectiondisp);
						uiModel.addAttribute("isperson",new Boolean(true));
					}else {
						// person not found
						bindingErrors.reject("error_nopersonforcode");

					}
					barcodeLendModel.setCode(null);
					uiModel.addAttribute("barcodeLendModel", barcodeLendModel);
					return "barcode/verify";
				} else if (isBook(code)) {
					// get book
					BookDao book = bookRepo.findBookByBarcode(code);
					
					// put book title and author and image (if available) in model
					if (book!=null) {
						// put book title and author and image (if available) in model
						String title = book.getTitle();
						String author = book.getAuthorsAsString();
						Boolean noauthor = author.trim().length()>0;
						String imagelink = book.getImagelink();
						uiModel.addAttribute("title",title);
						uiModel.addAttribute("author",author);
						uiModel.addAttribute("imagelink",imagelink);
						uiModel.addAttribute("isbook",new Boolean(true));
						uiModel.addAttribute("noauthor",noauthor);
					} else {
						bindingErrors.reject("error_nobookforcode");

					}
					barcodeLendModel.setCode(null);
					uiModel.addAttribute("barcodeLendModel", barcodeLendModel);
					return "barcode/verify";
				}
				barcodeLendModel.setCode(null);
				uiModel.addAttribute("barcodeLendModel", barcodeLendModel);
				bindingErrors.reject("error_codeunrecognized", null,
						"Code not recognized.");
				return "barcode/verify";
	}

	private boolean isBook(String code) {
		if (code != null && code.startsWith(BarcodeService.CodeType.BOOK)) {
			return true;
		}
		return false;
	}

	private boolean isPerson(String code) {
		if (code != null && code.startsWith(BarcodeService.CodeType.PERSON)) {
			return true;
		}
		return false;
	}

	private void clearUser(BarcodeLendModel model, Model uiModel) {
		model.setPerson(null);
		model.setCheckedoutForUser(null);
		uiModel.addAttribute("barcodeLendModel", model);
	}

}
