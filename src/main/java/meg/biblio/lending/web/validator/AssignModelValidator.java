package meg.biblio.lending.web.validator;

import java.util.List;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.LendingService;
import meg.biblio.lending.web.model.AssignCodeModel;
import meg.biblio.lending.web.model.ClassModel;
import meg.biblio.lending.web.model.LendingModel;
import meg.biblio.lending.web.model.LoanRecordDisplay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import antlr.CSharpCodeGenerator;

@Component
public class AssignModelValidator {

	@Autowired
	CatalogService catalogService;
	
	@Autowired
	BookRepository bookRepo;
	
	public void validateNewBookEntry(AssignCodeModel model, BindingResult errors,ClientDao client) {
		// validation - check isbn - OR - title 
		boolean hasisbn = model.getIsbnentry()!=null && model.getIsbnentry().trim().length()>0;
		boolean hastitle = model.getTitle()!=null && model.getTitle().trim().length()>0;
		if ((!hasisbn) && !(hastitle)) {
			errors.reject("field_eitheror",null,"Title or ISBN");
		}
		
		// if not generate new - no existing book for book code
		boolean generatenew = model.getCreatenewid();
		if (!generatenew) {
			String clientnr = model.getNewbooknr();
			BookDao book = catalogService.findBookByClientBookId(clientnr, client);
			if (book!=null) {
				errors.rejectValue("newbooknr","error_bookfound");
			}
		}

	}

	public void validateExistingBookEntry(AssignCodeModel assignCodeModel,
			BookDao book, BindingResult errors, ClientDao client) {
		// validate - book already assigned code, book not found
		if (book==null) {
			errors.rejectValue("existbooknr","error_booknotfound");
		} else if (book.getBarcodeid()!=null && book.getBarcodeid().trim().length()>0) {
			errors.rejectValue("existbooknr","error_book_hasbarcode");
		}
		
	}

	public void validateAssignCodeToBook(String code, BindingResult errors) {
		if (code==null) {
			errors.rejectValue("assignedcode","error_barcode_nocode");
		} else {
			BookDao book = bookRepo.findBookByBarcode(code);
			if (book!=null) {
				errors.rejectValue("assignedcode", "error_barcode_alreadyused");
			}
		}
		// check whether code has already been used
		
		
	}
	
	
	
	
}
