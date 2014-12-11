package meg.biblio.common.db.dao;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity(table = "userlogin")
public class UserLoginDao {
	
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
	private Long id;
    
    @Size(min = 3, max = 50)
	private String username;
    
    @Size(min = 3, max = 250)
	private String password;
	
    private Boolean enabled;
	
    @Size(min = 3, max = 250)
    private String textpassword;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
	private Date createdOn;
    
	@ManyToOne( fetch=FetchType.EAGER)
	@JoinColumn(name = "clientid")
	private ClientDao client;
	
	@OneToOne( fetch=FetchType.EAGER)
	private RoleDao role;
	
	@Transient
	private String rolename;
	
	@Transient
	private Long clientkey;	

	@Transient
    private String passwordverify;

	@Transient
    private String oldpassword;
	

    

}
