package meg.biblio.catalog.web;

import javax.servlet.http.HttpServletRequest;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.catalog.web.validator.BookModelValidator;
import meg.biblio.common.ClientService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@RequestMapping("/books")
@Controller
public class BookController {

	@Autowired
	CatalogService catalogService;
	
	@Autowired
	ClientService clientService;	
	
	@Autowired
	BookModelValidator bookValidator;
	
    @RequestMapping(params = "form",method = RequestMethod.GET, produces = "text/html")
    public String createBookEntryForm(Model uiModel,BindingResult bindingResult, HttpServletRequest httpServletRequest) {
    	// create empty book model
    	BookModel model = new BookModel();
    	// place in uiModel
    	uiModel.addAttribute("bookModel",model);
    	// return book/create
    	return "book/create";
    	
    }
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String createBookEntry(BookModel model,  Model uiModel,BindingResult bindingResult, HttpServletRequest httpServletRequest) {
    	Long clientkey = clientService.getCurrentClientKey();
    	
    	bookValidator.validateSimpleEntry(model, bindingResult);

		if (bindingResult.hasErrors()) {
			uiModel.addAttribute("bookModel", model);
			return "book/create";
		}
        uiModel.asMap().clear();
        
        // process book entries (author, illustrator)
        model.processAuthorEntry();
        model.processIllustratorEntry();
        
        // add book to catalog
        BookModel book = catalogService.createCatalogEntryFromBookModel(clientkey,model);
        Long bookid = book.getBookid();
        
        uiModel.addAttribute("bookModel", book);

        return "redirect:/books/display/" + bookid;
    }    
    
    @RequestMapping(value="/display/{id}", method = RequestMethod.PUT, produces = "text/html")
    public String showBook(@PathVariable("id") Long id, Model uiModel,BindingResult bindingResult, HttpServletRequest httpServletRequest) {
    	BookModel model = new BookModel();
    	if (id!=null) {
    		model = catalogService.loadBookModel(id);	
    	} 
    	
    	uiModel.addAttribute("bookModel",model);

    	return "book/show";
    }    
}
