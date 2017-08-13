package meg.biblio.common.db.dao;

import meg.biblio.common.db.SelectValueRepository;
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
public class SelectValueDaoIntegrationTest {


    @Autowired
    SelectValueDaoDataOnDemand dod;
    @Autowired
    SelectValueRepository selectValueRepository;

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testDelete() {
        SelectValueDao obj = dod.getNewTransientSelectValueDao(99);
        selectValueRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'SelectValueDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SelectValueDao' failed to provide an identifier", id);
        obj = selectValueRepository.findOne(id);
        selectValueRepository.delete(obj);
        selectValueRepository.flush();
        Assert.assertNull("Failed to remove 'SelectValueDao' with identifier '" + id + "'", selectValueRepository.findOne(id));
    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'SelectValueDao' failed to initialize correctly", dod.getRandomSelectValueDao());
        long count = selectValueRepository.count();
        Assert.assertTrue("Counter for 'SelectValueDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFind() {
        SelectValueDao obj = dod.getRandomSelectValueDao();
        Assert.assertNotNull("Data on demand for 'SelectValueDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SelectValueDao' failed to provide an identifier", id);
        obj = selectValueRepository.findOne(id);
        Assert.assertNotNull("Find method for 'SelectValueDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'SelectValueDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindAll() {
        Assert.assertNotNull("Data on demand for 'SelectValueDao' failed to initialize correctly", dod.getRandomSelectValueDao());
        long count = selectValueRepository.count();
        Assert.assertTrue("Too expensive to perform a find all test for 'SelectValueDao', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<SelectValueDao> result = selectValueRepository.findAll();
        Assert.assertNotNull("Find all method for 'SelectValueDao' illegally returned null", result);
        Assert.assertTrue("Find all method for 'SelectValueDao' failed to return any data", result.size() > 0);
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'SelectValueDao' failed to initialize correctly", dod.getRandomSelectValueDao());
        long count = selectValueRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<SelectValueDao> result = selectValueRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'SelectValueDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'SelectValueDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testFlush() {
        SelectValueDao obj = dod.getRandomSelectValueDao();
        Assert.assertNotNull("Data on demand for 'SelectValueDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SelectValueDao' failed to provide an identifier", id);
        obj = selectValueRepository.findOne(id);
        Assert.assertNotNull("Find method for 'SelectValueDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifySelectValueDao(obj);
        Integer currentVersion = obj.getVersion();
        selectValueRepository.flush();
        Assert.assertTrue("Version for 'SelectValueDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSaveUpdate() {
        SelectValueDao obj = dod.getRandomSelectValueDao();
        Assert.assertNotNull("Data on demand for 'SelectValueDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SelectValueDao' failed to provide an identifier", id);
        obj = selectValueRepository.findOne(id);
        boolean modified = dod.modifySelectValueDao(obj);
        Integer currentVersion = obj.getVersion();
        SelectValueDao merged = selectValueRepository.save(obj);
        selectValueRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'SelectValueDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'SelectValueDao' failed to initialize correctly", dod.getRandomSelectValueDao());
        SelectValueDao obj = dod.getNewTransientSelectValueDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'SelectValueDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'SelectValueDao' identifier to be null", obj.getId());
        try {
            selectValueRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        selectValueRepository.flush();
        Assert.assertNotNull("Expected 'SelectValueDao' identifier to no longer be null", obj.getId());
    }
}
