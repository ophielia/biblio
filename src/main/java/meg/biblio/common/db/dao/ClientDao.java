package meg.biblio.common.db.dao;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
@Table(name="client")
public class ClientDao {
	
	@NotNull
	private String name;
	
	private String importmapconfig;
	
	private String importfileconfig;
	
	private String classifyimplementation;
	
	private String imagepath;
	
	@OneToMany(mappedBy = "client",cascade = CascadeType.ALL, fetch=FetchType.LAZY)
	private List<UserLoginDao> users;
}

