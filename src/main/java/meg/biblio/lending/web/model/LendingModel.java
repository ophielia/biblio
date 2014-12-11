package meg.biblio.lending.web.model;

import java.util.HashMap;
import java.util.List;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.lending.db.dao.StudentDao;

public class LendingModel {

	List<LoanRecordDisplay> checkedout;
	List<StudentDao> studentlist;
	Long classid;
	Long borrowerid;
	String bookid;
	Long clientid;
	TeacherInfo teacher;
	private String borrowerfn;
	private String borrowerln;
	private HashMap<Long, TeacherInfo> classinfo;
	private List<LoanRecordDisplay> usercheckedout;
	private int borrowerlimit;
	private BookDao book;
	
	public List<LoanRecordDisplay> getCheckedOutList() {
		return checkedout;

	}

	public void setCheckedOutList(List<LoanRecordDisplay> checkedout) {
		this.checkedout = checkedout;
	}

	public Long getClassid() {
		return classid;
	}

	public void setClassid(Long classid) {
		this.classid = classid;
		// now set teacher from classid
		for (Long id:classinfo.keySet()) {
			if (id.longValue()==classid.longValue()) {
				teacher = classinfo.get(id);
			}
		}
	}

	public List<StudentDao> getStudentList() {
		return this.studentlist;
	}

	public void setStudentList(List<StudentDao> studentlist) {
		this.studentlist = studentlist;
	}

	public Long getBorrowerId() {
		return borrowerid;
	}

	public void setBorrowerId(Long personid, List<StudentDao> studentList) {
		this.borrowerid = personid;
		for (StudentDao student:studentList) {
			if (student.getId().longValue()==borrowerid.longValue()) {
				this.borrowerfn = student.getFirstname();
				this.borrowerln = student.getLastname();
			}
		}
	}

	
	public String getBorrowerfn() {
		return borrowerfn;
	}

	public void setBorrowerfn(String borrowerfn) {
		this.borrowerfn = borrowerfn;
	}

	public String getBorrowerln() {
		return borrowerln;
	}

	public void setBorrowerln(String borrowerln) {
		this.borrowerln = borrowerln;
	}

	public void setBookid(String bookid) {
		this.bookid = bookid;
	}

	public String getBookid() {
		return bookid;
	}

	public Long getClientid() {
		return this.clientid;
	}

	public void setClientid(Long clientid) {
		this.clientid=clientid;
	}

	public void setClassInfo(HashMap<Long, TeacherInfo> classinfo) {
		this.classinfo = classinfo;
	}

	public HashMap<Long, TeacherInfo> getClassinfo() {
		return classinfo;
	}

	public void setBorrowerCheckedOut(List<LoanRecordDisplay> checkedoutforuser) {
		this.usercheckedout = checkedoutforuser;
		
	}
	
	public List<LoanRecordDisplay> getBorrowerCheckedOut( ) {
		 return this.usercheckedout;
		
	}
	
	

	public void setBorrowerLimit(int borrowerlimit) {
		this.borrowerlimit=borrowerlimit;
		
	}
	
	public boolean getBorrowerReachedLimit() {
		int co = this.usercheckedout!=null?this.usercheckedout.size():0;
		return co>=this.borrowerlimit;
	}

	public void setBook(BookDao book) {
		this.book = book;
		
	}
	
	public BookDao getBook() {
		return this.book;
		
	}	


	
	
}
