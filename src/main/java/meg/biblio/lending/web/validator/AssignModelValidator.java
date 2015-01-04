package meg.biblio.lending.web.validator;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.web.model.AssignCodeModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class AssignModelValidator {

	@Autowired
	CatalogService catalogService;
	
	@Autowired
	BookRepository bookRepo;
	
	public void validateNewBookEntry(AssignCodeModel model, BindingResult errors,ClientDao client) {

		
		// if not generate new - no existing book for book code
		boolean generatenew = model.getCreatenewid();
		if (!generatenew) {
			String clientnr = model.getNewbooknr();
			BookDao book = catalogService.findBookByClientBookId(clientnr, client);
			if (book!=null) {
				if (book.getBarcodeid()!=null) {
					errors.reject("error_book_hasbarcode");
				} else {
					errors.rejectValue("newbooknr","error_bookfound");
				}
				return;
			}
		}
		
		// validation - check isbn - OR - title 
		boolean hasisbn = model.getIsbnentry()!=null && model.getIsbnentry().trim().length()>0;
		boolean hastitle = model.getTitle()!=null && model.getTitle().trim().length()>0;
		if ((!hasisbn) && !(hastitle)) {
			errors.reject("error_eitheror",null,"Title or ISBN");
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

	public void validateUpdateBook(AssignCodeModel assignCodeModel,
			BindingResult bindingResult) {
		// called when updating book.  Ensures that book has a title
		String booktitle=assignCodeModel.getBook().getTitle();
		if (booktitle ==null ||
				booktitle.equals(CatalogService.titledefault) || 
				booktitle.trim().length()==0) {
			// book doesn't have title  - - ensure that title has been entered in model
			// but this only if detailstatus is not found (could be that the
			// isbn has been entered, and a search will be made...
			if (assignCodeModel.getBook().getDetailstatus().longValue()==CatalogService.DetailStatus.DETAILNOTFOUND) {
				String enteredtitle = assignCodeModel.getTitle();
				if (enteredtitle == null || enteredtitle.trim().length()==0) {
					bindingResult.rejectValue("title", "error_entertitle");
				}
			}
		}
	}
	
	
	
	
}
