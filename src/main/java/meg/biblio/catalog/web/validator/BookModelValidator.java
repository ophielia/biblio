package meg.biblio.catalog.web.validator;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.common.db.dao.ClientDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class BookModelValidator {

    @Autowired
    CatalogService catalogService;

    @Autowired
    BookRepository bookRepo;

    public void validateNewBookEntry(BookModel model, BindingResult errors,
                                     ClientDao client) {

        // if not generate new - check no existing book for book code
        String clientnr = model.getClientbookid();
        if (clientnr != null) {
            BookDao book = catalogService.findBookByClientBookId(clientnr,
                    client);
            if (book != null) {
                errors.rejectValue("clientbookid", "error_bookfound");
                return;
            }
        }

        // validation - check isbn - OR - title
        boolean hasisbn = model.hasIsbn();
        boolean hastitle = model.getTitle() != null
                && model.getTitle().trim().length() > 0;
        if ((!hasisbn) && !(hastitle)) {
            errors.reject("error_eitheror", null, "Title or ISBN");
        }

        // check searchagain
        BookDetailDao bd = model.getBook().getBookdetail();
        Long status = model.getDetailstatus();
        String publishername = model.getPublishername() != null ? model
                .getPublishername() : "";
        boolean haspublisher = publishername != null
                && publishername.trim().length() > 0;
        if (status != null) {
            if (status.longValue() == CatalogService.DetailStatus.DETAILNOTFOUNDWISBN) {
                // search already done by isbn - search now by title AND author
                if (!hastitle && (!bd.hasAuthor() || !haspublisher)) {
                    errors.reject("error_searchagain", null, "Title and author");
                }
            }
        }

    }

    public void validateUpdateBook(BookModel bookModel,
                                   BindingResult bindingResult) {
        // called when updating book. Ensures that book has a title
        String booktitle = bookModel.getTitle();
        if (booktitle == null || booktitle.equals(CatalogService.titledefault)
                || booktitle.trim().length() == 0) {
            // book doesn't have title - - ensure that title has been entered in
            // model
            // but this only if detailstatus is not found (could be that the
            // isbn has been entered, and a search will be made...
            if (bookModel.getBook().getBookdetail().getDetailstatus()
                    .longValue() == CatalogService.DetailStatus.DETAILNOTFOUND) {
                if (bookModel.getIsbnentry() == null) {
                    String enteredtitle = bookModel.getTitle();
                    if (enteredtitle == null
                            || enteredtitle.trim().length() == 0) {
                        bindingResult.rejectValue("title", "error_entertitle");
                    }
                }
            }
        }
    }

    public void validateAssignIdToBook(String id, ClientDao client,
                                       BindingResult errors) {
        if (id == null) {
            errors.rejectValue("newClientId", "error_noclientid");
        } else if (id.length() == 0) {
            errors.rejectValue("newClientId", "error_noclientid");
        } else {
            // check whether code has already been used
            BookDao book = catalogService.findBookByClientBookId(id, client);
            if (book != null) {
                errors.rejectValue("newClientId", "error_bookfound");
                return;
            }
        }
    }

}
