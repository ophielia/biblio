package meg.biblio.common.web.validator;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import meg.biblio.common.LoginService;
import meg.biblio.common.db.dao.UserLoginDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;


import org.springframework.validation.Validator;


@Component
public class UserLoginValidator implements Validator {

	@Autowired
    LoginService userloginService;	
	
	@Override
    public boolean supports(Class clazz) {
      return UserLoginDao.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
    	UserLoginDao acct = (UserLoginDao)target;
    	
    	boolean isnewUserLoginDao=false;
    	isnewUserLoginDao=acct.getId()==null;
    	// Username is unique
    	if (acct.getId()==null && userloginService.userExists(acct.getUsername())) {
    		errors.rejectValue("username","field_usernametaken");
    	}

    	
		// password and verification match
		if (acct.getTextpassword() != null && acct.getTextpassword().trim().length()>0) {
			// verification there
			ValidationUtils.rejectIfEmpty(errors, "passwordverify",
					"field_verificationrequired");

			if (acct.getPasswordverify() != null) {
				if (!acct.getTextpassword().equals(acct.getPasswordverify())) {
					errors.rejectValue("passwordverify",
							"field_verificationnomatch");
				}
			}
		}
    	
    	
    	// if old password is there, check match
    	if (acct.getOldpassword()!=null) {
    		boolean match = userloginService.oldPasswordMatches(acct.getOldpassword(), acct.getId());
    		if (!match) {
    			errors.rejectValue("oldpassword","field_oldverificationnomath");	
    		}
    	}
    	
    	// check other standard fields
    	 ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
         javax.validation.Validator validator = factory.getValidator();
         Set<ConstraintViolation<UserLoginDao>> valerrors = validator.validate(acct);
    
    	// put JSR-303 errors into standard errors
         for (ConstraintViolation<UserLoginDao> cv:valerrors) {
        	 if  (!(isnewUserLoginDao && cv.getPropertyPath().toString().equals("password") ) ){
        		 errors.rejectValue(cv.getPropertyPath().toString(), stripBraces(cv.getMessageTemplate()));	 
        	 }
        	 
         }
    }

	public void validateUpdate(UserLoginDao target,
			Errors errors) {
	   	UserLoginDao acct = (UserLoginDao)target;
	   	// password and verification match
		if (acct.getTextpassword() != null && acct.getTextpassword().trim().length()>0) {
			// verification there
			ValidationUtils.rejectIfEmpty(errors, "passwordverify",
					"field_verificationrequired");

			if (acct.getPasswordverify() != null) {
				if (!acct.getTextpassword().equals(acct.getPasswordverify())) {
					errors.rejectValue("passwordverify",
							"field_verificationnomatch");
				}
			}
		}

		
	}

	private String stripBraces(String tostrip) {
		tostrip=tostrip.substring(1);
		tostrip=tostrip.substring(0,tostrip.length()-1);
		return tostrip;
	}
}
