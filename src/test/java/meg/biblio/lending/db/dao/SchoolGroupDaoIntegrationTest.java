package meg.biblio.lending.db.dao;

import meg.biblio.lending.db.SchoolGroupRepository;
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
public class SchoolGroupDaoIntegrationTest {

    @Autowired
    SchoolGroupDaoDataOnDemand dod;
    @Autowired
    SchoolGroupRepository schoolGroupRepository;

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testDelete() {
        SchoolGroupDao obj = dod.getNewTransientSchoolGroupDao(99);
        obj = schoolGroupRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'SchoolGroupDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SchoolGroupDao' failed to provide an identifier", id);
        obj = schoolGroupRepository.findOne(id);
        schoolGroupRepository.delete(obj);
        schoolGroupRepository.flush();
        Assert.assertNull("Failed to remove 'SchoolGroupDao' with identifier '" + id + "'", schoolGroupRepository.findOne(id));
    }

    @Test
    public void testFlush() {
        SchoolGroupDao obj = dod.getRandomSchoolGroupDao();
        Assert.assertNotNull("Data on demand for 'SchoolGroupDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SchoolGroupDao' failed to provide an identifier", id);
        obj = schoolGroupRepository.findOne(id);
        Assert.assertNotNull("Find method for 'SchoolGroupDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifySchoolGroupDao(obj);
        Integer currentVersion = obj.getVersion();
        schoolGroupRepository.flush();
        Assert.assertTrue("Version for 'SchoolGroupDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'SchoolGroupDao' failed to initialize correctly", dod.getRandomSchoolGroupDao());
        long count = schoolGroupRepository.count();
        Assert.assertTrue("Counter for 'SchoolGroupDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFind() {
        SchoolGroupDao obj = dod.getRandomSchoolGroupDao();
        Assert.assertNotNull("Data on demand for 'SchoolGroupDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SchoolGroupDao' failed to provide an identifier", id);
        obj = schoolGroupRepository.findOne(id);
        Assert.assertNotNull("Find method for 'SchoolGroupDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'SchoolGroupDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'SchoolGroupDao' failed to initialize correctly", dod.getRandomSchoolGroupDao());
        long count = schoolGroupRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<SchoolGroupDao> result = schoolGroupRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'SchoolGroupDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'SchoolGroupDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testSaveUpdate() {
        SchoolGroupDao obj = dod.getRandomSchoolGroupDao();
        Assert.assertNotNull("Data on demand for 'SchoolGroupDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SchoolGroupDao' failed to provide an identifier", id);
        obj = schoolGroupRepository.findOne(id);
        boolean modified = dod.modifySchoolGroupDao(obj);
        Integer currentVersion = obj.getVersion();
        SchoolGroupDao merged = schoolGroupRepository.save(obj);
        schoolGroupRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'SchoolGroupDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testFindAll() {
        Assert.assertNotNull("Data on demand for 'SchoolGroupDao' failed to initialize correctly", dod.getRandomSchoolGroupDao());
        long count = schoolGroupRepository.count();
        Assert.assertTrue("Too expensive to perform a find all test for 'SchoolGroupDao', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<SchoolGroupDao> result = schoolGroupRepository.findAll();
        Assert.assertNotNull("Find all method for 'SchoolGroupDao' illegally returned null", result);
        Assert.assertTrue("Find all method for 'SchoolGroupDao' failed to return any data", result.size() > 0);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'SchoolGroupDao' failed to initialize correctly", dod.getRandomSchoolGroupDao());
        SchoolGroupDao obj = dod.getNewTransientSchoolGroupDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'SchoolGroupDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'SchoolGroupDao' identifier to be null", obj.getId());
        try {
            schoolGroupRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        schoolGroupRepository.flush();
        Assert.assertNotNull("Expected 'SchoolGroupDao' identifier to no longer be null", obj.getId());
    }
}
