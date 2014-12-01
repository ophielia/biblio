package meg.biblio.common.db.dao;
import org.springframework.roo.addon.dod.RooDataOnDemand;

@RooDataOnDemand(entity = RoleDao.class)
public class RoleDaoDataOnDemand {

	public void setRolename(RoleDao obj, int index) {
        String rolename = "ROLE_" + index;
        if (rolename.length() > 50) {
            rolename = rolename.substring(0, 50);
        }
        obj.setRolename(rolename);
    }
}
