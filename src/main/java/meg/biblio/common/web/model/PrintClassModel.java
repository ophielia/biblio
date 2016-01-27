package meg.biblio.common.web.model;

import java.util.ArrayList;
import java.util.List;

import meg.biblio.lending.db.dao.PersonDao;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;

public class PrintClassModel {

	private SchoolGroupDao schoolgroup;
	private Long teacherid;

	private List<Boolean> checked;
	private List<String> idref;

	private Long newClassId;
	private Long currentClassId;
	
	private Long showBorder;
	private Long nudge;
	private Long startPos;
	
	public static final String startPosLkup="startpos65";
	public static final String nudgeLkup="printnudge";
	
	public List<Boolean> getChecked() {
		return checked;
	}

	public void setChecked(List<Boolean> checked) {
		this.checked = checked;
	}

	public SchoolGroupDao getSchoolgroup() {
		return schoolgroup;
	}

	public void setSchoolgroup(SchoolGroupDao sgroup) {
		this.schoolgroup = sgroup;
		this.currentClassId = sgroup.getId();
		// set teacherid
		this.teacherid = sgroup.getTeacher().getId();
		// set checked to all false
		checked = new ArrayList<Boolean>();
		idref = new ArrayList<String>();
		// adding for teacher
		checked.add(false);
		idref.add(String.valueOf(this.teacherid));
		for (StudentDao student : sgroup.getStudents()) {
			checked.add(false);
			idref.add(String.valueOf(student.getId()));
		}
	}

	public Long getTeacherid() {
		return this.teacherid;
	}

	public void setTeacherid(Long teacherid) {
		this.teacherid = teacherid;
	}

	public List<PersonDao> getPrintoutList() {
		List<PersonDao> printout = new ArrayList<PersonDao>();
		if (schoolgroup != null) {
			// return teacher
			printout.add(schoolgroup.getTeacher());
			// return all students
			for (StudentDao student : schoolgroup.getStudents()) {
				printout.add(student);
			}
			return printout;
		}
		return null;
	}

	public void selectEntireClass() {
		// set checked to all false
		if (checked == null) {
			checked = new ArrayList<Boolean>();
		} else {
			checked.clear();
		}
		for (int i = 0; i < idref.size(); i++) {
			checked.add(true);
		}
	}

	public void setSelectedList(List<String> cachevals) {
		// go through printout list (students and teachers)
		// if id is in cachevals, mark corresponding checked as true
		if (idref != null) {
			for (int i = 0; i < idref.size(); i++) {
				if (cachevals.contains(idref.get(i))) {
					checked.set(i, true);
				}

			}
		}
	}

	public List<String> getSelectedValuesAsStrings() {
		// go through checked. If true, pull id for
		// corresponding student/teacher id, and add to String list
		List<String> selectedvalues = new ArrayList<String>();

		if (checked != null) {
			int i = 0;
			for (Boolean check : checked) {
				if (check != null && check) {
					selectedvalues.add(idref.get(i));
				}
				i++;
			}
		}
		return selectedvalues;
	}

	public List<String> getIdref() {
		return idref;
	}

	public void setIdref(List<String> idref) {
		this.idref = idref;
	}

	public Long getCurrentClassId() {
		return currentClassId;
	}

	public void setCurrentClassId(Long currentClassId) {
		this.currentClassId = currentClassId;
	}

	public Long getNewClassId() {
		return newClassId;
	}

	public void setNewClassId(Long classId) {
		this.newClassId = classId;
	}

	public Long getShowBorder() {
		return showBorder;
	}

	public void setShowBorder(Long showBorder) {
		this.showBorder = showBorder;
	}

	public Long getNudge() {
		return nudge;
	}

	public void setNudge(Long nudge) {
		this.nudge = nudge;
	}

	public Long getStartPos() {
		return startPos;
	}

	public void setStartPos(Long startPos) {
		this.startPos = startPos;
	}



}
