package meg.biblio.catalog.db.dao;

import meg.biblio.catalog.db.ClassificationRepository;
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
public class ClassificationDaoDataOnDemand {
    @Autowired
    ClassificationRepository classificationRepository;
    private Random rnd = new SecureRandom();
    private List<ClassificationDao> data;

    public void setClientid(ClassificationDao obj, int index) {
        Long clientid = new Integer(index).longValue();
        obj.setClientid(clientid);
    }

    public ClassificationDao getNewTransientClassificationDao(int index) {
        ClassificationDao obj = new ClassificationDao();
        setClientid(obj, index);
        setDescription(obj, index);
        setImagedisplay(obj, index);
        setKey(obj, index);
        setLanguage(obj, index);
        setTextdisplay(obj, index);
        return obj;
    }

    public void setDescription(ClassificationDao obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = classificationRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'ClassificationDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<ClassificationDao>();
        for (int i = 0; i < 10; i++) {
            ClassificationDao obj = getNewTransientClassificationDao(i);
            try {
                classificationRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            classificationRepository.flush();
            data.add(obj);
        }
    }

    public void setImagedisplay(ClassificationDao obj, int index) {
        String imagedisplay = "imagedisplay_" + index;
        obj.setImagedisplay(imagedisplay);
    }

    public void setKey(ClassificationDao obj, int index) {
        Long key = new Integer(index).longValue();
        obj.setKey(key);
    }

    public void setLanguage(ClassificationDao obj, int index) {
        String language = "language_" + index;
        obj.setLanguage(language);
    }

    public void setTextdisplay(ClassificationDao obj, int index) {
        String textdisplay = "textdisplay_" + index;
        obj.setTextdisplay(textdisplay);
    }

    public ClassificationDao getSpecificClassificationDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        ClassificationDao obj = data.get(index);
        Long id = obj.getId();
        return classificationRepository.findOne(id);
    }

    public ClassificationDao getRandomClassificationDao() {
        init();
        ClassificationDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return classificationRepository.findOne(id);
    }

    public boolean modifyClassificationDao(ClassificationDao obj) {
        return false;
    }
}
