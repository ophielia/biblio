package meg.biblio.common;

import meg.biblio.common.db.UserCacheRepository;
import meg.biblio.common.db.dao.UserCacheDao;
import meg.biblio.common.db.dao.UserLoginDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@EnableScheduling
public class CacheServiceImpl implements CacheService {

    @Autowired
    UserCacheRepository userCacheRepository;

    @Autowired
    LoginService loginService;


    @Override
    public List<String> getValidCacheAsList(String username, String cachetag) {
        // get userlogin for name
        UserLoginDao userlogin = loginService.getUserLoginDaoByName(username);

        // get current date
        Date expiry = new Date();

        if (userlogin != null) {
            // retrieve cache for tag
            List<UserCacheDao> usrcache = userCacheRepository
                    .getValidCacheForUser(userlogin, cachetag, expiry, new Sort(
                            "expiration"));

            // put values in list
            if (usrcache != null) {
                List<String> cachevals = new ArrayList<String>();
                for (UserCacheDao cachedao : usrcache) {
                    if (cachedao != null && cachedao.getValue() != null) {
                        cachevals.add(cachedao.getValue());
                    }
                }
                // return list
                return cachevals;
            }
        }
        return null;
    }

    @Override
    public List<String> getValidCacheAsList(String username, String cachetag, String name) {
        // get userlogin for name
        UserLoginDao userlogin = loginService.getUserLoginDaoByName(username);

        // get current date
        Date expiry = new Date();

        if (userlogin != null) {
            // retrieve cache for tag
            List<UserCacheDao> usrcache = userCacheRepository
                    .getValidCacheForUser(userlogin, cachetag, name, expiry, new Sort(
                            "expiration"));

            // put values in list
            if (usrcache != null) {
                List<String> cachevals = new ArrayList<String>();
                for (UserCacheDao cachedao : usrcache) {
                    if (cachedao != null && cachedao.getValue() != null) {
                        cachevals.add(cachedao.getValue());
                    }
                }
                // return list
                return cachevals;
            }
        }
        return null;
    }

    public List<Long> getValidCacheAsListofLongs(String username, String cachetag, String name) {
        // get userlogin for name
        UserLoginDao userlogin = loginService.getUserLoginDaoByName(username);

        // get current date
        Date expiry = new Date();

        if (userlogin != null) {
            // retrieve cache for tag
            List<UserCacheDao> usrcache = userCacheRepository
                    .getValidCacheForUser(userlogin, cachetag, name, expiry, new Sort(
                            "expiration"));

            // put values in list
            if (usrcache != null) {
                List<Long> cachevals = new ArrayList<Long>();
                for (UserCacheDao cachedao : usrcache) {
                    if (cachedao != null && cachedao.getValue() != null) {
                        Long addval = Long.valueOf(cachedao.getValue());
                        cachevals.add(addval);
                    }
                }
                // return list
                return cachevals;
            }
        }
        return null;
    }


    @Override
    @Scheduled(fixedRate = 600000)
    public void clearExpiredCache() {
        // get expired cache
        List<UserCacheDao> expired = userCacheRepository
                .getExpiredCache(new Date());
        // delete each one
        if (expired != null) {
            userCacheRepository.delete(expired);
        }

        // flush them
        userCacheRepository.flush();
    }

    @Override
    public void clearUserCacheForTag(String username, String cachetag) {
        // get userlogin for name
        UserLoginDao userlogin = loginService.getUserLoginDaoByName(username);

        // get  cache for user
        List<UserCacheDao> usercache = userCacheRepository
                .getAllCacheForUser(userlogin, cachetag);
        // delete each one
        if (usercache != null) {
            userCacheRepository.delete(usercache);
        }

        // flush them
        userCacheRepository.flush();
    }


    @Override
    public void saveValueInCache(String username, String cachetag, String name,
                                 String value, Long validfor) {
        // get existing cache value, if available (no duplicates saved)
        UserCacheDao existing = getSingleCacheValue(username, cachetag, name, value);
        // if no existing, add cache value
        if (existing == null) {
            // create new UserCacheDao
            UserCacheDao newcache = new UserCacheDao();
            // fill in info
            UserLoginDao ulogin = loginService.getUserLoginDaoByName(username);
            newcache.setCacheuser(ulogin);
            newcache.setCachetag(cachetag);
            newcache.setName(name);
            newcache.setValue(value);

            // calculate expiration datetime and set
            Calendar exp = Calendar.getInstance();
            exp.setTime(new Date());
            exp.add(Calendar.MINUTE, validfor.intValue());
            newcache.setExpiration(exp.getTime());

            // save in db
            userCacheRepository.save(newcache);
        }


    }

    private UserCacheDao getSingleCacheValue(String username, String cachetag,
                                             String name, String value) {
        UserLoginDao ulogin = loginService.getUserLoginDaoByName(username);

        // get userlogin for name
        UserLoginDao userlogin = loginService.getUserLoginDaoByName(username);

        // get current date
        Date expiry = new Date();

        if (userlogin != null) {
            // retrieve cache for tag
            UserCacheDao usrcache = userCacheRepository
                    .getValidCacheValueForUser(ulogin, cachetag, value,
                            expiry);

            return usrcache;
        }
        return null;

    }

    @Override
    public void deleteValueFromCache(String username, String cachetag, String name,
                                     String value) {
        // get existing cache value, if available (no duplicates saved)
        UserCacheDao existing = getSingleCacheValue(username, cachetag, name, value);
        // if no existing, add cache value
        if (existing != null) {
            userCacheRepository.delete(existing);
        }
        userCacheRepository.flush();
    }

    @Override
    public void replaceValuesInCache(String username, String cachetag,
                                     String name, List<String> values, Long minutesvalid) {
        // get userlogin for name
        UserLoginDao userlogin = loginService.getUserLoginDaoByName(username);

        // get current date
        Date expiry = new Date();

        // retrieve cachevalues for user, cachetag, name
        List<UserCacheDao> todeletecache = userCacheRepository
                .getValidCacheForUser(userlogin, cachetag, name, expiry, new Sort(
                        "expiration"));
        // delete them all
        userCacheRepository.delete(todeletecache);
        // create new cachevalues from passed list
        if (values != null) {
            List<UserCacheDao> newcache = new ArrayList<UserCacheDao>();
            // determine expiration date
            Calendar exp = Calendar.getInstance();
            exp.setTime(new Date());
            exp.add(Calendar.MINUTE, minutesvalid.intValue());
            Date newexpiration = exp.getTime();
            // create list of values
            for (String value : values) {
                UserCacheDao uc = new UserCacheDao();
                uc.setCacheuser(userlogin);
                uc.setCachetag(cachetag);
                uc.setName(name);
                uc.setExpiration(newexpiration);
                uc.setValue(value);
                newcache.add(uc);
            }
            userCacheRepository.save(newcache);
        }
        userCacheRepository.flush();
    }

}
