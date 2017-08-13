package meg.biblio.common.db.dao;

import meg.biblio.common.db.LoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.security.SecureRandom;
import java.util.*;

@Configurable
@Component
public class LoginDaoDataOnDemand {
    @Autowired
    ClientDaoDataOnDemand clientDaoDataOnDemand;
    @Autowired
    RoleDaoDataOnDemand roleDaoDataOnDemand;
    @Autowired
    LoginRepository loginRepository;
    private Random rnd = new SecureRandom();
    private List<UserLoginDao> data;

    public boolean modifyUserLoginDao(UserLoginDao obj) {
        return false;
    }

    public UserLoginDao getNewTransientUserLoginDao(int index) {
        UserLoginDao obj = new UserLoginDao();
        setCreatedOn(obj, index);
        setEnabled(obj, index);
        setPassword(obj, index);
        setTextpassword(obj, index);
        setUsername(obj, index);
        return obj;
    }

    public void setCreatedOn(UserLoginDao obj, int index) {
        Date createdOn = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setCreatedOn(createdOn);
    }

    public void setEnabled(UserLoginDao obj, int index) {
        Boolean enabled = Boolean.TRUE;
        obj.setEnabled(enabled);
    }

    public void setPassword(UserLoginDao obj, int index) {
        String password = "password_" + index;
        if (password.length() > 250) {
            password = password.substring(0, 250);
        }
        obj.setPassword(password);
    }

    public void setTextpassword(UserLoginDao obj, int index) {
        String textpassword = "textpassword_" + index;
        if (textpassword.length() > 250) {
            textpassword = textpassword.substring(0, 250);
        }
        obj.setTextpassword(textpassword);
    }

    public void setUsername(UserLoginDao obj, int index) {
        String username = "username_" + index;
        if (username.length() > 50) {
            username = username.substring(0, 50);
        }
        obj.setUsername(username);
    }

    public void init() {
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
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            loginRepository.flush();
            data.add(obj);
        }
    }

    public UserLoginDao getSpecificUserLoginDao(int index) {
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

    public UserLoginDao getRandomUserLoginDao() {
        init();
        UserLoginDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return loginRepository.findOne(id);
    }
}
