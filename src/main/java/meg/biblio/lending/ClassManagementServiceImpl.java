package meg.biblio.lending;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import meg.biblio.common.ClientService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.db.SchoolGroupRepository;
import meg.biblio.lending.db.StudentRepository;
import meg.biblio.lending.db.TeacherRepository;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.db.dao.TeacherDao;
import meg.biblio.lending.web.model.ClassModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClassManagementServiceImpl implements ClassManagementService {

	@Autowired
	ClientService clientService;

	@Autowired
	SchoolGroupRepository sgroupRepo;

	@Autowired
	TeacherRepository teacherRepo;

	@Autowired
	StudentRepository studentRepo;

	/**
	 * This method creates a class from a (rather empty) ClassModel. It takes
	 * the teacher entry, creates a TeacherDao object, and places this in a new
	 * SchoolGroupDao object. Then, the schoolyear information is added, and the
	 * SchoolGroupDao object is persisted, reloaded, and returned.
	 */
	@Override
	public ClassModel createClassFromClassModel(ClassModel model, Long clientkey) {
		// get client
		ClientDao client = clientService.getClientForKey(clientkey);
		// get class and teacher
		SchoolGroupDao sgroup = model.getSchoolGroup();
		TeacherDao teacher = sgroup.getTeacher();

		// set client in teacher and class
		sgroup.setClient(client);
		teacher.setClient(client);
		teacher.setActive(true);
		// save teacher, and reset in class
		teacher = teacherRepo.save(teacher);
		sgroup.setTeacher(teacher);

		// configure school year dates
		Integer schoolbegin = getSchoolYearBeginForDate(new Date());
		sgroup.setSchoolyearbegin(schoolbegin);
		sgroup.setSchoolyearend(schoolbegin + 1);

		// persist
		sgroup = sgroupRepo.save(sgroup);

		// save class in teacher
		teacher.setSchoolgroup(sgroup);
		teacherRepo.save(teacher);
		
		// reload model
		ClassModel newclass = loadClassModelById(sgroup.getId());
		// return reloaded model
		return newclass;
	}

	@Override
	public ClassModel loadClassModelById(Long id) {
		// get schoolgroup
		SchoolGroupDao schoolgroup = sgroupRepo.findOne(id);

		if (schoolgroup != null) {
			// get active student list
			List<StudentDao> students = getStudentsForClass(schoolgroup,schoolgroup.getClient());
			schoolgroup.setStudents(students);
		}

		ClassModel loadclass = new ClassModel(schoolgroup);
		// set in model
		return loadclass;
	}

	/**
	 * Returns the beginning of the schoolyear according to date - for example,
	 * if called in september 1989, the school year will be 1989/1990. If called
	 * in march 2000, it will also be be 1999/2000. But in august 2000, it will
	 * be 2000/2001. Any date before July will be classified as the current
	 * school year. Anything after July will be classified as the next school
	 * year.
	 */
	@Override
	public Integer getSchoolYearBeginForDate(Date currentdate) {
		// make calendar
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentdate);
		// get month, year
		int currentmonth = cal.get(Calendar.MONTH);
		int currentyear = cal.get(Calendar.YEAR);
		// if month between January and June, the begin year is the previous
		// year
		if (currentmonth < Calendar.JULY) {
			return new Integer(currentyear - 1);
		} else {
			// if month after June, the begin year is the current
			return new Integer(currentyear);
		}

	}

	@Override
	public StudentDao addNewStudentToClass(String name, Long sectionkey,
			SchoolGroupDao sgroup, Long clientkey) {
		// get schoolgroup
		SchoolGroupDao schoolgroup = sgroupRepo.findOne(sgroup.getId());
		// create student
		StudentDao student = new StudentDao();
		student.fillInName(name);
		// fill in section key
		student.setSectionkey(sectionkey);
		// set schoolgroup in student
		student.setSchoolgroup(schoolgroup);
		// set client in student
		ClientDao client = clientService.getClientForKey(clientkey);
		student.setClient(client);
		student.setActive(true);
		// persist student
		student = studentRepo.saveAndFlush(student);
		// save in schoolgroup
		List<StudentDao> students = schoolgroup.getStudents();
		if (students == null) {
			students = new ArrayList<StudentDao>();
		}
		students.add(student);
		schoolgroup.setStudents(students);
		sgroupRepo.save(schoolgroup);

		return student;
	}

	@Override
	public ClassModel removeStudentsFromClass(List<Long> removelist,
			SchoolGroupDao schoolgroup, Long clientid) {
		// ensure clientid matches schoolgroup client
		if (schoolgroup.getClient() != null
				&& schoolgroup.getClient().getId().longValue() == clientid
						.longValue()) {

			// make array of students who aren't removed
			// make array of students who are - and who will need to be saved
			List<StudentDao> noremove = new ArrayList<StudentDao>();
			List<StudentDao> toremove = new ArrayList<StudentDao>();

			// get students from studentgroup
			List<StudentDao> students = schoolgroup.getStudents();
			if (students != null) {
				for (StudentDao student : students) {
					// ensure clientids match
					if (student.getClient().getId().longValue() == clientid
							.longValue()) {
						// if studentid is in removelist set client to null, and
						// add to tosave list
						if (removelist.contains(student.getId())) {
							student.setSchoolgroup(null);
							toremove.add(student);
						} else {
							// if studentid is not in removelist, add to
							// noremove list
							noremove.add(student);
						}

					}
				}

				// save all students in save list
				studentRepo.save(toremove);
				// set noremove in schoolgroup and save schoolgroup
				schoolgroup.setStudents(noremove);
				sgroupRepo.save(schoolgroup);

				// reload classlist, and return
				ClassModel model = loadClassModelById(schoolgroup
						.getId());
				return model;
			}
		}
		return null;
	}

	@Override
	public ClassModel assignStudentsToClass(List<Long> toassign,
			SchoolGroupDao schoolgroup, Long clientid) {
		// load schoolgroup
		schoolgroup = sgroupRepo.findOne(schoolgroup.getId());
		// load client
		ClientDao client = clientService.getClientForKey(clientid);
		// ensure clientid matches schoolgroup client
		if (schoolgroup.getClient() != null
				&& schoolgroup.getClient().getId().longValue() == clientid
						.longValue()) {

			// get students from studentgroup
			List<StudentDao> students = schoolgroup.getStudents();
			if (students == null) {
				students = new ArrayList<StudentDao>();
			}
			// go through students to assign
			if (toassign != null && toassign.size() > 0) {
				List<StudentDao> studentstoassign = studentRepo
						.findAll(toassign);
				for (StudentDao student : studentstoassign) {
					// ensure clientids match
					Long studentclient = student.getClient() != null ? student
							.getClient().getId() : 0L;
					if (studentclient.longValue() == clientid.longValue()) {
						// set new class in student
						student.setSchoolgroup(schoolgroup);
					}
				}
				// persist all changes to student
				studentRepo.save(studentstoassign);
				// add all new students to the student list in the class
				students.addAll(studentstoassign);
				schoolgroup.setStudents(students);
				// persist the change to the class
				sgroupRepo.save(schoolgroup);
			}
		}
		// load class and return
		ClassModel model = loadClassModelById(schoolgroup.getId());
		return model;
	}

	@Override
	public StudentDao editStudent(Long clientid, Long studentid,
			String firstname, String lastname, Long section) {
		// get original student from db
		StudentDao dbstudent = studentRepo.findOne(studentid);
		// get client
		ClientDao client = clientService.getClientForKey(clientid);
		// ensure student belongs to client
		if (clientid.longValue() == dbstudent.getClient().getId().longValue()) {
			// set changed data in student object
			dbstudent.setFirstname(firstname);
			dbstudent.setLastname(lastname);
			dbstudent.setSectionkey(section);
			// save student object
			dbstudent = studentRepo.save(dbstudent);
		}
		// return student
		return dbstudent;
	}

	@Override
	public List<SchoolGroupDao> getClassesForClient(Long clientkey) {
		ClientDao client = clientService.getClientForKey(clientkey);
	
		return sgroupRepo.findSchoolGroupByClient(client);
	}

	@Override
	public List<StudentDao> getUnassignedStudents(Long clientid) {
		ClientDao client = clientService.getClientForKey(clientid);

		List<StudentDao> unassigned = studentRepo.findActiveUnassignedStudents(client, new Sort(Sort.Direction.ASC, "sectionkey").and(new Sort(Sort.Direction.ASC, "firstname")));

		return unassigned;
	}

	@Override
	public ClassModel loadClassModelForStudent(Long studentid) {
		StudentDao student = studentRepo.findOne(studentid);
		// get class from student
		SchoolGroupDao sclass = student.getSchoolgroup();
		// load model
		ClassModel model = loadClassModelById(sclass.getId());
		return model;
	}

	@Override
	public void deleteClass(Long classid, Long clientid) {
		// get class
		SchoolGroupDao sgroup = sgroupRepo.findOne(classid);
		if (sgroup != null) {
			if (sgroup.getClient().getId().longValue()==clientid.longValue()) {
				// set client to null
				sgroup.setClient(null);
				// delete teacher - set null in class
				TeacherDao teacher = sgroup.getTeacher();
				teacher.setClient(null);
				teacher.setActive(false);
				sgroup.setTeacher(null);
				
				// unassign students - set null in class
				List<StudentDao> students = sgroup.getStudents();
				for (StudentDao student : students) {
					student.setSchoolgroup(null);
				}
				sgroup.setStudents(null);
				
				// save class and students
				sgroupRepo.save(sgroup);
				teacherRepo.save(teacher);
				for (StudentDao student:students) {
					studentRepo.saveAndFlush(student);	
				}
				
				
				// delete class and teacher
				sgroupRepo.delete(sgroup);
			}
		}
	}
	
	@Override
	public void moveAllStudentsToNextSection(Long clientid) {
		ClientDao client = clientService.getClientForKey(clientid);
		// get active students for client
		List<StudentDao> activestudents = studentRepo.findActiveStudentsForClient(client);
		// increment all section keys by 1
		for (StudentDao student:activestudents) {
			student.setSectionkey(student.getSectionkey()+1);
			// save active students			
			studentRepo.saveAndFlush(student);
		}
		
		// update school year in classes for client
		Integer newbegin = getSchoolYearBeginForDate(new Date());
		List<SchoolGroupDao> classes = getClassesForClient(clientid);
		for (SchoolGroupDao clientclass:classes) {
			clientclass.setSchoolyearbegin(newbegin);
			clientclass.setSchoolyearend(newbegin+1);
			sgroupRepo.saveAndFlush(clientclass);
		}
	}
	
	@Override
	public void clearStudentListsForClient(Long clientid) {
		ClientDao client = clientService.getClientForKey(clientid);
		// clear students from class side first
		List<SchoolGroupDao> classes = getClassesForClient(clientid);
		for (SchoolGroupDao clientclass:classes) {
			clientclass.setStudents(null);
			sgroupRepo.save(clientclass);
		}
		
		
		// get active students for client
		List<StudentDao> activestudents = studentRepo.findActiveStudentsForClient(client);
		// for each active student - set schoolgroup to null, save schoolgroup
		for (StudentDao student:activestudents) {
			student.setSchoolgroup(null);
			studentRepo.saveAndFlush(student);
		}
	}
	
	@Override
	public void setStudentsAsInactive(List<Long> inactivelist,Long clientid) {
		ClientDao client = clientService.getClientForKey(clientid);
		// get Students for ids
		List<StudentDao> toupdate = studentRepo.findAll(inactivelist);
		// go through all students - 
		for (StudentDao upd:toupdate) {
			// ensure client id matches
			Long studentclient = upd.getClient().getId();
			if (studentclient.longValue()==clientid.longValue()) {
				// setting active to false, and saving students
				upd.setActive(false);
				studentRepo.saveAndFlush(upd);
			}
		}
	}

	@Override
	public List<StudentDao> getStudentsForClass(Long classid, Long clientid) {
		// get client id
		ClientDao client = clientService.getClientForKey(clientid);
		
		// get sgroupid
		SchoolGroupDao sgroup = sgroupRepo.findOne(classid);
		
		if (sgroup.getClient().getId().longValue() == clientid.longValue()) {
			// get students			
			List<StudentDao> students = getStudentsForClass(sgroup,client);
					return students;
		}

		
		// return students
		return new ArrayList<StudentDao>();
	}


	@Override
	public HashMap<Long, TeacherDao> getTeacherByClassForClient(Long clientid) {
		// get Client
		ClientDao client = clientService.getClientForKey(clientid);
		// make result hash
		 HashMap<Long, TeacherDao> info = new  HashMap<Long, TeacherDao>();
		 
		 // get teachers
		 List<TeacherDao> teachers = teacherRepo.findActiveTeachersForClient(client);
		 
		 // put into hash
		 for (TeacherDao teacher:teachers) {
			 // key is schoolgroupid, value is teacher object
			 Long schoolgroupid = teacher.getSchoolgroup().getId();
			 info.put(schoolgroupid,teacher);
		 }
		return info;
	}
	

	
	
	
	private List<StudentDao> getStudentsForClass(SchoolGroupDao sgroup,ClientDao client) {
		if (sgroup.getClient().getId().longValue() == client.getId().longValue()) {
			// get students			
			List<StudentDao> students = studentRepo.findActiveStudentsForClass(sgroup, client, new Sort(Sort.Direction.ASC, "sectionkey").and(new Sort(Sort.Direction.ASC, "firstname")));
					return students;
		}

		
		// return students
		return new ArrayList<StudentDao>();		
	}
	
}