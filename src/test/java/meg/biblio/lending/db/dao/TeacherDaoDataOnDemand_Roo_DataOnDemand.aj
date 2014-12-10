// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package meg.biblio.lending.db.dao;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import meg.biblio.common.db.dao.ClientDaoDataOnDemand;
import meg.biblio.lending.db.TeacherRepository;
import meg.biblio.lending.db.dao.SchoolGroupDaoDataOnDemand;
import meg.biblio.lending.db.dao.TeacherDao;
import meg.biblio.lending.db.dao.TeacherDaoDataOnDemand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

privileged aspect TeacherDaoDataOnDemand_Roo_DataOnDemand {
    
    declare @type: TeacherDaoDataOnDemand: @Component;
    
    private Random TeacherDaoDataOnDemand.rnd = new SecureRandom();
    
    private List<TeacherDao> TeacherDaoDataOnDemand.data;
    
    @Autowired
    ClientDaoDataOnDemand TeacherDaoDataOnDemand.clientDaoDataOnDemand;
    
    @Autowired
    SchoolGroupDaoDataOnDemand TeacherDaoDataOnDemand.schoolGroupDaoDataOnDemand;
    
    @Autowired
    TeacherRepository TeacherDaoDataOnDemand.teacherRepository;
    
    public TeacherDao TeacherDaoDataOnDemand.getNewTransientTeacherDao(int index) {
        TeacherDao obj = new TeacherDao();
        setActive(obj, index);
        setEmail(obj, index);
        setFirstname(obj, index);
        setLastname(obj, index);
        setPsn_type(obj, index);
        return obj;
    }
    
    public void TeacherDaoDataOnDemand.setActive(TeacherDao obj, int index) {
        Boolean active = Boolean.TRUE;
        obj.setActive(active);
    }
    
    public void TeacherDaoDataOnDemand.setEmail(TeacherDao obj, int index) {
        String email = "foo" + index + "@bar.com";
        obj.setEmail(email);
    }
    
    public void TeacherDaoDataOnDemand.setFirstname(TeacherDao obj, int index) {
        String firstname = "firstname_" + index;
        obj.setFirstname(firstname);
    }
    
    public void TeacherDaoDataOnDemand.setLastname(TeacherDao obj, int index) {
        String lastname = "lastname_" + index;
        obj.setLastname(lastname);
    }
    
    public void TeacherDaoDataOnDemand.setPsn_type(TeacherDao obj, int index) {
        String psn_type = "psn_type_" + index;
        obj.setPsn_type(psn_type);
    }
    
    public TeacherDao TeacherDaoDataOnDemand.getSpecificTeacherDao(int index) {
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
    
    public TeacherDao TeacherDaoDataOnDemand.getRandomTeacherDao() {
        init();
        TeacherDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return teacherRepository.findOne(id);
    }
    
    public boolean TeacherDaoDataOnDemand.modifyTeacherDao(TeacherDao obj) {
        return false;
    }
    
    public void TeacherDaoDataOnDemand.init() {
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
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext();) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            teacherRepository.flush();
            data.add(obj);
        }
    }
    
}