package meg.biblio.catalog.db;

import meg.biblio.catalog.db.dao.BookDetailDaoDataOnDemand;
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
public class FoundWordsDaoDataOnDemand {
    @Autowired
    BookDetailDaoDataOnDemand bookDetailDaoDataOnDemand;
    @Autowired
    FoundWordsRepository foundWordsRepository;
    private Random rnd = new SecureRandom();
    private List<FoundWordsDao> data;

    public FoundWordsDao getNewTransientFoundWordsDao(int index) {
        FoundWordsDao obj = new FoundWordsDao();
        setCountintext(obj, index);
        setWord(obj, index);
        return obj;
    }

    public void setCountintext(FoundWordsDao obj, int index) {
        Integer countintext = new Integer(index);
        obj.setCountintext(countintext);
    }

    public void setWord(FoundWordsDao obj, int index) {
        String word = "word_" + index;
        obj.setWord(word);
    }

    public FoundWordsDao getSpecificFoundWordsDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        FoundWordsDao obj = data.get(index);
        Long id = obj.getId();
        return foundWordsRepository.findOne(id);
    }

    public FoundWordsDao getRandomFoundWordsDao() {
        init();
        FoundWordsDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return foundWordsRepository.findOne(id);
    }

    public boolean modifyFoundWordsDao(FoundWordsDao obj) {
        return false;
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = foundWordsRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'FoundWordsDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<FoundWordsDao>();
        for (int i = 0; i < 10; i++) {
            FoundWordsDao obj = getNewTransientFoundWordsDao(i);
            try {
                foundWordsRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            foundWordsRepository.flush();
            data.add(obj);
        }
    }
}
