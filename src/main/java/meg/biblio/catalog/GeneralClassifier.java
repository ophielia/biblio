package meg.biblio.catalog;

import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeneralClassifier implements Classifier {

    @Override
    public BookDao classifyBook(BookDao book) {
        // get book, book detail
        BookDetailDao bookdetail = book.getBookdetail();

        boolean treatedasfiction = true;
        if (bookdetail.getListedtype() != null && bookdetail.getListedtype() == CatalogService.BookType.FICTION) {
        } else {
            // not marked as fiction. So:
            // assumed fiction, unless

            // shelf class contains numbers
            String shelfclass = bookdetail.getShelfclass();
            // MM bookdetail.getShelfClass();
            if (shelfclass != null && shelfclass.matches("^[\\d .]*$")) {
                treatedasfiction = false;

            }
            // some subjects set - this is much fuzzier, because it's possible
            // that fiction might have subjects
            if (bookdetail.getSubjects() != null
                    && bookdetail.getSubjects().size() > 0) {
                treatedasfiction = false;
            }
        }

        // set book type if not set
        if (treatedasfiction) {
            bookdetail.setListedtype(CatalogService.BookType.FICTION);
        } else {
            bookdetail.setListedtype(CatalogService.BookType.NONFICTION);
        }

        // if book shelf class not set
        if (bookdetail.getShelfclass() == null) {
            if (bookdetail.getListedtype() == CatalogService.BookType.FICTION) {
                // fiction is a plus first three of authors name
                String classification = "A ";
                List<ArtistDao> authors = bookdetail.getAuthors();
                if (authors != null && authors.size() > 0) {
                    String author = authors.get(0).getLastname().toUpperCase();
                    int len = author.length() > 3 ? 3 : author.length();
                    classification = classification + author.substring(0, len);
                    bookdetail.setShelfclass(classification);
                }
            } else {
                // non fiction is dewey if available
                // which is either found in the detailssearch (already run by
                // this point) or not available - nothing to do here.
            }
        }
// return book

        return book;
    }
}
