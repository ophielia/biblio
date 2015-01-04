package meg.biblio.lending.web;

import java.security.Principal;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.AppSettingService;
import meg.biblio.common.BarcodeService;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.LendingService;
import meg.biblio.lending.db.PersonRepository;
import meg.biblio.lending.db.dao.LoanHistoryDao;
import meg.biblio.lending.db.dao.PersonDao;
import meg.biblio.lending.web.model.BarcodeLendModel;
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

	@RequestMapping( method = RequestMethod.GET, produces = "text/html")
	public String showMainCheckoutPage(BarcodeLendModel barcodeLendModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal) {
		barcodeLendModel.setPerson(null);
		uiModel.addAttribute("barcodeLendModel",barcodeLendModel);
		// return choosebook page
		return "barcode/maincheckout";
	}

	@RequestMapping( method = RequestMethod.POST, produces = "text/html")
	public String processBarcode(BarcodeLendModel barcodeLendModel,
			Model uiModel, BindingResult bindingErrors, HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);

		// get code
		String code = barcodeLendModel.getCode();
		// person or book
		if (isPerson(code)) {
			return setPersonInModel(code,barcodeLendModel, uiModel,bindingErrors,client);
		} else if (isBook(code)) {
			return processBook(code,barcodeLendModel, uiModel,bindingErrors,client);
		}
		barcodeLendModel.setCode(null);
		uiModel.addAttribute("barcodeLendModel",barcodeLendModel);
		bindingErrors.reject("error_codeunrecognized",null,"Code not recognized.");
		return "barcode/maincheckout";
	
	}

	private String processBook(String code, BarcodeLendModel barcodeLendModel, Model uiModel, BindingResult bindingErrors,
			ClientDao client) {
		// get book for code
		BookDao book = bookRepo.findBookByBarcode(code);
		// is checkout ? (do we have a person in model??)
		boolean ischeckout = barcodeLendModel.getPerson()!=null;
		
		// validate (book not found - ischeckout && book already checkedout - error... or !ischeckout, and book not checkedout)
		barcodeLendValidator.validateBook(book,ischeckout,bindingErrors);
		if (bindingErrors.hasErrors()) {
			barcodeLendModel.setCode(null);
			uiModel.addAttribute("barcodeLendModel",barcodeLendModel);
			barcodeLendModel.setPerson(null);
			return "barcode/maincheckout";
		}
		
		// if checkout - checkout book for user, and return checkout success page
		String firstname = barcodeLendModel.getPerson()!=null?barcodeLendModel.getPerson().getFulldisplayname():"";
		String booktitle = book.getTitle();
		String bookauthor = book.getAuthorsAsString();
		String bookimg=book.getImagelink();
		if (ischeckout) {
			// checkout book
			lendingService.checkoutBook(book.getId(), barcodeLendModel.getPerson().getId(),
					client.getId());
			

			//---- put name, book title in uiModel 
			uiModel.addAttribute("personname",firstname);
			uiModel.addAttribute("booktitle", booktitle);
			uiModel.addAttribute("bookauthor",bookauthor);
			uiModel.addAttribute("bookimagelink",bookimg);
			// delete from LendingModel
			barcodeLendModel.setPerson(null);
			barcodeLendModel.setCode(null);
			uiModel.addAttribute("barcodeLendModel",barcodeLendModel);
			// return success
			return "barcode/checkoutsuccess";
		} else {
			// if return - return book and return success page
			// return book
			LoanHistoryDao lh = lendingService.returnBookByBookid(book.getId(), client.getId());
			
			firstname = lh.getBorrower().getFirstname();
			//---- put name, book title in uiModel 
			uiModel.addAttribute("personname",firstname);
			uiModel.addAttribute("booktitle", booktitle);
			uiModel.addAttribute("bookauthor",bookauthor);
			uiModel.addAttribute("bookimagelink",bookimg);
			// delete from LendingModel
			barcodeLendModel.setPerson(null);
			barcodeLendModel.setCode(null);
			uiModel.addAttribute("barcodeLendModel",barcodeLendModel);
			// return success
			return "barcode/returnsuccess";
		}
	}

	private String setPersonInModel(String code, BarcodeLendModel barcodeLendModel, Model uiModel, BindingResult bindingErrors, ClientDao client) {
		// get person for code
		PersonDao person = personRepo.findPersonByBarcode(code);
		
		// validate (found??person already reached limit of checkout books?
		barcodeLendValidator.validatePerson(person, client, bindingErrors);
		if (bindingErrors.hasErrors()) {
			barcodeLendModel.setCode(null);
			uiModel.addAttribute("barcodeLendModel",barcodeLendModel);
			return "barcode/maincheckout";
		}
		// put person in model
		barcodeLendModel.setPerson(person);
		barcodeLendModel.setCode(null);
		uiModel.addAttribute("barcodeLendModel",barcodeLendModel);
		// return person page
		return "barcode/personcheckout";
	}

	@RequestMapping(value = "/verify", method = RequestMethod.GET, produces = "text/html")
	public String showVerifyPage(BarcodeLendModel barcodeLendModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();
		Locale locale = httpServletRequest.getLocale();
		String lang = locale.getLanguage();
		return null;
	}
	
	@RequestMapping(value = "/verify", method = RequestMethod.POST, produces = "text/html")
	public String verifyCode(BarcodeLendModel barcodeLendModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();
		Locale locale = httpServletRequest.getLocale();
		String lang = locale.getLanguage();
		return null;
	}
	
	private boolean isBook(String code) {
		if (code!=null && code.startsWith(BarcodeService.CodeType.BOOK)) {
			return true;
		}
		return false;
	}
	
	private boolean isPerson(String code) {
		if (code!=null && code.startsWith(BarcodeService.CodeType.PERSON)) {
			return true;
		}
		return false;
	}
	
	
	
	
}
