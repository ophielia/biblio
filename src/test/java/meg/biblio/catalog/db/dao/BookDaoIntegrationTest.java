package meg.biblio.catalog.db.dao;
import meg.biblio.catalog.db.dao.BookDao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = BookDao.class,findAll=false)
public class BookDaoIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Test
    public void testDelete() {
        BookDao obj = dod.getNewTransientBookDao(99);
        obj = bookRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'BookDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'BookDao' failed to provide an identifier", id);
        obj = bookRepository.findOne(id);
        bookRepository.delete(obj);
        bookRepository.flush();
        Assert.assertNull("Failed to remove 'BookDao' with identifier '" + id + "'", bookRepository.findOne(id));
    }
}
