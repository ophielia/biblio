package meg.biblio.lending;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.ClientService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.report.ClassSummaryReport;
import meg.biblio.common.report.DailySummaryReport;
import meg.biblio.common.report.OverdueBookReport;
import meg.biblio.lending.db.LoanRecordRepository;
import meg.biblio.lending.db.PersonRepository;
import meg.biblio.lending.db.dao.LoanRecordDao;
import meg.biblio.lending.db.dao.PersonDao;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.db.dao.TeacherDao;
import meg.biblio.lending.web.model.LoanRecordDisplay;
import meg.biblio.lending.web.model.TeacherInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LendingServiceImpl implements LendingService {

	@Autowired
	ClientService clientService;

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
		String teachername = null;
		Long teacherid = null;
		if (person instanceof StudentDao) {
			isteacher = false;
			StudentDao st = (StudentDao) person;
			SchoolGroupDao sg = st.getSchoolgroup();
			schoolyear = sg.getSchoolyearbegin();
			TeacherDao tch = sg.getTeacher();
			if (tch!=null ) {
				teacherid =tch.getId();
			}
		} else if (person instanceof TeacherDao) {
			TeacherDao st = (TeacherDao) person;
			SchoolGroupDao sg = st.getSchoolgroup();
			schoolyear = sg.getSchoolyearbegin();
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
		ClientDao client = clientService.getClientForKey(clientid);

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
		LendingSearchCriteria criteria = new LendingSearchCriteria();
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
		LendingSearchCriteria criteria = new LendingSearchCriteria();
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
		LendingSearchCriteria criteria = new LendingSearchCriteria();
		criteria.setOverdueOnly(true);
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
		LendingSearchCriteria criteria = new LendingSearchCriteria();
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
		if (criteria!=null) {
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
		LendingSearchCriteria criteria = new LendingSearchCriteria();
		criteria.setSchoolgroup(classid);
		criteria.setCheckedouton(date);
		criteria.setCheckedoutOnly(true);
		List<LoanRecordDisplay> checkedout = lendingSearch
				.findLoanRecordsByCriteria(criteria, clientid);
		summaryreport.setCheckedoutlist(checkedout);

		// overdue on date
		criteria.reset();
		criteria.setSchoolgroup(classid);
		criteria.setOverdueOnly(true);
		criteria.setCheckedoutOnly(true);
		List<LoanRecordDisplay> overdue = lendingSearch
				.findLoanRecordsByCriteria(criteria, clientid);
		summaryreport.setOverduelist(overdue);

		// returned on date
		criteria.reset();
		criteria.setOverdueOnly(null);
		criteria.setSchoolgroup(classid);
		criteria.setReturnedon(date);
		List<LoanRecordDisplay> returned = lendingSearch
				.findLoanRecordsByCriteria(criteria, clientid);
		summaryreport.setReturnedlist(returned);

		return summaryreport;
	}

	@Override
	public LoanRecordDao returnBookByBookid(Long bookid, Long clientid) {
		// find loanrecord
		// build criteria
		LendingSearchCriteria criteria = new LendingSearchCriteria();
		criteria.setBookid(bookid);
		criteria.setCheckedoutOnly(true);
		// search for loan records
		List<LoanRecordDisplay> checkedout = lendingSearch
				.findLoanRecordsByCriteria(criteria, clientid);
		
		// get loan record
		if (checkedout!=null && checkedout.size()>0) {
			LoanRecordDisplay disp = checkedout.get(0);
			// return book
			return returnBook(disp.getLoanrecordid(),clientid);
		} 
		return null;
	}

	@Override
	public DailySummaryReport assembleDailySummaryReport(Date date,
			Long clientid, Boolean includeEmpties) {
		List<SchoolGroupDao> classes = classService.getClassesForClient(clientid);
		List<ClassSummaryReport> results = new ArrayList<ClassSummaryReport>();
		for (SchoolGroupDao sgroup:classes) {
			ClassSummaryReport csum = assembleClassSummaryReport(sgroup.getId(),date, clientid);
			if (!csum.isEmpty()) {
				results.add(csum);
			} else if (includeEmpties) {
				results.add(csum);
			}
		}
		DailySummaryReport report = new DailySummaryReport(results); 
		return report;
	}

}
