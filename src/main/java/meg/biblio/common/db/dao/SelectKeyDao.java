package meg.biblio.common.db.dao;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
@Table(name="select_key")
public class SelectKeyDao {
	
	@NotNull
	@Size(max = 100)
	private String lookup;
	
	
	@OneToMany(mappedBy = "selectkey",cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	private List<SelectValueDao> selectvalues;
}
