package meg.biblio.lending.db.dao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = PersonDao.class)
public class PersonDaoIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Test
    public void testDelete() {
        PersonDao obj = dod.getNewTransientPersonDao(99);
        obj = personRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'PersonDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PersonDao' failed to provide an identifier", id);
        obj = personRepository.findOne(id);
        personRepository.delete(obj);
        personRepository.flush();
        Assert.assertNull("Failed to remove 'PersonDao' with identifier '" + id + "'", personRepository.findOne(id));
    }
}
