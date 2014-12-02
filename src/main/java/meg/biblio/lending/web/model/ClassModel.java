package meg.biblio.lending.web.model;

import java.util.ArrayList;
import java.util.List;

import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.db.dao.TeacherDao;

public class ClassModel {

	private SchoolGroupDao schoolgroup;

	private String teachername;

	private String teacheremail;

	private String studentname;

	private Long studentsection;

	public ClassModel(SchoolGroupDao schoolgroup) {
		super();
		this.schoolgroup = schoolgroup;
	}

	public ClassModel() {
		super();
		this.schoolgroup = new SchoolGroupDao();
	}

	/** getters and setters - main object **/
	public SchoolGroupDao getSchoolGroup() {
		return schoolgroup;
	}

	public void setSchoolGroup(SchoolGroupDao schoolgroup) {
		this.schoolgroup = schoolgroup;
	}

	public TeacherDao getTeacher() {
		return this.schoolgroup.getTeacher();
	}
	
	public List<StudentDao> getStudents() {
		if (this.schoolgroup!=null && this.schoolgroup.getStudents()!=null) {
			return this.schoolgroup.getStudents();
		}
		return new ArrayList<StudentDao>();
	}

	/** getters and setters - fields **/
	public String getTeachername() {
		return teachername;
	}

	public void setTeachername(String teachername) {
		this.teachername = teachername;
	}

	public String getTeacheremail() {
		return teacheremail;
	}

	public void setTeacheremail(String teacheremail) {
		this.teacheremail = teacheremail;
	}

	public String getStudentname() {
		return studentname;
	}

	public void setStudentname(String studentname) {
		this.studentname = studentname;
	}

	public Long getStudentsection() {
		return studentsection;
	}

	public void setStudentsection(Long studentsection) {
		this.studentsection = studentsection;
	}

	// convenience methods
	public void fillInTeacherFromEntry() {
		TeacherDao teacher = new TeacherDao();
		if (teachername != null) {
			teacher.fillInName(teachername);
		}
		if (teacheremail != null) {
			teacher.setEmail(teacheremail);
		}
		this.schoolgroup.setTeacher(teacher);
	}
	
	public Long getClassid() {
		if (this.getSchoolGroup()!=null) {
			return this.getSchoolGroup().getId();
		}
		return null;
	}

}
