package meg.biblio.catalog.db.dao;

import meg.biblio.catalog.db.BookDetailRepository;
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

@Configurable
@Component
public class BookDetailDaoDataOnDemand {
    @Autowired
    PublisherDaoDataOnDemand publisherDaoDataOnDemand;
    @Autowired
    BookDetailRepository bookDetailRepository;
    private Random rnd = new SecureRandom();
    private List<BookDetailDao> data;

    public void setArk(BookDetailDao obj, int index) {
        String ark = "ark_" + index;
        obj.setArk(ark);
    }

    public BookDetailDao getNewTransientBookDetailDao(int index) {
        BookDetailDao obj = new BookDetailDao();
        setArk(obj, index);
        setClientspecific(obj, index);
        setDescription(obj, index);
        setDetailstatus(obj, index);
        setFinderlog(obj, index);
        setImagelink(obj, index);
        setIsbn10(obj, index);
        setIsbn13(obj, index);
        setLanguage(obj, index);
        setListedtype(obj, index);
        setPublishyear(obj, index);
        setShelfclass(obj, index);
        setTitle(obj, index);
        return obj;
    }

    public void setListedtype(BookDetailDao obj, int index) {
        Long listedtype = new Integer(index).longValue();
        obj.setListedtype(listedtype);
    }

    public void setLanguage(BookDetailDao obj, int index) {
        String language = "language_" + index;
        obj.setLanguage(language);
    }

    public void setClientspecific(BookDetailDao obj, int index) {
        Boolean clientspecific = false;
        obj.setClientspecific(clientspecific);
    }

    public void setDescription(BookDetailDao obj, int index) {
        String description = "description_" + index;
        if (description.length() > 2000) {
            description = description.substring(0, 2000);
        }
        obj.setDescription(description);
    }

    public boolean modifyBookDetailDao(BookDetailDao obj) {
        return false;
    }

    public void setPublishyear(BookDetailDao obj, int index) {
        Long publishyear = new Integer(index).longValue();
        obj.setPublishyear(publishyear);
    }

    public void setDetailstatus(BookDetailDao obj, int index) {
        Long detailstatus = new Integer(index).longValue();
        obj.setDetailstatus(detailstatus);
    }

    public void setFinderlog(BookDetailDao obj, int index) {
        Long finderlog = new Integer(index).longValue();
        obj.setFinderlog(finderlog);
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = bookDetailRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'BookDetailDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<BookDetailDao>();
        for (int i = 0; i < 10; i++) {
            BookDetailDao obj = getNewTransientBookDetailDao(i);
            try {
                bookDetailRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            bookDetailRepository.flush();
            data.add(obj);
        }
    }

    public void setShelfclass(BookDetailDao obj, int index) {
        String shelfclass = "shelfclass_" + index;
        obj.setShelfclass(shelfclass);
    }

    public BookDetailDao getSpecificBookDetailDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        BookDetailDao obj = data.get(index);
        Long id = obj.getId();
        return bookDetailRepository.findOne(id);
    }

    public void setTitle(BookDetailDao obj, int index) {
        String title = "title_" + index;
        obj.setTitle(title);
    }

    public void setImagelink(BookDetailDao obj, int index) {
        String imagelink = "imagelink_" + index;
        if (imagelink.length() > 500) {
            imagelink = imagelink.substring(0, 500);
        }
        obj.setImagelink(imagelink);
    }

    public void setIsbn10(BookDetailDao obj, int index) {
        String isbn10 = "isbn10_" + index;
        obj.setIsbn10(isbn10);
    }

    public void setIsbn13(BookDetailDao obj, int index) {
        String isbn13 = "isbn13_" + index;
        obj.setIsbn13(isbn13);
    }

    public BookDetailDao getRandomBookDetailDao() {
        init();
        BookDetailDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return bookDetailRepository.findOne(id);
    }
}
