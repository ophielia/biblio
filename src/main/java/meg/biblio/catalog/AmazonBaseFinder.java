package meg.biblio.catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import meg.biblio.catalog.db.PublisherRepository;
import meg.biblio.catalog.db.SubjectRepository;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.common.AppSettingService;
import meg.biblio.search.SearchService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Component
public class AmazonBaseFinder extends BaseDetailFinder {

	@Autowired
	AppSettingService settingService;

	@Autowired
	SearchService searchService;

	@Autowired
	PublisherRepository pubRepo;

	@Autowired
	SubjectRepository subjectRepo;

	final static class NameMatchType {
		public static final long FIRSTINITIAL = 1;
		public static final long LASTNAME = 2;
	}

	/* Get actual class name to be printed on */
	static Logger log = Logger.getLogger(AmazonBaseFinder.class.getName());

	Boolean lookupwithamazon;
	String apikeyid;
	String apisecretkey;


	private String apiassociatetag;

	/*
	 * Use one of the following end-points, according to the region you are
	 * interested in:
	 * 
	 * US: ecs.amazonaws.com CA: ecs.amazonaws.ca UK: ecs.amazonaws.co.uk DE:
	 * ecs.amazonaws.de FR: ecs.amazonaws.fr JP: ecs.amazonaws.jp
	 */
	private static final String ENDPOINT = "ecs.amazonaws.fr";

	protected boolean isEnabled() throws Exception {
		if (lookupwithamazon == null) {
			lookupwithamazon = settingService
					.getSettingAsBoolean("biblio.amazon.turnedon");
		}
		return lookupwithamazon;
	}


	@Override
	public List<FinderObject> findDetailsForList(List<FinderObject> objects,
			long clientcomplete, Integer batchsearchmax) throws Exception {
		// check enabled
		if (isEnabled()) {

			// go through list
			for (FinderObject findobj : objects) {
				// check eligibility for object (eligible and not complete)
				if (isEligible(findobj)
						&& !resultsComplete(findobj, clientcomplete)) {
					// do search
					findobj = searchLogic(findobj);
					// log, process search
					findobj.logFinderRun(getIdentifier());
				}
				// build in  tiny pause to not exceed requests per second
				try {
				    Thread.sleep(200);                 //1000 milliseconds is one second.
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}
			} // end list loop
		}
		// pass to next in chain, or return
		if (getNext() != null) {
			objects = getNext().findDetailsForList(objects, clientcomplete,
					batchsearchmax);
		}

		return objects;
	
	}

