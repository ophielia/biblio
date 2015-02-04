package meg.biblio.common.db.dao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = ClientDao.class)
public class ClientDaoIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Test
    public void testDelete() {
        ClientDao obj = dod.getNewTransientClientDao(99);
        obj = clientRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'ClientDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ClientDao' failed to provide an identifier", id);
        obj = clientRepository.findOne(id);
        clientRepository.delete(obj);
        clientRepository.flush();
        Assert.assertNull("Failed to remove 'ClientDao' with identifier '" + id + "'", clientRepository.findOne(id));
    }
}
