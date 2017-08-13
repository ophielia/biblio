package meg.biblio.common.db.dao;

import meg.biblio.common.db.AppSettingRepository;
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
public class AppSettingDaoIntegrationTest {

    @Autowired
    AppSettingDaoDataOnDemand dod;
    @Autowired
    AppSettingRepository appSettingRepository;

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testDelete() {
        AppSettingDao obj = dod.getNewTransientAppSettingDao(99);
        obj = appSettingRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'AppSettingDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'AppSettingDao' failed to provide an identifier", id);
        obj = appSettingRepository.findOne(id);
        appSettingRepository.delete(obj);
        appSettingRepository.flush();
        Assert.assertNull("Failed to remove 'AppSettingDao' with identifier '" + id + "'", appSettingRepository.findOne(id));
    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'AppSettingDao' failed to initialize correctly", dod.getRandomAppSettingDao());
        long count = appSettingRepository.count();
        Assert.assertTrue("Counter for 'AppSettingDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFind() {
        AppSettingDao obj = dod.getRandomAppSettingDao();
        Assert.assertNotNull("Data on demand for 'AppSettingDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'AppSettingDao' failed to provide an identifier", id);
        obj = appSettingRepository.findOne(id);
        Assert.assertNotNull("Find method for 'AppSettingDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'AppSettingDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindAll() {
        Assert.assertNotNull("Data on demand for 'AppSettingDao' failed to initialize correctly", dod.getRandomAppSettingDao());
        long count = appSettingRepository.count();
        Assert.assertTrue("Too expensive to perform a find all test for 'AppSettingDao', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<AppSettingDao> result = appSettingRepository.findAll();
        Assert.assertNotNull("Find all method for 'AppSettingDao' illegally returned null", result);
        Assert.assertTrue("Find all method for 'AppSettingDao' failed to return any data", result.size() > 0);
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'AppSettingDao' failed to initialize correctly", dod.getRandomAppSettingDao());
        long count = appSettingRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<AppSettingDao> result = appSettingRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'AppSettingDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'AppSettingDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testFlush() {
        AppSettingDao obj = dod.getRandomAppSettingDao();
        Assert.assertNotNull("Data on demand for 'AppSettingDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'AppSettingDao' failed to provide an identifier", id);
        obj = appSettingRepository.findOne(id);
        Assert.assertNotNull("Find method for 'AppSettingDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifyAppSettingDao(obj);
        Integer currentVersion = obj.getVersion();
        appSettingRepository.flush();
        Assert.assertTrue("Version for 'AppSettingDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSaveUpdate() {
        AppSettingDao obj = dod.getRandomAppSettingDao();
        Assert.assertNotNull("Data on demand for 'AppSettingDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'AppSettingDao' failed to provide an identifier", id);
        obj = appSettingRepository.findOne(id);
        boolean modified = dod.modifyAppSettingDao(obj);
        Integer currentVersion = obj.getVersion();
        AppSettingDao merged = appSettingRepository.save(obj);
        appSettingRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'AppSettingDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'AppSettingDao' failed to initialize correctly", dod.getRandomAppSettingDao());
        AppSettingDao obj = dod.getNewTransientAppSettingDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'AppSettingDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'AppSettingDao' identifier to be null", obj.getId());
        try {
            appSettingRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        appSettingRepository.flush();
        Assert.assertNotNull("Expected 'AppSettingDao' identifier to no longer be null", obj.getId());
    }
}
