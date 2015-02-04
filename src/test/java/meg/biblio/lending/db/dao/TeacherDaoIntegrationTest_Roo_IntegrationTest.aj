// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package meg.biblio.lending.db.dao;

import java.util.Iterator;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import meg.biblio.lending.db.TeacherRepository;
import meg.biblio.lending.db.dao.TeacherDaoDataOnDemand;
import meg.biblio.lending.db.dao.TeacherDaoIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

privileged aspect TeacherDaoIntegrationTest_Roo_IntegrationTest {
    
    declare @type: TeacherDaoIntegrationTest: @RunWith(SpringJUnit4ClassRunner.class);
    
    declare @type: TeacherDaoIntegrationTest: @ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml");
    
    declare @type: TeacherDaoIntegrationTest: @Transactional;
    
    @Autowired
    TeacherDaoDataOnDemand TeacherDaoIntegrationTest.dod;
    
    @Autowired
    TeacherRepository TeacherDaoIntegrationTest.teacherRepository;
    
    @Test
    public void TeacherDaoIntegrationTest.testCount() {
        Assert.assertNotNull("Data on demand for 'TeacherDao' failed to initialize correctly", dod.getRandomTeacherDao());
        long count = teacherRepository.count();
        Assert.assertTrue("Counter for 'TeacherDao' incorrectly reported there were no entries", count > 0);
    }
    
    @Test
    public void TeacherDaoIntegrationTest.testFind() {
        TeacherDao obj = dod.getRandomTeacherDao();
        Assert.assertNotNull("Data on demand for 'TeacherDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'TeacherDao' failed to provide an identifier", id);
        obj = teacherRepository.findOne(id);
        Assert.assertNotNull("Find method for 'TeacherDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'TeacherDao' returned the incorrect identifier", id, obj.getId());
    }
    
    @Test
    public void TeacherDaoIntegrationTest.testFindAll() {
        Assert.assertNotNull("Data on demand for 'TeacherDao' failed to initialize correctly", dod.getRandomTeacherDao());
        long count = teacherRepository.count();
        Assert.assertTrue("Too expensive to perform a find all test for 'TeacherDao', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<TeacherDao> result = teacherRepository.findAll();
        Assert.assertNotNull("Find all method for 'TeacherDao' illegally returned null", result);
        Assert.assertTrue("Find all method for 'TeacherDao' failed to return any data", result.size() > 0);
    }
    
    @Test
    public void TeacherDaoIntegrationTest.testFindEntries() {
        Assert.assertNotNull("Data on demand for 'TeacherDao' failed to initialize correctly", dod.getRandomTeacherDao());
        long count = teacherRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<TeacherDao> result = teacherRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'TeacherDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'TeacherDao' returned an incorrect number of entries", count, result.size());
    }
    
    @Test
    public void TeacherDaoIntegrationTest.testFlush() {
        TeacherDao obj = dod.getRandomTeacherDao();
        Assert.assertNotNull("Data on demand for 'TeacherDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'TeacherDao' failed to provide an identifier", id);
        obj = teacherRepository.findOne(id);
        Assert.assertNotNull("Find method for 'TeacherDao' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyTeacherDao(obj);
        Integer currentVersion = obj.getVersion();
        teacherRepository.flush();
        Assert.assertTrue("Version for 'TeacherDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void TeacherDaoIntegrationTest.testSaveUpdate() {
        TeacherDao obj = dod.getRandomTeacherDao();
        Assert.assertNotNull("Data on demand for 'TeacherDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'TeacherDao' failed to provide an identifier", id);
        obj = teacherRepository.findOne(id);
        boolean modified =  dod.modifyTeacherDao(obj);
        Integer currentVersion = obj.getVersion();
        TeacherDao merged = (TeacherDao)teacherRepository.save(obj);
        teacherRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'TeacherDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void TeacherDaoIntegrationTest.testSave() {
        Assert.assertNotNull("Data on demand for 'TeacherDao' failed to initialize correctly", dod.getRandomTeacherDao());
        TeacherDao obj = dod.getNewTransientTeacherDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'TeacherDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'TeacherDao' identifier to be null", obj.getId());
        try {
            teacherRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext();) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        teacherRepository.flush();
        Assert.assertNotNull("Expected 'TeacherDao' identifier to no longer be null", obj.getId());
    }
    
}
