package meg.biblio.catalog.db;

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
public class IgnoredWordsDaoIntegrationTest {

    @Autowired
    IgnoredWordsDaoDataOnDemand dod;
    @Autowired
    IgnoredWordsRepository ignoredWordsRepository;

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'IgnoredWordsDao' failed to initialize correctly", dod.getRandomIgnoredWordsDao());
        long count = ignoredWordsRepository.count();
        Assert.assertTrue("Counter for 'IgnoredWordsDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFind() {
        IgnoredWordsDao obj = dod.getRandomIgnoredWordsDao();
        Assert.assertNotNull("Data on demand for 'IgnoredWordsDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'IgnoredWordsDao' failed to provide an identifier", id);
        obj = ignoredWordsRepository.findOne(id);
        Assert.assertNotNull("Find method for 'IgnoredWordsDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'IgnoredWordsDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindAll() {
        Assert.assertNotNull("Data on demand for 'IgnoredWordsDao' failed to initialize correctly", dod.getRandomIgnoredWordsDao());
        long count = ignoredWordsRepository.count();
        Assert.assertTrue("Too expensive to perform a find all test for 'IgnoredWordsDao', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<IgnoredWordsDao> result = ignoredWordsRepository.findAll();
        Assert.assertNotNull("Find all method for 'IgnoredWordsDao' illegally returned null", result);
        Assert.assertTrue("Find all method for 'IgnoredWordsDao' failed to return any data", result.size() > 0);
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'IgnoredWordsDao' failed to initialize correctly", dod.getRandomIgnoredWordsDao());
        long count = ignoredWordsRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<IgnoredWordsDao> result = ignoredWordsRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'IgnoredWordsDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'IgnoredWordsDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testFlush() {
        IgnoredWordsDao obj = dod.getRandomIgnoredWordsDao();
        Assert.assertNotNull("Data on demand for 'IgnoredWordsDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'IgnoredWordsDao' failed to provide an identifier", id);
        obj = ignoredWordsRepository.findOne(id);
        Assert.assertNotNull("Find method for 'IgnoredWordsDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifyIgnoredWordsDao(obj);
        Integer currentVersion = obj.getVersion();
        ignoredWordsRepository.flush();
        Assert.assertTrue("Version for 'IgnoredWordsDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSaveUpdate() {
        IgnoredWordsDao obj = dod.getRandomIgnoredWordsDao();
        Assert.assertNotNull("Data on demand for 'IgnoredWordsDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'IgnoredWordsDao' failed to provide an identifier", id);
        obj = ignoredWordsRepository.findOne(id);
        boolean modified = dod.modifyIgnoredWordsDao(obj);
        Integer currentVersion = obj.getVersion();
        IgnoredWordsDao merged = ignoredWordsRepository.save(obj);
        ignoredWordsRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'IgnoredWordsDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'IgnoredWordsDao' failed to initialize correctly", dod.getRandomIgnoredWordsDao());
        IgnoredWordsDao obj = dod.getNewTransientIgnoredWordsDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'IgnoredWordsDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'IgnoredWordsDao' identifier to be null", obj.getId());
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
        Assert.assertNotNull("Expected 'IgnoredWordsDao' identifier to no longer be null", obj.getId());
    }

    @Test
    public void testDelete() {
        IgnoredWordsDao obj = dod.getRandomIgnoredWordsDao();
        Assert.assertNotNull("Data on demand for 'IgnoredWordsDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'IgnoredWordsDao' failed to provide an identifier", id);
        obj = ignoredWordsRepository.findOne(id);
        ignoredWordsRepository.delete(obj);
        ignoredWordsRepository.flush();
        Assert.assertNull("Failed to remove 'IgnoredWordsDao' with identifier '" + id + "'", ignoredWordsRepository.findOne(id));
    }
}
