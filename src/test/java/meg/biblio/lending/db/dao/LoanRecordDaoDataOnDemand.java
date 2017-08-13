package meg.biblio.lending.db.dao;

import meg.biblio.catalog.db.dao.BookDaoDataOnDemand;
import meg.biblio.common.db.dao.ClientDaoDataOnDemand;
import meg.biblio.lending.db.LoanRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.security.SecureRandom;
import java.util.*;

@Configurable
@Component
public class LoanRecordDaoDataOnDemand {
    @Autowired
    BookDaoDataOnDemand bookDaoDataOnDemand;
    @Autowired
    PersonDaoDataOnDemand personDaoDataOnDemand;
    @Autowired
    ClientDaoDataOnDemand clientDaoDataOnDemand;
    @Autowired
    LoanRecordRepository loanRecordRepository;
    private Random rnd = new SecureRandom();
    private List<LoanRecordDao> data;

    public void setCheckoutdate(LoanRecordDao obj, int index) {
        Date checkoutdate = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setCheckoutdate(checkoutdate);
    }

    public LoanRecordDao getNewTransientLoanRecordDao(int index) {
        LoanRecordDao obj = new LoanRecordDao();
        setBorrowersection(obj, index);
        setCheckoutdate(obj, index);
        setDuedate(obj, index);
        setReturned(obj, index);
        setSchoolyear(obj, index);
        setTeacherid(obj, index);
        return obj;
    }

    public void setBorrowersection(LoanRecordDao obj, int index) {
        Long borrowersection = new Integer(index).longValue();
        obj.setBorrowersection(borrowersection);
    }

    public void setDuedate(LoanRecordDao obj, int index) {
        Date duedate = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setDuedate(duedate);
    }

    public void setReturned(LoanRecordDao obj, int index) {
        Date returned = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setReturned(returned);
    }

    public void setSchoolyear(LoanRecordDao obj, int index) {
        Integer schoolyear = new Integer(index);
        obj.setSchoolyear(schoolyear);
    }

    public void setTeacherid(LoanRecordDao obj, int index) {
        Long teacherid = new Integer(index).longValue();
        obj.setTeacherid(teacherid);
    }

    public LoanRecordDao getSpecificLoanRecordDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        LoanRecordDao obj = data.get(index);
        Long id = obj.getId();
        return loanRecordRepository.findOne(id);
    }

    public LoanRecordDao getRandomLoanRecordDao() {
        init();
        LoanRecordDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return loanRecordRepository.findOne(id);
    }

    public boolean modifyLoanRecordDao(LoanRecordDao obj) {
        return false;
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = loanRecordRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'LoanRecordDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<LoanRecordDao>();
        for (int i = 0; i < 10; i++) {
            LoanRecordDao obj = getNewTransientLoanRecordDao(i);
            try {
                loanRecordRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            loanRecordRepository.flush();
            data.add(obj);
        }
    }
}
