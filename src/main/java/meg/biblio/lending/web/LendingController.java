package meg.biblio.lending.web;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.report.ClassSummaryReport;
import meg.biblio.common.report.DailySummaryReport;
import meg.biblio.lending.ClassManagementService;
import meg.biblio.lending.LendingService;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.web.model.LendingModel;
import meg.biblio.lending.web.model.LoanRecordDisplay;
import meg.biblio.lending.web.model.TeacherInfo;
import meg.biblio.lending.web.validator.LendingModelValidator;

import org.apache.fop.apps.FOPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

@RequestMapping("/lending")
@SessionAttributes("lendingModel")
@Controller
public class LendingController {

	@Autowired 
	private ServletContext servletContext;
	
	@Autowired
	LendingService lendingService;

	@Autowired
	ClassManagementService classService;
	
	@Autowired
	CatalogService catalogService;	

	@Autowired
	SelectKeyService keyService;

	@Autowired
	ClientService clientService;

	@Autowired
	LendingModelValidator lendingValidator;

	@RequestMapping(value="/return",method = RequestMethod.GET, produces = "text/html")
	public String showReturnPageForClass(LendingModel model, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// check for unfilled client and class - will be the case in first call
		if (model.getClientid()==null) {
			model.setClientid(clientid);
		}
		if (model.getClassid()==null) {
			if (model.getClassinfo()==null) {
				// set classinfo in model
			HashMap<Long,TeacherInfo> classinfo = getClassInfo(clientid);
			model.setClassInfo(classinfo);
			} 

			Long classid = 0L;
			if (model.getClassinfo()!=null) {
				for (Long id:model.getClassinfo().keySet()) {
					classid = id;
					break;
				}
				model.setClassid(classid);
			}
		}

		// get list of checkedout books for the currentclass
		List<LoanRecordDisplay> checkedout = lendingService
				.getCheckedOutBooksForClass(model.getClassid(), clientid);
		model.setCheckedOutList(checkedout);

		populateLendingModel(model, uiModel);

		// to return page
		return "lending/return";
	}

	@RequestMapping(value="/return/selectclass/{id}",method = RequestMethod.POST, produces = "text/html")
	public String changeReturnClass(@PathVariable("id") Long classid,LendingModel model, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();
		// set new classid in model
		model.setClassid(classid);
		// get list of checkedout books for the new class
		List<LoanRecordDisplay> checkedout = lendingService
				.getCheckedOutBooksForClass(model.getClassid(), clientid);
		model.setCheckedOutList(checkedout);

		populateLendingModel(model, uiModel);

		// to returnpage
		return "lending/return";
	}

	@RequestMapping(value="/return/{id}",method = RequestMethod.POST, produces = "text/html")
	public String returnBook(@PathVariable("id") Long loanrecordid,LendingModel model, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);

		// get loanrecordid - from path or from model
		Long clientid = client.getId();
		// return the book
		lendingService.returnBook(loanrecordid, clientid);

		// repopulate checkedout list
		List<LoanRecordDisplay> checkedout = lendingService
				.getCheckedOutBooksForClass(model.getClassid(), clientid);
		model.setCheckedOutList(checkedout);

		populateLendingModel(model, uiModel);
		// TODO add success elements
		
