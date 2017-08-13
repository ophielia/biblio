package meg.biblio.common.db.dao;

import meg.biblio.common.db.ImportBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Component
@Configurable
public class ImportBookDaoDataOnDemand {
    @Autowired
    ImportBookRepository importBookRepository;
    private Random rnd = new SecureRandom();
    private List<ImportBookDao> data;

    public void setBarcode(ImportBookDao obj, int index) {
        String barcode = "barcode_" + index;
        obj.setBarcode(barcode);
    }

    public ImportBookDao getNewTransientImportBookDao(int index) {
        ImportBookDao obj = new ImportBookDao();
        setAuthor(obj, index);
        setBarcode(obj, index);
        setClientbookid(obj, index);
        setError(obj, index);
        setIllustrator(obj, index);
        setIsbn10(obj, index);
        setIsbn13(obj, index);
        setPublisher(obj, index);
        setTitle(obj, index);
        return obj;
    }

    public ImportBookDao getRandomImportBookDao() {
        init();
        ImportBookDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return importBookRepository.findOne(id);
    }

    public void setTitle(ImportBookDao obj, int index) {
        String title = "title_" + index;
        obj.setTitle(title);
    }

    public void setAuthor(ImportBookDao obj, int index) {
        String author = "author_" + index;
        obj.setAuthor(author);
    }

    public void setClientbookid(ImportBookDao obj, int index) {
        String clientbookid = "clientbookid_" + index;
        obj.setClientbookid(clientbookid);
    }

    public void setError(ImportBookDao obj, int index) {
        String error = "error_" + index;
        obj.setError(error);
    }

    public ImportBookDao getSpecificImportBookDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        ImportBookDao obj = data.get(index);
        Long id = obj.getId();
        return importBookRepository.findOne(id);
    }

    public boolean modifyImportBookDao(ImportBookDao obj) {
        return false;
    }

    public void setIllustrator(ImportBookDao obj, int index) {
        String illustrator = "illustrator_" + index;
        obj.setIllustrator(illustrator);
    }

    public void setIsbn10(ImportBookDao obj, int index) {
        String isbn10 = "isbn10_" + index;
        obj.setIsbn10(isbn10);
    }

    public void setIsbn13(ImportBookDao obj, int index) {
        String isbn13 = "isbn13_" + index;
        obj.setIsbn13(isbn13);
    }

    public void setPublisher(ImportBookDao obj, int index) {
        String publisher = "publisher_" + index;
        obj.setPublisher(publisher);
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = importBookRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'ImportBookDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<ImportBookDao>();
        for (int i = 0; i < 10; i++) {
            ImportBookDao obj = getNewTransientImportBookDao(i);
            try {
                importBookRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            importBookRepository.flush();
            data.add(obj);
        }
    }
}
