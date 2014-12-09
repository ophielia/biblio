package meg.biblio.lending.web;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.ClassManagementService;
import meg.biblio.lending.LendingService;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.web.model.ClassInfo;
import meg.biblio.lending.web.model.LendingModel;
import meg.biblio.lending.web.model.LoanRecordDisplay;
import meg.biblio.lending.web.validator.LendingModelValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

//@RequestMapping("/classes")
@Controller
public class LendingController {

	@Autowired
	LendingService lendingService;

	@Autowired
	ClassManagementService classService;

	@Autowired
	SelectKeyService keyService;

	@Autowired
	ClientService clientService;

	@Autowired
	LendingModelValidator lendingValidator;

	public String showReturnPageForClass(LendingModel model, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();
		
		// check for unfilled client and class - will be the case in first call
		if (model.getClientid()==null) {
			model.setClientid(clientid);
		}
		if (model.getClassid()==null) {
			// set to first classid
			List<SchoolGroupDao> classes = classService.getClassesForClient(clientid);
			if (classes!=null && classes.size()>0) {
				SchoolGroupDao sgroup = classes.get(0);
				model.setClassid(sgroup.getId());
			}
			// set classinfo in model
			HashMap<Long,ClassInfo> classinfo = getClassInfo(clientid);
			populateClassInfo(classinfo,uiModel);			
		}
		
		// get list of checkedout books for the currentclass
		List<LoanRecordDisplay> checkedout = lendingService
				.getCheckedOutBooksForClass(model.getClassid());
		model.setCheckedOutList(checkedout);

		populateLendingModel(model, uiModel);

		// to return page
		return null;
	}

	public String changeReturnClass(LendingModel model, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		// pathparam to be set with PathParam
		Long classid = 1L;
		// set new classid in model
		model.setClassid(classid);
		// get list of checkedout books for the new class
		List<LoanRecordDisplay> checkedout = lendingService
				.getCheckedOutBooksForClass(model.getClassid());
		model.setCheckedOutList(checkedout);

		populateLendingModel(model, uiModel);

		// to returnpage
		return null;
	}

	public String returnBook(LendingModel model, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);

		// get loanrecordid - from path or from model
		Long clientid = client.getId();
		Long loanrecordid = 1L;
		// return the book
		lendingService.returnBook(loanrecordid, clientid);

		// repopulate checkedout list
		List<LoanRecordDisplay> checkedout = lendingService
				.getCheckedOutBooksForClass(model.getClassid());
		model.setCheckedOutList(checkedout);

		populateLendingModel(model, uiModel);

		// to return page (with success message)
		return null;
	}

	public String showCheckoutPageForClass(LendingModel model, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();
		
		// check for unfilled client and class - will be the case in first call
		if (model.getClientid()==null) {
			model.setClientid(clientid);
		}
		if (model.getClassid()==null) {
			// set to first classid
			List<SchoolGroupDao> classes = classService.getClassesForClient(clientid);
			if (classes!=null && classes.size()>0) {
				SchoolGroupDao sgroup = classes.get(0);
				model.setClassid(sgroup.getId());
			}
			// set classinfo in model
			HashMap<Long,ClassInfo> classinfo = getClassInfo(clientid);
			populateClassInfo(classinfo,uiModel);
		}
		
		// get list of students for class
		List<StudentDao> studentlist = classService.getStudentsForClass(
				model.getClassid(), clientid);
		model.setStudentList(studentlist);

		populateLendingModel(model, uiModel);

		// to checkout page
		return null;
	}

	public String changeCheckoutClass(LendingModel model, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// new classid from pathparam or model....
		Long classid = 1L;
		// set classid in model
		model.setClassid(classid);

		// get list of students for class
		List<StudentDao> studentlist = classService.getStudentsForClass(
				model.getClassid(), clientid);
		model.setStudentList(studentlist);

		populateLendingModel(model, uiModel);

		// to checkout page
		return null;
	}

	public String selectStudentForCheckout(LendingModel model, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		// new personid from pathparam or model....
		Long personid = 1L;
		// set studentid in model
		model.setBorrowerId(personid, model.getStudentList());

		// to book select page
		return null;
	}

	public String checkoutBook(LendingModel model, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		// bookid from path or model
		Long clientid = client.getId();
		Long bookid = model.getBookid();
		// checkout the book
		lendingService.checkoutBook(bookid, model.getBorrowerId(), clientid);

		// to checkout success page
		return null;
	}

	private void populateLendingModel(LendingModel model, Model uiModel) {
		uiModel.addAttribute("lendingModel", model);

	}
	
	private void populateClassInfo(HashMap<Long, ClassInfo> classinfo,
			Model uiModel) {
		uiModel.addAttribute("classInfo", classinfo);
		
	}

	public HashMap<Long,ClassInfo> getClassInfo(Long clientid) {
		HashMap<Long,ClassInfo> classinfo  = classService.getClassInfoForClient(clientid);
		return classinfo;
	}

}
