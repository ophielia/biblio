package meg.biblio.catalog.db.dao;

import java.io.Serializable;

import javax.persistence.Table;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
@Table(name = "publisher")
public class PublisherDao  implements Serializable  {
	public String name;

	public void copyFrom(PublisherDao copyfrom) {
		if (copyfrom != null) {
			if (copyfrom.name != null) {
				this.name = copyfrom.name;
			}
		}
	}
}
