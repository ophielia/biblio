package meg.biblio.catalog.db.dao;
import meg.biblio.catalog.db.dao.SubjectDao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = SubjectDao.class)
public class SubjectDaoIntegrationTest {

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
}
