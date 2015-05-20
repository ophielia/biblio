package meg.biblio.catalog.db.dao;
import java.io.Serializable;

import javax.persistence.Table;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
@Table(name="subject")
public class SubjectDao implements Serializable {
	private String listing;
	
	
	public void copyFrom(SubjectDao copyfrom) {
		if (copyfrom!=null) {
			if (copyfrom.listing != null) {
				this.listing = copyfrom.listing;
			}
		}
	}
}
