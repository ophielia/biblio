package meg.biblio.common;

import java.util.HashMap;

public interface SelectKeyService {

	public HashMap<Long,String> getDisplayHashForKey(String key, String lang);
}
