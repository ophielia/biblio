package meg.biblio.lending;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.ClientService;
import meg.biblio.common.ScalarFunction;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.report.ClassSummaryReport;
import meg.biblio.common.report.DailySummaryReport;
import meg.biblio.common.report.OverdueBookReport;
import meg.biblio.common.report.TableReport;
import meg.biblio.lending.db.LoanRecordRepository;
import meg.biblio.lending.db.PersonRepository;
import meg.biblio.lending.db.dao.*;
import meg.tools.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.text.DateFormat;
import java.util.*;

@Service
@Transactional
public class LendingServiceImpl implements LendingService {

    @Autowired
    ClientService clientService;

    @Autowired
    ScalarFunction<Integer> scalarDb;

    @Autowired
    ScalarFunction<BigInteger> scalarCountDb;

    @Autowired
    CatalogService catalogService;

    @Autowired
    ClassManagementService classService;

    @Autowired
    LendingSearchService lendingSearch;

    @Autowired
    BookRepository bookRepo;

    @Autowired
    PersonRepository personRepo;

    @Autowired
    LoanRecordRepository lrRepo;


    @Autowired
    ApplicationContext appContext;

    @Override
    public LoanRecordDao checkoutBook(Long bookid, Long borrowerid,
                                      Long clientid) {
        // get objects for ids - book, borrower, client
        ClientDao client = clientService.getClientForKey(clientid);
        BookDao book = bookRepo.findOne(bookid);
        PersonDao person = personRepo.findOne(borrowerid);

        Integer schoolyear = 0;
        boolean isteacher = true;
        Long teacherid = null;
        Long studentsection = null;
        schoolyear = DateUtils.getSchoolYearBeginForDate(new Date());
        if (person instanceof StudentDao) {
            isteacher = false;
            StudentDao st = (StudentDao) person;
            SchoolGroupDao sg = st.getSchoolgroup();
            TeacherDao tch = sg.getTeacher();
            if (tch != null) {
                teacherid = tch.getId();
            }
            studentsection = st.getSectionkey();
        } else if (person instanceof TeacherDao) {
            teacherid = person.getId();
        }

        // make new loan record
        LoanRecordDao loanrec = new LoanRecordDao();
        // insert objects
        loanrec.setBorrower(person);
        loanrec.setBook(book);
        loanrec.setClient(client);
        loanrec.setSchoolyear(schoolyear);
        loanrec.setTeacherid(teacherid);
        loanrec.setBorrowersection(studentsection);

        // insert dates - checkedout and due
        Integer checkoutdays = isteacher ? client.getTeachercheckouttime()
                : client.getStudentcheckouttime();
        Calendar ddatecal = Calendar.getInstance();
        ddatecal.setTime(new Date());
        ddatecal.add(Calendar.DAY_OF_MONTH, checkoutdays.intValue());
        Date duedate = ddatecal.getTime();

        loanrec.setCheckoutdate(new Date());
        loanrec.setDuedate(duedate);

        // persist loan record
        loanrec = lrRepo.save(loanrec);

        // update book itself - set status to checked out
        book = catalogService.updateBookStatus(book.getId(),
                CatalogService.Status.CHECKEDOUT);

        // return loan record
        return loanrec;
    }

    @Override
    public LoanRecordDao returnBook(Long loanrecordid, Long clientid) {
        // get loanrecord, client
        LoanRecordDao lrecord = lrRepo.findOne(loanrecordid);

        if (lrecord != null) {

            // fill in return date
            lrecord.setReturned(new Date());

            // persist loanrecord
            lrecord = lrRepo.save(lrecord);

            // update book itself - set status to shelved
            catalogService.updateBookStatus(lrecord.getBook().getId(),
                    CatalogService.Status.SHELVED);

            // return loanrecord
            return lrecord;

        }
        return null;
    }

    @Override
    public List<LoanRecordDisplay> getCheckedOutBooksForClass(Long classid,
                                                              Long clientid) {
        // build criteria
        LendingSearchCriteria criteria = new LendingSearchCriteria(LendingSearchCriteria.LendingType.CURRENT_CHECKEDOUT);
        criteria.setSchoolgroup(classid);
        criteria.setCheckedoutOnly(true);
        // search for loan records
        List<LoanRecordDisplay> checkedout = lendingSearch
                .findLoanRecordsByCriteria(criteria, clientid);
        // return list
        return checkedout;
    }

    @Override
    public List<LoanRecordDisplay> getCheckedOutBooksForUser(Long borrowerId,
                                                             Long clientid) {
        // build criteria
        LendingSearchCriteria criteria = new LendingSearchCriteria(LendingSearchCriteria.LendingType.CURRENT_CHECKEDOUT);
        criteria.setBorrowerid(borrowerId);
        criteria.setCheckedoutOnly(true);
        // search for loan records
        List<LoanRecordDisplay> checkedout = lendingSearch
                .findLoanRecordsByCriteria(criteria, clientid);
        // return list
        return checkedout;
    }

