package meg.biblio.common;

import java.util.HashMap;
import java.util.List;
import meg.biblio.common.db.dao.SelectValueDao;

public interface SelectKeyService {

	public HashMap<Long,String> getDisplayHashForKey(String key, String lang);
	
	public List<SelectValueDao> getSelectValuesForKey(String key, String lang);
}
