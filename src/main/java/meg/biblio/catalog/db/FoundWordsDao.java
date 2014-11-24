package meg.biblio.catalog.db;
import javax.persistence.ManyToOne;

import meg.biblio.catalog.db.dao.BookDao;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
public class FoundWordsDao {
	
	@ManyToOne
	private BookDao book;
	private String word;
	private Integer countintext;
}
