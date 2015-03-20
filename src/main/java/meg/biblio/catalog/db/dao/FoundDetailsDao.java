package meg.biblio.catalog.db.dao;
import javax.persistence.Column;
import javax.persistence.Table;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
@Table(name="found_details")
public class FoundDetailsDao {
	
	private Long bookdetailid;
	private String title;
	private String authors;
	private String illustrators;
	@Column( length=2000)	
	private String description;
	private String publisher;
	private Long publishyear;
	private String isbn10;
	private String isbn13;
	private String language;
	private String type;
	private String imagelink;
	private String searchserviceid;
	private Long searchsource;
	

	public void setDescription(String description) {
		if (description!=null && description.length()>1510) {
			this.description = description.substring(0,1510);	
		}
		this.description = description;
    }


}
