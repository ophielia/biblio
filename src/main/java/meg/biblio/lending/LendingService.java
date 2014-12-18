package meg.biblio.lending;

import java.util.Date;
import java.util.List;

import meg.biblio.common.report.ClassSummaryReport;
import meg.biblio.common.report.OverdueBookReport;
import meg.biblio.lending.db.dao.LoanHistoryDao;
import meg.biblio.lending.db.dao.LoanRecordDao;
import meg.biblio.lending.web.model.LoanRecordDisplay;

public interface LendingService {

	LoanRecordDao checkoutBook(Long bookid, Long borrowerid, Long clientid);

	LoanHistoryDao returnBook(Long loanrecordid, Long clientid);

	List<LoanRecordDisplay> getCheckedOutBooksForClass(Long classid, Long clientid);

	List<LoanRecordDisplay> getCheckedOutBooksForUser(Long borrowerId,
			Long clientid);

	int getLendLimitForBorrower(Long borrowerId, Long clientid);

	List<LoanRecordDisplay> getOverdueBooksForClient(Long id);

	List<LoanRecordDisplay> getCheckedOutBooksForClient(Long id);

	OverdueBookReport assembleOverdueBookReport(Long clientid);

	ClassSummaryReport assembleClassSummaryReport(Long classid, Date date,
			Long clientid);

}
