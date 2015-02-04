package meg.biblio.lending.db.dao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = SchoolGroupDao.class)
public class SchoolGroupDaoIntegrationTest {

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
}
