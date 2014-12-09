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
import meg.biblio.lending.db.SchoolGroupRepository;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.SchoolGroupDaoDataOnDemand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

privileged aspect SchoolGroupDaoDataOnDemand_Roo_DataOnDemand {
    
    declare @type: SchoolGroupDaoDataOnDemand: @Component;
    
    private Random SchoolGroupDaoDataOnDemand.rnd = new SecureRandom();
    
    private List<SchoolGroupDao> SchoolGroupDaoDataOnDemand.data;
    
    @Autowired
    ClientDaoDataOnDemand SchoolGroupDaoDataOnDemand.clientDaoDataOnDemand;
    
    @Autowired
    SchoolGroupRepository SchoolGroupDaoDataOnDemand.schoolGroupRepository;
    
    public SchoolGroupDao SchoolGroupDaoDataOnDemand.getNewTransientSchoolGroupDao(int index) {
        SchoolGroupDao obj = new SchoolGroupDao();
        setSchoolyearbegin(obj, index);
        setSchoolyearend(obj, index);
        return obj;
    }
    
    public void SchoolGroupDaoDataOnDemand.setSchoolyearbegin(SchoolGroupDao obj, int index) {
        Integer schoolyearbegin = new Integer(index);
        obj.setSchoolyearbegin(schoolyearbegin);
    }
    
    public void SchoolGroupDaoDataOnDemand.setSchoolyearend(SchoolGroupDao obj, int index) {
        Integer schoolyearend = new Integer(index);
        obj.setSchoolyearend(schoolyearend);
    }
    
    public SchoolGroupDao SchoolGroupDaoDataOnDemand.getSpecificSchoolGroupDao(int index) {
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
    
    public SchoolGroupDao SchoolGroupDaoDataOnDemand.getRandomSchoolGroupDao() {
        init();
        SchoolGroupDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return schoolGroupRepository.findOne(id);
    }
    
    public boolean SchoolGroupDaoDataOnDemand.modifySchoolGroupDao(SchoolGroupDao obj) {
        return false;
    }
    
    public void SchoolGroupDaoDataOnDemand.init() {
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
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext();) {
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
