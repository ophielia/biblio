package meg.biblio.catalog.db.dao;

import meg.biblio.catalog.db.FoundDetailsRepository;
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
public class FoundDetailsDaoDataOnDemand {
    @Autowired
    FoundDetailsRepository foundDetailsRepository;
    private Random rnd = new SecureRandom();
    private List<FoundDetailsDao> data;

    public void setDescription(FoundDetailsDao obj, int index) {
        String description = "description_" + index;
        if (description.length() > 2000) {
            description = description.substring(0, 2000);
        }
        obj.setDescription(description);
    }

    public FoundDetailsDao getNewTransientFoundDetailsDao(int index) {
        FoundDetailsDao obj = new FoundDetailsDao();
        setAuthors(obj, index);
        setBookdetailid(obj, index);
        setDescription(obj, index);
        setIllustrators(obj, index);
        setImagelink(obj, index);
        setIsbn10(obj, index);
        setIsbn13(obj, index);
        setLanguage(obj, index);
        setPublisher(obj, index);
        setPublishyear(obj, index);
        setSearchserviceid(obj, index);
        setSearchsource(obj, index);
        setTitle(obj, index);
        setType(obj, index);
        return obj;
    }

    public void setType(FoundDetailsDao obj, int index) {
        String type = "type_" + index;
        obj.setType(type);
    }

    public void setAuthors(FoundDetailsDao obj, int index) {
        String authors = "authors_" + index;
        obj.setAuthors(authors);
    }

    public void setBookdetailid(FoundDetailsDao obj, int index) {
        Long bookdetailid = new Integer(index).longValue();
        obj.setBookdetailid(bookdetailid);
    }

    public void setIllustrators(FoundDetailsDao obj, int index) {
        String illustrators = "illustrators_" + index;
        obj.setIllustrators(illustrators);
    }

    public void setImagelink(FoundDetailsDao obj, int index) {
        String imagelink = "imagelink_" + index;
        obj.setImagelink(imagelink);
    }

    public FoundDetailsDao getSpecificFoundDetailsDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        FoundDetailsDao obj = data.get(index);
        Long id = obj.getId();
        return foundDetailsRepository.findOne(id);
    }

    public boolean modifyFoundDetailsDao(FoundDetailsDao obj) {
        return false;
    }

    public void setSearchserviceid(FoundDetailsDao obj, int index) {
        String searchserviceid = "searchserviceid_" + index;
        obj.setSearchserviceid(searchserviceid);
    }

    public void setTitle(FoundDetailsDao obj, int index) {
        String title = "title_" + index;
        obj.setTitle(title);
    }

    public void setIsbn10(FoundDetailsDao obj, int index) {
        String isbn10 = "isbn10_" + index;
        obj.setIsbn10(isbn10);
    }

    public void setPublisher(FoundDetailsDao obj, int index) {
        String publisher = "publisher_" + index;
        obj.setPublisher(publisher);
    }

    public void setSearchsource(FoundDetailsDao obj, int index) {
        Long searchsource = new Integer(index).longValue();
        obj.setSearchsource(searchsource);
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = foundDetailsRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'FoundDetailsDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<FoundDetailsDao>();
        for (int i = 0; i < 10; i++) {
            FoundDetailsDao obj = getNewTransientFoundDetailsDao(i);
            try {
                foundDetailsRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            foundDetailsRepository.flush();
            data.add(obj);
        }
    }

    public void setIsbn13(FoundDetailsDao obj, int index) {
        String isbn13 = "isbn13_" + index;
        obj.setIsbn13(isbn13);
    }

    public void setLanguage(FoundDetailsDao obj, int index) {
        String language = "language_" + index;
        obj.setLanguage(language);
    }

    public FoundDetailsDao getRandomFoundDetailsDao() {
        init();
        FoundDetailsDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return foundDetailsRepository.findOne(id);
    }

    public void setPublishyear(FoundDetailsDao obj, int index) {
        Long publishyear = new Integer(index).longValue();
        obj.setPublishyear(publishyear);
    }
}
