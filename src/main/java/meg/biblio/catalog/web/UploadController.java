package meg.biblio.catalog.web;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import meg.biblio.catalog.web.model.BookImportModel;
import meg.biblio.common.ClientService;
import meg.biblio.common.ImportManager;
import meg.biblio.common.SelectKeyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

@RequestMapping("/import")
@Controller
public class UploadController {
	
    @Autowired
    SelectKeyService cvManager;
	
    @Autowired
    ImportManager importManager;
    
    @Autowired
    ClientService clientService;	
	
	@RequestMapping(value = "/upload",method = RequestMethod.POST, produces = "text/html")
	public String create(@Valid BookImportModel bookImportModel, BindingResult bindingResult, Model uiModel,
	       @RequestParam("content") CommonsMultipartFile content,
	       HttpServletRequest httpServletRequest) {
		Long clientkey = clientService.getCurrentClientKey(httpServletRequest);
		String filestr = "";
	   byte[] file = content.getBytes();
	   filestr = new String(file);
	      bookImportModel.setContentType(content.getContentType());

	   uiModel.asMap().clear();
	   
	   // import the file here....
	   HashMap<String,Integer> importresults = importManager.importBookList(clientkey, filestr);
	   
	   bookImportModel.setImportListSize(importresults.get(ImportManager.Results.listsize));   
	   bookImportModel.setImportedSize(importresults.get(ImportManager.Results.importsize));		
	   bookImportModel.setTotalErrorsSize(importresults.get(ImportManager.Results.totalerrorssize));  
	   bookImportModel.setDuplicatesSize(importresults.get(ImportManager.Results.duplicatesize));  
	   bookImportModel.setNoIdSize(importresults.get(ImportManager.Results.noidsize)); 

	   populateEditForm(uiModel,bookImportModel);
	   // put results in BookImportModel
	   return "import/results";
	  
	}
	
    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new BookImportModel());
        return "import/create";
    }
	
    String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        } catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }
	
    
    private void populateEditForm(Model uiModel, BookImportModel bookImportModel) {
        uiModel.addAttribute("bookImportModel", bookImportModel);
    }
}
