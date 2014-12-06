package meg.biblio.lending.web;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import meg.biblio.catalog.CatalogService;
import meg.biblio.common.ClientService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.ClassManagementService;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.web.model.ClassModel;
import meg.biblio.lending.web.validator.ClassModelValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@RequestMapping("/classes")
@Controller
public class SchoolGroupController {

	
	@Autowired
	ClassManagementService lendingService;
	
	
	@Autowired
	SelectKeyService keyService;
	
	@Autowired
	ClientService clientService;	
	
	@Autowired
	ClassModelValidator classValidator;
	
	
	
    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
	public String showClassList(Model uiModel, HttpServletRequest request) {
    	Long clientkey = clientService.getCurrentClientKey(request);
		
		List<SchoolGroupDao> classes = lendingService.getClassesForClient(clientkey);
    	uiModel.addAttribute("listofclasses",classes);
		return "schoolgroups/list";
	}

	@RequestMapping(value="/create",params = "form",method = RequestMethod.GET, produces = "text/html")
    public String createClassForm(Model uiModel, HttpServletRequest httpServletRequest) {
    	// create empty book model
    	ClassModel model = new ClassModel();
    	// place in uiModel
    	uiModel.addAttribute("classModel",model);
    	// return book/create
    	return "schoolgroups/create";
    }
    
    @RequestMapping(value="/create",method = RequestMethod.POST, produces = "text/html")
    public String createClass(ClassModel model,  Model uiModel,BindingResult bindingResult, HttpServletRequest httpServletRequest) {
    	Long clientkey = clientService.getCurrentClientKey(httpServletRequest);
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	
    	model.fillInTeacherFromEntry();
    	
    	classValidator.validateTeacherEntry(model,bindingResult);
    	
    	if (bindingResult.hasErrors()) {
			uiModel.addAttribute("classModel", model);
			return "schoolgroups/create";
		}
    	
    	uiModel.asMap().clear();
    	
    	// create class for teacher - and load class model
    	ClassModel newclass = lendingService.createClassFromClassModel(model, clientkey);
    	
    	// add list of unassigned students to model
    	List<StudentDao> unassigned = lendingService.getUnassignedStudents(clientkey);
    	newclass.setUnassignedstudents(unassigned);
    	
    	uiModel.addAttribute("classModel", newclass);
    	uiModel.addAttribute("newlycreated",true);
    	
    	// return edit/display page
    	return "schoolgroups/edit";
    	
    }    
    
    @RequestMapping(value="/editstudent/{id}", method = RequestMethod.GET, produces = "text/html")
    public String showEditStudentForm(@PathVariable("id") Long studentid, Model uiModel, HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	ClientDao client = clientService.getCurrentClient(httpServletRequest);
    	// load ClassModel
    	ClassModel sclass = lendingService.loadClassModelForStudent(studentid);

    	// set student in model
    	sclass.setStudentInModel(studentid);
    	
    	// put classmodel in model
    	uiModel.addAttribute("classModel",sclass);
    	
    	// return edit view
    	return "schoolgroups/editstudent";
    	}    
    
    @RequestMapping(value="/editstudent/{id}",  method = RequestMethod.POST, produces = "text/html")
    public String saveEditStudent(@ModelAttribute("classModel") ClassModel classModel,@PathVariable("id") Long studentid, Model uiModel, HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	ClientDao client = clientService.getCurrentClient(httpServletRequest);
    	
    	// get info from model
    	Long sid = classModel.getStudentid(); 
    	String firstname= classModel.getStudentfirstname(); 
    	String lastname = classModel.getStudentname(); 
    	Long sectionid = classModel.getStudentsection(); 

    	// save info in database
    	lendingService.editStudent(client.getId(), sid, firstname, lastname, sectionid);
    	
    	// load model
    	ClassModel sclass = lendingService.loadClassModelForStudent(sid);
    	
    	// add list of unassigned students to model
    	List<StudentDao> unassigned = lendingService.getUnassignedStudents(client.getId());
    	sclass.setUnassignedstudents(unassigned);
    	
    	// put classmodel in model
    	uiModel.addAttribute("classModel",sclass);
    	
    	// return edit class view
    	return "schoolgroups/edit";

    	} 
    
