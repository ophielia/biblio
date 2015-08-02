package meg.biblio.lending.web;

import java.security.Principal;
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
import meg.biblio.lending.web.model.LendingSearchModel;
import meg.biblio.lending.web.model.LoanRecordDisplay;
import meg.biblio.lending.web.model.TeacherInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
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





	@RequestMapping(value = "/sortby/{sortby}", method = RequestMethod.GET, produces = "text/html")
	public String sortHistoryPage(
			@ModelAttribute("lendingSearchModel") LendingSearchModel lendingSearchModel,
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

	@RequestMapping(value = "/pdf",method = RequestMethod.GET, produces = "text/html")
	public String exportAsPdf(
			@ModelAttribute("lendingSearchModel") LendingSearchModel lendingSearchModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);

		// return view
		return "lending/history";
	}

	@RequestMapping(value = "/student/{id}", method = RequestMethod.GET, produces = "text/html")
	public String drillDownToStudent(
			@ModelAttribute("lendingSearchModel") LendingSearchModel lendingSearchModel,
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

		// get display hash for key, language
		HashMap<Long, String> classselect = keyService.getDisplayHashForKey(
				LendingSearchCriteria.TimeTypeLkup, lang);
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
