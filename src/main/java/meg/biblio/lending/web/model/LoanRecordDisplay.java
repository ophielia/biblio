package meg.biblio.lending.web.model;

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
		@EntityResult(entityClass = meg.biblio.lending.web.model.LoanRecordDisplay.class, fields = {
		@FieldResult(name = "loanrecordid", column = "loanrecordid"),
		@FieldResult(name = "bookid", column = "bookid"),
		@FieldResult(name = "classid", column = "classid"),
		@FieldResult(name = "clientid", column = "clientid"),
		@FieldResult(name = "borrowerid", column = "borrowerid"),
		@FieldResult(name = "borrowerfn", column = "borrowerfn"),
		@FieldResult(name = "borrowerln", column = "borrowerln"),
		@FieldResult(name = "booktitle", column = "booktitle"),
		@FieldResult(name = "bookclientid", column = "bookclientid"),
		@FieldResult(name = "author", column = "author"),
		@FieldResult(name = "shelfclass", column = "shelfclass"),
		@FieldResult(name = "checkedout", column = "checkedout"),
		@FieldResult(name = "returned", column = "returned"),
		@FieldResult(name = "duedate", column = "duedate"),
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
	private String author;
	private Long shelfclass;
	private Date checkedout;
	private Date returned;
	private Date duedate;
	private String teacherfirstname;
	private String teacherlastname;

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

	@XmlElement
	public Date getReturned() {
		return returned;
	}

	@XmlElement
	public Date getDuedate() {
		return duedate;
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

}
