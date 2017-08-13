package meg.biblio.common.web;

import meg.biblio.common.ClientService;
import meg.biblio.common.TestDataService;
import meg.biblio.common.db.dao.ClientDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@RequestMapping("/testdata")
@Controller
public class TestDataController {

    @Autowired
    protected TestDataService testDataService;

    @Autowired
    protected ClientService clientService;

    @RequestMapping(value = "/manage", method = RequestMethod.GET, produces = "text/html")
    public String showManagementPage(Model uiModel, HttpServletRequest httpServletRequest) {

        // return edit class view
        return "testdata/settings";
    }

    @RequestMapping(value = "/manage", params = "cleartestdata", method = RequestMethod.POST, produces = "text/html")
    public String clearTestAllData(Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);

        testDataService.clearAllTestData();

        // return edit class view
        return "testdata/settings";

    }

    @RequestMapping(value = "/manage", params = "filltestdata", method = RequestMethod.POST, produces = "text/html")
    public String fillAllTestData(Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);

        testDataService.setAllTestData();

        // return edit class view
        return "testdata/settings";

    }

    @RequestMapping(value = "/manage", params = "clearlendingtestdata", method = RequestMethod.POST, produces = "text/html")
    public String clearTestLendingData(Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);

        testDataService.clearLendingTestData();

        // return edit class view
        return "testdata/settings";

    }

    @RequestMapping(value = "/manage", params = "filllendingtestdata", method = RequestMethod.POST, produces = "text/html")
    public String fillTestLendingData(Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);

        testDataService.setLendingTestData();

        // return edit class view
        return "testdata/settings";

    }
}

