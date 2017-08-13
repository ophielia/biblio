package meg.biblio.lending.db.dao;

import meg.biblio.lending.db.PersonRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Iterator;
import java.util.List;

@Transactional
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Configurable
public class PersonDaoIntegrationTest {

    @Autowired
    PersonDaoDataOnDemand dod;
    @Autowired
    PersonRepository personRepository;

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testDelete() {
        PersonDao obj = dod.getNewTransientPersonDao(99);
        obj = personRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'PersonDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PersonDao' failed to provide an identifier", id);
        obj = personRepository.findOne(id);
        personRepository.delete(obj);
        personRepository.flush();
        Assert.assertNull("Failed to remove 'PersonDao' with identifier '" + id + "'", personRepository.findOne(id));
    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'PersonDao' failed to initialize correctly", dod.getRandomPersonDao());
        long count = personRepository.count();
        Assert.assertTrue("Counter for 'PersonDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFind() {
        PersonDao obj = dod.getRandomPersonDao();
        Assert.assertNotNull("Data on demand for 'PersonDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PersonDao' failed to provide an identifier", id);
        obj = personRepository.findOne(id);
        Assert.assertNotNull("Find method for 'PersonDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'PersonDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindAll() {
        Assert.assertNotNull("Data on demand for 'PersonDao' failed to initialize correctly", dod.getRandomPersonDao());
        long count = personRepository.count();
        Assert.assertTrue("Too expensive to perform a find all test for 'PersonDao', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<PersonDao> result = personRepository.findAll();
        Assert.assertNotNull("Find all method for 'PersonDao' illegally returned null", result);
        Assert.assertTrue("Find all method for 'PersonDao' failed to return any data", result.size() > 0);
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'PersonDao' failed to initialize correctly", dod.getRandomPersonDao());
        long count = personRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<PersonDao> result = personRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'PersonDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'PersonDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testFlush() {
        PersonDao obj = dod.getRandomPersonDao();
        Assert.assertNotNull("Data on demand for 'PersonDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PersonDao' failed to provide an identifier", id);
        obj = personRepository.findOne(id);
        Assert.assertNotNull("Find method for 'PersonDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifyPersonDao(obj);
        Integer currentVersion = obj.getVersion();
        personRepository.flush();
        Assert.assertTrue("Version for 'PersonDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSaveUpdate() {
        PersonDao obj = dod.getRandomPersonDao();
        Assert.assertNotNull("Data on demand for 'PersonDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PersonDao' failed to provide an identifier", id);
        obj = personRepository.findOne(id);
        boolean modified = dod.modifyPersonDao(obj);
        Integer currentVersion = obj.getVersion();
        PersonDao merged = personRepository.save(obj);
        personRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'PersonDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'PersonDao' failed to initialize correctly", dod.getRandomPersonDao());
        PersonDao obj = dod.getNewTransientPersonDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'PersonDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'PersonDao' identifier to be null", obj.getId());
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
        Assert.assertNotNull("Expected 'PersonDao' identifier to no longer be null", obj.getId());
    }
}
