package meg.biblio.catalog.db.dao;
import javax.persistence.Table;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
@Table(name="artist")
public class ArtistDao {
	
	private String lastname;
	private String firstname;
	private String middlename;
	
	
	public boolean hasFirstname() {
		return firstname!=null && firstname.length()>0;
	}
	
	public boolean hasMiddlename() {
		return middlename!=null && middlename.length()>0;
	}
	
	public boolean hasLastname() {
		return lastname!=null && lastname.length()>0;
	}	
}
