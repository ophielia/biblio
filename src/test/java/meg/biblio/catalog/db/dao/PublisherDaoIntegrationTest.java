package meg.biblio.catalog.db.dao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = PublisherDao.class,findAll=false)
public class PublisherDaoIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Test
    public void testDelete() {
        PublisherDao obj = dod.getNewTransientPublisherDao(99);
        obj = publisherRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'PublisherDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'PublisherDao' failed to provide an identifier", id);
        obj = publisherRepository.findOne(id);
        publisherRepository.delete(obj);
        publisherRepository.flush();
        Assert.assertNull("Failed to remove 'PublisherDao' with identifier '" + id + "'", publisherRepository.findOne(id));
    }
}
