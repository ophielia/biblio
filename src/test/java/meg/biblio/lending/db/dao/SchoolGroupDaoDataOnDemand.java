package meg.biblio.lending.db.dao;

import meg.biblio.common.db.dao.ClientDaoDataOnDemand;
import meg.biblio.lending.db.SchoolGroupRepository;
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
public class SchoolGroupDaoDataOnDemand {
    @Autowired
    ClientDaoDataOnDemand clientDaoDataOnDemand;
    @Autowired
    SchoolGroupRepository schoolGroupRepository;
    private Random rnd = new SecureRandom();
    private List<SchoolGroupDao> data;

    public SchoolGroupDao getNewTransientSchoolGroupDao(int index) {
        SchoolGroupDao obj = new SchoolGroupDao();
        setSchoolyearbegin(obj, index);
        setSchoolyearend(obj, index);
        return obj;
    }

    public void setSchoolyearbegin(SchoolGroupDao obj, int index) {
        Integer schoolyearbegin = new Integer(index);
        obj.setSchoolyearbegin(schoolyearbegin);
    }

    public void setSchoolyearend(SchoolGroupDao obj, int index) {
        Integer schoolyearend = new Integer(index);
        obj.setSchoolyearend(schoolyearend);
    }

    public SchoolGroupDao getSpecificSchoolGroupDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        SchoolGroupDao obj = data.get(index);
        Long id = obj.getId();
        return schoolGroupRepository.findOne(id);
    }

    public SchoolGroupDao getRandomSchoolGroupDao() {
        init();
        SchoolGroupDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return schoolGroupRepository.findOne(id);
    }

    public boolean modifySchoolGroupDao(SchoolGroupDao obj) {
        return false;
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = schoolGroupRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'SchoolGroupDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<SchoolGroupDao>();
        for (int i = 0; i < 10; i++) {
            SchoolGroupDao obj = getNewTransientSchoolGroupDao(i);
            try {
                schoolGroupRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            schoolGroupRepository.flush();
            data.add(obj);
        }
    }
}
