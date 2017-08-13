package meg.biblio.catalog.db.dao;

import meg.biblio.catalog.db.ArtistRepository;
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
public class ArtistDaoIntegrationTest {

    @Autowired
    ArtistDaoDataOnDemand dod;
    @Autowired
    ArtistRepository artistRepository;

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testDelete() {
        ArtistDao obj = dod.getNewTransientArtistDao(99);
        artistRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'ArtistDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ArtistDao' failed to provide an identifier", id);
        obj = artistRepository.findOne(id);
        artistRepository.delete(obj);
        artistRepository.flush();
        Assert.assertNull("Failed to remove 'ArtistDao' with identifier '" + id + "'", artistRepository.findOne(id));
    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'ArtistDao' failed to initialize correctly", dod.getRandomArtistDao());
        long count = artistRepository.count();
        Assert.assertTrue("Counter for 'ArtistDao' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFind() {
        ArtistDao obj = dod.getRandomArtistDao();
        Assert.assertNotNull("Data on demand for 'ArtistDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ArtistDao' failed to provide an identifier", id);
        obj = artistRepository.findOne(id);
        Assert.assertNotNull("Find method for 'ArtistDao' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'ArtistDao' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'ArtistDao' failed to initialize correctly", dod.getRandomArtistDao());
        long count = artistRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<ArtistDao> result = artistRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'ArtistDao' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'ArtistDao' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testFlush() {
        ArtistDao obj = dod.getRandomArtistDao();
        Assert.assertNotNull("Data on demand for 'ArtistDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ArtistDao' failed to provide an identifier", id);
        obj = artistRepository.findOne(id);
        Assert.assertNotNull("Find method for 'ArtistDao' illegally returned null for id '" + id + "'", obj);
        boolean modified = dod.modifyArtistDao(obj);
        Integer currentVersion = obj.getVersion();
        artistRepository.flush();
        Assert.assertTrue("Version for 'ArtistDao' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSaveUpdate() {
        ArtistDao obj = dod.getRandomArtistDao();
        Assert.assertNotNull("Data on demand for 'ArtistDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ArtistDao' failed to provide an identifier", id);
        obj = artistRepository.findOne(id);
        boolean modified = dod.modifyArtistDao(obj);
        Integer currentVersion = obj.getVersion();
        ArtistDao merged = artistRepository.save(obj);
        artistRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'ArtistDao' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'ArtistDao' failed to initialize correctly", dod.getRandomArtistDao());
        ArtistDao obj = dod.getNewTransientArtistDao(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'ArtistDao' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'ArtistDao' identifier to be null", obj.getId());
        try {
            artistRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        artistRepository.flush();
        Assert.assertNotNull("Expected 'ArtistDao' identifier to no longer be null", obj.getId());
    }
}
