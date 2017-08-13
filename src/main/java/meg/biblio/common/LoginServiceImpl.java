package meg.biblio.common;

import meg.biblio.common.db.LoginRepository;
import meg.biblio.common.db.RoleRepository;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.db.dao.RoleDao;
import meg.biblio.common.db.dao.UserLoginDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class LoginServiceImpl implements LoginService {

    private static final String ROLE_SUPERADMIN = "ROLE_SUPERADMIN";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";

    @Autowired
    private LoginRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ClientService clientService;

    @Override
    public UserLoginDao createNewUserLogin(UserLoginDao account, Long clientkey) {
        // receives a validated account object, needs to have password
        // encrypted,
        // and account persisted to the db

        StandardPasswordEncoder encoder = new StandardPasswordEncoder();

        // encrypt password
        String result = encoder.encode(account.getTextpassword());
        account.setPassword(result);

        // enable account
        account.setEnabled(true);

        // set client
        ClientDao client = clientService.getClientForKey(clientkey);
        account.setClient(client);

        // set createdon date
        account.setCreatedOn(new Date());

        // add account to role
        // pull role
        RoleDao role = getRoleByName(ROLE_USER);
        if (account.getRolename() != null) {
            List<RoleDao> roles = roleRepository.findRolesByName(account.getRolename());
            if (roles != null && roles.size() > 0) {
                role = roles.get(0);
            }
        }

        if (role != null) {
            // set role in object
            account.setRole(role);
        }

        // persist account
        account = accountRepository.save(account);

        return account;
    }

    @Override
    public boolean userExists(String username) {
        // lookup user by username
        List<UserLoginDao> accounts = accountRepository
                .findUsersByName(username);
        // if user is returned, return true
        return (accounts != null && accounts.size() > 0);
    }

    @Override
    public UserLoginDao getUserLoginDaoByName(String username) {
        if (username != null) {
            List<UserLoginDao> accounts = accountRepository
                    .findUsersByName(username);
            if (accounts != null && accounts.size() == 1) {
                return accounts.get(0);
            }
        }
        return null;
    }

    @Override
    public UserLoginDao getUserLoginDaoById(Long id) {
        if (id != null) {
            UserLoginDao account = accountRepository.findOne(id);
            return account;
        }
        return null;
    }

    @Override
    public UserLoginDao updateUserLoginDao(UserLoginDao account) {
        // get userlogin from db
        UserLoginDao dblogin = getUserLoginDaoById(account.getId());
        // check for password switch
        if (account.getTextpassword() != null) {
            // textpassword has been entered to trigger password
            // change. Put the new password in.
            StandardPasswordEncoder encoder = new StandardPasswordEncoder();
            String result = encoder.encode(account.getTextpassword());
            dblogin.setPassword(result);
        }


        dblogin.setUsername(account.getUsername());

        if (account.getRolename() != null) {
            RoleDao role = getRoleByName(account.getRolename());
            dblogin.setRole(role);
        }

        dblogin = accountRepository.save(dblogin);
        return dblogin;
    }

    private RoleDao getRoleByName(String rolename) {
        List<RoleDao> allroles = roleRepository.findRolesByName(rolename);
        if (allroles != null && allroles.size() > 0) {
            return allroles.get(0);
        }
        return null;
    }

    public boolean oldPasswordMatches(String oldpassword, Long id) {
        // pull account
        UserLoginDao acct = getUserLoginDaoById(id);
        // check for match with password
        StandardPasswordEncoder encoder = new StandardPasswordEncoder();
        boolean match = encoder.matches(oldpassword, acct.getPassword());
        // return result
        return match;
    }

    @Override
    public List<UserLoginDao> getUsersForClient(Long clientkey, boolean includesuperadmin) {
        ClientDao client = clientService.getClientForKey(clientkey);
        if (includesuperadmin) {
            return accountRepository.findAllUsersByClient(client, new Sort("username"));
        } else {
            return accountRepository.findUsersForClient(client, new Sort("username"));
        }

    }

    @Override
    public ClientDao getClientForUsername(String username) {
        if (username != null) {
            List<UserLoginDao> users = accountRepository.findUsersByName(username.toLowerCase());
            if (users != null && users.size() > 0) {
                UserLoginDao user = users.get(0);
                return user.getClient();
            }
        }
        return null;
    }

}
