package meg.biblio.lending.db.dao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = TeacherDao.class)
public class TeacherDaoIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Test
    public void testDelete() {
        TeacherDao obj = dod.getNewTransientTeacherDao(99);
        obj = teacherRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'TeacherDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'TeacherDao' failed to provide an identifier", id);
        obj = teacherRepository.findOne(id);
        teacherRepository.delete(obj);
        teacherRepository.flush();
        Assert.assertNull("Failed to remove 'TeacherDao' with identifier '" + id + "'", teacherRepository.findOne(id));
    }
}
