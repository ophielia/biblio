package meg.biblio.lending.web;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import meg.biblio.common.AppSettingService;
import meg.biblio.common.CacheService;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.db.dao.SelectValueDao;
import meg.biblio.common.web.model.PrintClassModel;
import meg.biblio.lending.ClassManagementService;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.TeacherDao;
import meg.biblio.lending.web.model.TeacherInfo;
import meg.biblio.lending.web.validator.LendingModelValidator;

import org.apache.fop.apps.FOPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/generatebarcode")
@Controller
public class GenerateBarcodeController {
	@Autowired
	MessageSource appMessageSource;

	@Autowired
	AppSettingService appSetting;
	
	@Autowired
	ClientService clientService;

	@Autowired
	CacheService cacheService;

	@Autowired
	ClassManagementService classService;

	@Autowired
	SelectKeyService keyService;

	@Autowired
	LendingModelValidator lendingValidator;

	@RequestMapping(value = "/books", method = RequestMethod.GET, produces = "text/html")
	public String showGenerateBarcodesForBooks(Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal,
			Locale locale) {

			// add print model
			PrintClassModel pcModel = new PrintClassModel();
			pcModel.setNudge(getDefaultNudge());
			pcModel.setStartPos(getDefaultStartPos());
			pcModel.setShowBorder(getDefaultShowBorder());
			uiModel.addAttribute("printClassModel",pcModel);
			return "barcode/generatebooksrange";


	}

	@RequestMapping(value = "/books/custom", method = RequestMethod.GET, produces = "text/html")
	public String showGenerateBarcodesCustomValues(PrintClassModel pcModel,Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal,
			Locale locale) {
		String username = principal.getName();

		// get list of cache values
		putCacheValuesInModel(username, uiModel);


		// pop them into the model

		// add print model
		pcModel = new PrintClassModel();
		pcModel.setNudge(getDefaultNudge());
		pcModel.setStartPos(getDefaultStartPos());
		pcModel.setShowBorder(getDefaultShowBorder());
		uiModel.addAttribute("printClassModel",pcModel);
		
		// return the custom book values page
		return "barcode/generatebookscustom";

	}
	
	private void putCacheValuesInModel(String username,Model uiModel) {
		// get list of cache values
		List<String> cacheValues = cacheService.getValidCacheAsList(username,
				CacheService.CodeTag.CustomBarcodes);
		Integer maxcodes = appSetting.getSettingAsInteger("biblio.maxcodes");
		
		if (cacheValues.size()>maxcodes.intValue()) {
			uiModel.addAttribute("toomanycodes",true);
		}
		// pop them into the model
		uiModel.addAttribute("customvals", cacheValues);
	}


	// Entry point class page
	@RequestMapping(value = "/class/custom",  method = RequestMethod.GET, produces = "text/html")
	public String showPrintClassBarcodesPage(
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();
		// select first teacher
		HashMap<Long, TeacherInfo> classinfo = classService
				.getTeacherByClassForClient(clientid);
		Set<Long> classes = classinfo.keySet();
		
		Long teacherid = null;
		SchoolGroupDao schoolgroup = null;
		for (Long id : classes) {
			TeacherInfo teacher = classinfo.get(id);
			teacherid = teacher.getId();
			schoolgroup = classService.getClassByTeacher(teacherid, clientid);
			break;
		}
	
		// create print class model
		PrintClassModel pcModel = new PrintClassModel();
		pcModel.setSchoolgroup(schoolgroup);
		// set default printing options
		pcModel.setNudge(getDefaultNudge());
		pcModel.setStartPos(getDefaultStartPos());
		pcModel.setShowBorder(getDefaultShowBorder());
		
		uiModel.addAttribute("printClassModel",pcModel);
		
		// show page for teacher
		return displayPageForTeacher(teacherid, pcModel, uiModel,
				httpServletRequest, principal);
	}

	// change teacher page - post submit value changeteacher
	@RequestMapping(value = "/class/custom",  method = RequestMethod.POST, produces = "text/html")
	public String showDifferentClassPage(PrintClassModel pcModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();
		
		 Long classid = pcModel.getNewClassId();
		 
		// save changes from previous class
		saveChangesToModel(pcModel, principal);
	
		// get teacherid for classid 
		TeacherDao teacher = classService.getTeacherForClass(clientid, classid);
		// show page for teacher
		return displayPageForTeacher(teacher.getId(), pcModel, uiModel,
				httpServletRequest, principal);
	}

