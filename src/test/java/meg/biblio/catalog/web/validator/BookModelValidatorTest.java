package meg.biblio.catalog.web.validator;

import junit.framework.Assert;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.web.model.BookModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class BookModelValidatorTest {
	
	
	@Autowired
	BookModelValidator bookValidator;

	@Test
	public void testValidateSimpleEntry() {
		// just tests with an empty title
		BookDao book = new BookDao();
		BookModel model = new BookModel(book);

		BindingResult errors=new BeanPropertyBindingResult(model,"bookModel");
		
		bookValidator.validateSimpleEntry(model, errors);
		
		Assert.assertTrue(1==errors.getErrorCount());
	}

}
