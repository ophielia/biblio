package meg.biblio.catalog.db.dao;

import meg.biblio.catalog.db.BookDetailRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Iterator;
import java.util.List;

@Configurable
@Transactional
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class BookDetailDaoIntegrationTest {

    @Autowired
    BookDetailDaoDataOnDemand dod;
    @Autowired
    BookDetailRepository bookDetailRepository;

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

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'BookDetailDao' failed to initialize correctly", dod.getRandomBookDetailDao());
        long count = bookDetailRepository.count();
        Assert.assertTrue("Counter for 'BookDetailDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFind() {
        BookDetailDao obj = dod.getRandomBookDetailDao();
        Assert.assertNotNull("Data on demand for 'BookDetailDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'BookDetailDao' failed to provide an identifier", id);
        obj = bookDetailRepository.findOne(id);
        Assert.assertNotNull("Find method for 'BookDetailDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'BookDetailDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'BookDetailDao' failed to initialize correctly", dod.getRandomBookDetailDao());
        long count = bookDetailRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<BookDetailDao> result = bookDetailRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'BookDetailDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'BookDetailDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testFlush() {
        BookDetailDao obj = dod.getRandomBookDetailDao();
        Assert.assertNotNull("Data on demand for 'BookDetailDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'BookDetailDao' failed to provide an identifier", id);
        obj = bookDetailRepository.findOne(id);
        Assert.assertNotNull("Find method for 'BookDetailDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifyBookDetailDao(obj);
        Integer currentVersion = obj.getVersion();
        bookDetailRepository.flush();
        Assert.assertTrue("Version for 'BookDetailDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSaveUpdate() {
        BookDetailDao obj = dod.getRandomBookDetailDao();
        Assert.assertNotNull("Data on demand for 'BookDetailDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'BookDetailDao' failed to provide an identifier", id);
        obj = bookDetailRepository.findOne(id);
        boolean modified = dod.modifyBookDetailDao(obj);
        Integer currentVersion = obj.getVersion();
        BookDetailDao merged = bookDetailRepository.save(obj);
        bookDetailRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'BookDetailDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'BookDetailDao' failed to initialize correctly", dod.getRandomBookDetailDao());
        BookDetailDao obj = dod.getNewTransientBookDetailDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'BookDetailDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'BookDetailDao' identifier to be null", obj.getId());
        try {
            bookDetailRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        bookDetailRepository.flush();
        Assert.assertNotNull("Expected 'BookDetailDao' identifier to no longer be null", obj.getId());
    }
}
