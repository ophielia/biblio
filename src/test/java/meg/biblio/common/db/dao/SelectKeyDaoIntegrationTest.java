package meg.biblio.common.db.dao;

import meg.biblio.common.db.SelectKeyRepository;
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

@Configurable
@Transactional
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class SelectKeyDaoIntegrationTest {

    @Autowired
    SelectKeyDaoDataOnDemand dod;
    @Autowired
    SelectKeyRepository selectKeyRepository;

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testFindAll() {
        Assert.assertNotNull("Data on demand for 'SelectKeyDao' failed to initialize correctly", dod.getRandomSelectKeyDao());
        long count = selectKeyRepository.count();
        Assert.assertTrue("Too expensive to perform a find all test for 'SelectKeyDao', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<SelectKeyDao> result = selectKeyRepository.findAll();
        Assert.assertNotNull("Find all method for 'SelectKeyDao' illegally returned null", result);
        Assert.assertTrue("Find all method for 'SelectKeyDao' failed to return any data", result.size() > 0);
    }

    @Test
    public void testFind() {
        SelectKeyDao obj = dod.getRandomSelectKeyDao();
        Assert.assertNotNull("Data on demand for 'SelectKeyDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SelectKeyDao' failed to provide an identifier", id);
        obj = selectKeyRepository.findOne(id);
        Assert.assertNotNull("Find method for 'SelectKeyDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'SelectKeyDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'SelectKeyDao' failed to initialize correctly", dod.getRandomSelectKeyDao());
        long count = selectKeyRepository.count();
        Assert.assertTrue("Counter for 'SelectKeyDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'SelectKeyDao' failed to initialize correctly", dod.getRandomSelectKeyDao());
        long count = selectKeyRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<SelectKeyDao> result = selectKeyRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'SelectKeyDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'SelectKeyDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testFlush() {
        SelectKeyDao obj = dod.getRandomSelectKeyDao();
        Assert.assertNotNull("Data on demand for 'SelectKeyDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SelectKeyDao' failed to provide an identifier", id);
        obj = selectKeyRepository.findOne(id);
        Assert.assertNotNull("Find method for 'SelectKeyDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifySelectKeyDao(obj);
        Integer currentVersion = obj.getVersion();
        selectKeyRepository.flush();
        Assert.assertTrue("Version for 'SelectKeyDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSaveUpdate() {
        SelectKeyDao obj = dod.getRandomSelectKeyDao();
        Assert.assertNotNull("Data on demand for 'SelectKeyDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SelectKeyDao' failed to provide an identifier", id);
        obj = selectKeyRepository.findOne(id);
        boolean modified = dod.modifySelectKeyDao(obj);
        Integer currentVersion = obj.getVersion();
        SelectKeyDao merged = selectKeyRepository.save(obj);
        selectKeyRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'SelectKeyDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'SelectKeyDao' failed to initialize correctly", dod.getRandomSelectKeyDao());
        SelectKeyDao obj = dod.getNewTransientSelectKeyDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'SelectKeyDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'SelectKeyDao' identifier to be null", obj.getId());
        try {
            selectKeyRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        selectKeyRepository.flush();
        Assert.assertNotNull("Expected 'SelectKeyDao' identifier to no longer be null", obj.getId());
    }

    @Test
    public void testDelete() {
        SelectKeyDao obj = dod.getRandomSelectKeyDao();
        Assert.assertNotNull("Data on demand for 'SelectKeyDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SelectKeyDao' failed to provide an identifier", id);
        obj = selectKeyRepository.findOne(id);
        selectKeyRepository.delete(obj);
        selectKeyRepository.flush();
        Assert.assertNull("Failed to remove 'SelectKeyDao' with identifier '" + id + "'", selectKeyRepository.findOne(id));
    }
}
