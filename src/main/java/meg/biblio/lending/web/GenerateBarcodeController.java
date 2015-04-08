package meg.biblio.lending.web;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import meg.biblio.common.BarcodeService;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.ClassManagementService;
import meg.biblio.lending.web.model.TeacherInfo;
import meg.biblio.lending.web.validator.LendingModelValidator;

import org.apache.fop.apps.FOPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/generatebarcode")
@Controller
public class GenerateBarcodeController {
	@Autowired
	MessageSource appMessageSource;

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
			HttpServletRequest httpServletRequest, Principal principal,Locale locale) {
		String lang = locale.getLanguage();
		ClientDao client = clientService.getCurrentClient(principal);
		Boolean usesclientidforbarcode = client.getIdForBarcode();

		if (usesclientidforbarcode==null) usesclientidforbarcode=true;
		
		if (usesclientidforbarcode) {
			// this client uses the client assigned book id for the barcode
			// that means that the user will be interested in controlling which barcodes
			// are printed out - a range.
			return "barcode/generatebooksrange";
		} else {
			// this client doesn't uses the client bookid for the barcode - instead,
			// this client assigns a random barcode to each book.  The barcodes are
			// printed out by count, and a counter is incremented to make sure that each code is only
			// printed out once. - in other words - by count.
			// fill in select - 50, 100, 150, etc....
	    	HashMap<Long, String> countselect = keyService
	    			.getDisplayHashForKey(BarcodeService.codecountlkup, lang);
			// put select in model
	    	uiModel.addAttribute("codeselect",countselect);
			return "barcode/generatebookscount";
		}
		

	}

	@RequestMapping(params = "range",value = "/books", method = RequestMethod.POST, produces = "text/html")
	public String generateBookBarcodeSheetRange(
			@RequestParam("from") Integer startcode,@RequestParam("to") Integer endcode, Model uiModel,
			HttpServletRequest request, HttpServletRequest httpServletRequest,
			HttpServletResponse response, Principal principal, Locale locale)
			throws FOPException, JAXBException, TransformerException,
			IOException, ServletException {
	
		if (startcode==null || endcode == null) {
			uiModel.addAttribute("errorenterrange",true);
			return "barcode/generatebooksrange";
		} else if (endcode.intValue()<startcode.intValue()){
			uiModel.addAttribute("errorrangeinvalid",true);
			return "barcode/generatebooksrange";
		}
		return "redirect:/pdfwrangler/bookbarcodes?range=true&from=" + startcode.intValue() + "&to=" + endcode.intValue();
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
