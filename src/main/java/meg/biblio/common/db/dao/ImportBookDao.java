package meg.biblio.common.db.dao;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
public class ImportBookDao {
	
	private String clientbookid;
	private String title;
	private String author;
	private String illustrator;
	private String publisher;
	private String error;
	
}
