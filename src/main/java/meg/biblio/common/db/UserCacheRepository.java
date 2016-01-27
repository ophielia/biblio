package meg.biblio.common.db;
import java.util.Date;
import java.util.List;

import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.db.dao.UserCacheDao;
import meg.biblio.common.db.dao.UserLoginDao;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = UserCacheDao.class)
public interface UserCacheRepository {
	
	@Query("select r from UserCacheDao as r where r.cacheuser = :user and r.cachetag = :cachetag and r.expiration >= :expiration")
	List<UserCacheDao> getValidCacheForUser(@Param("user") UserLoginDao user,@Param("cachetag") String cachetag,@Param("expiration") Date expiration, Sort sort); 

	@Query("select r from UserCacheDao as r where r.cacheuser = :user and r.cachetag = :cachetag and r.name = :name and r.expiration >= :expiration")
	List<UserCacheDao> getValidCacheForUser(@Param("user") UserLoginDao user,@Param("cachetag") String cachetag,
			@Param("name") String name,@Param("expiration") Date expiration, Sort sort); 

	@Query("select r from UserCacheDao as r where r.cacheuser = :user and r.cachetag = :cachetag ")
	List<UserCacheDao> getAllCacheForUser(@Param("user") UserLoginDao user,@Param("cachetag") String cachetag); 
	
	@Query("select r from UserCacheDao as r where  r.expiration < :expiration")
	List<UserCacheDao> getExpiredCache(@Param("expiration") Date expiration);

	@Query("select r from UserCacheDao as r where r.cacheuser = :user and r.cachetag = :cachetag and r.value = :value and r.expiration >= :expiration")
	UserCacheDao getValidCacheValueForUser(@Param("user") UserLoginDao user,@Param("cachetag") String cachetag,@Param("value") String value,@Param("expiration") Date expiration);


}
