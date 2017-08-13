package meg.biblio.catalog.web;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.StatBreakout;
import meg.biblio.catalog.StatService;
import meg.biblio.catalog.web.model.StatsModel;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.search.SearchService;
import meg.tools.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


@RequestMapping("/dashboard")
@Controller
public class DashboardController {

    @Autowired
    SearchService searchService;

    @Autowired
    SelectKeyService keyService;

    @Autowired
    ClientService clientService;


    @Autowired
    StatService statService;

    @RequestMapping(value = "/old", method = RequestMethod.GET, produces = "text/html")
    public String showDashboardOld(Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        // get client key
        ClientDao client = clientService.getCurrentClient(principal);
        // get total book count
        Long bookcount = searchService.getBookCount(client.getId());
        // get status breakout
        HashMap<Long, Long> statusbkout = searchService.breakoutByBookField(SearchService.Breakoutfield.STATUS, client.getId());
        // put together in model
        uiModel.addAttribute("client", client);
        uiModel.addAttribute("bookcount", bookcount);
        uiModel.addAttribute("statusbkout", statusbkout);

        return "dashboard/show";
    }

    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    public String showDashboard(Model uiModel, HttpServletRequest httpServletRequest, Principal principal, Locale loc) {
        // get client key
        ClientDao client = clientService.getCurrentClient(principal);

        StatsModel sm = statService.fillStatsForClient(client, loc);

        // get current year
        Date startdate = DateUtils.getFirstDayOfSchoolYear(new Date());
        Calendar schoolyear = Calendar.getInstance();
        schoolyear.setTime(startdate);
        int startyear = schoolyear.get(Calendar.YEAR);
        int endyear = startyear + 1;
        String schoolyrstr = startyear + "-" + endyear;

        // put together in model
        uiModel.addAttribute("client", client);
        uiModel.addAttribute("schoolyear", schoolyrstr);
        uiModel.addAttribute("stats", sm);

        return "dashboard/stats";
    }


    @ModelAttribute("statusLkup")
    public HashMap<Long, String> getStatusLkup(HttpServletRequest httpServletRequest, Locale locale) {
        String lang = locale.getLanguage();

        HashMap<Long, String> booktypedisps = keyService
                .getDisplayHashForKey(CatalogService.bookstatuslkup, lang);
        return booktypedisps;
    }

    @RequestMapping(value = "/stat/{type}", produces = "text/html")
    public String showBreakout(@PathVariable("type") Long type, Model uiModel,
                               HttpServletRequest httpServletRequest, Principal principal,
                               Locale locale) {
        // get client key
        ClientDao client = clientService.getCurrentClient(principal);

        if (type != null && type.longValue() > 100) {
            // retrieve stat for type
            StatBreakout bkout = statService.runBreakoutStatByType(client, locale, type);

            // add client name, school year to model
            // get current year
            Date startdate = DateUtils.getFirstDayOfSchoolYear(new Date());
            Calendar schoolyear = Calendar.getInstance();
            schoolyear.setTime(startdate);
            int startyear = schoolyear.get(Calendar.YEAR);
            int endyear = startyear + 1;
            String schoolyrstr = startyear + "-" + endyear;

            // put together in model
            uiModel.addAttribute("client", client);
            uiModel.addAttribute("schoolyear", schoolyrstr);
            uiModel.addAttribute("stats", bkout);

            return "dashboard/breakout";

        }

        return showDashboard(uiModel, httpServletRequest, principal, locale);
    }
}
