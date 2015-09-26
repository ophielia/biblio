package meg.biblio.lending.web;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.ClassManagementService;
import meg.biblio.lending.LendingSearchCriteria;
import meg.biblio.lending.LendingService;
import meg.biblio.lending.db.dao.TeacherDao;
import meg.biblio.lending.web.model.LendingSearchModel;
import meg.biblio.lending.web.model.LoanRecordDisplay;
import meg.biblio.lending.web.model.TeacherInfo;
import meg.tools.DateUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/lendinghistory")
@Controller
public class LendingHistoryController {

	@Autowired
	ClassManagementService classService;

	@Autowired
	ClientService clientService;

	@Autowired
	SelectKeyService keyService;
	
	@Autowired
	LendingService lendingService;

	/** Entry Point Methods **/
	@RequestMapping(method = RequestMethod.GET, produces = "text/html")
	public String showHistoryPage(
			Model uiModel, HttpServletRequest httpServletRequest,
			Locale locale, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);

		LendingSearchModel searchModel = getDefaultLendingSearchModel();
		LendingSearchCriteria criteria = searchModel.getCriteria();
		List<LoanRecordDisplay> historyrecords = searchLoanHistory(client,criteria);

		// put results in model
		uiModel.addAttribute("historyRecords", historyrecords);
		uiModel.addAttribute("lendingSearchModel",searchModel);

		// return view
		return "lending/history";
	}



	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
	public String showHistoryPage(@ModelAttribute("lendingSearchModel") LendingSearchModel lendingSearchModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);

		LendingSearchCriteria criteria = lendingSearchModel.getCriteria();
		List<LoanRecordDisplay> historyrecords = searchLoanHistory(client,criteria);

		// put results in model
		uiModel.addAttribute("historyRecords", historyrecords);
		uiModel.addAttribute("lendingSearchModel",lendingSearchModel);

		// return view
		return "lending/history";
	}





	@RequestMapping(value = "/sortby/{sortby}", method = RequestMethod.POST, produces = "text/html")
	public String sortHistoryPage(@PathVariable("sortby") Long sortkey,
			@ModelAttribute("lendingSearchModel") LendingSearchModel lendingSearchModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);

		LendingSearchCriteria criteria = lendingSearchModel.getCriteria();

		// determine new direction
		Long origsort = lendingSearchModel.getSorttype();
		Long origdir = lendingSearchModel.getSortdir();
		Long newdir=origdir;
		if (origsort!=null) {
			if (origsort==sortkey) {
				newdir =origdir==LendingSearchCriteria.SortByDir.ASC?LendingSearchCriteria.SortByDir.DESC:LendingSearchCriteria.SortByDir.ASC;
			}
		}
		// set in criteria
		criteria.setSortKey(sortkey);
		criteria.setSortDir(newdir);
		// set in model
		lendingSearchModel.setSorttype(sortkey);
		lendingSearchModel.setSortdir(newdir);
		
		// corresponding search
		List<LoanRecordDisplay> historyrecords = searchLoanHistory(client,criteria);

		// put results in model
		uiModel.addAttribute("historyRecords", historyrecords);
		uiModel.addAttribute("lendingSearchModel",lendingSearchModel);

		// return view
		return "lending/history";

	}

	@RequestMapping(value = "/pdf",method = RequestMethod.GET, produces = "text/html")
	public String exportAsPdf(
			@ModelAttribute("lendingSearchModel") LendingSearchModel lendingSearchModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);

		// return view
		return "lending/history";
	}

	@RequestMapping(value = "/class/{id}", method = RequestMethod.GET, produces = "text/html")
	public String drillDownToStudent(@PathVariable("id") Long teacherid,
			@ModelAttribute("lendingSearchModel") LendingSearchModel lendingSearchModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		LendingSearchCriteria criteria = lendingSearchModel.getCriteria();
		
		// set teacheridin criteria
		criteria.setBorrowerid(teacherid);
		// set order by, direction
		criteria.setSortKey(LendingSearchCriteria.SortKey.CHECKEDOUT);
		criteria.setSortDir(LendingSearchCriteria.SortByDir.DESC);
		// retrieve list
		List<LoanRecordDisplay> historyrecords = searchLoanHistory(client,criteria);

		// retrieve teacher name
		TeacherInfo info= classService.getTeacherByTeacherid(teacherid);
		if (info!=null) {
			String teachername = info.getDisplayname();
			uiModel.addAttribute("teachername", teachername);
		}
		
		// put results in model
		uiModel.addAttribute("historyRecords", historyrecords);
		uiModel.addAttribute("lendingSearchModel",lendingSearchModel);
		
		// return view
		return "lending/classhistory";
	}

			
			
			
			
			
			
			
			
	/** Methods where the work is done **/
	private List<LoanRecordDisplay> searchLoanHistory(ClientDao client,
			LendingSearchCriteria lendingSearchCriteria) {
		// TODO Auto-generated method stub
		return lendingService.searchLendingHistory(lendingSearchCriteria, client.getId());
	}
	
	private LendingSearchModel getDefaultLendingSearchModel() {
		LendingSearchModel lsm = new LendingSearchModel();
		lsm.setClassselect(LendingSearchCriteria.ClassType.ALL);
		lsm.setLendtypeselect(LendingSearchCriteria.LendingType.ALL);
		lsm.setTimeselect(LendingSearchCriteria.TimePeriodType.THISWEEK);
		
		return lsm;
	}
	


	@ModelAttribute("classselect")
	private HashMap<Long, String> getClassInfo(
			HttpServletRequest httpServletRequest, Locale locale,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();
		String lang = locale.getLanguage();

		// get display hash for key, language
		HashMap<Long, String> classselect = keyService.getDisplayHashForKey(
				LendingSearchCriteria.ClassTypeLkup, lang);
		// add individual teachers
		HashMap<Long, TeacherInfo> classinfo = classService
				.getTeacherByClassForClient(clientid);
		for (Long key : classinfo.keySet()) {
			TeacherInfo ti = classinfo.get(key);
			classselect.put(key, ti.getDisplayname());
		}

		return classselect;
	}

	@ModelAttribute("timeperiods")
	private HashMap<Long, String> getTimePeriods(HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {
		String lang = locale.getLanguage();
		ClientDao client = clientService.getCurrentClient(principal);
		// get display hash for key, language
		HashMap<Long, String> classselect = keyService.getDisplayHashForKey(
				LendingSearchCriteria.TimeTypeLkup, lang);
		// determine new school years to add
		// get current year
		Integer currentyear = DateUtils.getSchoolYearBeginForDate(new Date());
		// get first year
		Integer firstlent = lendingService.getFirstLendingYearForClient(client.getId());
		// compare and add lines if necessary
		if (firstlent!=null && firstlent.intValue()!=currentyear.intValue()) {
			// get year label
			String label = keyService.getDisplayForKeyValue(LendingSearchCriteria.SchoolYearLkup, LendingSearchCriteria.SchoolYearKey, lang);
			while (firstlent.intValue()<currentyear.intValue()) {
				String yearlabel = label + " " + firstlent + " - " + (firstlent.intValue()+1);
				classselect.put(new Long(firstlent), yearlabel);		
				firstlent = firstlent+ 1;
			}
		} 
		
		return classselect;
	}
	
	@ModelAttribute("lendtypeselect")
	private HashMap<Long, String> getLendType(HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {
		String lang = locale.getLanguage();

		// get display hash for key, language
		HashMap<Long, String> classselect = keyService.getDisplayHashForKey(
				LendingSearchCriteria.LendTypeLkup, lang);
		return classselect;
	}	

}
