package meg.biblio.lending.web.validator;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.LendingService;
import meg.biblio.lending.db.dao.LoanRecordDisplay;
import meg.biblio.lending.db.dao.PersonDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.List;

@Component
public class BarcodeLendValidator {

    @Autowired
    CatalogService catalogService;

    @Autowired
    BookRepository bookRepo;

    @Autowired
    LendingService lendingService;

    public void validatePerson(PersonDao person, ClientDao client,
                               BindingResult bindingErrors) {
        if (person == null) {
            bindingErrors.reject("error_nopersonforcode", null, "No one found belonging to this code");
        }
        // person reached limit
        // ensure that borrower hasn't reached borrowing limit
        int maxco = lendingService.getLendLimitForBorrower(person.getId(), client.getId());
        List<LoanRecordDisplay> checkedoutforuser = lendingService
                .getCheckedOutBooksForUser(person.getId(), client.getId());
        if (checkedoutforuser != null & checkedoutforuser.size() >= maxco) {
            bindingErrors.reject("error_maxbookscheckedout", new Object[]{new Integer(maxco)}, "TOO MANY BOOKS!!!");
        }

    }

    public void validateBook(BookDao book, boolean ischeckout,
                             BindingResult bindingErrors) {
        // validate (book not found -
        if (book == null) {
            bindingErrors.reject("error_nobookforcode", null, "No book found belonging to this code");
        }
        if (ischeckout) {
            if (book.getStatus().longValue() == CatalogService.Status.CHECKEDOUT) {
                bindingErrors.reject("error_alreadycheckedout", null, "Already checked out!");
            }
        } else {
            // !ischeckout, and book not checkedout)
            if (book.getStatus().longValue() != CatalogService.Status.CHECKEDOUT) {
                bindingErrors.reject("error_returningnotcheckedout", null, "Already checked out!");
            }

        }
    }


}
