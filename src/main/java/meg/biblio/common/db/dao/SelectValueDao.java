package meg.biblio.common.db.dao;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
public class SelectValueDao {
	
	@NotNull
	@Size(max = 100)
	private String value;

	private Long languagekey;
	
	@NotNull
	@Size(max = 100)
	private String display;


	private Boolean active;

	@NotNull
	private Long disporder;

	@ManyToOne
	@JoinColumn(name = "keyid")
	private SelectKeyDao selectkey;
}
