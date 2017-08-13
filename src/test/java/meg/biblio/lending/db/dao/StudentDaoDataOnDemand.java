package meg.biblio.lending.db.dao;

import meg.biblio.common.db.dao.ClientDaoDataOnDemand;
import meg.biblio.lending.db.StudentRepository;
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
public class StudentDaoDataOnDemand {
    @Autowired
    ClientDaoDataOnDemand clientDaoDataOnDemand;
    @Autowired
    SchoolGroupDaoDataOnDemand schoolGroupDaoDataOnDemand;
    @Autowired
    StudentRepository studentRepository;
    private Random rnd = new SecureRandom();
    private List<StudentDao> data;

    public StudentDao getNewTransientStudentDao(int index) {
        StudentDao obj = new StudentDao();
        setActive(obj, index);
        setBarcodeid(obj, index);
        setFirstname(obj, index);
        setLastname(obj, index);
        setPsn_type(obj, index);
        setSectionkey(obj, index);
        return obj;
    }

    public void setActive(StudentDao obj, int index) {
        Boolean active = Boolean.TRUE;
        obj.setActive(active);
    }

    public void setBarcodeid(StudentDao obj, int index) {
        String barcodeid = "barcodeid_" + index;
        obj.setBarcodeid(barcodeid);
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = studentRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'StudentDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<StudentDao>();
        for (int i = 0; i < 10; i++) {
            StudentDao obj = getNewTransientStudentDao(i);
            try {
                studentRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            studentRepository.flush();
            data.add(obj);
        }
    }

    public void setFirstname(StudentDao obj, int index) {
        String firstname = "firstname_" + index;
        obj.setFirstname(firstname);
    }

    public void setLastname(StudentDao obj, int index) {
        String lastname = "lastname_" + index;
        obj.setLastname(lastname);
    }

    public void setPsn_type(StudentDao obj, int index) {
        String psn_type = "psn_type_" + index;
        obj.setPsn_type(psn_type);
    }

    public void setSectionkey(StudentDao obj, int index) {
        Long sectionkey = new Integer(index).longValue();
        obj.setSectionkey(sectionkey);
    }

    public boolean modifyStudentDao(StudentDao obj) {
        return false;
    }

    public StudentDao getSpecificStudentDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        StudentDao obj = data.get(index);
        Long id = obj.getId();
        return studentRepository.findOne(id);
    }

    public StudentDao getRandomStudentDao() {
        init();
        StudentDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return studentRepository.findOne(id);
    }
}
