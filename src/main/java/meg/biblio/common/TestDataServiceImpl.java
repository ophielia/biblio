package meg.biblio.common;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.ClassManagementService;
import meg.biblio.lending.LendingSearchCriteria;
import meg.biblio.lending.LendingSearchService;
import meg.biblio.lending.LendingService;
import meg.biblio.lending.db.*;
import meg.biblio.lending.db.dao.*;
import meg.biblio.lending.web.model.ClassModel;
import meg.biblio.search.BookSearchCriteria;
import meg.biblio.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class TestDataServiceImpl implements TestDataService {

    @Autowired
    ClassManagementService classService;

    @Autowired
    ClientService clientService;

    @Autowired
    SearchService searchService;

    @Autowired
    LendingService lendingService;

    @Autowired
    LendingSearchService lendingSearchService;


    @Autowired
    LoanRecordRepository lrRepo;


    @Autowired
    SchoolGroupRepository sgRepo;

    @Autowired
    PersonRepository personRepo;

    @Autowired
    StudentRepository studentRepo;

    @Autowired
    TeacherRepository teacherRepo;

    /**
     * clears all information for test client except for books. (students, and
     * lending history)
     */
    @Override
    public void clearAllTestData() {
        Long clientid = clientService.getTestClientId();
        ClientDao client = clientService.getClientForKey(clientid);
        clearLendingTestData();


        // delete from schoolgroup
        List<SchoolGroupDao> sgdelete = sgRepo.findSchoolGroupsByClient(client, new Sort("id"));
        List<SchoolGroupDao> sgnewdel = new ArrayList<SchoolGroupDao>();
        for (SchoolGroupDao sg : sgdelete) {
            sg.setTeacherlist(null);
            sgnewdel.add(sgRepo.save(sg));
        }

        // delete from students
        List<TeacherDao> teacherdel = teacherRepo.findAllTeachersForClient(client);
        List<TeacherDao> teachernewdel = new ArrayList<TeacherDao>();
        for (TeacherDao sg : teacherdel) {
            sg.setSchoolgroup(null);
            teachernewdel.add(teacherRepo.save(sg));
        }


        // delete from students
        List<StudentDao> persondel = studentRepo.findAllStudentsForClient(client);
        List<StudentDao> newdel = new ArrayList<StudentDao>();
        for (StudentDao sg : persondel) {
            sg.setSchoolgroup(null);
            newdel.add(studentRepo.save(sg));
        }
        studentRepo.delete(newdel);

        // now, delete from schoolgroup
        sgnewdel = sgRepo.findSchoolGroupsByClient(client, new Sort("id"));
        sgRepo.delete(sgnewdel);

        // and finally, the teachers
        teacherRepo.delete(teachernewdel);
    }

    @Override
    public void setAllTestData() {
        Long clientid = clientService.getTestClientId();
        // set classes
        // gryffindor
        SchoolGroupDao sgroup = new SchoolGroupDao();
        ClassModel model = new ClassModel(sgroup);
        model.setTeachername("Minerva McGonagall");
        model.fillInTeacherFromEntry();
        model = classService.createClassFromClassModel(model, clientid);

        // students
        createStudent("Harry Potter", ClassManagementService.Sections.PS, 1L, model.getSchoolGroup(), clientid);
        createStudent("Hermione Granger", ClassManagementService.Sections.PS, 2L, model.getSchoolGroup(), clientid);
        createStudent("Ron Weasley", ClassManagementService.Sections.PS, 3L, model.getSchoolGroup(), clientid);
        createStudent("Neville Longbottom", ClassManagementService.Sections.PS, 4L, model.getSchoolGroup(), clientid);
        createStudent("Fred Weasley", ClassManagementService.Sections.MS, 5L, model.getSchoolGroup(), clientid);
        createStudent("George Weasley", ClassManagementService.Sections.MS, 6L, model.getSchoolGroup(), clientid);
        createStudent("Seamus Finnegan", ClassManagementService.Sections.MS, 7L, model.getSchoolGroup(), clientid);
        createStudent("Parvati Patil", ClassManagementService.Sections.GS, 8L, model.getSchoolGroup(), clientid);
        createStudent("Lavender Brown", ClassManagementService.Sections.GS, 9L, model.getSchoolGroup(), clientid);
        createStudent("Lee Jordan", ClassManagementService.Sections.GS, 10L, model.getSchoolGroup(), clientid);

        // hufflepuff
        sgroup = new SchoolGroupDao();
        model = new ClassModel(sgroup);
        model.setTeachername("Pomona Sprout");
        model.fillInTeacherFromEntry();
        model = classService.createClassFromClassModel(model, clientid);

        // students
        createStudent("Cedric Diggory", ClassManagementService.Sections.PS, 11L, model.getSchoolGroup(), clientid);
        createStudent("Hannah Longbottom", ClassManagementService.Sections.PS, 12L, model.getSchoolGroup(), clientid);
        createStudent("Justin Finch-Fletchley", ClassManagementService.Sections.PS, 13L, model.getSchoolGroup(), clientid);
        createStudent("Ernie Macmillan", ClassManagementService.Sections.PS, 14L, model.getSchoolGroup(), clientid);
        createStudent("Nymphadora Tonks", ClassManagementService.Sections.MS, 15L, model.getSchoolGroup(), clientid);
        createStudent("Zacharias Smith", ClassManagementService.Sections.MS, 16L, model.getSchoolGroup(), clientid);
        createStudent("Bridget Wenlock", ClassManagementService.Sections.MS, 17L, model.getSchoolGroup(), clientid);
        createStudent("Grogan Stump", ClassManagementService.Sections.GS, 18L, model.getSchoolGroup(), clientid);
        createStudent("Newton Scamander", ClassManagementService.Sections.GS, 19L, model.getSchoolGroup(), clientid);
        createStudent("Anthony Otterburn", ClassManagementService.Sections.GS, 20L, model.getSchoolGroup(), clientid);

        // ravenclaw
        sgroup = new SchoolGroupDao();
        model = new ClassModel(sgroup);
        model.setTeachername("Filius Flitwick");
        model.fillInTeacherFromEntry();
        model = classService.createClassFromClassModel(model, clientid);

        // students
        createStudent("Luna Lovegood", ClassManagementService.Sections.PS, 21L, model.getSchoolGroup(), clientid);
        createStudent("Cho Chang", ClassManagementService.Sections.PS, 22L, model.getSchoolGroup(), clientid);
        createStudent("Penelope Clearwater", ClassManagementService.Sections.PS, 23L, model.getSchoolGroup(), clientid);
        createStudent("Padma Patil", ClassManagementService.Sections.MS, 24L, model.getSchoolGroup(), clientid);
        createStudent("Michael Corner", ClassManagementService.Sections.MS, 25L, model.getSchoolGroup(), clientid);
        createStudent("Anthony Goldstein", ClassManagementService.Sections.MS, 26L, model.getSchoolGroup(), clientid);
        createStudent("Marietta Edgecombe", ClassManagementService.Sections.MS, 27L, model.getSchoolGroup(), clientid);
        createStudent("Marcus Belby", ClassManagementService.Sections.GS, 28L, model.getSchoolGroup(), clientid);
        createStudent("Gilderoy Lockhart", ClassManagementService.Sections.GS, 29L, model.getSchoolGroup(), clientid);
        createStudent("Sybill Trelawney", ClassManagementService.Sections.GS, 30L, model.getSchoolGroup(), clientid);


        // slytherin
        sgroup = new SchoolGroupDao();
        model = new ClassModel(sgroup);
        model.setTeachername("Severus Snape");
        model.fillInTeacherFromEntry();
        model = classService.createClassFromClassModel(model, clientid);

        // students
        createStudent("Draco Malfoy", ClassManagementService.Sections.PS, 31L, model.getSchoolGroup(), clientid);
        createStudent("Gregory Goyle", ClassManagementService.Sections.PS, 32L, model.getSchoolGroup(), clientid);
        createStudent("Vincent Crabbe", ClassManagementService.Sections.PS, 33L, model.getSchoolGroup(), clientid);
        createStudent("Flora Carrow", ClassManagementService.Sections.PS, 34L, model.getSchoolGroup(), clientid);
        createStudent("Hestia Carrow", ClassManagementService.Sections.MS, 35L, model.getSchoolGroup(), clientid);
        createStudent("Marcus Flint", ClassManagementService.Sections.MS, 36L, model.getSchoolGroup(), clientid);
        createStudent("Miles Bletchley", ClassManagementService.Sections.MS, 37L, model.getSchoolGroup(), clientid);
        createStudent("Millicent Bulstrode", ClassManagementService.Sections.GS, 38L, model.getSchoolGroup(), clientid);
        createStudent("Pansy Parkinson", ClassManagementService.Sections.GS, 39L, model.getSchoolGroup(), clientid);
        createStudent("Blaise Zabini", ClassManagementService.Sections.GS, 40L, model.getSchoolGroup(), clientid);

        // work on checkout
        setLendingTestData();


    }

    private void createStudent(String name, Long sectionnr, Long codenr,
                               SchoolGroupDao schoolGroup, Long clientid) {
        StudentDao student = classService.addNewStudentToClass(name, sectionnr,
                schoolGroup, clientid);
        student.setBarcodeid(getCodeForNumber(codenr, BarcodeService.CodeType.PERSON));
        studentRepo.save(student);

    }

    public String getCodeForNumber(Long number, String type) {
        int barcodelength = 12;
        String filler = "000000000000";
        if (number != null && type != null) {
            String base = String.valueOf(number);
            int fillerlength = barcodelength - base.length();
            String codefiller = filler.substring(0, fillerlength);
            String barcode = type + codefiller + base;
            return barcode;
        }
        return null;
    }

    @Override
    public void clearLendingTestData() {
        Long clientid = clientService.getTestClientId();
        ClientDao client = clientService.getClientForKey(clientid);
        // return all checked out books
        LendingSearchCriteria lsc = new LendingSearchCriteria(LendingSearchCriteria.LendingType.ALL);
        List<LoanRecordDisplay> lrs = lendingSearchService.findLoanRecordsByCriteria(lsc, clientid);

        for (LoanRecordDisplay lrd : lrs) {
            lendingService.returnBookByBookid(lrd.getBookid(), clientid);
        }

        // delete from loan history
        List<LoanRecordDao> lhdelete = lrRepo.findForClient(client);
        lrRepo.delete(lhdelete);


    }


    public void setLendingTestData() {
        Long clientid = clientService.getTestClientId();
        // get classes
        List<SchoolGroupDao> sgroups = classService.getClassesForClient(clientid);
        List<ClassModel> classes = new ArrayList<ClassModel>();
        Long teachercode = 41L;
        for (SchoolGroupDao sg : sgroups) {
            ClassModel cmodel = classService.loadClassModelById(sg.getId());
            TeacherDao teacher = cmodel.getTeacher();
            teacher.setBarcodeid(getCodeForNumber(teachercode, BarcodeService.CodeType.PERSON));
            teacherRepo.save(teacher);
            teachercode++;
            classes.add(cmodel);
        }

        // determine last week checkout date
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, -8);
        Date lastweekco = cal.getTime();
        Date lastweekdue = null;
        // get books to checkout
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setSingleStatus(CatalogService.Status.SHELVED);
        List<BookDao> books = searchService.findBooksForCriteria(criteria, null, clientid);
        int bookcount = 0;
        int booklength = books.size();
        // go through classes
        for (ClassModel cm : classes) {
            List<StudentDao> students = cm.getStudents();
            int studentcount = 0;
            for (StudentDao student : students) {
                // checkout books to everyone - first five - manipulate to last week
                if (bookcount < booklength) {
                    BookDao book = books.get(bookcount);
                    bookcount++;
                    // checkout book
                    LoanRecordDao lr = lendingService.checkoutBook(book.getId(), student.getId(), clientid);
                    if (studentcount < 5) {
                        // move to last week
                        lr.setCheckoutdate(lastweekco);
                        if (lastweekdue == null) {
                            cal.setTime(lr.getDuedate());
                            cal.add(Calendar.DAY_OF_MONTH, -8);
                            lastweekdue = cal.getTime();
                        }
                        lr.setDuedate(lastweekdue);
                    }
                    studentcount++;
                }
            }
        }


        // return books from "last week" - all except 1 or 2
        for (ClassModel cm : classes) {
            LendingSearchCriteria lsc = new LendingSearchCriteria(LendingSearchCriteria.LendingType.ALL);
            lsc.setSchoolgroup(cm.getClassid());
            lsc.setStartDate(lastweekco);
            lsc.setCheckedoutOnly(true);
            List<LoanRecordDisplay> lrs = lendingSearchService.findLoanRecordsByCriteria(lsc, clientid);

            int i = 0;
            int top = 4;
            if (cm.getTeachername() != null && cm.getTeachername().contains("Snape")) {
                top = 3;
            }

            for (LoanRecordDisplay lrd : lrs) {
                if (i < top) {
                    lendingService.returnBook(lrd.getLoanrecordid(), clientid);
                } else {
                    break;
                }
                i++;
            }

        }

    }
}