    @RequestMapping(value="/editstudent/{id}", params="cancel", method = RequestMethod.POST, produces = "text/html")
    public String cancelEditStudent(@ModelAttribute("classModel") ClassModel classModel,@PathVariable("id") Long studentid, Model uiModel, HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	ClientDao client = clientService.getCurrentClient(httpServletRequest);
    	
    	// load model
    	ClassModel sclass = lendingService.loadClassModelById(classModel.getClassid());
    	
    	// add list of unassigned students to model
    	List<StudentDao> unassigned = lendingService.getUnassignedStudents(client.getId());
    	sclass.setUnassignedstudents(unassigned);
    	
    	// put classmodel in model
    	uiModel.addAttribute("classModel",sclass);
    	
    	// return edit class view
    	return "schoolgroups/edit";

    	}     
    
    @RequestMapping(value="/display/{id}", method = RequestMethod.GET, produces = "text/html")
    public String showEditClassForm(@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	ClientDao client = clientService.getCurrentClient(httpServletRequest);
    	// load ClassModel
    	ClassModel sclass = lendingService.loadClassModelById(id);

    	
    	// add list of unassigned students to model
    	List<StudentDao> unassigned = lendingService.getUnassignedStudents(client.getId());
    	sclass.setUnassignedstudents(unassigned);
    	
    	
    	// put classmodel in model
    	uiModel.addAttribute("classModel",sclass);
    	
    	// return edit view
    	return "schoolgroups/edit";
    	}   
    

    @RequestMapping(value="/display/{id}", method = RequestMethod.POST, produces = "text/html")
	public String saveEditClass(@ModelAttribute("classModel") ClassModel bookModel,@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest) {
		Locale locale = httpServletRequest.getLocale();
		Long clientkey = clientService.getCurrentClientKey(httpServletRequest);
		String lang = locale.getLanguage();
	

		return null;
	}
    
    @RequestMapping(value="/display/{id}", params="addnew",method = RequestMethod.POST, produces = "text/html")
	public String addNewStudent(@ModelAttribute("classModel") ClassModel classModel,@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest) {
		Locale locale = httpServletRequest.getLocale();
		Long clientkey = clientService.getCurrentClientKey(httpServletRequest);
		String lang = locale.getLanguage();
	
		// validation
		// MM todo - validate that name has been filled in
		
		// save student
		ClassModel start = lendingService.loadClassModelById(id);
		lendingService.addNewStudentToClass(classModel.getStudentname(), classModel.getStudentsection(), start.getSchoolGroup(), clientkey);

    	// add list of unassigned students to model
    	List<StudentDao> unassigned = lendingService.getUnassignedStudents(clientkey);
    	classModel.setUnassignedstudents(unassigned);
    	
    	// put classmodel in model
    	uiModel.addAttribute("classModel",classModel);
    	
    	// return edit view
    	return "schoolgroups/edit";
	}   
    
    @RequestMapping(value="/display/{id}", params="addstudents",method = RequestMethod.POST, produces = "text/html")
	public String assignStudents(@ModelAttribute("classModel") ClassModel classModel,@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest) {
		Locale locale = httpServletRequest.getLocale();
		Long clientkey = clientService.getCurrentClientKey(httpServletRequest);
		String lang = locale.getLanguage();
	
		// get selected students
		List<Long> idstoadd=classModel.getSelectedUnassignedIds();
		
		// assign students
		ClassModel start = lendingService.loadClassModelById(id);
		ClassModel model = lendingService.assignStudentsToClass(idstoadd, start.getSchoolGroup(), clientkey);
				
    	// add list of unassigned students to model
    	List<StudentDao> unassigned = lendingService.getUnassignedStudents(clientkey);
    	model.setUnassignedstudents(unassigned);	

    	// put classmodel in model
    	uiModel.addAttribute("classModel",model);
    	
    	// return edit view
    	return "schoolgroups/edit";
	}     

    
    @RequestMapping(value="/display/{id}", params="removestudents",method = RequestMethod.POST, produces = "text/html")
	public String removeStudents(@ModelAttribute("classModel") ClassModel classModel,@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest) {
		Locale locale = httpServletRequest.getLocale();
		Long clientkey = clientService.getCurrentClientKey(httpServletRequest);
		String lang = locale.getLanguage();
	
		// get selected students
		List<Long> idstoremove=classModel.getSelectedIdsToRemove();
		
		// assign students
		ClassModel start = lendingService.loadClassModelById(id);
		ClassModel model = lendingService.removeStudentsFromClass(idstoremove, start.getSchoolGroup(), clientkey);
				
    	// add list of unassigned students to model
    	List<StudentDao> unassigned = lendingService.getUnassignedStudents(clientkey);
    	model.setUnassignedstudents(unassigned);	

    	// put classmodel in model
    	uiModel.addAttribute("classModel",model);
    	
    	// return edit view
    	return "schoolgroups/edit";
	}    
    
