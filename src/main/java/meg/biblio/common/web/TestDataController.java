package meg.biblio.common.web;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import meg.biblio.common.ClientService;
import meg.biblio.common.LoginService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.TestDataService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.db.dao.RoleDao;
import meg.biblio.common.db.dao.UserLoginDao;
import meg.biblio.common.web.validator.UserLoginValidator;

import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/testdata")
@Controller
public class TestDataController {

	@Autowired
	protected TestDataService testDataService;
	
	@Autowired
	protected ClientService clientService;	

    @RequestMapping(value="/manage",  method = RequestMethod.GET, produces = "text/html")
    public String showManagementPage(Model uiModel, HttpServletRequest httpServletRequest) {

    	// return edit class view
    	return "testdata/settings";
    	}

    @RequestMapping(value="/manage", params="cleartestdata", method = RequestMethod.POST, produces = "text/html")
    public String clearTestAllData( Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
    	ClientDao client = clientService.getCurrentClient(principal);

    	testDataService.clearAllTestData();

    	// return edit class view
    	return "testdata/settings";

    	}
    
    @RequestMapping(value="/manage", params="filltestdata", method = RequestMethod.POST, produces = "text/html")
    public String fillAllTestData( Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
    	ClientDao client = clientService.getCurrentClient(principal);

    	testDataService.setAllTestData();

    	// return edit class view
    	return "testdata/settings";

    	}    
    
    @RequestMapping(value="/manage", params="clearlendingtestdata", method = RequestMethod.POST, produces = "text/html")
    public String clearTestLendingData( Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
    	ClientDao client = clientService.getCurrentClient(principal);

    	testDataService.clearLendingTestData();

    	// return edit class view
    	return "testdata/settings";

    	}    
    
    @RequestMapping(value="/manage", params="filllendingtestdata", method = RequestMethod.POST, produces = "text/html")
    public String fillTestLendingData( Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
    	ClientDao client = clientService.getCurrentClient(principal);

    	testDataService.setLendingTestData();

    	// return edit class view
    	return "testdata/settings";

    	}        
}

