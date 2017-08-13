package meg.biblio.catalog.db.dao;

import meg.biblio.catalog.db.PublisherRepository;
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
public class PublisherDaoDataOnDemand {
    @Autowired
    PublisherRepository publisherRepository;
    private Random rnd = new SecureRandom();
    private List<PublisherDao> data;

    public PublisherDao getNewTransientPublisherDao(int index) {
        PublisherDao obj = new PublisherDao();
        setName(obj, index);
        return obj;
    }

    public void setName(PublisherDao obj, int index) {
        String name = "name_" + index;
        obj.setName(name);
    }

    public PublisherDao getSpecificPublisherDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        PublisherDao obj = data.get(index);
        Long id = obj.getId();
        return publisherRepository.findOne(id);
    }

    public PublisherDao getRandomPublisherDao() {
        init();
        PublisherDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return publisherRepository.findOne(id);
    }

    public boolean modifyPublisherDao(PublisherDao obj) {
        return false;
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = publisherRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'PublisherDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<PublisherDao>();
        for (int i = 0; i < 10; i++) {
            PublisherDao obj = getNewTransientPublisherDao(i);
            try {
                publisherRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            publisherRepository.flush();
            data.add(obj);
        }
    }
}
