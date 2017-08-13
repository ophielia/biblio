package meg.biblio.common.db.dao;

import meg.biblio.common.db.SelectKeyRepository;
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
public class SelectKeyDaoDataOnDemand {
    @Autowired
    SelectKeyRepository selectKeyRepository;
    private Random rnd = new SecureRandom();
    private List<SelectKeyDao> data;

    public SelectKeyDao getNewTransientSelectKeyDao(int index) {
        SelectKeyDao obj = new SelectKeyDao();
        setLookup(obj, index);
        return obj;
    }

    public void setLookup(SelectKeyDao obj, int index) {
        String lookup = "lookup_" + index;
        if (lookup.length() > 100) {
            lookup = lookup.substring(0, 100);
        }
        obj.setLookup(lookup);
    }

    public SelectKeyDao getSpecificSelectKeyDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        SelectKeyDao obj = data.get(index);
        Long id = obj.getId();
        return selectKeyRepository.findOne(id);
    }

    public SelectKeyDao getRandomSelectKeyDao() {
        init();
        SelectKeyDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return selectKeyRepository.findOne(id);
    }

    public boolean modifySelectKeyDao(SelectKeyDao obj) {
        return false;
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = selectKeyRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'SelectKeyDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<SelectKeyDao>();
        for (int i = 0; i < 10; i++) {
            SelectKeyDao obj = getNewTransientSelectKeyDao(i);
            try {
                selectKeyRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            selectKeyRepository.flush();
            data.add(obj);
        }
    }
}
