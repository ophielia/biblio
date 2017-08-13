package meg.biblio.inventory.db.dao;

import meg.biblio.inventory.db.InventoryRepository;
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
public class InventoryDaoIntegrationTest {

    @Autowired
    InventoryDaoDataOnDemand dod;
    @Autowired
    InventoryRepository inventoryRepository;

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'InventoryDao' failed to initialize correctly", dod.getRandomInventoryDao());
        long count = inventoryRepository.count();
        Assert.assertTrue("Counter for 'InventoryDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFind() {
        InventoryDao obj = dod.getRandomInventoryDao();
        Assert.assertNotNull("Data on demand for 'InventoryDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'InventoryDao' failed to provide an identifier", id);
        obj = inventoryRepository.findOne(id);
        Assert.assertNotNull("Find method for 'InventoryDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'InventoryDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindAll() {
        Assert.assertNotNull("Data on demand for 'InventoryDao' failed to initialize correctly", dod.getRandomInventoryDao());
        long count = inventoryRepository.count();
        Assert.assertTrue("Too expensive to perform a find all test for 'InventoryDao', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<InventoryDao> result = inventoryRepository.findAll();
        Assert.assertNotNull("Find all method for 'InventoryDao' illegally returned null", result);
        Assert.assertTrue("Find all method for 'InventoryDao' failed to return any data", result.size() > 0);
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'InventoryDao' failed to initialize correctly", dod.getRandomInventoryDao());
        long count = inventoryRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<InventoryDao> result = inventoryRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'InventoryDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'InventoryDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testFlush() {
        InventoryDao obj = dod.getRandomInventoryDao();
        Assert.assertNotNull("Data on demand for 'InventoryDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'InventoryDao' failed to provide an identifier", id);
        obj = inventoryRepository.findOne(id);
        Assert.assertNotNull("Find method for 'InventoryDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifyInventoryDao(obj);
        Integer currentVersion = obj.getVersion();
        inventoryRepository.flush();
        Assert.assertTrue("Version for 'InventoryDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSaveUpdate() {
        InventoryDao obj = dod.getRandomInventoryDao();
        Assert.assertNotNull("Data on demand for 'InventoryDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'InventoryDao' failed to provide an identifier", id);
        obj = inventoryRepository.findOne(id);
        boolean modified = dod.modifyInventoryDao(obj);
        Integer currentVersion = obj.getVersion();
        InventoryDao merged = inventoryRepository.save(obj);
        inventoryRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'InventoryDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'InventoryDao' failed to initialize correctly", dod.getRandomInventoryDao());
        InventoryDao obj = dod.getNewTransientInventoryDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'InventoryDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'InventoryDao' identifier to be null", obj.getId());
        try {
            inventoryRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        inventoryRepository.flush();
        Assert.assertNotNull("Expected 'InventoryDao' identifier to no longer be null", obj.getId());
    }

    @Test
    public void testDelete() {
        InventoryDao obj = dod.getRandomInventoryDao();
        Assert.assertNotNull("Data on demand for 'InventoryDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'InventoryDao' failed to provide an identifier", id);
        obj = inventoryRepository.findOne(id);
        inventoryRepository.delete(obj);
        inventoryRepository.flush();
        Assert.assertNull("Failed to remove 'InventoryDao' with identifier '" + id + "'", inventoryRepository.findOne(id));
    }
}
