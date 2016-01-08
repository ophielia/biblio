package meg.biblio.common.db.dao;
import java.util.Date;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity(table = "usercache")
public class UserCacheDao {

	@ManyToOne( fetch=FetchType.EAGER)
	@JoinColumn(name = "cacheuser")
	private UserLoginDao cacheuser;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date expiration;
	
	private String cachetag;
	
	private String name;
	
	private String value;


}
