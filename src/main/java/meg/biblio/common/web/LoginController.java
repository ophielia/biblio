package meg.biblio.common.web;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import meg.biblio.common.ClientService;
import meg.biblio.common.LoginService;
import meg.biblio.common.SelectKeyService;
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

@RequestMapping("/userlogins")
@Controller
public class LoginController {

	@Autowired
	protected LoginService accountService;

	@Autowired
	protected SelectKeyService keyService;
	
	@Autowired
	protected ClientService clientService;
	
	@Autowired
	protected AuthenticationManager authenticationManager;
	
	@Autowired
	UserLoginValidator userloginValidator;

	@RequestMapping(value = "/create",params = "form", produces = "text/html")
	public String createForm(Model uiModel,HttpServletRequest request) {
		Long clientkey = clientService.getCurrentClientKey(request);
		populateEditForm(uiModel, new UserLoginDao(), clientkey, null);
		return "userlogins/create";
	}

	@RequestMapping(value = "/create",method = RequestMethod.POST, produces = "text/html")
	public String create(@ModelAttribute("userLoginDao") UserLoginDao userlogin,
			BindingResult bindingResult, Model uiModel,
			HttpServletRequest httpServletRequest) {
		Long clientkey = clientService.getCurrentClientKey(httpServletRequest);
		userloginValidator.validate(userlogin, bindingResult);

		if (bindingResult.hasErrors()) {
			populateEditForm(uiModel, userlogin, clientkey, null);
			return "userlogins/create";
		}
		uiModel.asMap().clear();
		String origpassword = userlogin.getTextpassword();
		// use account service to create account
		userlogin = accountService.createNewUserLogin(userlogin, clientkey);
		// authenticate account
		authenticateUserAndSetSession(userlogin.getUsername(), origpassword,
				httpServletRequest);

		return "redirect:/userlogins";
	}

	private void authenticateUserAndSetSession(String username,
			String password, HttpServletRequest request) {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
				username, password);

		// generate session if one doesn't exist
		request.getSession();

		token.setDetails(new WebAuthenticationDetails(request));
		Authentication authenticatedUser = authenticationManager
				.authenticate(token);

		SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
	}

	@RequestMapping(produces = "text/html")
	public String list(Model uiModel, HttpServletRequest httpServletRequest)  {
		// get client key
		Long clientkey = clientService.getCurrentClientKey(httpServletRequest);
		// get users for client
		List<UserLoginDao> users = accountService.getUsersForClient(clientkey);
		// put in model
		uiModel.addAttribute("users",users);
		// return list view
		return "userlogins/list";
	}



	@RequestMapping(value = "/update/{id}", method = RequestMethod.GET,params = "form", produces = "text/html")
	public String updateForm(@PathVariable("id") Long id, Model uiModel,HttpServletRequest request) {
		Long clientkey = clientService.getCurrentClientKey(request);
		// does userlogin belong to client
		// get client
		ClientDao client = clientService.getClientForKey(clientkey);

		// get userlogin
		UserLoginDao userlogin = accountService.getUserLoginDaoById(id);
		
		// assure that userlogin belongs to current account
		long userclientid = userlogin.getClient()!=null?userlogin.getClient().getId().longValue():0;
		if (clientkey.longValue() == userclientid) {
			populateEditForm(uiModel, userlogin, clientkey, userlogin.getRole());
			return "userlogins/edit";
		}
		return "redirect:/userlogins";
	}

	@RequestMapping(value = "/update/{id}",method = RequestMethod.POST, produces = "text/html")
	public String update(
			@ModelAttribute("userLoginDao") UserLoginDao userlogin,
			Model uiModel, BindingResult bindingResult,
			HttpServletRequest httpServletRequest) {
		Long clientkey = clientService.getCurrentClientKey(httpServletRequest);
		// does userlogin belong to client
		// get client
		ClientDao client = clientService.getClientForKey(clientkey);

		// assure that userlogin belongs to current account
		long userclientid = userlogin.getClientkey() != null ? userlogin
				.getClientkey().longValue() : 0;
		if (clientkey.longValue() == userclientid) {
			userloginValidator.validateUpdate(userlogin,bindingResult);
			if (bindingResult.hasErrors()) {
				populateEditForm(uiModel, userlogin, null, null);
				return "userlogins/update";
			}
			// save changes to userlogin
			accountService.updateUserLoginDao(userlogin);
		}
		// redirect to userlogin list
		return "redirect:/userlogins";
	}

	void populateEditForm(Model uiModel, UserLoginDao userlogin, Long clientkey, RoleDao role) {
		// set transient fields
		userlogin.setClientkey(clientkey);
		if (role!=null) {
			userlogin.setRolename(role.getRolename());
		}
		
		uiModel.addAttribute("userLoginDao", userlogin);
		addDateTimeFormatPatterns(uiModel);
	}

	private void addDateTimeFormatPatterns(Model uiModel) {
		uiModel.addAttribute(
				"account_createdon_date_format",
				DateTimeFormat.patternForStyle("M-",
						LocaleContextHolder.getLocale()));
	}
	
	@ModelAttribute("clientname") 
	public String getClientName(HttpServletRequest httpServletRequest) {
		ClientDao clientkey = clientService.getCurrentClient(httpServletRequest);
		return clientkey.getName();
	}
	
    @ModelAttribute("roleLkup")
    public HashMap<String,String> getRoleLkup(HttpServletRequest httpServletRequest) {
    	Locale locale = httpServletRequest.getLocale();
    	String lang = locale.getLanguage();
    	
    	HashMap<String, String> langdisps = keyService
    			.getStringDisplayHashForKey(LoginService.rolelkup, lang);
    	return langdisps; 
    }  	
/*
 * 
 * 
	
	
  	private String encodeUrlPathSegment(String pathSegment,
			HttpServletRequest httpServletRequest) {
		String enc = httpServletRequest.getCharacterEncoding();
		if (enc == null) {
			enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
		}
		try {
			pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
		} catch (UnsupportedEncodingException uee) {
		}
		return pathSegment;
	}
	
		@RequestMapping(value = "/display/{id}", produces = "text/html")
	public String show(@PathVariable("id") Long id, Model uiModel,HttpServletRequest httpServletRequest) {
		Long clientkey = clientService.getCurrentClientKey();
		// get client
		ClientDao client = clientService.getClientForKey(clientkey);
		
		// get userlogin
		UserLoginDao user = accountService.getUserLoginDaoById(id);
		
		// assure that userlogin belongs to current account
		long userclientid = user.getClient()!=null?user.getClient().getId().longValue():0;
		if (clientkey.longValue()==userclientid) {
			// put user in model
			uiModel.addAttribute("user", user);
			// return view			
			return "userlogins/display";
		}

		
		// otherwise return redirect to list
		return "redirect:/userlogins";
	}
 */
}

