package meg.biblio.inventory.db.dao;

import meg.biblio.catalog.db.dao.BookDaoDataOnDemand;
import meg.biblio.inventory.db.InventoryHistRepository;
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
public class InventoryHistoryDaoDataOnDemand {
    @Autowired
    BookDaoDataOnDemand bookDaoDataOnDemand;
    @Autowired
    InventoryDaoDataOnDemand inventoryDaoDataOnDemand;
    @Autowired
    InventoryHistRepository inventoryHistRepository;
    private Random rnd = new SecureRandom();
    private List<InventoryHistoryDao> data;

    public InventoryHistoryDao getNewTransientInventoryHistoryDao(int index) {
        InventoryHistoryDao obj = new InventoryHistoryDao();
        setFoundbook(obj, index);
        setNewstatus(obj, index);
        setOriginalstatus(obj, index);
        return obj;
    }

    public void setFoundbook(InventoryHistoryDao obj, int index) {
        Boolean foundbook = Boolean.TRUE;
        obj.setFoundbook(foundbook);
    }

    public void setNewstatus(InventoryHistoryDao obj, int index) {
        Long newstatus = new Integer(index).longValue();
        obj.setNewstatus(newstatus);
    }

    public void setOriginalstatus(InventoryHistoryDao obj, int index) {
        Long originalstatus = new Integer(index).longValue();
        obj.setOriginalstatus(originalstatus);
    }

    public InventoryHistoryDao getSpecificInventoryHistoryDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        InventoryHistoryDao obj = data.get(index);
        Long id = obj.getId();
        return inventoryHistRepository.findOne(id);
    }

    public InventoryHistoryDao getRandomInventoryHistoryDao() {
        init();
        InventoryHistoryDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return inventoryHistRepository.findOne(id);
    }

    public boolean modifyInventoryHistoryDao(InventoryHistoryDao obj) {
        return false;
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = inventoryHistRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'InventoryHistoryDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<InventoryHistoryDao>();
        for (int i = 0; i < 10; i++) {
            InventoryHistoryDao obj = getNewTransientInventoryHistoryDao(i);
            try {
                inventoryHistRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            inventoryHistRepository.flush();
            data.add(obj);
        }
    }
}
