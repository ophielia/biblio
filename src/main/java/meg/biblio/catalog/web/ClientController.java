package meg.biblio.catalog.web;
import meg.biblio.common.db.dao.ClientDao;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/client")
@Controller
@RooWebScaffold(path = "client", formBackingObject = ClientDao.class)
public class ClientController {
}
