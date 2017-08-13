package meg.biblio.common.db.dao;

import meg.biblio.common.db.AppSettingRepository;
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
public class AppSettingDaoDataOnDemand {
    @Autowired
    AppSettingRepository appSettingRepository;
    private Random rnd = new SecureRandom();
    private List<AppSettingDao> data;

    public AppSettingDao getNewTransientAppSettingDao(int index) {
        AppSettingDao obj = new AppSettingDao();
        setKey(obj, index);
        setValue(obj, index);
        return obj;
    }

    public void setKey(AppSettingDao obj, int index) {
        String key = "key_" + index;
        obj.setKey(key);
    }

    public void setValue(AppSettingDao obj, int index) {
        String value = "value_" + index;
        obj.setValue(value);
    }

    public AppSettingDao getSpecificAppSettingDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        AppSettingDao obj = data.get(index);
        Long id = obj.getId();
        return appSettingRepository.findOne(id);
    }

    public AppSettingDao getRandomAppSettingDao() {
        init();
        AppSettingDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return appSettingRepository.findOne(id);
    }

    public boolean modifyAppSettingDao(AppSettingDao obj) {
        return false;
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = appSettingRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'AppSettingDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<AppSettingDao>();
        for (int i = 0; i < 10; i++) {
            AppSettingDao obj = getNewTransientAppSettingDao(i);
            try {
                appSettingRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            appSettingRepository.flush();
            data.add(obj);
        }
    }
}
