package meg.biblio.lending.db.dao;

import meg.biblio.common.db.dao.ClientDaoDataOnDemand;
import meg.biblio.lending.db.PersonRepository;
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
public class PersonDaoDataOnDemand {
    @Autowired
    ClientDaoDataOnDemand clientDaoDataOnDemand;
    @Autowired
    SchoolGroupDaoDataOnDemand schoolGroupDaoDataOnDemand;
    @Autowired
    PersonRepository personRepository;
    private Random rnd = new SecureRandom();
    private List<PersonDao> data;

    public PersonDao getRandomPersonDao() {
        init();
        PersonDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return personRepository.findOne(id);
    }

    public PersonDao getNewTransientPersonDao(int index) {
        PersonDao obj = new PersonDao();
        setActive(obj, index);
        setBarcodeid(obj, index);
        setFirstname(obj, index);
        setLastname(obj, index);
        setPsn_type(obj, index);
        return obj;
    }

    public void setActive(PersonDao obj, int index) {
        Boolean active = Boolean.TRUE;
        obj.setActive(active);
    }

    public void setBarcodeid(PersonDao obj, int index) {
        String barcodeid = "barcodeid_" + index;
        obj.setBarcodeid(barcodeid);
    }

    public void setFirstname(PersonDao obj, int index) {
        String firstname = "firstname_" + index;
        obj.setFirstname(firstname);
    }

    public void setLastname(PersonDao obj, int index) {
        String lastname = "lastname_" + index;
        obj.setLastname(lastname);
    }

    public void setPsn_type(PersonDao obj, int index) {
        String psn_type = "psn_type_" + index;
        obj.setPsn_type(psn_type);
    }

    public PersonDao getSpecificPersonDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        PersonDao obj = data.get(index);
        Long id = obj.getId();
        return personRepository.findOne(id);
    }

    public boolean modifyPersonDao(PersonDao obj) {
        return false;
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = personRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'PersonDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<PersonDao>();
        for (int i = 0; i < 10; i++) {
            PersonDao obj = getNewTransientPersonDao(i);
            try {
                personRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            personRepository.flush();
            data.add(obj);
        }
    }
}