    @Override
    public int getLendLimitForBorrower(Long borrowerId, Long clientid) {
        // get client
        ClientDao client = clientService.getClientForKey(clientid);
        // get borrower
        PersonDao person = personRepo.findOne(borrowerId);
        boolean isteacher = (person != null && person instanceof TeacherDao);

        if (isteacher) {
            return client.getTeacherCOLimit().intValue();
        }
        return client.getStudentCOLimit();
    }

    @Override
    public List<LoanRecordDisplay> getOverdueBooksForClient(Long clientid) {
        // build criteria
        LendingSearchCriteria criteria = new LendingSearchCriteria(LendingSearchCriteria.LendingType.CURRENT_OVERDUE);
        criteria.setCheckedoutOnly(true);
        // search for loan records
        List<LoanRecordDisplay> overdue = lendingSearch
                .findLoanRecordsByCriteria(criteria, clientid);
        // return list
        return overdue;
    }

    @Override
    public List<LoanRecordDisplay> getCheckedOutBooksForClient(Long clientid) {
        // build criteria
        LendingSearchCriteria criteria = new LendingSearchCriteria(LendingSearchCriteria.LendingType.CURRENT_CHECKEDOUT);
        criteria.setCheckedoutOnly(true);
        // search for loan records
        List<LoanRecordDisplay> checkedout = lendingSearch
                .findLoanRecordsByCriteria(criteria, clientid);
        // return list
        return checkedout;
    }

    @Override
    public OverdueBookReport assembleOverdueBookReport(Long clientid) {
        ClientDao client = clientService.getClientForKey(clientid);
        OverdueBookReport obr = new OverdueBookReport();
        obr.setRundate(new Date());
        obr.setClientname(client.getName());

        List<LoanRecordDisplay> overdue = getOverdueBooksForClient(clientid);

        obr.setBooklist(overdue);

        return obr;
    }

    @Override
    public List<LoanRecordDisplay> searchLendingHistory(LendingSearchCriteria criteria, Long clientid) {
        if (criteria != null) {
            List<LoanRecordDisplay> checkedout = lendingSearch
                    .findLoanRecordsByCriteria(criteria, clientid);
            return checkedout;
        }
        return null;

    }

    @Override
    public ClassSummaryReport assembleClassSummaryReport(Long classid,
                                                         Date date, Long clientid) {
        // get client
        ClientDao client = clientService.getClientForKey(clientid);
        // get schoolgroup
        SchoolGroupDao sg = classService.getClassForClient(classid, clientid);

        // create ClassSummaryReport from schoolgroup
        ClassSummaryReport summaryreport = new ClassSummaryReport(sg);

        // fill in rundate, client
        summaryreport.setClientname(client.getName());
        summaryreport.setRundate(date);

        // fill in lists...
        // checkedout on date
        LendingSearchCriteria criteria = new LendingSearchCriteria(LendingSearchCriteria.LendingType.CHECKEDOUT);
        criteria.setSchoolgroup(classid);
        criteria.setTimeselect(LendingSearchCriteria.TimePeriodType.THISWEEK);
        criteria.setCheckedoutOnly(true);
        List<LoanRecordDisplay> checkedout = lendingSearch
                .findLoanRecordsByCriteria(criteria, clientid);
        summaryreport.setCheckedoutlist(checkedout);

        // overdue on date
        criteria = new LendingSearchCriteria(LendingSearchCriteria.LendingType.CURRENT_OVERDUE);
        criteria.setSchoolgroup(classid);
        criteria.setCheckedoutOnly(true);
        List<LoanRecordDisplay> overdue = lendingSearch
                .findLoanRecordsByCriteria(criteria, clientid);
        summaryreport.setOverduelist(overdue);

        // returned on date
        criteria = new LendingSearchCriteria(LendingSearchCriteria.LendingType.RETURNED);
        criteria.setSchoolgroup(classid);
        criteria.setTimeselect(LendingSearchCriteria.TimePeriodType.THISWEEK);
        List<LoanRecordDisplay> returned = lendingSearch
                .findLoanRecordsByCriteria(criteria, clientid);
        summaryreport.setReturnedlist(returned);

        return summaryreport;
    }

    @Override
    public LoanRecordDao returnBookByBookid(Long bookid, Long clientid) {
        // find loanrecord
        // build criteria
        LendingSearchCriteria criteria = new LendingSearchCriteria(LendingSearchCriteria.LendingType.CURRENT_CHECKEDOUT);
        criteria.setBookid(bookid);
        criteria.setCheckedoutOnly(true);
        // search for loan records
        List<LoanRecordDisplay> checkedout = lendingSearch
                .findLoanRecordsByCriteria(criteria, clientid);

        // get loan record
        if (checkedout != null && checkedout.size() > 0) {
            LoanRecordDisplay disp = checkedout.get(0);
            // return book
            return returnBook(disp.getLoanrecordid(), clientid);
        }
        return null;
    }

