package meg.biblio.lending.web.validator;

import java.util.List;

import meg.biblio.catalog.CatalogService;
import meg.biblio.lending.LendingService;
import meg.biblio.lending.db.dao.LoanRecordDisplay;
import meg.biblio.lending.web.model.ClassModel;
import meg.biblio.lending.web.model.LendingModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import antlr.CSharpCodeGenerator;

@Component
public class LendingModelValidator {

	@Autowired
	LendingService lendingService;
	
	public void validateBorrowerEntry(LendingModel model, BindingResult errors) {
		// ensure that borrower hasn't reached borrowing limit
		int maxco = lendingService.getLendLimitForBorrower(model.getBorrowerId(), model.getClientid());
		List<LoanRecordDisplay> checkedoutforuser = lendingService
				.getCheckedOutBooksForUser(model.getBorrowerId(), model.getClientid());
		if (checkedoutforuser!=null & checkedoutforuser.size()>=maxco) {
			errors.reject("error_maxbookscheckedout",new Object[]{new Integer(maxco)},"TOO MANY BOOKS!!!");
		}
	}
	public void validateBookEntry(LendingModel model, BindingResult errors) {
		// check that book was found
		if (model.getBook() == null) {
			errors.reject("error_booknotfound");			
		} else if (model.getBook().getStatus().longValue()==CatalogService.Status.CHECKEDOUT){
			// check that book isn't already checkedout
			errors.reject("error_alreadycheckedout");
		}
	}
	
	public void validateNewStudentEntry(ClassModel model, BindingResult errors) {
		// check name of student is entered and isn't too long
		String studententry = model.getStudentname();
		if (studententry!=null) {
			int length = studententry.trim().length();
			if (studententry.trim().length()==0) {
				errors.rejectValue("studentname","field_required");	
			}else if (length>250) {
				errors.rejectValue("studentname","field_toolong");
			}
		} else {
			errors.rejectValue("studentname","field_required");
		}
	}	
	
	public void validateEditStudentEntry(ClassModel model, BindingResult errors) {
		// check name of student is entered and isn't too long
		String studententry = model.getStudentname();
		if (studententry!=null) {
			int length = studententry.trim().length();
			if (length==0) {
				errors.rejectValue("studentname","field_required");	
			}else if (length>250) {
				errors.rejectValue("studentname","field_toolong");
			}	
			String studentfn = model.getStudentfirstname();
			if (studentfn!=null) {
				length=studentfn.trim().length();
				if (length==0) {
					errors.rejectValue("studentfirstname","field_required");	
				}else if (length>250) {
					errors.rejectValue("studentfirstname","field_toolong");
				}				
			}			
		} else {
			String studentfn = model.getStudentfirstname();
			if (studentfn!=null) {
				int length=studentfn.trim().length();
				if (length==0) {
					errors.rejectValue("studentfirstname","field_required");	
				}else if (length>250) {
					errors.rejectValue("studentfirstname","field_toolong");
				}				
			} else {
				errors.rejectValue("studentname","field_required");
				errors.rejectValue("studentfirstname","field_required");
			}
			
			
		}
	}		

}