	protected FinderObject searchLogic(FinderObject findobj) throws Exception {
		BookDetailDao bookdetail = findobj.getBookdetail();
		if (apikeyid == null) {
			apikeyid = settingService.getSettingAsString("biblio.am.keyd");
		}
		if (apisecretkey == null) {
			apisecretkey = settingService.getSettingAsString("biblio.am.keys");

		}
		if (apiassociatetag == null) {
			apiassociatetag = settingService
					.getSettingAsString("biblio.am.atag");
		}

		AmazonSignedRequestHelper helper;
		try {
			helper = AmazonSignedRequestHelper.getInstance(ENDPOINT, apikeyid,
					apisecretkey);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		Map<String, String> params = new HashMap<String, String>();
		// common params
		params.put("Service", "AWSECommerceService");
		params.put("Version", "2009-03-31");
		params.put("ResponseGroup", "Large");
		params.put("AssociateTag", apiassociatetag);
		params.put("SearchIndex", "Books");
		
		// add params by search type (isbn, or other (title, author, publisher)
		if (bookdetail.hasIsbn()) {
			// doing an isbn search
			if (bookdetail.getIsbn13()!=null) {
				params.put("ItemId",bookdetail.getIsbn13());	
				params.put("IdType","EAN");
			} else {
				params.put("ItemId",bookdetail.getIsbn10());	
				params.put("IdType","ISBN");
			}
			params.put("Operation", "ItemLookup");
		} else {
			// searching by title, author, and / or publisher
			String title = bookdetail.getTitle();
			String artist=null;
			String publisher=null;
			if (bookdetail.getAuthors()!=null && bookdetail.getAuthors().size()>0) {
				artist = bookdetail.getAuthors().get(0).getDisplayName();
			} else {
				if (bookdetail.getIllustrators()!=null && bookdetail.getIllustrators().size()>0) {
					artist = bookdetail.getAuthors().get(0).getDisplayName();
				}
			}
			if (bookdetail.getPublisher() != null) {
				PublisherDao pub = bookdetail.getPublisher();
				publisher = pub.getName();
			}			
			
			// now put in parameters
			params.put("Title", title);
			if (artist!=null) {
				params.put("Author", artist);	
			}
			if (publisher!=null) {
				params.put("Publisher", publisher);	
			}

			// standard parameters
			params.put("Operation", "ItemSearch");
			
		}
	
		String requestUrl = helper.sign(params);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(requestUrl);

		/*
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		OutputStream out = new BufferedOutputStream(new FileOutputStream(
				new File("C:/Temp/myfile.xml")));
		Result output = new StreamResult(out);

		Source input = new DOMSource(doc);
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(input, output);
*/
		
		// make list of Item documents (Item nodes from returned request as entire document)
		List<Document> items = new ArrayList<Document>();
		// get Item nodes from returned documents
		NodeList itemnodes = doc.getElementsByTagName("Item");
		// make each node into a new document
		if (itemnodes!=null) {
			for (int i=0;i<itemnodes.getLength();i++) {
				Node node = itemnodes.item(i);
				Document newDocument = db.newDocument();
				Node imported = newDocument.importNode(node, true);
				newDocument.appendChild(imported);
				// add document to the list
				items.add(newDocument);
			}
		}

		// process item documents into bookdetails
		List<FoundDetailsDao> copiedinfo = copyResultsIntoFoundDetails(items);
		
		// process list according to size - (one, none, or multiple results found)
		if (copiedinfo !=null) {
			if (copiedinfo.size()==0) {
				findobj.setSearchStatus(CatalogService.DetailStatus.DETAILNOTFOUND);	
			} else if (copiedinfo.size()==1){
				findobj.setSearchStatus(CatalogService.DetailStatus.DETAILFOUND);
				FoundDetailsDao found = copiedinfo.get(0);
				bookdetail = mergeFoundIntoBookDetail(found,bookdetail);
				findobj.setBookdetail(bookdetail);
			} else {
				findobj.setSearchStatus(CatalogService.DetailStatus.MULTIDETAILSFOUND);
				findobj.setMultiresults(copiedinfo);
			}
		}else {
			// no detail found in data
			findobj.setSearchStatus(CatalogService.DetailStatus.DETAILNOTFOUND);
		}
		
		
		// return finderobject
		return findobj;

	}

	
	private BookDetailDao mergeFoundIntoBookDetail(FoundDetailsDao found,
			BookDetailDao bookdetail) {
		// copy basic info into bookdetail
		String title = found.getTitle();
		String imagelink = found.getImagelink();
		String isbn10 = found.getIsbn10();
		String isbn13 = found.getIsbn13();
		String publisher = found.getPublisher();
		Long publishyear = found.getPublishyear();
		String language = found.getLanguage();
		String description = found.getDescription();
		String authors = found.getAuthors();
		
		
		// set title
		bookdetail.setTitle(title);
		bookdetail.setImagelink(imagelink);

		// isbn- 10 or 13
		if (isbn10 != null) {
			bookdetail.setIsbn10(isbn10);
		}
		if (isbn13 != null) {
			bookdetail.setIsbn10(isbn13);
		}


		// publisher
		if (publisher != null && bookdetail.getPublisher() == null) {
			PublisherDao pub = findPublisherForName(publisher);
			bookdetail.setPublisher(pub);
		}

		// publishyear
		if (publishyear != null) {
			bookdetail.setPublishyear(new Long(publishyear));
		}

		// language
		if (language != null) {
			bookdetail.setLanguage(language);
		}

		// description
		if (description.trim().length() > 0) {
			String origdesc = bookdetail.getDescription();
			if (origdesc == null
					|| origdesc.trim().length() < description.length()) {
				bookdetail.setDescription(description);
			}
		}

		// authors
		// break into a list
		if (authors!=null) {
			String[] autharray = authors.split(",");
			List<String> authorlist = new ArrayList<String>();
			for (int i=0;i<autharray.length;i++) {
				String toadd = autharray[i];
				if (toadd!=null && toadd.trim().length()>0) {
					authorlist.add(toadd.trim());
				}
			}
			bookdetail = insertAuthorsIntoBookDetail(authorlist, bookdetail);
		}
		
		// return bookdetail
		return bookdetail;
	}

	private List<FoundDetailsDao> copyResultsIntoFoundDetails(List<Document> items) throws Exception {
		Integer maxdetails = settingService
				.getSettingAsInteger("biblio.amazon.maxdetails");	
		int maxint = maxdetails!=null?maxdetails.intValue():5;
		
		int found = 0;
		if (items!=null && items.size()>0) {
			List<FoundDetailsDao> results = new ArrayList<FoundDetailsDao>();
			for (Document itemdoc:items) {
				found++;
				if (found>maxint) {
					break;
				}
				FoundDetailsDao fd = new FoundDetailsDao();
				fd.setSearchsource(getIdentifier());
				
				// gather info
				Node node = itemdoc.getElementsByTagName("ASIN").item(0);
				String catalognr= node != null ? node.getTextContent() : "";
				
				node = itemdoc.getElementsByTagName("Title").item(0);
				String title = node != null ? node.getTextContent() : "";

				NodeList nodes = itemdoc.getElementsByTagName("MediumImage");
				node = getChildnode("URL", nodes);
				String imagelink = node != null ? node.getChildNodes().item(0)
						.getTextContent() : "";

				nodes = itemdoc.getElementsByTagName("Content");
				String description = "";
				for (int i = 0; i < nodes.getLength(); i++) {
					Node nd = nodes.item(i);
					String parentnm = nd.getParentNode() != null ? nd.getParentNode()
							.getLocalName() : "";
					if (parentnm != null && parentnm.equals("EditorialReview")) {
						String newd = nd.getTextContent();
						description = newd.length() > description.length() ? newd
								: description;
					}
				}

				nodes = itemdoc.getElementsByTagName("ISBN");
				node = nodes.item(0);
				String isbn = node != null ? node.getTextContent() : "";

				nodes = itemdoc.getElementsByTagName("Language");
				node = getChildnode("Name", nodes);
				String rawlanguage = node != null ? node.getTextContent() : "";

				node = itemdoc.getElementsByTagName("Publisher").item(0);
				String publisher = node != null ? node.getTextContent() : "";

				node = itemdoc.getElementsByTagName("PublicationDate").item(0);
				String publishyear = node != null ? node.getTextContent() : "";

				nodes = itemdoc.getElementsByTagName("Author");
				List<String> authors = new ArrayList<String>();
				for (int i = 0; i < nodes.getLength(); i++) {
					Node nd = nodes.item(i);
					authors.add(nd.getTextContent());
				}

				// continue to next document, if no isbn listed
				if (isbn==null || isbn.trim().length()==0) {
					continue;
				}
				
				// copy info into book detail
				// set title
				fd.setTitle(title);
				fd.setImagelink(imagelink);

				// isbn- 10 or 13
				if (isbn != null) {
					String str = isbn.replaceAll("[^\\d.X]", "");
					if (str.length() > 10) {
						fd.setIsbn13(str);
					}
					fd.setIsbn10(str);
				}

				// publisher
				if (publisher != null && fd.getPublisher() == null) {
					fd.setPublisher(publisher);
				}

				// publishyear
				if (publishyear != null) {
					if (publishyear.contains("-")) {
						// chop off after dash
						publishyear = publishyear
								.substring(0, publishyear.indexOf("-"));
						fd.setPublishyear(new Long(publishyear));
					} else if (publishyear.contains("?")) {
						// do nothing - vague year
					} else {
						fd.setPublishyear(new Long(publishyear));
					}
				}

				// language
				if (rawlanguage != null) {
					if (rawlanguage.equals("Français")) {
						fd.setLanguage("fr");
					} else if (rawlanguage.equals("Anglais")) {
						fd.setLanguage("en");
					}
					// MM else - help with this else!! some kind of lookup!
				}

				// description
				fd.setDescription(description);

				// authors
				StringBuilder authorbuilder = new StringBuilder();
				if (authors != null) {
					for (String author : authors) {
						authorbuilder.append(author).append(",");
					}
				}
				if (authorbuilder.length() > 1) {
					authorbuilder.setLength(authorbuilder.length() - 1);
				}
				fd.setAuthors(authorbuilder.toString());
		
				// add catalog nr
				fd.setSearchserviceid(catalognr);
				
				// add bookdetail to result list
				results.add(fd);
				
			}// end of loop through items
			return results;
		}
		return null;
	}


	private Node getChildnode(String nodename, NodeList nodes) {
		for (int i = 0; i < nodes.getLength(); i++) {
			Node nd = nodes.item(i);
			NodeList children = nd.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node nnd = children.item(j);
				if (nnd != null) {
					String name = nnd.getNodeName();
					if (name != null && name.equals(nodename)) {
						return nnd;
					}
				}
			}

		}
		return null;
	}

	private PublisherDao findPublisherForName(String text) {
		if (text != null) {
			// clean up text
			text = text.trim();
			// query db
			List<PublisherDao> foundlist = pubRepo.findPublisherByName(text
					.toLowerCase());
			if (foundlist != null && foundlist.size() > 0) {
				return foundlist.get(0);
			} else {
				// if nothing found, make new PublisherDao
				PublisherDao pub = new PublisherDao();
				pub.setName(text);
				return pub;
			}
		}
		return null;
	}


}
