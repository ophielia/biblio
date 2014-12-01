// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package meg.biblio.common.db.dao;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import meg.biblio.common.db.LoginRepository;
import meg.biblio.common.db.dao.ClientDaoDataOnDemand;
import meg.biblio.common.db.dao.LoginDaoDataOnDemand;
import meg.biblio.common.db.dao.RoleDaoDataOnDemand;
import meg.biblio.common.db.dao.UserLoginDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

privileged aspect LoginDaoDataOnDemand_Roo_DataOnDemand {
    
    declare @type: LoginDaoDataOnDemand: @Component;
    
    private Random LoginDaoDataOnDemand.rnd = new SecureRandom();
    
    private List<UserLoginDao> LoginDaoDataOnDemand.data;
    
    @Autowired
    ClientDaoDataOnDemand LoginDaoDataOnDemand.clientDaoDataOnDemand;
    
    @Autowired
    RoleDaoDataOnDemand LoginDaoDataOnDemand.roleDaoDataOnDemand;
    
    @Autowired
    LoginRepository LoginDaoDataOnDemand.loginRepository;
    
    public UserLoginDao LoginDaoDataOnDemand.getNewTransientUserLoginDao(int index) {
        UserLoginDao obj = new UserLoginDao();
        setCreatedOn(obj, index);
        setEnabled(obj, index);
        setPassword(obj, index);
        setTextpassword(obj, index);
        setUsername(obj, index);
        return obj;
    }
    
    public void LoginDaoDataOnDemand.setCreatedOn(UserLoginDao obj, int index) {
        Date createdOn = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setCreatedOn(createdOn);
    }
    
    public void LoginDaoDataOnDemand.setEnabled(UserLoginDao obj, int index) {
        Boolean enabled = Boolean.TRUE;
        obj.setEnabled(enabled);
    }
    
    public void LoginDaoDataOnDemand.setPassword(UserLoginDao obj, int index) {
        String password = "password_" + index;
        if (password.length() > 250) {
            password = password.substring(0, 250);
        }
        obj.setPassword(password);
    }
    
    public void LoginDaoDataOnDemand.setTextpassword(UserLoginDao obj, int index) {
        String textpassword = "textpassword_" + index;
        if (textpassword.length() > 250) {
            textpassword = textpassword.substring(0, 250);
        }
        obj.setTextpassword(textpassword);
    }
    
    public void LoginDaoDataOnDemand.setUsername(UserLoginDao obj, int index) {
        String username = "username_" + index;
        if (username.length() > 50) {
            username = username.substring(0, 50);
        }
        obj.setUsername(username);
    }
    
    public UserLoginDao LoginDaoDataOnDemand.getSpecificUserLoginDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        UserLoginDao obj = data.get(index);
        Long id = obj.getId();
        return loginRepository.findOne(id);
    }
    
    public UserLoginDao LoginDaoDataOnDemand.getRandomUserLoginDao() {
        init();
        UserLoginDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return loginRepository.findOne(id);
    }
    
    public boolean LoginDaoDataOnDemand.modifyUserLoginDao(UserLoginDao obj) {
        return false;
    }
    
    public void LoginDaoDataOnDemand.init() {
        int from = 0;
        int to = 10;
        data = loginRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'UserLoginDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<UserLoginDao>();
        for (int i = 0; i < 10; i++) {
            UserLoginDao obj = getNewTransientUserLoginDao(i);
            try {
                loginRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext();) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            loginRepository.flush();
            data.add(obj);
        }
    }
    
}
