package meg.biblio.common.db.dao;

import meg.biblio.common.db.ImportBookRepository;
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
public class ImportBookDaoIntegrationTest {

    @Autowired
    ImportBookDaoDataOnDemand dod;
    @Autowired
    ImportBookRepository importBookRepository;

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testFindAll() {
        Assert.assertNotNull("Data on demand for 'ImportBookDao' failed to initialize correctly", dod.getRandomImportBookDao());
        long count = importBookRepository.count();
        Assert.assertTrue("Too expensive to perform a find all test for 'ImportBookDao', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<ImportBookDao> result = importBookRepository.findAll();
        Assert.assertNotNull("Find all method for 'ImportBookDao' illegally returned null", result);
        Assert.assertTrue("Find all method for 'ImportBookDao' failed to return any data", result.size() > 0);
    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'ImportBookDao' failed to initialize correctly", dod.getRandomImportBookDao());
        long count = importBookRepository.count();
        Assert.assertTrue("Counter for 'ImportBookDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFind() {
        ImportBookDao obj = dod.getRandomImportBookDao();
        Assert.assertNotNull("Data on demand for 'ImportBookDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ImportBookDao' failed to provide an identifier", id);
        obj = importBookRepository.findOne(id);
        Assert.assertNotNull("Find method for 'ImportBookDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'ImportBookDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'ImportBookDao' failed to initialize correctly", dod.getRandomImportBookDao());
        long count = importBookRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<ImportBookDao> result = importBookRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'ImportBookDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'ImportBookDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testFlush() {
        ImportBookDao obj = dod.getRandomImportBookDao();
        Assert.assertNotNull("Data on demand for 'ImportBookDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ImportBookDao' failed to provide an identifier", id);
        obj = importBookRepository.findOne(id);
        Assert.assertNotNull("Find method for 'ImportBookDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifyImportBookDao(obj);
        Integer currentVersion = obj.getVersion();
        importBookRepository.flush();
        Assert.assertTrue("Version for 'ImportBookDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSaveUpdate() {
        ImportBookDao obj = dod.getRandomImportBookDao();
        Assert.assertNotNull("Data on demand for 'ImportBookDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ImportBookDao' failed to provide an identifier", id);
        obj = importBookRepository.findOne(id);
        boolean modified = dod.modifyImportBookDao(obj);
        Integer currentVersion = obj.getVersion();
        ImportBookDao merged = importBookRepository.save(obj);
        importBookRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'ImportBookDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'ImportBookDao' failed to initialize correctly", dod.getRandomImportBookDao());
        ImportBookDao obj = dod.getNewTransientImportBookDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'ImportBookDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'ImportBookDao' identifier to be null", obj.getId());
        try {
            importBookRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        importBookRepository.flush();
        Assert.assertNotNull("Expected 'ImportBookDao' identifier to no longer be null", obj.getId());
    }

    @Test
    public void testDelete() {
        ImportBookDao obj = dod.getRandomImportBookDao();
        Assert.assertNotNull("Data on demand for 'ImportBookDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ImportBookDao' failed to provide an identifier", id);
        obj = importBookRepository.findOne(id);
        importBookRepository.delete(obj);
        importBookRepository.flush();
        Assert.assertNull("Failed to remove 'ImportBookDao' with identifier '" + id + "'", importBookRepository.findOne(id));
    }
}
