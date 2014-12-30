package meg.biblio.lending.web;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.BarcodeService;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.ClassManagementService;
import meg.biblio.lending.LendingService;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.web.model.LendingModel;
import meg.biblio.lending.web.model.LoanRecordDisplay;
import meg.biblio.lending.web.model.TeacherInfo;
import meg.biblio.lending.web.validator.LendingModelValidator;

import org.apache.fop.apps.FOPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

@RequestMapping("/generatebarcode")
@Controller
public class GenerateBarcodeController {


	@Autowired
	ClientService clientService;
	
	@Autowired
	ClassManagementService classService;
	
	@Autowired
	SelectKeyService keyService;

	@Autowired
	LendingModelValidator lendingValidator;

	@RequestMapping(value="/books",method = RequestMethod.GET, produces = "text/html")
	public String showGenerateBarcodesForBooks( Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();
		Locale locale = httpServletRequest.getLocale();
		String lang = locale.getLanguage();

		// fill in select - 50, 100, 150, etc....
    	HashMap<Long, String> countselect = keyService
    			.getDisplayHashForKey(BarcodeService.codecountlkup, lang);
		// put select in model
    	uiModel.addAttribute("codeselect",countselect);
		return "barcode/generatebooks";
	}


	@RequestMapping(value="/class",method = RequestMethod.GET, produces = "text/html")
	public String showGenerateBarcodesForClass(Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientid = client.getId();

		// fill in class info
		HashMap<Long,TeacherInfo> classinfo  = classService.getTeacherByClassForClient(clientid);
		// put classinfo in model
		uiModel.addAttribute("classinfo",classinfo);

		return "barcode/generateclass";
	}




}
