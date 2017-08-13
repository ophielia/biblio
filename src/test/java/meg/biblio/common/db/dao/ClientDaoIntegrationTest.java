package meg.biblio.common.db.dao;

import meg.biblio.common.db.ClientRepository;
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
public class ClientDaoIntegrationTest {

    @Autowired
    ClientDaoDataOnDemand dod;
    @Autowired
    ClientRepository clientRepository;

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testDelete() {
        ClientDao obj = dod.getNewTransientClientDao(99);
        obj = clientRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'ClientDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ClientDao' failed to provide an identifier", id);
        obj = clientRepository.findOne(id);
        clientRepository.delete(obj);
        clientRepository.flush();
        Assert.assertNull("Failed to remove 'ClientDao' with identifier '" + id + "'", clientRepository.findOne(id));
    }

    @Test
    public void testFind() {
        ClientDao obj = dod.getRandomClientDao();
        Assert.assertNotNull("Data on demand for 'ClientDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ClientDao' failed to provide an identifier", id);
        obj = clientRepository.findOne(id);
        Assert.assertNotNull("Find method for 'ClientDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'ClientDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'ClientDao' failed to initialize correctly", dod.getRandomClientDao());
        long count = clientRepository.count();
        Assert.assertTrue("Counter for 'ClientDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFindAll() {
        Assert.assertNotNull("Data on demand for 'ClientDao' failed to initialize correctly", dod.getRandomClientDao());
        long count = clientRepository.count();
        Assert.assertTrue("Too expensive to perform a find all test for 'ClientDao', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<ClientDao> result = clientRepository.findAll();
        Assert.assertNotNull("Find all method for 'ClientDao' illegally returned null", result);
        Assert.assertTrue("Find all method for 'ClientDao' failed to return any data", result.size() > 0);
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'ClientDao' failed to initialize correctly", dod.getRandomClientDao());
        long count = clientRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<ClientDao> result = clientRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'ClientDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'ClientDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testFlush() {
        ClientDao obj = dod.getRandomClientDao();
        Assert.assertNotNull("Data on demand for 'ClientDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ClientDao' failed to provide an identifier", id);
        obj = clientRepository.findOne(id);
        Assert.assertNotNull("Find method for 'ClientDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifyClientDao(obj);
        Integer currentVersion = obj.getVersion();
        clientRepository.flush();
        Assert.assertTrue("Version for 'ClientDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSaveUpdate() {
        ClientDao obj = dod.getRandomClientDao();
        Assert.assertNotNull("Data on demand for 'ClientDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ClientDao' failed to provide an identifier", id);
        obj = clientRepository.findOne(id);
        boolean modified = dod.modifyClientDao(obj);
        Integer currentVersion = obj.getVersion();
        ClientDao merged = clientRepository.save(obj);
        clientRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'ClientDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'ClientDao' failed to initialize correctly", dod.getRandomClientDao());
        ClientDao obj = dod.getNewTransientClientDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'ClientDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'ClientDao' identifier to be null", obj.getId());
        try {
            clientRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        clientRepository.flush();
        Assert.assertNotNull("Expected 'ClientDao' identifier to no longer be null", obj.getId());
    }
}
