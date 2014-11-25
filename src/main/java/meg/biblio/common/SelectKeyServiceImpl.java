package meg.biblio.common;

import java.util.HashMap;
import java.util.List;

import meg.biblio.common.db.SelectKeyRepository;
import meg.biblio.common.db.SelectValueRepository;
import meg.biblio.common.db.dao.SelectValueDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class SelectKeyServiceImpl implements SelectKeyService {

	@Autowired
	SelectKeyRepository keyRepo;

	@Autowired
	SelectValueRepository valueRepo;

	@Override
	public HashMap<Long, String> getDisplayHashForKey(String key, String lang) {
		// lookup values for key, in language
		List<SelectValueDao> values = getSelectValuesForKey(key, lang);
		// put found values in hashmap
		if (values != null) {
			HashMap<Long, String> map = new HashMap<Long, String>();
			for (SelectValueDao sv : values) {
				map.put(new Long(sv.getValue()), sv.getDisplay());
			}
			// return hashmap
			return map;
		}
		return null;
	}

	@Override
	public List<SelectValueDao> getSelectValuesForKey(String key, String lang) {
		// lookup values for key, in language
		List<SelectValueDao> values = valueRepo.findByKeyLanguageDisplay(key,
				lang, new Sort("disporder"));
		return values;
	}

}
