package meg.biblio.lending.web.model;

import java.util.ArrayList;
import java.util.List;

import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.db.dao.TeacherDao;

public class ClassModel {

	private SchoolGroupDao schoolgroup;

	private List<StudentDao> unassignedstudents;
	
	private String teachername;

	private String teacheremail;

	private String studentname;
	
	private String studentfirstname;

	private Long studentid;

	private Long studentsection;
	
	private List<Long> idref;
	private List<Boolean> checked;

	private List<Long> tridref;
	private List<Boolean> trchecked;

	public ClassModel(SchoolGroupDao schoolgroup) {
		super();
		this.schoolgroup = schoolgroup;
		if (schoolgroup!=null) {
			if (schoolgroup.getStudents()!=null) {
				createCheckedAndIdSlotsForRemove(schoolgroup.getStudents().size());		
			}
		}
		
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
		if (schoolgroup!=null) {
			if (schoolgroup.getStudents()!=null) {
				createCheckedAndIdSlotsForRemove(schoolgroup.getStudents().size());		
			}
		}
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

	
	public List<StudentDao> getUnassignedstudents() {
		return unassignedstudents;
	}

	public void setUnassignedstudents(List<StudentDao> unassignedstudents) {
		this.unassignedstudents = unassignedstudents;
		if (this.unassignedstudents!=null && this.unassignedstudents.size()>0) {
			// initialize checked list
			createCheckedAndIdSlotsForUnassigned(this.unassignedstudents.size());
		}	
	}

	public List<Long> getIdref() {
		return idref;
	}

	public void setIdref(List<Long> idref) {
		this.idref = idref;
	}

	public List<Boolean> getChecked() {
		return checked;
	}

	public void setChecked(List<Boolean> checked) {
		this.checked = checked;
	}

	
	
	public List<Long> getTridref() {
		return tridref;
	}

	public void setTridref(List<Long> tridref) {
		this.tridref = tridref;
	}

	public List<Boolean> getTrchecked() {
		return trchecked;
	}

	public void setTrchecked(List<Boolean> trchecked) {
		this.trchecked = trchecked;
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

	
	public String getStudentfirstname() {
		return studentfirstname;
	}

	public void setStudentfirstname(String studentfirstname) {
		this.studentfirstname = studentfirstname;
	}

	public Long getStudentid() {
		return studentid;
	}

	public void setStudentid(Long studentid) {
		this.studentid = studentid;
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
	
	private void createCheckedAndIdSlotsForUnassigned(int size) {
		checked = new ArrayList<Boolean>();
		idref = new ArrayList<Long>();
		for (int i=0;i<size;i++) {
			checked.add(false);
			idref.add(0L);
		}
		
	}
	
	public List<Long> getSelectedUnassignedIds() {
		// make new empty list 
		List<Long> checkedids = new ArrayList<Long>();
			// go through checked list
			for (int i=0;i<checked.size();i++) {
				// if checked is true, add expenseDao at same slot to checkedlist
				Boolean test = checked.get(i);
				if (test!=null && test) {
					checkedids.add(idref.get(i));
				}
			}
		// return checked list
		return checkedids;
	}	
	
	private void createCheckedAndIdSlotsForRemove(int size) {
		trchecked = new ArrayList<Boolean>();
		tridref = new ArrayList<Long>();
		for (int i=0;i<size;i++) {
			trchecked.add(false);
			tridref.add(0L);
		}
		
	}
	
	public List<Long> getSelectedIdsToRemove() {
		// make new empty list 
		List<Long> checkedids = new ArrayList<Long>();
			// go through checked list
			for (int i=0;i<trchecked.size();i++) {
				// if checked is true, add expenseDao at same slot to checkedlist
				Boolean test = trchecked.get(i);
				if (test!=null && test) {
					checkedids.add(tridref.get(i));
				}
			}
		// return checked list
		return checkedids;
	}

	public void setStudentInModel(Long studentid) {
// find student in list
		for (StudentDao student:getStudents()) {
			if (student.getId().longValue()==studentid.longValue()) {
				// set studentname, studentfirstname, sectionkey and id in model
				setStudentfirstname(student.getFirstname());
				setStudentname(student.getLastname());
				setStudentsection(student.getSectionkey());
				setStudentid(student.getId());
			}
		}
	}		

}
