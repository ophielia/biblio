package meg.biblio.lending;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LendingServiceImpl implements LendingService {

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

		// save teacher, and reset in class
		teacher = teacherRepo.save(teacher);
		sgroup.setTeacher(teacher);

		// configure school year dates
		Integer schoolbegin = getSchoolYearBeginForDate(new Date());
		sgroup.setSchoolyearbegin(schoolbegin);
		sgroup.setSchoolyearend(schoolbegin + 1);

		// persist
		sgroup = sgroupRepo.save(sgroup);

		// reload model
		ClassModel newclass = loadClassModelById(sgroup.getId());
		// return reloaded model
		return newclass;
	}

	@Override
	public ClassModel loadClassModelById(Long id) {
		// get schoolgroup
		SchoolGroupDao schoolgroup = sgroupRepo.findOne(id);
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
	public List<SchoolGroupDao> getClassesForClient(Long clientkey) {
		ClientDao client = clientService.getClientForKey(clientkey);

		return sgroupRepo.findSchoolGroupByClient(client);
	}

	@Override
	public ClassModel addNewStudentToClass(String name, Long sectionkey,
			SchoolGroupDao schoolgroup, Long clientkey) {
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
		// load classmodel and return
		ClassModel model = loadClassModelById(schoolgroup.getId());
		return model;
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
							student.setClient(null);
							toremove.add(student);
						} else {
							// if studentid is not in removelist, add to
							// noremove list
							noremove.add(student);
						}

						// save all students in save list
						studentRepo.save(toremove);
						// set noremove in schoolgroup and save schoolgroup
						schoolgroup.setStudents(noremove);
						sgroupRepo.save(schoolgroup);

						// reload classlist, and return
						ClassModel model = loadClassModelById(schoolgroup
								.getId());
					}
				}
			}
		}
		return null;
	}

}