    @Override
    public DailySummaryReport assembleWeeklySummaryReport(Date date,
                                                          Long clientid, Boolean includeEmpties) {
        List<SchoolGroupDao> classes = classService.getClassesForClient(clientid);
        List<ClassSummaryReport> results = new ArrayList<ClassSummaryReport>();
        for (SchoolGroupDao sgroup : classes) {
            ClassSummaryReport csum = assembleClassSummaryReport(sgroup.getId(), date, clientid);
            if (!csum.isEmpty()) {
                results.add(csum);
            } else if (includeEmpties) {
                results.add(csum);
            }
        }
        DailySummaryReport report = new DailySummaryReport(results);
        return report;
    }

    @Override
    public List<LoanRecordDisplay> getLendingHistoryByBorrower(Long studentid,
                                                               Long clientid) {
        // assemble criteria (studentid only, by checkout date, descending)
        LendingSearchCriteria criteria = new LendingSearchCriteria(LendingSearchCriteria.LendingType.CHECKEDOUT);
        criteria.setBorrowerid(studentid);
        criteria.setSortKey(LendingSearchCriteria.SortKey.CHECKEDOUT);
        criteria.setSortDir(LendingSearchCriteria.SortByDir.DESC);

        // perform search
        List<LoanRecordDisplay> history = searchLendingHistory(criteria, clientid);

        // return records
        return history;
    }

    @Override
    public List<LoanRecordDisplay> getLendingHistoryForBook(Long bookid,
                                                            Long clientid) {
        // assemble criteria (studentid only, by checkout date, descending)
        LendingSearchCriteria criteria = new LendingSearchCriteria(LendingSearchCriteria.LendingType.CHECKEDOUT);
        criteria.setBookid(bookid);
        criteria.setSortKey(LendingSearchCriteria.SortKey.CHECKEDOUT);
        criteria.setSortDir(LendingSearchCriteria.SortByDir.DESC);

        // perform search
        List<LoanRecordDisplay> history = searchLendingHistory(criteria, clientid);

        // return records
        return history;
    }

    @Override
    public Integer getFirstLendingYearForClient(Long clientid) {
        String sql = "select  min(schoolyear)  from loanrecord where client=" + clientid;
        Integer minlendyear = scalarDb.singleResult(sql);

        return minlendyear;
    }

    @Override
    public Integer getCheckoutCountForBook(Long bookid, Long clientid) {
        String sql = "select count(id) from loanrecord where book = " + bookid + " and client=" + clientid;
        BigInteger checkoutcnt = scalarCountDb.singleResult(sql);

        return checkoutcnt.intValue();
    }

    @Override
    public TableReport getLendingHistoryReport(LendingSearchCriteria criteria,
                                               Long clientid, Locale locale, MessageSource messageSource) {
        if (criteria != null) {
            if (locale == null) {
                locale = Locale.US;
            }

            List<LoanRecordDisplay> checkedout = lendingSearch
                    .findLoanRecordsByCriteria(criteria, clientid);

            // create TableReport
            String title = messageSource.getMessage("menu_lendinghistory",
                    null, locale);
            // add title
            TableReport tr = new TableReport(title);
            tr.setFontsize("10pt");

            // add column headers
            String lclass = messageSource.getMessage("label_class", null, locale);
            String lstudent = messageSource.getMessage("label_class_student", null, locale);
            String lbookid = messageSource.getMessage("label_book_clientbookid", null, locale);
            String ltitle = messageSource.getMessage("label_book_title", null, locale) + " / " +
                    messageSource.getMessage("label_book_author", null, locale);
            String lcheckedout = messageSource.getMessage("label_lending_checkedout", null, locale) + " / " +
                    messageSource.getMessage("label_lending_duedate", null, locale);
            String lreturned = messageSource.getMessage("label_lendingreturned", null, locale);

            tr.addColHeader(lclass, "1.5cm");
            tr.addColHeader(lstudent);
            tr.addColHeader(lbookid, "1.5cm");
            tr.addColHeader(ltitle);
            tr.addColHeader(lcheckedout, "2.5cm");
            tr.addColHeader(lreturned, "2.5cm");

            // add values
            DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
            for (LoanRecordDisplay record : checkedout) {

                tr.addValue(record.getTeacherfirstname());
                tr.addValue(record.getBorrowerfn() + " " + record.getBorrowerln());
                tr.addValue(record.getBookclientid());
                String titleval = record.getBooktitle() + " / " + record.getAuthor();
                tr.addValue(titleval);
                String checkedoutval = df.format(record.getCheckedout()) + " / " + df.format(record.getDuedate());
                tr.addValue(checkedoutval);
                String returnedval = record.getReturned() != null ? df.format(record.getReturned()) : " ";
                tr.addValue(returnedval);
            }

            // return TableReport
            return tr;

        }
        return null;
    }


}
