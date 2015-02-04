package meg.biblio.common.db.dao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = RoleDao.class)
public class RoleDaoIntegrationTest {

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
}
