package meg.biblio.catalog.db.dao;

import meg.biblio.catalog.db.ArtistRepository;
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
public class ArtistDaoDataOnDemand {
    @Autowired
    ArtistRepository artistRepository;
    private Random rnd = new SecureRandom();
    private List<ArtistDao> data;

    public ArtistDao getNewTransientArtistDao(int index) {
        ArtistDao obj = new ArtistDao();
        setFirstname(obj, index);
        setLastname(obj, index);
        setMiddlename(obj, index);
        return obj;
    }

    public void setFirstname(ArtistDao obj, int index) {
        String firstname = "firstname_" + index;
        obj.setFirstname(firstname);
    }

    public void setLastname(ArtistDao obj, int index) {
        String lastname = "lastname_" + index;
        obj.setLastname(lastname);
    }

    public void setMiddlename(ArtistDao obj, int index) {
        String middlename = "middlename_" + index;
        obj.setMiddlename(middlename);
    }

    public ArtistDao getSpecificArtistDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        ArtistDao obj = data.get(index);
        Long id = obj.getId();
        return artistRepository.findOne(id);
    }

    public ArtistDao getRandomArtistDao() {
        init();
        ArtistDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return artistRepository.findOne(id);
    }

    public boolean modifyArtistDao(ArtistDao obj) {
        return false;
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = artistRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'ArtistDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<ArtistDao>();
        for (int i = 0; i < 10; i++) {
            ArtistDao obj = getNewTransientArtistDao(i);
            try {
                artistRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            artistRepository.flush();
            data.add(obj);
        }
    }
}
