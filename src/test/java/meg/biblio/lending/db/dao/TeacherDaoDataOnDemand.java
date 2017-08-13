package meg.biblio.lending.db.dao;

import meg.biblio.common.db.dao.ClientDaoDataOnDemand;
import meg.biblio.lending.db.TeacherRepository;
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
public class TeacherDaoDataOnDemand {
    @Autowired
    ClientDaoDataOnDemand clientDaoDataOnDemand;
    @Autowired
    SchoolGroupDaoDataOnDemand schoolGroupDaoDataOnDemand;
    @Autowired
    TeacherRepository teacherRepository;
    private Random rnd = new SecureRandom();
    private List<TeacherDao> data;

    public TeacherDao getNewTransientTeacherDao(int index) {
        TeacherDao obj = new TeacherDao();
        setActive(obj, index);
        setBarcodeid(obj, index);
        setEmail(obj, index);
        setFirstname(obj, index);
        setLastname(obj, index);
        setPsn_type(obj, index);
        return obj;
    }

    public void setActive(TeacherDao obj, int index) {
        Boolean active = Boolean.TRUE;
        obj.setActive(active);
    }

    public void setBarcodeid(TeacherDao obj, int index) {
        String barcodeid = "barcodeid_" + index;
        obj.setBarcodeid(barcodeid);
    }

    public void setEmail(TeacherDao obj, int index) {
        String email = "foo" + index + "@bar.com";
        obj.setEmail(email);
    }

    public void setFirstname(TeacherDao obj, int index) {
        String firstname = "firstname_" + index;
        obj.setFirstname(firstname);
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = teacherRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'TeacherDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<TeacherDao>();
        for (int i = 0; i < 10; i++) {
            TeacherDao obj = getNewTransientTeacherDao(i);
            try {
                teacherRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            teacherRepository.flush();
            data.add(obj);
        }
    }

    public void setLastname(TeacherDao obj, int index) {
        String lastname = "lastname_" + index;
        obj.setLastname(lastname);
    }

    public void setPsn_type(TeacherDao obj, int index) {
        String psn_type = "psn_type_" + index;
        obj.setPsn_type(psn_type);
    }

    public boolean modifyTeacherDao(TeacherDao obj) {
        return false;
    }

    public TeacherDao getSpecificTeacherDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        TeacherDao obj = data.get(index);
        Long id = obj.getId();
        return teacherRepository.findOne(id);
    }

    public TeacherDao getRandomTeacherDao() {
        init();
        TeacherDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return teacherRepository.findOne(id);
    }
}
