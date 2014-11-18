package meg.biblio.common.db.dao;
import org.springframework.roo.addon.dod.RooDataOnDemand;

@RooDataOnDemand(entity = SelectValueDao.class)
public class SelectValueDaoDataOnDemand {

	public void setLanguagekey(SelectValueDao obj, int index) {
        String languagekey = "en";
        obj.setLanguagekey(languagekey);
    }
}
