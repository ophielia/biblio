package meg.biblio.catalog.db.dao;

import meg.biblio.catalog.db.SubjectRepository;
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
public class SubjectDaoDataOnDemand {
    @Autowired
    SubjectRepository subjectRepository;
    private Random rnd = new SecureRandom();
    private List<SubjectDao> data;

    public SubjectDao getNewTransientSubjectDao(int index) {
        SubjectDao obj = new SubjectDao();
        setListing(obj, index);
        return obj;
    }

    public void setListing(SubjectDao obj, int index) {
        String listing = "listing_" + index;
        obj.setListing(listing);
    }

    public SubjectDao getSpecificSubjectDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        SubjectDao obj = data.get(index);
        Long id = obj.getId();
        return subjectRepository.findOne(id);
    }

    public SubjectDao getRandomSubjectDao() {
        init();
        SubjectDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return subjectRepository.findOne(id);
    }

    public boolean modifySubjectDao(SubjectDao obj) {
        return false;
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = subjectRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'SubjectDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<SubjectDao>();
        for (int i = 0; i < 10; i++) {
            SubjectDao obj = getNewTransientSubjectDao(i);
            try {
                subjectRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            subjectRepository.flush();
            data.add(obj);
        }
    }
}
