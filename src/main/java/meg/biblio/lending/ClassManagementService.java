package meg.biblio.lending;

import java.util.HashMap;
import java.util.List;

import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.web.model.ClassModel;
import meg.biblio.lending.web.model.TeacherInfo;

public interface ClassManagementService {

	public final static class Sections {
		public final static Long PS=1L;
		public final static Long MS=2L;
		public final static Long GS=3L;
	} 
	
	public static final String sectionLkup = "grades";
	public static final String sectionSelect = "gradesSelect";
	
	ClassModel createClassFromClassModel(ClassModel model, Long clientkey);


	List<SchoolGroupDao> getClassesForClient(Long clientkey);

	ClassModel loadClassModelById(Long id);

	StudentDao addNewStudentToClass(String name, Long sectionkey,
			SchoolGroupDao sgroup, Long clientkey);

	ClassModel removeStudentsFromClass(List<Long> removelist,
			SchoolGroupDao schoolgroup, Long clientid);

	ClassModel assignStudentsToClass(List<Long> toassign,
			SchoolGroupDao schoolgroup, Long clientid);

	StudentDao editStudent(Long clientid, Long studentid, String firstname,
			String lastname, Long section);

	List<StudentDao> getUnassignedStudents(Long clientid);

	ClassModel loadClassModelForStudent(Long studentid);

	void deleteClass(Long classid, Long clientid);

	void moveAllStudentsToNextSection(Long clientid);

	void clearStudentListsForClient(Long clientid);

	void setStudentsAsInactive(List<Long> inactivelist, Long clientid);

	List<StudentDao> getStudentsForClass(Long classid, Long clientid);

	HashMap<Long,TeacherInfo> getTeacherByClassForClient(Long clientid);

	SchoolGroupDao getClassForClient(Long classid, Long clientid);


	TeacherInfo getTeacherByTeacherid(Long teacherid);


	StudentDao getStudentById(Long studentid, Long clientid);


}
