package meg.biblio.common;

import java.util.List;

import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.db.dao.UserLoginDao;

public interface LoginService {

	public final String rolelkup = "roles";

	boolean oldPasswordMatches(String oldpassword, Long id);

	boolean userExists(String username);

	UserLoginDao getUserLoginDaoByName(String username);

	UserLoginDao getUserLoginDaoById(Long id);

	UserLoginDao updateUserLoginDao(UserLoginDao account);

	UserLoginDao createNewUserLogin(UserLoginDao account, Long clientkey);

	List<UserLoginDao> getUsersForClient(Long clientkey, boolean includesuperadmin);

	ClientDao getClientForUsername(String username);
}
