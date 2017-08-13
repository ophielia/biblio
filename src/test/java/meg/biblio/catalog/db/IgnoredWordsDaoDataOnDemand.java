package meg.biblio.catalog.db;

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
public class IgnoredWordsDaoDataOnDemand {
    @Autowired
    IgnoredWordsRepository ignoredWordsRepository;
    private Random rnd = new SecureRandom();
    private List<IgnoredWordsDao> data;

    public IgnoredWordsDao getNewTransientIgnoredWordsDao(int index) {
        IgnoredWordsDao obj = new IgnoredWordsDao();
        setWord(obj, index);
        return obj;
    }

    public void setWord(IgnoredWordsDao obj, int index) {
        String word = "word_" + index;
        obj.setWord(word);
    }

    public IgnoredWordsDao getSpecificIgnoredWordsDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        IgnoredWordsDao obj = data.get(index);
        Long id = obj.getId();
        return ignoredWordsRepository.findOne(id);
    }

    public IgnoredWordsDao getRandomIgnoredWordsDao() {
        init();
        IgnoredWordsDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return ignoredWordsRepository.findOne(id);
    }

    public boolean modifyIgnoredWordsDao(IgnoredWordsDao obj) {
        return false;
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = ignoredWordsRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'IgnoredWordsDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<IgnoredWordsDao>();
        for (int i = 0; i < 10; i++) {
            IgnoredWordsDao obj = getNewTransientIgnoredWordsDao(i);
            try {
                ignoredWordsRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            ignoredWordsRepository.flush();
            data.add(obj);
        }
    }
}
