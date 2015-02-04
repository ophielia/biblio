package meg.biblio.lending.db.dao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = StudentDao.class)
public class StudentDaoIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Test
    public void testDelete() {
        StudentDao obj = dod.getNewTransientStudentDao(99);
        obj = studentRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'StudentDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'StudentDao' failed to provide an identifier", id);
        obj = studentRepository.findOne(id);
        studentRepository.delete(obj);
        studentRepository.flush();
        Assert.assertNull("Failed to remove 'StudentDao' with identifier '" + id + "'", studentRepository.findOne(id));
    }
}
