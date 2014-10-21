package meg.biblio.catalog.web.validator;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.web.model.BookModel;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class BookModelValidator {

	public void validateSimpleEntry(BookModel model, BindingResult errors) {
		BookDao book = model.getBook();
		
		// check other standard fields
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		javax.validation.Validator validator = factory.getValidator();
		Set<ConstraintViolation<BookDao>> valerrors = validator.validate(book);

		// put JSR-303 errors into standard errors
		for (ConstraintViolation<BookDao> cv : valerrors) {
			errors.rejectValue(cv.getPropertyPath().toString(),
					cv.getMessageTemplate());

		}

	}

}
