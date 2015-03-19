package meg.biblio.common.db.dao;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
@Table(name="client")
public class ClientDao {
	
	private Long clientnr;
	
	@NotNull
	private String name;
	
	private String importmapconfig;
	
	private String importfileconfig;
	
	private String classifyimplementation;
	
	private String imagepath;
	
	private Long lastBcBase;
	
	private String barcodesheetxsl;
	
	@OneToMany(mappedBy = "client",cascade = CascadeType.PERSIST, fetch=FetchType.LAZY)
	private List<UserLoginDao> users;
	
	private Integer studentcheckouttime;
	
	private Integer teachercheckouttime;
	
	private Integer studentCOLimit;
	
	private Integer teacherCOLimit;
	
	private String overduexslbase;
	
	private String shortname;
	
	private Long lastBookNr;
	
	private String classsummaryxslbase;
	
	private Boolean usesBarcodes;
	
	private Long detailCompleteCode;
	
	private Boolean idForBarcode;
	
	private Long defaultStatus;
}