	@RequestMapping(value = "/books/custom", params = "toadd", method = RequestMethod.POST, produces = "text/html")
	public String addCustomValues(@RequestParam("newid") String newid,PrintClassModel pcModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {
		String username = principal.getName();

		if (newid!=null) {

			// Validate newid
			if (newid.length()>11) {
				uiModel.addAttribute("errorcodelength", true);
			} else {
				// add new id to cache
				cacheService.saveValueInCache(username,
						CacheService.CodeTag.CustomBarcodes, "", newid, 360L);
			}
		}

		// get list of cache values
		putCacheValuesInModel(username, uiModel);

		// pop them into the model
		uiModel.addAttribute("printClassModel", pcModel);

		// return the custom book values page
		return "barcode/generatebookscustom";

	}

	@RequestMapping(value = "/books/custom", params = "clearall", method = RequestMethod.POST, produces = "text/html")
	public String clearCustomList(PrintClassModel pcModel,Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal,
			Locale locale) {
		String username = principal.getName();

		// clear cache
		cacheService.clearUserCacheForTag(username,
				CacheService.CodeTag.CustomBarcodes);

		// get list of cache values
		putCacheValuesInModel(username, uiModel);


		// pop them into the model
		uiModel.addAttribute("printClassModel", pcModel);

		// return the custom book values page
		return "barcode/generatebookscustom";
	}

	@RequestMapping(value = "/books/custom/delete", method = RequestMethod.GET, produces = "text/html")
	public String deleteSingleCustomValue(@RequestParam("value") String value,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {
		String username = principal.getName();

		// make sure value is there
		if (value != null && value.trim().length() > 0) {
			// clear value from cache
			cacheService.deleteValueFromCache(username,
					CacheService.CodeTag.CustomBarcodes, "", value);
		}

		// redirect to basic custom cache page

		return "redirect:/generatebarcode/books/custom";

	}

	@RequestMapping(value = "/books/range", method = RequestMethod.POST, produces = "text/html")
	public String printBookBarcodeSheetRange(PrintClassModel pcModel,
			Model uiModel, HttpServletRequest request,
			HttpServletRequest httpServletRequest,
			HttpServletResponse response, Principal principal, Locale locale)
			throws FOPException, JAXBException, TransformerException,
			IOException, ServletException {
		Integer startcode = pcModel.getRangeFrom();
		Integer endcode = pcModel.getRangeTo();
		Long nudge = pcModel.getNudge();
		Long border = pcModel.getShowBorder();
		Long startpos = pcModel.getStartPos();

		Long offset = pcModel.getStartPos();
		Integer maxcodes = appSetting.getSettingAsInteger("biblio.maxcodes");
		if (startcode == null || endcode == null) {
			uiModel.addAttribute("errorenterrange", true);
			return "barcode/generatebooksrange";
		} else if (endcode.intValue() < startcode.intValue()) {
			uiModel.addAttribute("errorrangeinvalid", true);
			return "barcode/generatebooksrange";
		} else if ((endcode.intValue() - startcode.intValue())>maxcodes) {
			uiModel.addAttribute("errortoomanycodes", true);
			return "barcode/generatebooksrange";
		}
		if (offset == null) {
			offset = 0L;
		}

		
		return "redirect:/pdfwrangler/bookbarcodes/range?from="
				+ startcode.intValue() + "&to=" + endcode.intValue()
				 + "&nudge=" + nudge.intValue()
				 + "&startpos=" + startpos.intValue()
				 + "&border=" + border.intValue() ;
	}

	@RequestMapping( value = "/books/custom", params = "print",method = RequestMethod.POST, produces = "text/html")
	public String printBookBarcodeSheetCustom(
			PrintClassModel pcModel, Model uiModel,
			HttpServletRequest request, HttpServletRequest httpServletRequest,
			HttpServletResponse response, Principal principal, Locale locale)
			throws FOPException, JAXBException, TransformerException,
			IOException, ServletException {

		Long startpos = pcModel.getStartPos();
		Long nudge = pcModel.getNudge();
		Long border = pcModel.getShowBorder();

		// start the class barcode print
		return "redirect:/pdfwrangler/bookbarcodes?startpos="
		+startpos+  "&border=" + border + "&nudge=" + nudge;

		
	}
	
	
	// post to class/custom, submit param, print (changed in html to be new page
	// target
	@RequestMapping(params = "print",value = "/class/custom",  method = RequestMethod.POST, produces = "text/html")
	public String printClassBarcodePage(PrintClassModel pcModel, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		saveChangesToModel(pcModel, principal);

		Long startpos = pcModel.getStartPos();
		Long classid = pcModel.getCurrentClassId();
		Long nudge = pcModel.getNudge();
		Long border = pcModel.getShowBorder();
		
		// start the class barcode print
		return "redirect:/pdfwrangler/classbarcodes?startpos="
		+startpos+ "&classId=" + classid + "&border=" + border + "&nudge=" + nudge;
	}

	// process changes in pcModel
	private void saveChangesToModel(PrintClassModel pcModel, Principal principal) {
		// get username
		String username = principal.getName();

		// get selected values from pcModel
		List<String> selectedvalues = pcModel.getSelectedValuesAsStrings();

		// classid
		Long classid = pcModel.getCurrentClassId();

		// replace cache values
		cacheService.replaceValuesInCache(username,
				CacheService.CodeTag.ClassBarcodes, String.valueOf(classid),
				selectedvalues, 120L);
	}

	private String displayPageForTeacher(Long teacherid,
			PrintClassModel pcModel, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		String username = principal.getName();
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// get schoolgroupdao and put in model
		SchoolGroupDao sgroup = classService.getClassByTeacher(teacherid,
				clientid);
		pcModel.setSchoolgroup(sgroup);
		String classidstr = String.valueOf(sgroup.getId());
		
		// get cache for teacher
		List<String> cachevals = cacheService.getValidCacheAsList(username,
				CacheService.CodeTag.ClassBarcodes, classidstr);
		// if empty, fill cache for teacher (select all students)
		if (cachevals == null || cachevals.size() == 0) {
			// select all in model
			pcModel.selectEntireClass();
		} else {
			// set cachevals in model
			pcModel.setSelectedList(cachevals);
		}

		// put classinfo in model
		HashMap<Long, TeacherInfo> classinfo = classService
				.getTeacherByClassForClient(clientid);
		uiModel.addAttribute("classinfo", classinfo);

		// put printClassModel in model
		uiModel.addAttribute("printClassModel",pcModel);
		
		// return print class barcodes page
		return "barcode/generateclasscustom";
	}

	private Long getDefaultShowBorder() {
		return new Long(0);
	}

	private Long getDefaultStartPos() {
		return new Long(1);
	}

	private Long getDefaultNudge() {
		return new Long(0);
	}
	
	
	@ModelAttribute("positionselect")
	private List<SelectValueDao> getClassInfo(
			HttpServletRequest httpServletRequest, Locale locale,
			Principal principal) {
		String lang = locale.getLanguage();

		// get display hash for key, language
		List<SelectValueDao> positionselect = keyService.getSelectValuesForKey(
				PrintClassModel.startPosLkup, lang);

		return positionselect;
	}
	
	@ModelAttribute("yesno")
	private List<SelectValueDao> getYesNoSelectValues(
			HttpServletRequest httpServletRequest, Locale locale,
			Principal principal) {
		String lang = locale.getLanguage();

		// get display hash for key, language
		List<SelectValueDao> yesno = keyService.getSelectValuesForKey(
			 SelectKeyService.Common.YESNO, lang);

		return yesno;
	}	
	
	@ModelAttribute("nudge")
	private List<SelectValueDao> getNudgeSelectValues(
			HttpServletRequest httpServletRequest, Locale locale,
			Principal principal) {
		String lang = locale.getLanguage();

		// get display hash for key, language
		List<SelectValueDao> nudge = keyService.getSelectValuesForKey(
			 PrintClassModel.nudgeLkup, lang);

		return nudge;
	}		
}
