package meg.biblio.catalog.db.dao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = BookDetailDao.class,findAll=false)
public class BookDetailDaoIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Test
    public void testDelete() {
        BookDetailDao obj = dod.getNewTransientBookDetailDao(999);
        bookDetailRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'BookDetailDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'BookDetailDao' failed to provide an identifier", id);
        obj = bookDetailRepository.findOne(id);
        bookDetailRepository.delete(obj);
        bookDetailRepository.flush();
        Assert.assertNull("Failed to remove 'BookDetailDao' with identifier '" + id + "'", bookDetailRepository.findOne(id));
    }
}
