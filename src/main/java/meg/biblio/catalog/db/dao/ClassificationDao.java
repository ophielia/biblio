package meg.biblio.catalog.db.dao;
import javax.persistence.Table;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
@Table(name="classification")
public class ClassificationDao {
	
	private Long clientid;
	private Long key;
	private String textdisplay;
	private String imagedisplay;
	private String language;
	private String description;
	

}
