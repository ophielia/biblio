package meg.biblio.lending.web.model;

import java.util.Date;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.lending.db.dao.LoanRecordDao;
import meg.biblio.lending.db.dao.PersonDao;

@XmlRootElement(name = "LoanRecord")
public class LoanRecordDisplay {

	private LoanRecordDao loanrecord;
	private PersonDao person;
	private BookDao book;
	private Long classid;
	private TeacherInfo teacherinfo;

	public LoanRecordDisplay(LoanRecordDao loanrecord, PersonDao person,
			BookDao book, Long classid) {
		super();
		this.loanrecord = loanrecord;
		this.person = person;
		this.book = book;
		this.classid = classid;
	}

	public LoanRecordDisplay() {
		super();
	}

	@XmlTransient
	public Long getLoanrecordid() {
		return this.loanrecord.getId();
	}

	@XmlTransient
	public Long getBorrowerid() {
		return this.person.getId();
	}

	@XmlTransient
	public Long getClientid() {
		return this.person.getClient().getId();
	}

	@XmlTransient
	public Long getBookid() {
		return this.book.getId();
	}

	@XmlTransient
	public Long getClassid() {
		return classid;
	}

	@XmlElement(name = "firstname_borrower")
	public String getBorrowerfn() {
		return this.person.getFirstname();
	}

	@XmlElement(name = "lastname_borrower")
	public String getBorrowerln() {
		return this.person.getLastname();
	}

	@XmlElement
	public String getBooktitle() {
		return this.book.getBookdetail().getTitle();
	}

	@XmlElement
	public String getBookclientid() {
		return this.book.getClientbookid();
	}

	@XmlElement(name = "bookauthor")
	public String getAuthor() {
		return this.book.getBookdetail().getAuthorsAsString();
	}

	@XmlTransient
	public Long getShelfclass() {
		return this.book.getClientshelfcode();
	}

	@XmlElement
	public Date getCheckedout() {
		return this.loanrecord.getCheckoutdate();
	}
	
	@XmlElement
	public Date getReturned() {
		return this.loanrecord.getReturned();
	}
	

	@XmlElement
	public Date getDuedate() {
		return this.loanrecord.getDuedate();
	}

	public Boolean getIsoverdue() {
		Date today = new Date();
		return today.after(getDuedate());
	}

	@XmlElement(name = "firstname_teacher")
	public String getTeacherfirstname() {
		if (teacherinfo != null && teacherinfo.getFirstname() != null) {
			return teacherinfo.getFirstname();
		}
		return "";
	}

	@XmlElement(name = "lastname_teacher")
	public String getTeacherlastname() {
		if (teacherinfo != null && teacherinfo.getLastname() != null) {
			return teacherinfo.getLastname();
		}
		return "";
	}

	@XmlTransient
	public void setTeacherInfo(HashMap<Long, TeacherInfo> teacherInfo) {
		if (teacherInfo.containsKey(classid)) {
			this.teacherinfo = teacherInfo.get(classid);
		}
	}

}
