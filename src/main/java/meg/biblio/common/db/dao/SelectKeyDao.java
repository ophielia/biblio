package meg.biblio.common.db.dao;
import java.util.List;

import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
public class SelectKeyDao {
	
	@NotNull
	@Size(max = 100)
	private String lookup;
	
	
	@OneToMany(mappedBy = "selectkey", fetch=FetchType.EAGER)
	private List<SelectValueDao> selectvalues;
}
