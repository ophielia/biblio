package meg.biblio.common;

import meg.biblio.common.db.AppSettingRepository;
import meg.biblio.common.db.dao.AppSettingDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppSettingServiceImpl implements AppSettingService {

	@Autowired
	AppSettingRepository settingRepo;

	@Autowired
	LoginService loginService;

	@Override
	public Long getSettingAsLong(String key) {
		try {
			Long value = Long.parseLong(getSetting(key));
			return value;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String getSettingAsString(String key) {
		return getSetting(key);
	}

	@Override
	public Boolean getSettingAsBoolean(String key) {
		try {
			Boolean value = Boolean.parseBoolean(getSetting(key));
			return value;
		} catch (Exception e) {
			return null;
		}
	}
	

	@Override
	public Integer getSettingAsInteger(String key) {
		try {
			Integer value = Integer.parseInt(getSetting(key));
			return value;
		} catch (Exception e) {
			return null;
		}
	}	

	private String getSetting(String key) {
		AppSettingDao setting = settingRepo.findByKey(key);
		if (setting!=null) {
			return setting.getValue();
		}
		return null;
	}
}
