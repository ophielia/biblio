package meg.biblio.inventory.db.dao;

import meg.biblio.inventory.db.InventoryHistRepository;
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
public class InventoryHistoryDaoIntegrationTest {

    @Autowired
    InventoryHistoryDaoDataOnDemand dod;
    @Autowired
    InventoryHistRepository inventoryHistRepository;

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'InventoryHistoryDao' failed to initialize correctly", dod.getRandomInventoryHistoryDao());
        long count = inventoryHistRepository.count();
        Assert.assertTrue("Counter for 'InventoryHistoryDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFind() {
        InventoryHistoryDao obj = dod.getRandomInventoryHistoryDao();
        Assert.assertNotNull("Data on demand for 'InventoryHistoryDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'InventoryHistoryDao' failed to provide an identifier", id);
        obj = inventoryHistRepository.findOne(id);
        Assert.assertNotNull("Find method for 'InventoryHistoryDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'InventoryHistoryDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindAll() {
        Assert.assertNotNull("Data on demand for 'InventoryHistoryDao' failed to initialize correctly", dod.getRandomInventoryHistoryDao());
        long count = inventoryHistRepository.count();
        Assert.assertTrue("Too expensive to perform a find all test for 'InventoryHistoryDao', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<InventoryHistoryDao> result = inventoryHistRepository.findAll();
        Assert.assertNotNull("Find all method for 'InventoryHistoryDao' illegally returned null", result);
        Assert.assertTrue("Find all method for 'InventoryHistoryDao' failed to return any data", result.size() > 0);
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'InventoryHistoryDao' failed to initialize correctly", dod.getRandomInventoryHistoryDao());
        long count = inventoryHistRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<InventoryHistoryDao> result = inventoryHistRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'InventoryHistoryDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'InventoryHistoryDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testFlush() {
        InventoryHistoryDao obj = dod.getRandomInventoryHistoryDao();
        Assert.assertNotNull("Data on demand for 'InventoryHistoryDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'InventoryHistoryDao' failed to provide an identifier", id);
        obj = inventoryHistRepository.findOne(id);
        Assert.assertNotNull("Find method for 'InventoryHistoryDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifyInventoryHistoryDao(obj);
        Integer currentVersion = obj.getVersion();
        inventoryHistRepository.flush();
        Assert.assertTrue("Version for 'InventoryHistoryDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSaveUpdate() {
        InventoryHistoryDao obj = dod.getRandomInventoryHistoryDao();
        Assert.assertNotNull("Data on demand for 'InventoryHistoryDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'InventoryHistoryDao' failed to provide an identifier", id);
        obj = inventoryHistRepository.findOne(id);
        boolean modified = dod.modifyInventoryHistoryDao(obj);
        Integer currentVersion = obj.getVersion();
        InventoryHistoryDao merged = inventoryHistRepository.save(obj);
        inventoryHistRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'InventoryHistoryDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'InventoryHistoryDao' failed to initialize correctly", dod.getRandomInventoryHistoryDao());
        InventoryHistoryDao obj = dod.getNewTransientInventoryHistoryDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'InventoryHistoryDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'InventoryHistoryDao' identifier to be null", obj.getId());
        try {
            inventoryHistRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        inventoryHistRepository.flush();
        Assert.assertNotNull("Expected 'InventoryHistoryDao' identifier to no longer be null", obj.getId());
    }

    @Test
    public void testDelete() {
        InventoryHistoryDao obj = dod.getRandomInventoryHistoryDao();
        Assert.assertNotNull("Data on demand for 'InventoryHistoryDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'InventoryHistoryDao' failed to provide an identifier", id);
        obj = inventoryHistRepository.findOne(id);
        inventoryHistRepository.delete(obj);
        inventoryHistRepository.flush();
        Assert.assertNull("Failed to remove 'InventoryHistoryDao' with identifier '" + id + "'", inventoryHistRepository.findOne(id));
    }
}
