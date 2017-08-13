package meg.biblio.catalog.db.dao;

import meg.biblio.catalog.db.SubjectRepository;
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
public class SubjectDaoIntegrationTest {

    @Autowired
    SubjectDaoDataOnDemand dod;
    @Autowired
    SubjectRepository subjectRepository;

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testDelete() {
        SubjectDao obj = dod.getNewTransientSubjectDao(99);
        Assert.assertNotNull("Data on demand for 'SubjectDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SubjectDao' failed to provide an identifier", id);
        obj = subjectRepository.findOne(id);
        subjectRepository.delete(obj);
        subjectRepository.flush();
        Assert.assertNull("Failed to remove 'SubjectDao' with identifier '" + id + "'", subjectRepository.findOne(id));
    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'SubjectDao' failed to initialize correctly", dod.getRandomSubjectDao());
        long count = subjectRepository.count();
        Assert.assertTrue("Counter for 'SubjectDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFind() {
        SubjectDao obj = dod.getRandomSubjectDao();
        Assert.assertNotNull("Data on demand for 'SubjectDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SubjectDao' failed to provide an identifier", id);
        obj = subjectRepository.findOne(id);
        Assert.assertNotNull("Find method for 'SubjectDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'SubjectDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindAll() {
        Assert.assertNotNull("Data on demand for 'SubjectDao' failed to initialize correctly", dod.getRandomSubjectDao());
        long count = subjectRepository.count();
        Assert.assertTrue("Too expensive to perform a find all test for 'SubjectDao', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<SubjectDao> result = subjectRepository.findAll();
        Assert.assertNotNull("Find all method for 'SubjectDao' illegally returned null", result);
        Assert.assertTrue("Find all method for 'SubjectDao' failed to return any data", result.size() > 0);
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'SubjectDao' failed to initialize correctly", dod.getRandomSubjectDao());
        long count = subjectRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<SubjectDao> result = subjectRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'SubjectDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'SubjectDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testFlush() {
        SubjectDao obj = dod.getRandomSubjectDao();
        Assert.assertNotNull("Data on demand for 'SubjectDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SubjectDao' failed to provide an identifier", id);
        obj = subjectRepository.findOne(id);
        Assert.assertNotNull("Find method for 'SubjectDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifySubjectDao(obj);
        Integer currentVersion = obj.getVersion();
        subjectRepository.flush();
        Assert.assertTrue("Version for 'SubjectDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSaveUpdate() {
        SubjectDao obj = dod.getRandomSubjectDao();
        Assert.assertNotNull("Data on demand for 'SubjectDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SubjectDao' failed to provide an identifier", id);
        obj = subjectRepository.findOne(id);
        boolean modified = dod.modifySubjectDao(obj);
        Integer currentVersion = obj.getVersion();
        SubjectDao merged = subjectRepository.save(obj);
        subjectRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'SubjectDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'SubjectDao' failed to initialize correctly", dod.getRandomSubjectDao());
        SubjectDao obj = dod.getNewTransientSubjectDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'SubjectDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'SubjectDao' identifier to be null", obj.getId());
        try {
            subjectRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        subjectRepository.flush();
        Assert.assertNotNull("Expected 'SubjectDao' identifier to no longer be null", obj.getId());
    }
}
