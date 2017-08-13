package meg.biblio.catalog.db.dao;

import meg.biblio.catalog.db.FoundDetailsRepository;
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
public class FoundDetailsDaoIntegrationTest {

    @Autowired
    FoundDetailsDaoDataOnDemand dod;
    @Autowired
    FoundDetailsRepository foundDetailsRepository;

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'FoundDetailsDao' failed to initialize correctly", dod.getRandomFoundDetailsDao());
        long count = foundDetailsRepository.count();
        Assert.assertTrue("Counter for 'FoundDetailsDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFind() {
        FoundDetailsDao obj = dod.getRandomFoundDetailsDao();
        Assert.assertNotNull("Data on demand for 'FoundDetailsDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'FoundDetailsDao' failed to provide an identifier", id);
        obj = foundDetailsRepository.findOne(id);
        Assert.assertNotNull("Find method for 'FoundDetailsDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'FoundDetailsDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindAll() {
        Assert.assertNotNull("Data on demand for 'FoundDetailsDao' failed to initialize correctly", dod.getRandomFoundDetailsDao());
        long count = foundDetailsRepository.count();
        Assert.assertTrue("Too expensive to perform a find all test for 'FoundDetailsDao', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<FoundDetailsDao> result = foundDetailsRepository.findAll();
        Assert.assertNotNull("Find all method for 'FoundDetailsDao' illegally returned null", result);
        Assert.assertTrue("Find all method for 'FoundDetailsDao' failed to return any data", result.size() > 0);
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'FoundDetailsDao' failed to initialize correctly", dod.getRandomFoundDetailsDao());
        long count = foundDetailsRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<FoundDetailsDao> result = foundDetailsRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'FoundDetailsDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'FoundDetailsDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testFlush() {
        FoundDetailsDao obj = dod.getRandomFoundDetailsDao();
        Assert.assertNotNull("Data on demand for 'FoundDetailsDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'FoundDetailsDao' failed to provide an identifier", id);
        obj = foundDetailsRepository.findOne(id);
        Assert.assertNotNull("Find method for 'FoundDetailsDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifyFoundDetailsDao(obj);
        Integer currentVersion = obj.getVersion();
        foundDetailsRepository.flush();
        Assert.assertTrue("Version for 'FoundDetailsDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSaveUpdate() {
        FoundDetailsDao obj = dod.getRandomFoundDetailsDao();
        Assert.assertNotNull("Data on demand for 'FoundDetailsDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'FoundDetailsDao' failed to provide an identifier", id);
        obj = foundDetailsRepository.findOne(id);
        boolean modified = dod.modifyFoundDetailsDao(obj);
        Integer currentVersion = obj.getVersion();
        FoundDetailsDao merged = foundDetailsRepository.save(obj);
        foundDetailsRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'FoundDetailsDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'FoundDetailsDao' failed to initialize correctly", dod.getRandomFoundDetailsDao());
        FoundDetailsDao obj = dod.getNewTransientFoundDetailsDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'FoundDetailsDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'FoundDetailsDao' identifier to be null", obj.getId());
        try {
            foundDetailsRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        foundDetailsRepository.flush();
        Assert.assertNotNull("Expected 'FoundDetailsDao' identifier to no longer be null", obj.getId());
    }

    @Test
    public void testDelete() {
        FoundDetailsDao obj = dod.getRandomFoundDetailsDao();
        Assert.assertNotNull("Data on demand for 'FoundDetailsDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'FoundDetailsDao' failed to provide an identifier", id);
        obj = foundDetailsRepository.findOne(id);
        foundDetailsRepository.delete(obj);
        foundDetailsRepository.flush();
        Assert.assertNull("Failed to remove 'FoundDetailsDao' with identifier '" + id + "'", foundDetailsRepository.findOne(id));
    }
}
