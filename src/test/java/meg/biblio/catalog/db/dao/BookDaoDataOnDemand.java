package meg.biblio.catalog.db.dao;

import meg.biblio.catalog.db.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.security.SecureRandom;
import java.util.*;

@Configurable
@Component
public class BookDaoDataOnDemand {
    @Autowired
    BookDetailDaoDataOnDemand bookDetailDaoDataOnDemand;
    @Autowired
    BookRepository bookRepository;
    private Random rnd = new SecureRandom();
    private List<BookDao> data;

    public void setClientbookid(BookDao obj, int index) {
        String clientbookid = "clientbookid_" + index;
        obj.setClientbookid(clientbookid);
    }

    public BookDao getNewTransientBookDao(int index) {
        BookDao obj = new BookDao();
        setBarcodeid(obj, index);
        setClientbookid(obj, index);
        setClientbookidsort(obj, index);
        setClientbooktype(obj, index);
        setClientid(obj, index);
        setClientshelfclass(obj, index);
        setClientshelfcode(obj, index);
        setCounteddate(obj, index);
        setCountstatus(obj, index);
        setCreatedon(obj, index);
        setNote(obj, index);
        setStatus(obj, index);
        setTocount(obj, index);
        setUserid(obj, index);
        return obj;
    }

    public void setStatus(BookDao obj, int index) {
        Long status = new Integer(index).longValue();
        obj.setStatus(status);
    }

    public void setBarcodeid(BookDao obj, int index) {
        String barcodeid = "barcodeid_" + index;
        obj.setBarcodeid(barcodeid);
    }

    public void setClientbookidsort(BookDao obj, int index) {
        Long clientbookidsort = new Integer(index).longValue();
        obj.setClientbookidsort(clientbookidsort);
    }

    public void setCountstatus(BookDao obj, int index) {
        Long countstatus = new Integer(index).longValue();
        obj.setCountstatus(countstatus);
    }

    public boolean modifyBookDao(BookDao obj) {
        return false;
    }

    public void setClientbooktype(BookDao obj, int index) {
        Long clientbooktype = new Integer(index).longValue();
        obj.setClientbooktype(clientbooktype);
    }

    public void setUserid(BookDao obj, int index) {
        Long userid = new Integer(index).longValue();
        obj.setUserid(userid);
    }

    public BookDao getRandomBookDao() {
        init();
        BookDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return bookRepository.findOne(id);
    }

    public void setCreatedon(BookDao obj, int index) {
        Date createdon = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setCreatedon(createdon);
    }

    public void setTocount(BookDao obj, int index) {
        Boolean tocount = Boolean.TRUE;
        obj.setTocount(tocount);
    }

    public void setClientid(BookDao obj, int index) {
        Long clientid = new Integer(index).longValue();
        obj.setClientid(clientid);
    }

    public void setClientshelfclass(BookDao obj, int index) {
        String clientshelfclass = "clientshelfclass_" + index;
        obj.setClientshelfclass(clientshelfclass);
    }

    public void setClientshelfcode(BookDao obj, int index) {
        Long clientshelfcode = new Integer(index).longValue();
        obj.setClientshelfcode(clientshelfcode);
    }

    public BookDao getSpecificBookDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        BookDao obj = data.get(index);
        Long id = obj.getId();
        return bookRepository.findOne(id);
    }

    public void setCounteddate(BookDao obj, int index) {
        Date counteddate = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setCounteddate(counteddate);
    }

    public void setNote(BookDao obj, int index) {
        String note = "note_" + index;
        obj.setNote(note);
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = bookRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'BookDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<BookDao>();
        for (int i = 0; i < 10; i++) {
            BookDao obj = getNewTransientBookDao(i);
            try {
                bookRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            bookRepository.flush();
            data.add(obj);
        }
    }
}
