package meg.biblio.lending;

import java.util.Date;
import java.util.List;

import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.web.model.ClassModel;

public interface LendingService {

	ClassModel createClassFromClassModel(ClassModel model, Long clientkey);

	Integer getSchoolYearBeginForDate(Date time);

	List<SchoolGroupDao> getClassesForClient(Long clientkey);

	ClassModel loadClassModelById(Long id);

}
