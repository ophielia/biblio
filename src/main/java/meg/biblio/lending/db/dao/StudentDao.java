package meg.biblio.lending.db.dao;
import javax.persistence.ManyToOne;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
public class StudentDao extends PersonDao {
	
	private Boolean active;
	private Long sectionkey;
	
	@ManyToOne
	private SchoolGroupDao schoolgroup;
	
}
