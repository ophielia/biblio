package meg.biblio.common.db.dao;

import meg.biblio.common.db.SelectValueRepository;
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
public class SelectValueDaoDataOnDemand {

    @Autowired
    SelectKeyDaoDataOnDemand selectKeyDaoDataOnDemand;
    @Autowired
    SelectValueRepository selectValueRepository;
    private Random rnd = new SecureRandom();
    private List<SelectValueDao> data;

    public void setLanguagekey(SelectValueDao obj, int index) {
        String languagekey = "en";
        obj.setLanguagekey(languagekey);
    }

    public void setDisplay(SelectValueDao obj, int index) {
        String display = "display_" + index;
        if (display.length() > 100) {
            display = display.substring(0, 100);
        }
        obj.setDisplay(display);
    }

    public SelectValueDao getNewTransientSelectValueDao(int index) {
        SelectValueDao obj = new SelectValueDao();
        setActive(obj, index);
        setDisplay(obj, index);
        setDisporder(obj, index);
        setLanguagekey(obj, index);
        setValue(obj, index);
        return obj;
    }

    public void setActive(SelectValueDao obj, int index) {
        Boolean active = Boolean.TRUE;
        obj.setActive(active);
    }

    public void setDisporder(SelectValueDao obj, int index) {
        Long disporder = new Integer(index).longValue();
        obj.setDisporder(disporder);
    }

    public void setValue(SelectValueDao obj, int index) {
        String value = "value_" + index;
        if (value.length() > 100) {
            value = value.substring(0, 100);
        }
        obj.setValue(value);
    }

    public SelectValueDao getSpecificSelectValueDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        SelectValueDao obj = data.get(index);
        Long id = obj.getId();
        return selectValueRepository.findOne(id);
    }

    public SelectValueDao getRandomSelectValueDao() {
        init();
        SelectValueDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return selectValueRepository.findOne(id);
    }

    public boolean modifySelectValueDao(SelectValueDao obj) {
        return false;
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = selectValueRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'SelectValueDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<SelectValueDao>();
        for (int i = 0; i < 10; i++) {
            SelectValueDao obj = getNewTransientSelectValueDao(i);
            try {
                selectValueRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            selectValueRepository.flush();
            data.add(obj);
        }
    }
}
