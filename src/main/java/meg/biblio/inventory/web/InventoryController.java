package meg.biblio.inventory.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.ClassificationDao;
import meg.biblio.common.AppSettingService;
import meg.biblio.common.ClientService;
import meg.biblio.common.LoginService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.db.dao.UserLoginDao;
import meg.biblio.inventory.InventoryService;
import meg.biblio.inventory.InventoryStatus;
import meg.biblio.inventory.db.dao.InvStackDisplay;
import meg.biblio.inventory.db.dao.InventoryDao;
import meg.biblio.inventory.db.dao.InventoryHistoryDao;
import meg.biblio.inventory.web.model.CountModel;
import meg.biblio.inventory.web.model.ReconcileModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/inventory")
@Controller
public class InventoryController {

	@Autowired
	InventoryService invService;

	@Autowired
	ClientService clientService;

	@Autowired
	AppSettingService appSetting;

	@Autowired
	LoginService loginService;

	@Autowired
	CatalogService catalogService;

	@Autowired
	SelectKeyService keyService;

	@Autowired
	AppSettingService settingService;

	@RequestMapping(method = RequestMethod.GET, produces = "text/html")
	public String showInventoryEntryPoint(Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal,
			Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);

		// check if inventory is in progress
		InventoryDao inv = invService.getCurrentInventory(client);
		if (inv != null) {
			// if in progress, show current inventory page
			return showCurrentInventory(client, inv, uiModel);
		} else {
			// if no inventory in progress, show inventory list
			return showNonCurrentInventoryList(client, uiModel);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = "text/html")
	public String showInventoryList(Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal,
			Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);

