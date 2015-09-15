package meg.biblio.lending.db.dao;

import java.util.Date;

import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.db.dao.ClientDao;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
@Table(name="loanrecord")
public class LoanRecordDao {

	@OneToOne
	private ClientDao client;
	
	@OneToOne
	private BookDao book;

	@OneToOne
	private PersonDao borrower;

	@Temporal(TemporalType.DATE)
	@DateTimeFormat(style = "M-")
	private Date checkoutdate;

	@Temporal(TemporalType.DATE)
	@DateTimeFormat(style = "M-")
	private Date duedate;
	
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(style = "M-")
	private Date returned;
	
	private Integer schoolyear;

	private Long teacherid;
	
	private Long borrowersection;
}
