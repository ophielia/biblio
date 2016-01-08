package meg.biblio.lending.web;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import meg.biblio.common.BarcodeService;
import meg.biblio.common.CacheService;
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
	CacheService cacheService;	
	
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

	@RequestMapping(value="/books/custom",method = RequestMethod.GET, produces = "text/html")
	public String showGenerateBarcodesCustomValues( Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal,Locale locale) {
		String lang = locale.getLanguage();
		ClientDao client = clientService.getCurrentClient(principal);
		String username = principal.getName();
		
		// get list of cache values
				List<String> cacheValues = cacheService.getValidCacheAsList(username, CacheService.CodeTag.CustomBarcodes);
				
				// pop them into the model
				uiModel.addAttribute("customvals",cacheValues);		

				// return the custom book values page
				return "barcode/generatebookscustom";

	}
	
	@RequestMapping(value="/books/custom",params = "toadd",method = RequestMethod.POST, produces = "text/html")
	public String addCustomValues( @RequestParam("newid") String newid,Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal,Locale locale) {
		String lang = locale.getLanguage();
		ClientDao client = clientService.getCurrentClient(principal);
		String username = principal.getName();
		
		// Validate newid
		// MM TODO
		
		// add new id to cache
		cacheService.saveValueInCache(username, CacheService.CodeTag.CustomBarcodes, "", newid, 360L);
		
		// get list of cache values
		List<String> cacheValues = cacheService.getValidCacheAsList(username, CacheService.CodeTag.CustomBarcodes);
		
		// pop them into the model
		uiModel.addAttribute("customvals",cacheValues);		

		// return the custom book values page
		return "barcode/generatebookscustom";

	}
	
	@RequestMapping(value="/books/custom", params = "toremove", method = RequestMethod.POST, produces = "text/html")
	public String clearCustomList( Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal,Locale locale) {
		String username = principal.getName();

		// clear cache
		cacheService.clearUserCacheForTag(username, CacheService.CodeTag.CustomBarcodes);
		
		// get list of cache values
		List<String> cacheValues = cacheService.getValidCacheAsList(username, CacheService.CodeTag.CustomBarcodes);
		
		// pop them into the model
		uiModel.addAttribute("customvals",cacheValues);		

		// return the custom book values page
		return "barcode/generatebookscustom";	
	}
	
	
	
	@RequestMapping(params = "range",value = "/books", method = RequestMethod.POST, produces = "text/html")
	public String generateBookBarcodeSheetRange(
			@RequestParam("from") Integer startcode,@RequestParam("to") Integer endcode, 
			@RequestParam("offset") Integer offset,Model uiModel,
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
		if (offset==null) {
			offset = 0;
		}
		
		return "redirect:/pdfwrangler/bookbarcodes?range=true&from=" + startcode.intValue() + "&to=" + endcode.intValue() + "&offset=" + offset.intValue();
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
