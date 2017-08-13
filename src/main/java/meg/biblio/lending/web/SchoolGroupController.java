package meg.biblio.lending.web;

import meg.biblio.catalog.CatalogService;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.ClassManagementService;
import meg.biblio.lending.LendingService;
import meg.biblio.lending.db.dao.LoanRecordDisplay;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.web.model.ClassModel;
import meg.biblio.lending.web.validator.ClassModelValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


@RequestMapping("/classes")
@Controller
public class SchoolGroupController {


    @Autowired
    ClassManagementService classMgmtService;

    @Autowired
    LendingService lendingService;

    @Autowired
    SelectKeyService keyService;

    @Autowired
    ClientService clientService;

    @Autowired
    ClassModelValidator classValidator;


    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    public String showClassList(Model uiModel, HttpServletRequest request, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);
        Long clientkey = client.getId();

        List<SchoolGroupDao> classes = classMgmtService.getClassesForClient(clientkey);
        uiModel.addAttribute("listofclasses", classes);
        return "schoolgroups/list";
    }

    @RequestMapping(value = "/create", params = "form", method = RequestMethod.GET, produces = "text/html")
    public String createClassForm(Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        // create empty book model
        ClassModel model = new ClassModel();
        // place in uiModel
        uiModel.addAttribute("classModel", model);
        // return book/create
        return "schoolgroups/create";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = "text/html")
    public String createClass(ClassModel model, Model uiModel, BindingResult bindingResult, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);
        Long clientkey = client.getId();

        model.fillInTeacherFromEntry();

        classValidator.validateTeacherEntry(model, bindingResult);
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("classModel", model);
            return "schoolgroups/create";
        }

        uiModel.asMap().clear();

        // create class for teacher - and load class model
        ClassModel newclass = classMgmtService.createClassFromClassModel(model, clientkey);

        // add list of unassigned students to model
        List<StudentDao> unassigned = classMgmtService.getUnassignedStudents(clientkey);
        newclass.setUnassignedstudents(unassigned);

        uiModel.addAttribute("classModel", newclass);
        uiModel.addAttribute("newlycreated", true);

        // return edit/display page
        return "schoolgroups/edit";

    }

    @RequestMapping(value = "/editstudent/{id}", method = RequestMethod.GET, produces = "text/html")
    public String showEditStudentForm(@PathVariable("id") Long studentid, Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        // load ClassModel
        ClassModel sclass = classMgmtService.loadClassModelForStudent(studentid);

        // set student in model
        sclass.setStudentInModel(studentid);

        // put classmodel in model
        uiModel.addAttribute("classModel", sclass);

        // return edit view
        return "schoolgroups/editstudent";
    }

    @RequestMapping(value = "/editstudent/{id}", method = RequestMethod.POST, produces = "text/html")
    public String saveEditStudent(@ModelAttribute("classModel") ClassModel classModel, @PathVariable("id") Long studentid, Model uiModel, BindingResult bindingResult, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);

        // validation
        classValidator.validateNewStudentEntry(classModel, bindingResult);
        if (bindingResult.hasErrors()) {
            // put model back in uiModel and return to edit
            uiModel.addAttribute("classModel", classModel);
            return "schoolgroups/editstudent";
        }

        // get info from model
        Long sid = classModel.getStudentid();
        String firstname = classModel.getStudentfirstname();
        String lastname = classModel.getStudentname();
        Long sectionid = classModel.getStudentsection();

        // save info in database
        classMgmtService.editStudent(client.getId(), sid, firstname, lastname, sectionid);

        // load model
        ClassModel sclass = classMgmtService.loadClassModelForStudent(sid);

        // add list of unassigned students to model
        List<StudentDao> unassigned = classMgmtService.getUnassignedStudents(client.getId());
        sclass.setUnassignedstudents(unassigned);

        // put classmodel in model
        uiModel.addAttribute("classModel", sclass);

        // return edit class view
        return "schoolgroups/edit";

    }

    @RequestMapping(value = "/showstudent/{id}", method = RequestMethod.GET, produces = "text/html")
    public String showStudent(@PathVariable("id") Long studentid, @RequestParam(value = "from", required = false) String lendinghistory, Model uiModel,
                              HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);
        // load ClassModel
        ClassModel sclass = classMgmtService.loadClassModelForStudent(studentid);

        // fill model with student info
        if (sclass != null) {
            // set student in model
            sclass.setStudentInModel(studentid);
            uiModel.addAttribute("classModel", sclass);
            uiModel.addAttribute("studentfirstname", sclass.getStudentfirstname());
            uiModel.addAttribute("studentname", sclass.getStudentname());
            uiModel.addAttribute("studentsection", sclass.getStudentsection());
            uiModel.addAttribute("studentid", sclass.getStudentid());
            uiModel.addAttribute("hasclass", true);
        } else {
            StudentDao student = classMgmtService.getStudentById(studentid, client.getId());
            uiModel.addAttribute("studentfirstname", student.getFirstname());
            uiModel.addAttribute("studentname", student.getLastname());
            uiModel.addAttribute("studentsection", student.getSectionkey());
            uiModel.addAttribute("studentid", student.getId());
            uiModel.addAttribute("hasclass", false);
        }

        // get lending history for student
        List<LoanRecordDisplay> lendhistory = lendingService.getLendingHistoryByBorrower(studentid, client.getId());

        // put classmodel in model
        uiModel.addAttribute("lendinghistory", lendhistory);
        if (lendinghistory != null) {
            uiModel.addAttribute("fromlending", true);
        }

        // return edit view
        return "schoolgroups/showstudent";
    }

    @RequestMapping(value = "/editstudent/{id}", params = "cancel", method = RequestMethod.POST, produces = "text/html")
    public String cancelEditStudent(@ModelAttribute("classModel") ClassModel classModel, @PathVariable("id") Long studentid, Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);

        // load model
        ClassModel sclass = classMgmtService.loadClassModelById(classModel.getClassid());

        // add list of unassigned students to model
        List<StudentDao> unassigned = classMgmtService.getUnassignedStudents(client.getId());
        sclass.setUnassignedstudents(unassigned);

        // put classmodel in model
        uiModel.addAttribute("classModel", sclass);

        // return edit class view
        return "schoolgroups/edit";

    }

    @RequestMapping(value = "/display/{id}", method = RequestMethod.GET, produces = "text/html")
    public String showEditClassForm(@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);
        // load ClassModel
        ClassModel sclass = classMgmtService.loadClassModelById(id);


        // add list of unassigned students to model
        List<StudentDao> unassigned = classMgmtService.getUnassignedStudents(client.getId());
        sclass.setUnassignedstudents(unassigned);


        // put classmodel in model
        uiModel.addAttribute("classModel", sclass);

        // return edit view
        return "schoolgroups/edit";
    }


    @RequestMapping(value = "/display/{id}", method = RequestMethod.POST, produces = "text/html")
    public String saveEditClass(@ModelAttribute("classModel") ClassModel bookModel, @PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        return null;
    }

    @RequestMapping(value = "/display/{id}", params = "addnew", method = RequestMethod.POST, produces = "text/html")
    public String addNewStudent(@ModelAttribute("classModel") ClassModel classModel, @PathVariable("id") Long id, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        // Add attribute "add another" here
        uiModel.addAttribute("addanother", false);

        return newStudent(classModel, id, bindingResult, uiModel, httpServletRequest, principal);

    }

    @RequestMapping(value = "/display/{id}", params = "addnewandagain", method = RequestMethod.POST, produces = "text/html")
    public String addNewStudentAndAgain(@ModelAttribute("classModel") ClassModel classModel, @PathVariable("id") Long id, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        // Add attribute "add another" here
        uiModel.addAttribute("addanother", true);

        return newStudent(classModel, id, bindingResult, uiModel, httpServletRequest, principal);
    }

    private String newStudent(ClassModel classModel, Long id, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);
        Long clientkey = client.getId();

        ClassModel start = classMgmtService.loadClassModelById(id);
        // validation
        classValidator.validateNewStudentEntry(classModel, bindingResult);
        if (bindingResult.hasErrors()) {
            classModel.setSchoolGroup(start.getSchoolGroup());

            // put classmodel in model
            uiModel.addAttribute("classModel", classModel);

            // return edit view
            return "schoolgroups/edit";
        }

        // save student
        classMgmtService.addNewStudentToClass(classModel.getStudentname(), classModel.getStudentsection(), start.getSchoolGroup(), clientkey);

        // add list of unassigned students to model
        start = classMgmtService.loadClassModelById(id);
        List<StudentDao> unassigned = classMgmtService.getUnassignedStudents(clientkey);
        start.setUnassignedstudents(unassigned);

        // put classmodel in model
        uiModel.addAttribute("classModel", start);

        // Add attribute "add another" here


        // return edit view
        return "schoolgroups/edit";
    }

    @RequestMapping(value = "/display/{id}", params = "addstudents", method = RequestMethod.POST, produces = "text/html")
    public String assignStudents(@ModelAttribute("classModel") ClassModel classModel, @PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);
        Long clientkey = client.getId();

        // get selected students
        List<Long> idstoadd = classModel.getSelectedUnassignedIds();

        // assign students
        ClassModel start = classMgmtService.loadClassModelById(id);
        ClassModel model = classMgmtService.assignStudentsToClass(idstoadd, start.getSchoolGroup(), clientkey);

        // add list of unassigned students to model
        List<StudentDao> unassigned = classMgmtService.getUnassignedStudents(clientkey);
        model.setUnassignedstudents(unassigned);

        // put classmodel in model
        uiModel.addAttribute("classModel", model);

        // return edit view
        return "schoolgroups/edit";
    }


    @RequestMapping(value = "/display/{id}", params = "removestudents", method = RequestMethod.POST, produces = "text/html")
    public String removeStudents(@ModelAttribute("classModel") ClassModel classModel, @PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);
        Long clientkey = client.getId();

        // get selected students
        List<Long> idstoremove = classModel.getSelectedIdsToRemove();

        // assign students
        ClassModel start = classMgmtService.loadClassModelById(id);
        ClassModel model = classMgmtService.removeStudentsFromClass(idstoremove, start.getSchoolGroup(), clientkey);

        // add list of unassigned students to model
        List<StudentDao> unassigned = classMgmtService.getUnassignedStudents(clientkey);
        model.setUnassignedstudents(unassigned);

        // put classmodel in model
        uiModel.addAttribute("classModel", model);

        // return edit view
        return "schoolgroups/edit";
    }

    @RequestMapping(value = "/delete/confirm/{id}", method = RequestMethod.GET, produces = "text/html")
    public String confirmDeleteClass(@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);

        uiModel.addAttribute("classid", id);

        // return class list view
        return "schoolgroups/confirmdelete";
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST, produces = "text/html")
    public String deleteClass(@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);

        classMgmtService.deleteClass(id, client.getId());

        // return class list view
        return "redirect:/classes";
    }

    @RequestMapping(value = "/manage", method = RequestMethod.GET, produces = "text/html")
    public String showManagementPage(Model uiModel, HttpServletRequest httpServletRequest) {

        // return edit class view
        return "schoolgroups/settings";
    }

    @RequestMapping(value = "/manage/increment", method = RequestMethod.GET, produces = "text/html")
    public String showIncrementStudents(Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);

        uiModel.addAttribute("incrementsuccess", false);
        // return edit class view
        return "schoolgroups/increment";

    }

    @RequestMapping(value = "/manage/increment", method = RequestMethod.POST, produces = "text/html")
    public String incrementStudents(Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);

        classMgmtService.moveAllStudentsToNextSection(client.getId());

        uiModel.addAttribute("incrementsuccess", true);
        // return edit class view
        return "schoolgroups/increment";

    }

    @RequestMapping(value = "/manage/clearlists", method = RequestMethod.GET, produces = "text/html")
    public String showClearClassLists(Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);

        uiModel.addAttribute("clearsuccess", false);
        // return clear class lists view
        return "schoolgroups/clearclasslists";
    }

    @RequestMapping(value = "/manage/clearlists", method = RequestMethod.POST, produces = "text/html")
    public String clearClassLists(Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);


        classMgmtService.clearStudentListsForClient(client.getId());

        // return edit class view
        return "schoolgroups/settings";

    }


    @RequestMapping(value = "/manage", params = "toremove", method = RequestMethod.POST, produces = "text/html")
    public String showRemoveFromSchoolPage(Model uiModel,
                                           HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);

        // add list of unassigned students to model
        ClassModel model = new ClassModel();
        List<StudentDao> unassigned = classMgmtService
                .getUnassignedStudents(client.getId());
        model.setUnassignedstudents(unassigned);

        // put classmodel in model
        uiModel.addAttribute("classModel", model);

        // return edit class view
        return "schoolgroups/remove";
    }

    @RequestMapping(value = "/remove", params = "toremove", method = RequestMethod.POST, produces = "text/html")
    public String removeStudentsFromSchool(@ModelAttribute("classModel") ClassModel classModel, Model uiModel, HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao client = clientService.getCurrentClient(principal);

        // get selected students
        List<Long> idstoremove = classModel.getSelectedUnassignedIds();

        classMgmtService.setStudentsAsInactive(idstoremove, client.getId());
        // return edit class view
        return "redirect:/classes";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAutoGrowCollectionLimit(100024);
    }


    @ModelAttribute("detailstatusLkup")
    public HashMap<Long, String> getDetailStatusLkup(HttpServletRequest httpServletRequest, Locale locale) {
        String lang = locale.getLanguage();

        HashMap<Long, String> booktypedisps = keyService
                .getDisplayHashForKey(CatalogService.detailstatuslkup, lang);
        return booktypedisps;
    }

    @ModelAttribute("sectionLkup")
    public HashMap<Long, String> getSectionLkup(HttpServletRequest httpServletRequest, Locale locale) {
        String lang = locale.getLanguage();

        HashMap<Long, String> booktypedisps = keyService
                .getDisplayHashForKey(ClassManagementService.sectionLkup, lang);
        return booktypedisps;
    }

    @ModelAttribute("sectionSelect")
    public HashMap<Long, String> getSectionSelect(HttpServletRequest httpServletRequest, Locale locale) {
        String lang = locale.getLanguage();

        HashMap<Long, String> booktypedisps = keyService
                .getDisplayHashForKey(ClassManagementService.sectionSelect, lang);
        return booktypedisps;
    }

    @ModelAttribute("clientname")
    public String getClientName(HttpServletRequest httpServletRequest, Principal principal) {
        ClientDao clientkey = clientService.getCurrentClient(principal);
        return clientkey.getName();
    }
}
