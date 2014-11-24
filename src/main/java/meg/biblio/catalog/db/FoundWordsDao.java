package meg.biblio.catalog.db;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import meg.biblio.catalog.db.dao.BookDao;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
@Table(name="foundwords")
public class FoundWordsDao {
	
	@ManyToOne
	private BookDao book;
	private String word;
	private Integer countintext;
}
