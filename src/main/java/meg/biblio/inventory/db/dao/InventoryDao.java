package meg.biblio.inventory.db.dao;
import java.util.Date;

import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity(inheritanceType = "SINGLE_TABLE")
@Table(name="inventory")
public class InventoryDao {
	
	private Long clientid;
	
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(style = "M-")
	private Date startdate;

	@Temporal(TemporalType.DATE)
	@DateTimeFormat(style = "M-")
	private Date enddate;
	
	private Integer tobecounted;
	private Integer totalcounted;
	private Integer addedtocount;
	private Integer reconciled;
	private Boolean completed;

}