		return showNonCurrentInventoryList(client, uiModel);
	}

	@RequestMapping(value = "/detail/{id}", method = RequestMethod.GET, produces = "text/html")
	public String showInventoryDetail(@PathVariable("id") Long inventoryid,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);

		// get inventory and status for id
		InventoryDao inventory = invService.getInventoryById(inventoryid);
		return showInventoryDetail(inventory, client,uiModel,httpServletRequest,principal,locale);
	}

	@RequestMapping(value = "/reconcile", method = RequestMethod.GET, produces = "text/html")
	public String showReconcileInventory(ReconcileModel reconcileModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);
		return showReconcileList(client, reconcileModel, uiModel, principal,
				locale, httpServletRequest);
	}

	private String showInventoryDetail(InventoryDao inventory, ClientDao client,Model uiModel,HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {


		// get inventory and status for id
		InventoryStatus status = invService.getInventoryStatus(inventory,
				client);

		// get history for id
		List<InventoryHistoryDao> reconciled = invService
				.getDetailForInventory(inventory,InventoryService.HistoryType.RECONCILED);
		List<InventoryHistoryDao> added = invService
				.getDetailForInventory(inventory,InventoryService.HistoryType.ADDED);
		// fill model
		uiModel.addAttribute("status", status);
		uiModel.addAttribute("history", reconciled);
		uiModel.addAttribute("historyadded", added);
		// fill lookups
		fillLookups(uiModel, httpServletRequest, principal, locale);
		// return view
		return "inventory/detail";
	}

	@RequestMapping(value = "/complete", method = RequestMethod.POST, produces = "text/html")
	public String completeInventory(ReconcileModel reconcileModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);
		// get current inventory
		InventoryDao current = invService.getCurrentInventory(client);

		boolean isComplete = invService.getInventoryIsComplete(client);

		if (isComplete) {
			Long invid = current.getId();
			invService.finishInventory(client);

			current = invService.getInventoryById(invid);
			return showInventoryDetail(current, client,uiModel,httpServletRequest,principal,locale);
		} else {
			// if not complete, add error (inv detail page)
			// MM TODO
		}

		return "inventory/detail";
	}

	@RequestMapping(value = "/reconcile/{id}", method = RequestMethod.GET, produces = "text/html")
	public String showBookToReconcile(@PathVariable("id") Long bookid,
			ReconcileModel recModel, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal,
			Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);
		InventoryDao current = invService.getCurrentInventory(client);
		fillLookups(uiModel, httpServletRequest, principal, locale);

		if (current != null) {
			// lookup book by id
			BookDao book = catalogService.findBookById(bookid);
			// place in model
			recModel.setReconcileBook(book);
			// get inventory status
			InventoryStatus status = invService.getInventoryStatus(current,
					client);
			recModel.setInventoryStatus(status);
		}
		// MM TODO inventtory not in progress error
		return "inventory/reconciledetail";
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET, produces = "text/html")
	public String showCountBooks(
			HttpServletRequest httpServletRequest,
			@RequestParam(value = "changepref", required = false) Long changepref,
			CountModel model, Model uiModel, Principal principal, Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);

		// switching count type??
		model.setCountTypePref(getCountType(client, model, changepref));

		InventoryDao current = invService.getCurrentInventory(client);
		if (current != null) {

			// get stack for user
			Long userid = getIdFromPrincipal(principal);
			List<InvStackDisplay> stack = invService.getStackForUser(userid,
					client);
			model.setUserStack(stack);

			// get inventory status
			InventoryStatus status = invService.getInventoryStatus(current,
					client);
			model.setInventoryStatus(status);

			// clear entry fields
			model.setBarcodeentry(null);
			model.setManualentry(null);
			uiModel.addAttribute("countModel",model);
		}

		// fill Lookups
		fillLookups(uiModel, httpServletRequest, principal, locale);

		if (model.getCountTypePref() == CountModel.CountType.BARCODE) {
			return "inventory/barcodecount";
		} else {
			return "inventory/manualcount";
		}
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = "text/html")
	public String createInventory(HttpServletRequest httpServletRequest,
			Model uiModel, Principal principal, Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);
		// check if current inventory in progress
		InventoryDao current = invService.getCurrentInventory(client);
		// if not, create new inventory
		if (current == null) {
			current = invService.beginInventory(client);

		}
		// get status for new inventory
		InventoryStatus status = invService.getInventoryStatus(current, client);
		// put data in page
		uiModel.addAttribute("status", status);
		// return inventory/current page
		return "inventory/current";
	}

	@RequestMapping(value = "/count", method = RequestMethod.POST, produces = "text/html")
	public String countBook(CountModel countModel, Model uiModel,
			BindingResult bindingResult, HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long userid = getIdFromPrincipal(principal);

		// get current inventory
		InventoryDao current = invService.getCurrentInventory(client);
		if (current != null) {
			// get book - according to entry type
			BookDao book = null;
			if (countModel.getCountTypePref() == CountModel.CountType.BARCODE) {
				String code = countModel.getBarcodeentry();
				if (code != null) {
					book = catalogService.findBookByBarcode(code);
				}
			} else {
				String clientnr = countModel.getManualentry();
				if (clientnr != null) {
					clientnr = clientnr.trim();
					book = catalogService.findBookByClientBookId(clientnr,
							client);
				}
			}

			if (book != null) {
				// count book
				invService.countBook(book, userid, client, true);
			} else {
				// Add error for unfound book
				bindingResult.reject("error_booknotfound", "Book Not Found");
			}
		} 
		// get stack for user
		List<InvStackDisplay> stack = invService.getStackForUser(
				userid, client);
		// get inventory status
		InventoryStatus status = invService.getInventoryStatus(current,
				client);
		// put info in model
		countModel.setUserStack(stack);
		countModel.setInventoryStatus(status);
		// reset entries
		countModel.setBarcodeentry(null);
		countModel.setManualentry(null);
		uiModel.addAttribute("countModel",countModel);
		// fill Lookups
		fillLookups(uiModel, httpServletRequest, principal, locale);

		// return page
		if (countModel.getCountTypePref() == CountModel.CountType.BARCODE) {
			return "inventory/barcodecount";
		} else {
			return "inventory/manualcount";
		}
	}

	@RequestMapping(value = "/clearstack", method = RequestMethod.POST, produces = "text/html")
	public String clearCountStack(CountModel countModel, Model uiModel,
			BindingResult bindingResult, HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);
		Long userid = getIdFromPrincipal(principal);

		// get current inventory
		InventoryDao current = invService.getCurrentInventory(client);
		if (current != null) {
			// clear user stack
			invService.clearStackForUser(userid, client);
			// get new (empty) stack for user
			List<InvStackDisplay> stack = invService.getStackForUser(userid,
					client);
			// get inventory status
			InventoryStatus status = invService.getInventoryStatus(current,
					client);
			// put info in model
			countModel.setUserStack(stack);
			countModel.setInventoryStatus(status);

		}

		// fill Lookups
		fillLookups(uiModel, httpServletRequest, principal, locale);
		// return page
		if (countModel.getCountTypePref() == CountModel.CountType.BARCODE) {
			return "inventory/barcodecount";
		} else {
			return "inventory/manualcount";
		}
	}

	@RequestMapping(value = "/reconcile", method = RequestMethod.POST, produces = "text/html")
	public String reconcileBookList(ReconcileModel reconcileModel,
			Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {
		// get client and inventory
		ClientDao client = clientService.getCurrentClient(principal);

		// get list of ids
		List<Long> bookids = reconcileModel.getCheckedBookIds();

		// get status
		Long status = reconcileModel.getUpdateStatus();

		// reconcile book list
		invService.reconcileBookList(client, bookids, status);

		return showReconcileList(client, reconcileModel, uiModel, principal,
				locale, httpServletRequest);
	}

	@RequestMapping(value = "/reconcile/{id}", method = RequestMethod.POST, produces = "text/html")
	public String reconcileSingleBook(@PathVariable("id") Long bookid,
			ReconcileModel recModel, Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal,
			Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);
		InventoryDao current = invService.getCurrentInventory(client);

		if (bookid != null) {
			// get update status
			Long status = recModel.getUpdateStatus();
			// note
			String note = recModel.getNote();
			// reconcile book
			invService.reconcileBook(client, bookid, status, note);
		}

		// show Reconcile list
return showReconcileList(client, recModel, uiModel, principal, locale, httpServletRequest);

	}

	private long getCountType(ClientDao client, CountModel model,
			Long changepref) {
		long origcounttype = model.getCountTypePref();
		if (changepref != null) {
			return changepref;
		} else {
			if (origcounttype == 0) {
				// setting default countype
				if (client.getUsesBarcodes() != null
						&& client.getUsesBarcodes()) {
					return CountModel.CountType.BARCODE;
				}
			}
		}
		return model.getCountTypePref();
	}

	private Long getIdFromPrincipal(Principal principal) {
		String username = principal.getName();
		UserLoginDao udet = loginService.getUserLoginDaoByName(username);
		Long userid = udet.getId();
		return userid;
	}

	private String showNonCurrentInventoryList(ClientDao client, Model uiModel) {
		// get previous inventories
		List<InventoryDao> inventories = invService.getPreviousInventories(client);
		
		// pull out last completed
		InventoryStatus lastcompleted = invService.getLastCompleted(client);

		// is inventory in progress?
		InventoryDao current = invService.getCurrentInventory(client);
		boolean inprogress = current!=null;
		
		// set in model
		uiModel.addAttribute("previous", inventories);
		uiModel.addAttribute("lastcompleted", lastcompleted);
		uiModel.addAttribute("invinprogress",inprogress);
		
		return "inventory/list";
	}

	private String showCurrentInventory(ClientDao client, InventoryDao inv,
			Model uiModel) {
		// put inventory in model
		uiModel.addAttribute("currentinv", inv);
		// get inventorystatus
		InventoryStatus status = invService.getInventoryStatus(inv, client);
		boolean isComplete = invService.getInventoryIsComplete(client);
		// put inventorystatus in model
		uiModel.addAttribute("status", status);
		uiModel.addAttribute("complete", isComplete);
		return "inventory/current";
	}

	private String showReconcileList(ClientDao client,
			ReconcileModel reconcileModel, Model uiModel, Principal principal,
			Locale locale, HttpServletRequest httpServletRequest) {
		// get current inventory
		InventoryDao current = invService.getCurrentInventory(client);

		boolean isComplete = invService.getInventoryIsComplete(client);
		// get inventory status
		InventoryStatus status = invService.getInventoryStatus(current, client);
		// get uncounted list
		List<InvStackDisplay> uncountedlist = invService
				.getUncountedBooks(client);
		// get maximum size of list
		Integer maxreconcile = appSetting.getSettingAsInteger("biblio.inventory.showtoreconcile");
		// check if we need to deal with this
		if (maxreconcile.intValue()<uncountedlist.size()) {
			// get number of results
			int totaltoreconcile = uncountedlist.size();
			// cut list
			uncountedlist = uncountedlist.subList(0, maxreconcile.intValue());
			// set result total and cap in model
			reconcileModel.setTotalUncounted(totaltoreconcile);
			reconcileModel.setMaxUncounted(maxreconcile);
		}


		// set info in model
		reconcileModel.setInventoryStatus(status);
		reconcileModel.setUncountedBooks(uncountedlist);
		reconcileModel.setInventoryComplete(isComplete);
		uiModel.addAttribute("reconcileModel",reconcileModel);
		
		// fill Lookups
		fillLookups(uiModel, httpServletRequest, principal, locale);
		return "inventory/reconcilelist";

	}

	private void fillLookups(Model uiModel,
			HttpServletRequest httpServletRequest, Principal principal,
			Locale locale) {
		String lang = locale.getLanguage();
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();

		// @ModelAttribute("statusLkup")
		HashMap<Long, String> statusLkup = keyService.getDisplayHashForKey(
				CatalogService.bookstatuslkup, lang);
		uiModel.addAttribute("statusLkup", statusLkup);

		String imagebasedir = settingService
				.getSettingAsString("biblio.imagebase");
		uiModel.addAttribute("imagebasedir", imagebasedir);

		HashMap<Long, ClassificationDao> shelfclasses = catalogService
				.getShelfClassHash(clientkey, lang);
		uiModel.addAttribute("classHash", shelfclasses);

		HashMap<Long, String> recStatusLkup = keyService.getDisplayHashForKey(
				InventoryService.reconcilestatuslkup, lang);
		uiModel.addAttribute("recStatusLkup", recStatusLkup);

		String shortname = client.getShortname();
		uiModel.addAttribute("clientname", shortname);
	}

	@RequestMapping(value = "/cancel", method = RequestMethod.GET, produces = "text/html")
	public String showCancelInventory(Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);
		// get current inventory
		InventoryDao current = invService.getCurrentInventory(client);

		// get status
		InventoryStatus status = invService.getInventoryStatus(current, client);

		// cancel inventory
		uiModel.addAttribute("status",status);
		uiModel.addAttribute("cancelsuccess",false);
		uiModel.addAttribute("showmessage",true);

		return "inventory/cancel";
	}

	@RequestMapping(value = "/cancel", method = RequestMethod.POST, produces = "text/html")
	public String cancelInventory(Model uiModel, HttpServletRequest httpServletRequest,
			Principal principal, Locale locale) {
		ClientDao client = clientService.getCurrentClient(principal);

		invService.cancelCurrentInventory(client);
		// cancel inventory
		uiModel.addAttribute("cancelsuccess",true);
		uiModel.addAttribute("showmessage",false);

		return "inventory/cancel";
	}

}
