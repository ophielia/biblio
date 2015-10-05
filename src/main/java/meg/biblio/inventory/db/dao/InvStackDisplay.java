package meg.biblio.inventory.db.dao;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

// use LoanRecordDisplay as model
public class InvStackDisplay {

	private Long bookid;
	private Long clientid;
	private String clientbooknr;
	private Long clientshelfcode;
	private Long status;
	private String note;
	private Date counteddate;
	private Boolean reconciled;
	private Boolean tocount;
	private Long userid;
	private Long countstatus;
	private String title;

	
	
	public InvStackDisplay(Long bookid, Long clientid, String clientbooknr,
			Long clientshelfcode, Long status, String note, Date counteddate,
			Boolean reconciled, Boolean tocount, Long userid, Long countstatus,
			String title) {
		super();
		this.bookid = bookid;
		this.clientid = clientid;
		this.clientbooknr = clientbooknr;
		this.clientshelfcode = clientshelfcode;
		this.status = status;
		this.note = note;
		this.counteddate = counteddate;
		this.reconciled = reconciled;
		this.tocount = tocount;
		this.userid = userid;
		this.countstatus = countstatus;
		this.title = title;
	}
	
	
	
	public Long getBookid() {
		return bookid;
	}
	public Long getClientid() {
		return clientid;
	}
	public String getClientbooknr() {
		return clientbooknr;
	}
	public Long getClientshelfcode() {
		return clientshelfcode;
	}
	public Long getStatus() {
		return status;
	}
	public String getNote() {
		return note;
	}
	public Date getCounteddate() {
		return counteddate;
	}
	public Boolean getReconciled() {
		return reconciled;
	}
	public Boolean getTocount() {
		return tocount;
	}
	public Long getUserid() {
		return userid;
	}
	public Long getCountstatus() {
		return countstatus;
	}
	public String getTitle() {
		return title;
	}

	
	
	
	
}
