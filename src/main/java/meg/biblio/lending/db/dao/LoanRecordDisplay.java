package meg.biblio.lending.db.dao;

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

@XmlRootElement(name = "LoanRecord")
@Entity
@Table(name = "loanrecorddisplay")
@SqlResultSetMapping(name = "LRDisp", entities = { 
		@EntityResult(entityClass = LoanRecordDisplay.class, fields = {
		@FieldResult(name = "loanrecordid", column = "loanrecordid"),
		@FieldResult(name = "bookid", column = "bookid"),
		@FieldResult(name = "classid", column = "classid"),
		@FieldResult(name = "clientid", column = "clientid"),
		@FieldResult(name = "borrowerid", column = "borrowerid"),
		@FieldResult(name = "borrowerfn", column = "borrowerfn"),
		@FieldResult(name = "borrowerln", column = "borrowerln"),
		@FieldResult(name = "booktitle", column = "booktitle"),
		@FieldResult(name = "bookclientid", column = "bookclientid"),
		@FieldResult(name = "bookclientidsort", column = "bookclientidsort"),
		@FieldResult(name = "author", column = "author"),
		@FieldResult(name = "shelfclass", column = "shelfclass"),
		@FieldResult(name = "checkedout", column = "checkedout"),
		@FieldResult(name = "returned", column = "returned"),
		@FieldResult(name = "duedate", column = "duedate"),
		@FieldResult(name = "returnedlate", column = "returnedlate"),
		@FieldResult(name = "currentlyoverdue", column = "currentlyoverdue"),
		@FieldResult(name = "currentlycheckedout", column = "currentlycheckedout"),
		@FieldResult(name = "overdue", column = "overdue"),
		@FieldResult(name = "isteacher", column = "isteacher"),
		@FieldResult(name = "teacherfirstname", column = "teacherfirstname"),
		@FieldResult(name = "teacherlastname", column = "teacherlastname")
 }) }, columns = {})
public class LoanRecordDisplay {

@Id
	private Long loanrecordid;
	private Long borrowerid;
	private Long bookid;
	private Long classid;
	private Long clientid;
	private String borrowerfn;
	private String borrowerln;
	private String booktitle;
	private String bookclientid;
	private Long bookclientidsort;
	private String author;
	private Long shelfclass;
	private Date checkedout;
	private Date returned;
	private Date duedate;
	private String teacherfirstname;
	private String teacherlastname;
	private Boolean returnedlate;
	private Boolean currentlyoverdue;
	private Boolean currentlycheckedout;
	private Boolean overdue;
	private Boolean isteacher;
	
	
	
	public Long getLoanrecordid() {
		return loanrecordid;
	}

	public Long getBorrowerid() {
		return borrowerid;
	}

	public Long getBookid() {
		return bookid;
	}

	public Long getClassid() {
		return classid;
	}

	public Long getClientid() {
		return clientid;
	}

	@XmlElement(name = "firstname_borrower")
	public String getBorrowerfn() {
		return borrowerfn;
	}

	@XmlElement(name = "lastname_borrower")
	public String getBorrowerln() {
		return borrowerln;
	}

	@XmlElement
	public String getBooktitle() {
		return booktitle;
	}

	@XmlElement
	public String getBookclientid() {
		return bookclientid;
	}

	@XmlElement(name = "bookauthor")
	public String getAuthor() {
		return author;
	}

	@XmlTransient
	public Long getShelfclass() {
		return shelfclass;
	}

	@XmlElement
	public Date getCheckedout() {
		return checkedout;
	}

	public String getCheckedoutDisplay() {

		
		if (duedate!=null) {
			SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-YYYY");
			return fmt.format(checkedout);	
		}
		return "";
	}

	@XmlElement
	public Date getReturned() {
		return returned;
	}

	public String getReturnedDisplay() {
		if (duedate!=null) {
			SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-YYYY");
			return fmt.format(returned);	
		}
		return "";
		
	}	
	@XmlElement
	public Date getDuedate() {
		return duedate;
	}

	public String getDuedateDisplay() {
		if (duedate!=null) {
			SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-YYYY");
			return fmt.format(duedate);	
		}
		return "";
	}	
	@XmlElement
	public Boolean getIsoverdue() {
		if (returned!=null && duedate!=null) {
			return returned.after(duedate);
		} else if (returned == null && duedate!=null) {
			return new Date().after(duedate);
		}
		return false;
	}

	@XmlElement(name = "firstname_teacher")
	public String getTeacherfirstname() {
		return teacherfirstname;
	}

	@XmlElement(name = "lastname_teacher")
	public String getTeacherlastname() {
		return teacherlastname;
	}

	@XmlElement
	public Boolean getReturnedlate() {
		return returnedlate;
	}

	@XmlElement
	public Boolean getCurrentlyoverdue() {
		return currentlyoverdue;
	}

	@XmlElement
	public Boolean getCurrentlycheckedout() {
		return currentlycheckedout;
	}

	@XmlElement
	public Boolean getOverdue() {
		return overdue;
	}

	@XmlElement
	public Boolean getIsteacher() {
		return isteacher!=null && isteacher;
	}

	public Long getBookclientidsort() {
		return bookclientidsort;
	}

	
}
