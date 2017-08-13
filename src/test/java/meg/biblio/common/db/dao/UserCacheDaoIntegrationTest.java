package meg.biblio.common.db.dao;

import meg.biblio.common.db.UserCacheRepository;
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
public class UserCacheDaoIntegrationTest {

    @Autowired
    UserCacheDaoDataOnDemand dod;
    @Autowired
    UserCacheRepository userCacheRepository;

    @Test
    public void testMarkerMethod() {

    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'UserCacheDao' failed to initialize correctly", dod.getRandomUserCacheDao());
        long count = userCacheRepository.count();
        Assert.assertTrue("Counter for 'UserCacheDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFind() {
        UserCacheDao obj = dod.getRandomUserCacheDao();
        Assert.assertNotNull("Data on demand for 'UserCacheDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'UserCacheDao' failed to provide an identifier", id);
        obj = userCacheRepository.findOne(id);
        Assert.assertNotNull("Find method for 'UserCacheDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'UserCacheDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindAll() {
        Assert.assertNotNull("Data on demand for 'UserCacheDao' failed to initialize correctly", dod.getRandomUserCacheDao());
        long count = userCacheRepository.count();
        Assert.assertTrue("Too expensive to perform a find all test for 'UserCacheDao', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<UserCacheDao> result = userCacheRepository.findAll();
        Assert.assertNotNull("Find all method for 'UserCacheDao' illegally returned null", result);
        Assert.assertTrue("Find all method for 'UserCacheDao' failed to return any data", result.size() > 0);
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'UserCacheDao' failed to initialize correctly", dod.getRandomUserCacheDao());
        long count = userCacheRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<UserCacheDao> result = userCacheRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'UserCacheDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'UserCacheDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testFlush() {
        UserCacheDao obj = dod.getRandomUserCacheDao();
        Assert.assertNotNull("Data on demand for 'UserCacheDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'UserCacheDao' failed to provide an identifier", id);
        obj = userCacheRepository.findOne(id);
        Assert.assertNotNull("Find method for 'UserCacheDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifyUserCacheDao(obj);
        Integer currentVersion = obj.getVersion();
        userCacheRepository.flush();
        Assert.assertTrue("Version for 'UserCacheDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSaveUpdate() {
        UserCacheDao obj = dod.getRandomUserCacheDao();
        Assert.assertNotNull("Data on demand for 'UserCacheDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'UserCacheDao' failed to provide an identifier", id);
        obj = userCacheRepository.findOne(id);
        boolean modified = dod.modifyUserCacheDao(obj);
        Integer currentVersion = obj.getVersion();
        UserCacheDao merged = userCacheRepository.save(obj);
        userCacheRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'UserCacheDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'UserCacheDao' failed to initialize correctly", dod.getRandomUserCacheDao());
        UserCacheDao obj = dod.getNewTransientUserCacheDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'UserCacheDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'UserCacheDao' identifier to be null", obj.getId());
        try {
            userCacheRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        userCacheRepository.flush();
        Assert.assertNotNull("Expected 'UserCacheDao' identifier to no longer be null", obj.getId());
    }

    @Test
    public void testDelete() {
        UserCacheDao obj = dod.getRandomUserCacheDao();
        Assert.assertNotNull("Data on demand for 'UserCacheDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'UserCacheDao' failed to provide an identifier", id);
        obj = userCacheRepository.findOne(id);
        userCacheRepository.delete(obj);
        userCacheRepository.flush();
        Assert.assertNull("Failed to remove 'UserCacheDao' with identifier '" + id + "'", userCacheRepository.findOne(id));
    }
}
