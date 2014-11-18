package meg.biblio.common.db.dao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = SelectValueDao.class)
public class SelectValueDaoIntegrationTest {

	
	
    @Test
    public void testMarkerMethod() {
    }

	@Test
    public void testDelete() {
        SelectValueDao obj = dod.getNewTransientSelectValueDao(99);
        selectValueRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'SelectValueDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'SelectValueDao' failed to provide an identifier", id);
        obj = selectValueRepository.findOne(id);
        selectValueRepository.delete(obj);
        selectValueRepository.flush();
        Assert.assertNull("Failed to remove 'SelectValueDao' with identifier '" + id + "'", selectValueRepository.findOne(id));
    }
}
