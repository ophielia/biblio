package meg.biblio.catalog.db;

import javax.persistence.ManyToOne;
import javax.persistence.Table;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDetailDao;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
@Table(name = "foundwords")
public class FoundWordsDao {

	@ManyToOne
	private BookDetailDao bookdetail;
	private String word;
	private Integer countintext;

	public void copyFrom(FoundWordsDao copyfrom) {
		if (copyfrom != null) {
			if (copyfrom.word != null) {
				this.word = copyfrom.word;
			}
			if (copyfrom.countintext != null) {
				this.countintext = copyfrom.countintext;
			}
		}
	}
}
