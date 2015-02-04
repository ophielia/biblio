package meg.biblio.common.db.dao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = AppSettingDao.class)
public class AppSettingDaoIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Test
    public void testDelete() {
        AppSettingDao obj = dod.getNewTransientAppSettingDao(99);
        obj = appSettingRepository.save(obj);
        Assert.assertNotNull("Data on demand for 'AppSettingDao' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'AppSettingDao' failed to provide an identifier", id);
        obj = appSettingRepository.findOne(id);
        appSettingRepository.delete(obj);
        appSettingRepository.flush();
        Assert.assertNull("Failed to remove 'AppSettingDao' with identifier '" + id + "'", appSettingRepository.findOne(id));
    }
}
