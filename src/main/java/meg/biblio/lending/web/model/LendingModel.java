package meg.biblio.lending.web.model;

import java.util.List;

import meg.biblio.lending.db.dao.StudentDao;

public class LendingModel {

	List<LoanRecordDisplay> checkedout;
	List<StudentDao> studentlist;
	Long classid;
	Long borrowerid;
	Long bookid;
	Long clientid;

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
	}

	public Object getStudentList() {
		return this.studentlist;
	}

	public void setStudentList(List<StudentDao> studentlist) {
		this.studentlist = studentlist;
	}

	public Long getBorrowerId() {
		return borrowerid;
	}

	public void setBorrowerId(Long personid, Object studentList) {
		this.borrowerid = borrowerid;
	}

	public void setBookid(Long bookid) {
		this.bookid = bookid;
	}

	public Long getBookid() {
		return bookid;
	}

	public Long getClientid() {
		return this.clientid;
	}

	public void setClientid(Long clientid) {
		this.clientid=clientid;
	}
}
