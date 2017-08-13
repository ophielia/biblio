package meg.biblio.inventory.db.dao;

import meg.biblio.inventory.db.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.security.SecureRandom;
import java.util.*;

@Component
@Configurable
public class InventoryDaoDataOnDemand {
    @Autowired
    InventoryRepository inventoryRepository;
    private Random rnd = new SecureRandom();
    private List<InventoryDao> data;

    public void setAddedtocount(InventoryDao obj, int index) {
        Integer addedtocount = new Integer(index);
        obj.setAddedtocount(addedtocount);
    }

    public InventoryDao getNewTransientInventoryDao(int index) {
        InventoryDao obj = new InventoryDao();
        setAddedtocount(obj, index);
        setClientid(obj, index);
        setCompleted(obj, index);
        setEnddate(obj, index);
        setReconciled(obj, index);
        setStartdate(obj, index);
        setTobecounted(obj, index);
        setTotalcounted(obj, index);
        return obj;
    }

    public void setClientid(InventoryDao obj, int index) {
        Long clientid = new Integer(index).longValue();
        obj.setClientid(clientid);
    }

    public void setCompleted(InventoryDao obj, int index) {
        Boolean completed = Boolean.TRUE;
        obj.setCompleted(completed);
    }

    public void setEnddate(InventoryDao obj, int index) {
        Date enddate = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setEnddate(enddate);
    }

    public void setReconciled(InventoryDao obj, int index) {
        Integer reconciled = new Integer(index);
        obj.setReconciled(reconciled);
    }

    public InventoryDao getSpecificInventoryDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        InventoryDao obj = data.get(index);
        Long id = obj.getId();
        return inventoryRepository.findOne(id);
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = inventoryRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'InventoryDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<InventoryDao>();
        for (int i = 0; i < 10; i++) {
            InventoryDao obj = getNewTransientInventoryDao(i);
            try {
                inventoryRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            inventoryRepository.flush();
            data.add(obj);
        }
    }

    public boolean modifyInventoryDao(InventoryDao obj) {
        return false;
    }

    public void setStartdate(InventoryDao obj, int index) {
        Date startdate = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setStartdate(startdate);
    }

    public void setTobecounted(InventoryDao obj, int index) {
        Integer tobecounted = new Integer(index);
        obj.setTobecounted(tobecounted);
    }

    public InventoryDao getRandomInventoryDao() {
        init();
        InventoryDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return inventoryRepository.findOne(id);
    }

    public void setTotalcounted(InventoryDao obj, int index) {
        Integer totalcounted = new Integer(index);
        obj.setTotalcounted(totalcounted);
    }
}