    @RequestMapping(value="/delete/{id}", method = RequestMethod.GET, produces = "text/html")
    public String deleteClass(@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	ClientDao client = clientService.getCurrentClient(httpServletRequest);
    	
    	lendingService.deleteClass(id, client.getId());

    	// return class list view
    	return "redirect:/classes";
    	}  
    
    @RequestMapping(value="/manage",  method = RequestMethod.GET, produces = "text/html")
    public String showManagementPage(Model uiModel, HttpServletRequest httpServletRequest) {
    	
    	// return edit class view
    	return "schoolgroups/settings";
    	}  
    
    @RequestMapping(value="/manage", params="increment", method = RequestMethod.POST, produces = "text/html")
    public String incrementStudents( Model uiModel, HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	ClientDao client = clientService.getCurrentClient(httpServletRequest);
    	
    	lendingService.moveAllStudentsToNextSection(client.getId());
    	
    	// return edit class view
    	return "schoolgroups/settings";

    	} 
    
    @RequestMapping(value="/manage", params="clearlists", method = RequestMethod.POST, produces = "text/html")
    public String clearClassLists(Model uiModel, HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	ClientDao client = clientService.getCurrentClient(httpServletRequest);
    	
    	
    	lendingService.clearStudentListsForClient(client.getId());
    	
    	// return edit class view
    	return "schoolgroups/settings";

    	}   
    
   
    @RequestMapping(value="/manage", params="toremove", method = RequestMethod.POST, produces = "text/html")
    public String showRemovePage(Model uiModel, HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	ClientDao client = clientService.getCurrentClient(httpServletRequest);
    	
    	// MM fill in model for remove
    	
    	// return edit class view
    	return "schoolgroups/remove";
    	}  
    
    @RequestMapping(value="/remove", params="toremove", method = RequestMethod.POST, produces = "text/html")
    public String removeStudents(Model uiModel, HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	ClientDao client = clientService.getCurrentClient(httpServletRequest);
    	
    	// get students to remove
    	
    	// remove them...
    	
    	// return edit class view
    	return "schoolgroups/remove";
    	}       
    
	@InitBinder
	public void initBinder(WebDataBinder binder) {
	    binder.setAutoGrowCollectionLimit(100024);
	}
	
   
    
    @ModelAttribute("detailstatusLkup")
    public HashMap<Long,String> getDetailStatusLkup(HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	
    	HashMap<Long, String> booktypedisps = keyService
    			.getDisplayHashForKey(CatalogService.detailstatuslkup, lang);
    	return booktypedisps; 
    }  
    
    @ModelAttribute("sectionLkup")
    public HashMap<Long,String> getSectionLkup(HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	
    	HashMap<Long, String> booktypedisps = keyService
    			.getDisplayHashForKey(ClassManagementService.sectionLkup, lang);
    	return booktypedisps; 
    }     
    
    @ModelAttribute("sectionSelect")
    public HashMap<Long,String> getSectionSelect(HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	
    	HashMap<Long, String> booktypedisps = keyService
    			.getDisplayHashForKey(ClassManagementService.sectionSelect, lang);
    	return booktypedisps; 
    }      
    
	@ModelAttribute("clientname") 
	public String getClientName(HttpServletRequest httpServletRequest) {
		ClientDao clientkey = clientService.getCurrentClient(httpServletRequest);
		return clientkey.getName();
	}
}
