package meg.biblio.catalog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volume.VolumeInfo;
import com.google.api.services.books.model.Volume.VolumeInfo.IndustryIdentifiers;

@Component
public class AmazonDetailFinder extends AmazonBaseFinder {

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
	static Logger log = Logger.getLogger(AmazonDetailFinder.class.getName());

	Boolean lookupwithamazon;
	String apikeyid;
	String apisecretkey;
	Long identifier = 3L;

	private String apiassociatetag;

	/*
	 * Use one of the following end-points, according to the region you are
	 * interested in:
	 * 
	 * US: ecs.amazonaws.com CA: ecs.amazonaws.ca UK: ecs.amazonaws.co.uk DE:
	 * ecs.amazonaws.de FR: ecs.amazonaws.fr JP: ecs.amazonaws.jp
	 */
	private static final String ENDPOINT = "ecs.amazonaws.fr";



	protected Long getIdentifier() throws Exception {
		return identifier;
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
		if (items!=null && items.size()>0) {
			List<FoundDetailsDao> results = new ArrayList<FoundDetailsDao>();
			for (Document itemdoc:items) {
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
