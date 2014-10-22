package meg.biblio.catalog.db.dao;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
@Table(name="found_details")
public class FoundDetailsDao {
	
	private Long bookid;
	private String title;
	private String authors;
	private String illustrators;
	@Lob 
	@Column( length=2512)	
	private String description;
	private String publisher;
	private Long publishyear;
	private String isbn10;
	private String isbn13;
	private String language;
	private String type;
	private String imagelink;
	private String searchserviceid;
	
}