		// to return page (with success message)
		return "lending/return";
	}

	@RequestMapping(value="/checkout",method = RequestMethod.GET, produces = "text/html")
	public String showCheckoutPageForClass(LendingModel model, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// check for unfilled client and class - will be the case in first call
		if (model.getClientid()==null) {
			model.setClientid(clientid);
		}
		if (model.getClassid()==null) {
			if (model.getClassinfo()==null) {
				// set classinfo in model
			HashMap<Long,TeacherInfo> classinfo = getClassInfo(clientid);
			model.setClassInfo(classinfo);
			} 

			Long classid = 0L;
			if (model.getClassinfo()!=null) {
				for (Long id:model.getClassinfo().keySet()) {
					classid = id;
					break;
				}
				model.setClassid(classid);
			}
		}

		// get list of students for class
		List<StudentDao> studentlist = classService.getStudentsForClass(
				model.getClassid(), clientid);
		model.setStudentList(studentlist);

		populateLendingModel(model, uiModel);

		// to checkout page
		return "lending/checkout";
	}
	
	@RequestMapping(value="/checkout/selectclass/{id}",method = RequestMethod.POST, produces = "text/html")
	public String changeCheckoutClass(@PathVariable("id") Long classid,LendingModel model, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// set classid in model
		model.setClassid(classid);

		// get list of students for class
		List<StudentDao> studentlist = classService.getStudentsForClass(
				model.getClassid(), clientid);
		model.setStudentList(studentlist);

		populateLendingModel(model, uiModel);

		// to checkout page
		return "lending/checkout";
	}

	@RequestMapping(value="/checkout/borrower/{id}",method = RequestMethod.POST, produces = "text/html")
	public String selectBorrowerForCheckout(@PathVariable("id") Long borrowerid,LendingModel model, Model uiModel,
			BindingResult bindingResult,HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		// new personid from pathparam or model....
		Long personid = borrowerid;
		
		// set studentid in model
		model.setBorrowerId(personid, model.getStudentList());
		
		// validate Student - make sure that hasn't checked out limit
    	lendingValidator.validateBorrowerEntry(model, bindingResult);

		if (bindingResult.hasErrors()) {
			populateLendingModel(model,uiModel);
			return "lending/checkout";
		}


		// to book select page
		return "lending/selectbook";
	}

	
	@RequestMapping(value="/selectbook",method = RequestMethod.GET, produces = "text/html")
	public String showSelectBookPage(LendingModel model, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {

		// to book select page
		return "lending/selectbook";
	}
	
	@RequestMapping(value = "/checkout/selectbook", method = RequestMethod.POST, produces = "text/html")
	public String checkoutBook(LendingModel model, Model uiModel,
			BindingResult bindingResult,HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		// bookid from path or model
		Long clientid = client.getId();
		String bookid = model.getBookid();

		// put book in model
		BookDao book = catalogService.findBookByClientBookId(bookid, client);
		model.setBook(book);
		model.setBookid(null);

		// Validation here...
    	lendingValidator.validateBookEntry(model, bindingResult);

		if (bindingResult.hasErrors()) {
			populateLendingModel(model,uiModel);
			return "lending/selectbook";
		}

		
		
		// checkout the book
		lendingService.checkoutBook(book.getId(), model.getBorrowerId(),
				clientid);

		// put checkout list for user and limit reached into model
		List<LoanRecordDisplay> checkedoutforuser = lendingService
				.getCheckedOutBooksForUser(model.getBorrowerId(), clientid);
		int borrowerlimit = lendingService.getLendLimitForBorrower(
				model.getBorrowerId(), clientid);
		model.setBorrowerCheckedOut(checkedoutforuser);
		model.setBorrowerLimit(borrowerlimit);

		// when removing checkout success page
		// put title directly into uiModel
		String booktitle = book.getTitle();
		uiModel.addAttribute("booktitle", booktitle);
		populateLendingModel(model, uiModel);
		
		// clear book info from model
		model.setBook(null);
		model.setBookid(null);

		// to checkout success page
		return "lending/checkoutsuccess";

	}	
	
	@RequestMapping(value="/checkout/all",method = RequestMethod.GET, produces = "text/html")
    public String showAllCheckedOutBooks(LendingModel model, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
	    // get list
		List<LoanRecordDisplay> checkedout = lendingService.getCheckedOutBooksForClient(client.getId());
		// put list directly in uiModel
		uiModel.addAttribute("checkedoutbooks",checkedout);
		// get classinfo from model
		HashMap<Long,TeacherInfo> classinfo = model.getClassinfo();
		if (classinfo == null) {
			classinfo = getClassInfo(client.getId());
			model.setClassInfo(classinfo);
		}
		
		uiModel.addAttribute("classInfo",model.getClassinfo());
		
		// to all checked out for client page
		return "lending/checkoutsummary";
		}

	@RequestMapping(value="/overdue/all",method = RequestMethod.GET, produces = "text/html")
	public String showAllOverdueBooks(LendingModel model, Model uiModel,
				HttpServletRequest httpServletRequest, Principal principal) {
			ClientDao client = clientService.getCurrentClient(principal);
		// get list
		List<LoanRecordDisplay> overdue = lendingService.getOverdueBooksForClient(client.getId());
		// put list directly in uiModel
		uiModel.addAttribute("overduebooks",overdue);
		// get classinfo from model
		HashMap<Long,TeacherInfo> classinfo = model.getClassinfo();
		if (classinfo == null) {
			classinfo = getClassInfo(client.getId());
			model.setClassInfo(classinfo);
		}
		
		uiModel.addAttribute("classInfo",model.getClassinfo());
		
		// to all overdue out for client page
		return "lending/overduesummary";
		}
	
	@RequestMapping(value="/summary/all",method = RequestMethod.GET, produces = "text/html")
	public String showAllClassSummary(LendingModel model, Model uiModel,
				HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
		Locale locale = httpServletRequest.getLocale();


		DailySummaryReport csr = lendingService
				.assembleDailySummaryReport( new Date(), clientkey, true);

		uiModel.addAttribute("dailySummaryReport",csr);
		// return checkout report 
		return "lending/checkoutreport";
		}
	

	
	private void populateLendingModel(LendingModel model, Model uiModel) {
		uiModel.addAttribute("lendingModel", model);

	}


	private HashMap<Long,TeacherInfo> getClassInfo(Long clientid) {
		HashMap<Long,TeacherInfo> classinfo  = classService.getTeacherByClassForClient(clientid);
		return classinfo;
	}

    @ModelAttribute("sectionLkup")
    public HashMap<Long,String> getSectionLkup(HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();

    	HashMap<Long, String> booktypedisps = keyService
    			.getDisplayHashForKey(ClassManagementService.sectionLkup, lang);
    	return booktypedisps;
    }
}
