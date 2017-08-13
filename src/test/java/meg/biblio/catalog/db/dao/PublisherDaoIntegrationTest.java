package meg.biblio.catalog.db.dao;

import meg.biblio.catalog.db.PublisherRepository;
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
public class PublisherDaoIntegrationTest {

    @Autowired
    PublisherDaoDataOnDemand dod;
    @Autowired
    PublisherRepository publisherRepository;

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testDelete() {
        PublisherDao obj = dod.getNewTransientPublisherDao(99);
        obj = publisherRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'PublisherDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PublisherDao' failed to provide an identifier", id);
        obj = publisherRepository.findOne(id);
        publisherRepository.delete(obj);
        publisherRepository.flush();
        Assert.assertNull("Failed to remove 'PublisherDao' with identifier '" + id + "'", publisherRepository.findOne(id));
    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'PublisherDao' failed to initialize correctly", dod.getRandomPublisherDao());
        long count = publisherRepository.count();
        Assert.assertTrue("Counter for 'PublisherDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFind() {
        PublisherDao obj = dod.getRandomPublisherDao();
        Assert.assertNotNull("Data on demand for 'PublisherDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PublisherDao' failed to provide an identifier", id);
        obj = publisherRepository.findOne(id);
        Assert.assertNotNull("Find method for 'PublisherDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'PublisherDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'PublisherDao' failed to initialize correctly", dod.getRandomPublisherDao());
        long count = publisherRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<PublisherDao> result = publisherRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'PublisherDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'PublisherDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testFlush() {
        PublisherDao obj = dod.getRandomPublisherDao();
        Assert.assertNotNull("Data on demand for 'PublisherDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PublisherDao' failed to provide an identifier", id);
        obj = publisherRepository.findOne(id);
        Assert.assertNotNull("Find method for 'PublisherDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifyPublisherDao(obj);
        Integer currentVersion = obj.getVersion();
        publisherRepository.flush();
        Assert.assertTrue("Version for 'PublisherDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSaveUpdate() {
        PublisherDao obj = dod.getRandomPublisherDao();
        Assert.assertNotNull("Data on demand for 'PublisherDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PublisherDao' failed to provide an identifier", id);
        obj = publisherRepository.findOne(id);
        boolean modified = dod.modifyPublisherDao(obj);
        Integer currentVersion = obj.getVersion();
        PublisherDao merged = publisherRepository.save(obj);
        publisherRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'PublisherDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'PublisherDao' failed to initialize correctly", dod.getRandomPublisherDao());
        PublisherDao obj = dod.getNewTransientPublisherDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'PublisherDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'PublisherDao' identifier to be null", obj.getId());
        try {
            publisherRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        publisherRepository.flush();
        Assert.assertNotNull("Expected 'PublisherDao' identifier to no longer be null", obj.getId());
    }
}
