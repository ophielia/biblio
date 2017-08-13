package meg.biblio.common.db.dao;

import meg.biblio.common.db.RoleRepository;
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
public class RoleDaoIntegrationTest {

    @Autowired
    RoleDaoDataOnDemand dod;
    @Autowired
    RoleRepository roleRepository;

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testDelete() {
        RoleDao obj = dod.getNewTransientRoleDao(99);
        obj = roleRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'RoleDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'RoleDao' failed to provide an identifier", id);
        obj = roleRepository.findOne(id);
        roleRepository.delete(obj);
        roleRepository.flush();
        Assert.assertNull("Failed to remove 'RoleDao' with identifier '" + id + "'", roleRepository.findOne(id));
    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'RoleDao' failed to initialize correctly", dod.getRandomRoleDao());
        long count = roleRepository.count();
        Assert.assertTrue("Counter for 'RoleDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFind() {
        RoleDao obj = dod.getRandomRoleDao();
        Assert.assertNotNull("Data on demand for 'RoleDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'RoleDao' failed to provide an identifier", id);
        obj = roleRepository.findOne(id);
        Assert.assertNotNull("Find method for 'RoleDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'RoleDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindAll() {
        Assert.assertNotNull("Data on demand for 'RoleDao' failed to initialize correctly", dod.getRandomRoleDao());
        long count = roleRepository.count();
        Assert.assertTrue("Too expensive to perform a find all test for 'RoleDao', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<RoleDao> result = roleRepository.findAll();
        Assert.assertNotNull("Find all method for 'RoleDao' illegally returned null", result);
        Assert.assertTrue("Find all method for 'RoleDao' failed to return any data", result.size() > 0);
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'RoleDao' failed to initialize correctly", dod.getRandomRoleDao());
        long count = roleRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<RoleDao> result = roleRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'RoleDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'RoleDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testFlush() {
        RoleDao obj = dod.getRandomRoleDao();
        Assert.assertNotNull("Data on demand for 'RoleDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'RoleDao' failed to provide an identifier", id);
        obj = roleRepository.findOne(id);
        Assert.assertNotNull("Find method for 'RoleDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifyRoleDao(obj);
        Integer currentVersion = obj.getVersion();
        roleRepository.flush();
        Assert.assertTrue("Version for 'RoleDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSaveUpdate() {
        RoleDao obj = dod.getRandomRoleDao();
        Assert.assertNotNull("Data on demand for 'RoleDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'RoleDao' failed to provide an identifier", id);
        obj = roleRepository.findOne(id);
        boolean modified = dod.modifyRoleDao(obj);
        Integer currentVersion = obj.getVersion();
        RoleDao merged = roleRepository.save(obj);
        roleRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'RoleDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'RoleDao' failed to initialize correctly", dod.getRandomRoleDao());
        RoleDao obj = dod.getNewTransientRoleDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'RoleDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'RoleDao' identifier to be null", obj.getId());
        try {
            roleRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        roleRepository.flush();
        Assert.assertNotNull("Expected 'RoleDao' identifier to no longer be null", obj.getId());
    }
}
