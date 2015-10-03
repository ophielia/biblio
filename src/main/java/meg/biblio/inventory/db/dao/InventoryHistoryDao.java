package meg.biblio.inventory.db.dao;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import meg.biblio.catalog.db.dao.BookDao;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity(inheritanceType = "SINGLE_TABLE")
@Table(name="inventoryhistory")
public class InventoryHistoryDao {
	
	@ManyToOne
	private InventoryDao inventory;
	
	@OneToOne( fetch=FetchType.EAGER)
	private BookDao book;
	
	private Long originalstatus;
	
	private Long newstatus;
	
	
}
