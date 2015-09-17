package meg.biblio.catalog.db.dao;
import meg.biblio.catalog.db.dao.ArtistDao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = ArtistDao.class,findAll=false)
public class ArtistDaoIntegrationTest {

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
}
