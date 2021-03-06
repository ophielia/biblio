package meg.biblio.catalog;

import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.common.AppSettingService;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
class BNFCatalogFinder extends BaseDetailFinder {

	
	/*


	@Autowired
	SearchService searchService;

	@Autowired
	PublisherRepository pubRepo;

	@Autowired
	SubjectRepository subjectRepo;


*/

    @Autowired
    AppSettingService settingService;

    @Autowired
    BookMemberService bMemberService;

    /* Get actual class name to be printed on */
    static Logger log = Logger.getLogger(BNFCatalogFinder.class.getName());

    private Boolean lookupwithbnf;
    private Long identifier = 7L;

    private static final String dataquery_titleauthor = "SELECT DISTINCT ?link ?ean ?isbn ?date WHERE { ?manifestation dcterms:title \"TITLE\". ?manifestation rdarelationships:expressionManifested ?expression. ?manifestation bnf-onto:EAN ?ean. ?manifestation dcterms:publisher ?date. ?manifestation bnf-onto:isbn ?isbn. ?manifestation rdfs:seeAlso ?link. ?expression marcrel:aut ?person. ?person foaf:name \"AUTHOR\". }";
    private static final String dataquery_isbn13 = "SELECT DISTINCT ?link WHERE { ?question rdfs:seeAlso ?link. ?question bnf-onto:EAN \"REPLACE\". }";
    private static final String dataquery_isbn10 = "SELECT DISTINCT ?link WHERE { ?question rdfs:seeAlso ?link. ?question bnf-onto:ISBN \"REPLACE\". }";
    private static final String databnfrequest = "http://data.bnf.fr/sparql?default-graph-uri=&query=REPLACE&should-sponge=&format=xml&timeout=0&debug=on";

    private static final String beginparse = "<!--Contenu de la notice-->";
    private static final String newlinemarker = "!|!";
    private static final String newlinemarkersplit = "\\!\\|\\!";

    protected boolean isEnabled() throws Exception {
        if (lookupwithbnf == null) {
            lookupwithbnf = settingService
                    .getSettingAsBoolean("biblio.bnf.turnedon");
        }
        return lookupwithbnf;
    }

    @Override
    protected boolean isEligible(FinderObject findobj) throws Exception {
        // Eligible to be run if both author and title are available
        if (findobj.getBookdetail().hasAuthor()
                && findobj.getBookdetail().getTitle() != null)
            return true;

        return false;
    }

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
            } // end list loop
        }
        // pass to next in chain, or return
        if (getNext() != null) {
            objects = getNext().findDetailsForList(objects, clientcomplete,
                    batchsearchmax);
        }

        return objects;

    }

    private List<BookIdentifier> findAlternateIdentifiers(
            FinderObject findobj, String authornameoverride) throws Exception {
        HashMap<String, String> ean2link = new HashMap<String, String>();
        BookDetailDao bookdetail = findobj.getBookdetail();

        // add params - title and author
        String querystring = "";
        querystring = dataquery_titleauthor;
        // replace string in query with values
        String title = bookdetail.getTitle();
        List<ArtistDao> authors = bookdetail.getAuthors();
        String author = "";
        if (authornameoverride != null) {
            author = authornameoverride;
        } else {
            if (authors != null && authors.size() > 0) {
                author = authors.get(0).getDisplayName();
            }
            if (author.length() == 0) {
                // need author to get additional codes
                return null;
            }
        }
        querystring = querystring.replace("TITLE", title);
        querystring = querystring.replace("AUTHOR", author);
        querystring = URLEncoder.encode(querystring, "UTF-8");

        String requestUrl = databnfrequest.replace("REPLACE", querystring);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(requestUrl);

        // get catalog uri from response
        NodeList eanresults = doc.getElementsByTagName("result");
        String requestedean = bookdetail.getIsbn13() != null ? bookdetail
                .getIsbn13().trim() : "";

        List<BookIdentifier> addlcodes = new ArrayList<BookIdentifier>();
        if (eanresults != null) {

            for (int i = 0; i < eanresults.getLength(); i++) {

                Node node = eanresults.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element resultel = (Element) node;
                    NodeList children = resultel
                            .getElementsByTagName("binding");
                    String link = null;
                    String ark = null;
                    String ean = null;
                    String isbn = null;
                    String date = null;
                    for (int j = 0; j < children.getLength(); j++) {
                        Node bindingnode = children.item(j);
                        // BookIdentifier bi = new BookIdentifier();
                        if (bindingnode.getNodeType() == Node.ELEMENT_NODE) {
                            Element bindingel = (Element) bindingnode;
                            String type = bindingel.getAttribute("name");
                            if (type != null) {
                                if (type.equals("link")) {
                                    Node linknode = bindingel
                                            .getElementsByTagName("uri")
                                            .item(0);
                                    if (linknode != null) {
                                        link = linknode.getTextContent();
                                        ark = parseArkFromUrl(link);
                                    }
                                } else if (type.equals("ean")) {
                                    Node eannode = bindingel
                                            .getElementsByTagName("literal")
                                            .item(0);
                                    if (eannode != null) {
                                        ean = eannode.getTextContent();
                                    }
                                } else if (type.equals("isbn")) {
                                    Node isbnnode = bindingel
                                            .getElementsByTagName("literal")
                                            .item(0);
                                    if (isbnnode != null) {
                                        isbn = isbnnode.getTextContent();
                                    }
                                } else if (type.equals("date")) {
                                    Node datenode = bindingel
                                            .getElementsByTagName("literal")
                                            .item(0);
                                    if (datenode != null) {
                                        date = datenode.getTextContent();
                                    }
                                }
                            }
                        }
                    } // end children loop
                    // process results
                    String cleandate = parseOutDate(date);

                    // fill ean2link hash
                    if (!requestedean.equals(ean.trim())) {
                        // put values in hash
                        ean2link.put(ean.trim(), link.trim());
                        // create book identifiers
                        BookIdentifier bi = new BookIdentifier();
                        bi.setEan(ean);
                        bi.setLink(link.trim());
                        bi.setIsbn(isbn);
                        if (cleandate.length() == 4) {
                            bi.setPublishyear(new Long(cleandate));
                        }
                        bi.setArk(ark);
                        addlcodes.add(bi);
                    }
                }// end result loop
            }

        }
        return addlcodes;
    }

    protected FinderObject searchLogic(FinderObject findobj) throws Exception {
        BookDetailDao bookdetail = findobj.getBookdetail();
        boolean addlcodessearch = false;
        boolean isbnsearch = false;
        // lookup by isbn
        // add params by search type (isbn, or other (title, author, publisher)
        String querystring = "";
        String catalogurl = null;
        if (bookdetail.hasIsbn()) {
            String value = "";
            // doing an isbn search
            if (bookdetail.getIsbn13() != null) {
                querystring = dataquery_isbn13;
                value = bookdetail.getIsbn13().trim();
            } else {
                // MM will need to add hypens to isbn here....
                value = bookdetail.getIsbn10().trim();
                querystring = dataquery_isbn10;
            }
            // replace string in query with value
            querystring = querystring.replace("REPLACE", value);
            querystring = URLEncoder.encode(querystring, "UTF-8");
            isbnsearch = true;
        }

        if (isbnsearch) {
            String requestUrl = databnfrequest.replace("REPLACE", querystring);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(requestUrl);

            // get catalog uri from response
            Node node = doc.getElementsByTagName("uri").item(0);
            catalogurl = node != null ? node.getTextContent() : null;
        }
        // if no catalogurl, do author / title search - to put either in
        // additionalcodes, or founddetails
        if (catalogurl == null) {
            addlcodessearch = true;

            List<BookIdentifier> addlcodes = findAlternateIdentifiers(findobj,
                    null);

            if (addlcodes != null && addlcodes.size() > 0) {
                // if !isbnsearch - this is our first search - and should be
                // processed accordingly
                if (!isbnsearch) {
                    if (addlcodes.size() == 1) {
                        // one result - becomes catalogurl
                        catalogurl = addlcodes.get(0).getLink();
                    } else {
                        // more than one result - becomes founddetails
                        List<FoundDetailsDao> details = processLinksIntoFoundDetails(addlcodes);
                        findobj.addToMultiresults(details);
                        findobj.setSearchStatus(CatalogService.DetailStatus.MULTIDETAILSFOUND);
                    }

                } else {
                    // isbnsearch -
                    // one or more results - first becomes catalogurl
                    BookIdentifier bi = addlcodes.get(0);
                    catalogurl = bi.getLink();
                    // any possible remaining results added to findobj
                    if (addlcodes.size() > 1) {
                        addlcodes = addlcodes.subList(1, addlcodes.size());
                        if (addlcodes != null && addlcodes.size() > 0) {
                            findobj.addAddlIdentifiers(addlcodes);
                        }
                    }
                }
            }
        }

        // now, lets get this record....
        if (catalogurl != null) {
            catalogurl = catalogurl + ".intermarc";
            HashMap<String, String> results = new HashMap<String, String>();
            String alternateauthor = null;

            // now, lets get this record....
            CloseableHttpClient httpclient = HttpClients.createDefault();
            try {
                HttpGet httpget = new HttpGet(catalogurl);

                // Create a response handler
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String responseBody = httpclient.execute(httpget,
                        responseHandler);
                processResponse(responseBody, results);

                if (results.size() > 0) {
                    // add ark to bookdetail
                    String ark = parseArkFromUrl(catalogurl);
                    findobj.getBookdetail().setArk(ark);
                    findobj.setSearchStatus(CatalogService.DetailStatus.DETAILFOUND);
                } else {
                    Long searchstatus = isbnsearch ? CatalogService.DetailStatus.DETAILNOTFOUNDWISBN
                            : CatalogService.DetailStatus.DETAILNOTFOUND;
                    findobj.setSearchStatus(searchstatus);
                }
                // nab this authors name here, just in case
                alternateauthor = stripAfterText("(", results.get("Auteur(s)"));
                alternateauthor = stripAfterText(newlinemarker, alternateauthor);
                alternateauthor = bMemberService
                        .normalizeArtistName(alternateauthor);

                // put results into bookdetail
                resultsIntoDetail(results, findobj);
            } finally {
                // When HttpClient instance is no longer needed,
                // shut down the connection manager to ensure
                // immediate deallocation of all system resources
                httpclient.getConnectionManager().shutdown();
            }

            // get additional codes for title and author
            if (!addlcodessearch && alternateauthor != null) {
                List<BookIdentifier> addlcodes = findAlternateIdentifiers(
                        findobj, alternateauthor);
                findobj.addAddlIdentifiers(addlcodes);
            }
        } else {
            if (findobj.getSearchStatus() != CatalogService.DetailStatus.MULTIDETAILSFOUND) {
                // nothing found
                if (findobj.getBookdetail().hasIsbn()) {
                    findobj.setSearchStatus(CatalogService.DetailStatus.DETAILNOTFOUNDWISBN);
                } else {
                    findobj.setSearchStatus(CatalogService.DetailStatus.DETAILNOTFOUND);
                }

            }
        }

        // return findobj
        return findobj;
    }

    protected FinderObject assignDetail(FinderObject findobj, FoundDetailsDao fd)
            throws Exception {
        // initializing
        BookDetailDao bookdetail = findobj.getBookdetail();

        // get searchid from found details
        String searchid = fd.getSearchserviceid();

        // do search for identifier
        // now, lets get this record....
        if (searchid != null) {
            HashMap<String, String> results = new HashMap<String, String>();

            // now, lets get this record....
            CloseableHttpClient httpclient = HttpClients.createDefault();
            try {
                HttpGet httpget = new HttpGet(searchid);

                // Create a response handler
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String responseBody = httpclient.execute(httpget,
                        responseHandler);
                processResponse(responseBody, results);

                if (results.size() > 0) {
                    // add ark to bookdetail
                    String ark = parseArkFromUrl(searchid);
                    findobj.getBookdetail().setArk(ark);
                    findobj.setSearchStatus(CatalogService.DetailStatus.DETAILFOUND);

                    // put results into bookdetail
                    resultsIntoDetail(results, findobj);

                    // if results found, update searchstatus
                    findobj.setSearchStatus(CatalogService.DetailStatus.DETAILFOUND);
                    // copy results into book

                    // set bookdetail in findobj
                    findobj.setBookdetail(bookdetail);

                }
            } finally {
                // When HttpClient instance is no longer needed,
                // shut down the connection manager to ensure
                // immediate deallocation of all system resources
                httpclient.getConnectionManager().shutdown();
            }

        }
        // return finderobject
        return findobj;
    }

    private List<FoundDetailsDao> processLinksIntoFoundDetails(
            List<BookIdentifier> addlcodes) throws ClientProtocolException,
            IOException {
        List<FoundDetailsDao> fdetails = new ArrayList<FoundDetailsDao>();
        if (addlcodes != null && addlcodes.size() > 0) {

            for (BookIdentifier bi : addlcodes) {
                FoundDetailsDao fd = new FoundDetailsDao();
                String catalogurl = bi.getLink();
                // String ark = parseArkFromUrl(catalogurl);
                fd.setSearchserviceid(catalogurl);
                fd.setSearchsource(identifier);

                if (catalogurl != null) {
                    HashMap<String, String> results = new HashMap<String, String>();

                    // now, lets get this record....
                    CloseableHttpClient httpclient = HttpClients
                            .createDefault();
                    try {
                        HttpGet httpget = new HttpGet(catalogurl);

                        // Create a response handler
                        ResponseHandler<String> responseHandler = new BasicResponseHandler();
                        String responseBody = httpclient.execute(httpget,
                                responseHandler);
                        processResponse(responseBody, results);

                        // put results into bookdetail
                        resultsIntoFoundDetail(results, fd);

                        // add isbn/ean to founddetails
                        fd.setIsbn10(bi.getIsbn());
                        fd.setIsbn13(bi.getEan());

                        fdetails.add(fd);
                    } finally {
                        // When HttpClient instance is no longer needed,
                        // shut down the connection manager to ensure
                        // immediate deallocation of all system resources
                        httpclient.getConnectionManager().shutdown();
                    }

                }

            }
        }
        return fdetails;
    }

    private void resultsIntoFoundDetail(HashMap<String, String> results,
                                        FoundDetailsDao fd) {

        List<String> addlauthors = new ArrayList<String>();
        List<String> addlillustrators = new ArrayList<String>();
        if (results != null) {
            for (String key : results.keySet()) {
                if (key.toLowerCase().equals("autre(s) auteur(s)")) {
                    if (results.get(key) != null) {
                        // split on linebreak
                        String[] addl = results.get(key).split(
                                newlinemarkersplit);
                        // for each line - divide on last period author, role
                        for (int i = 0; i < addl.length; i++) {
                            String toparse = addl[i];
                            int lastperiod = toparse.lastIndexOf(".");
                            if (lastperiod >= 0) {
                                String rawartist = toparse.substring(0,
                                        lastperiod);
                                String rawrole = toparse
                                        .substring(lastperiod + 1);

                                // processing author - strip after (
                                rawartist = stripAfterText("(", rawartist);
                                // normalize author name
                                String artist = bMemberService
                                        .normalizeArtistName(rawartist);
                                // add to book detail depending upon role

                                if (rawrole.toLowerCase().contains("auteur")) {
                                    addlauthors.add(artist);
                                } else if (rawrole.toLowerCase().contains(
                                        "illustrateur")) {
                                    // add to illustrators
                                    addlillustrators.add(artist);
                                }
                            }
                        }
                    }

                } else if (key.toLowerCase().equals("titre(s)")) {
                    String rawtitle = stripAfterText("[", results.get(key));
                    rawtitle = stripAfterText("/", rawtitle);
                    fd.setTitle(rawtitle);
                } else if (key.toLowerCase().equals("auteur(s)")) {
                    String value = stripAfterText("(", results.get(key));
                    // normalize author name
                    value = bMemberService.normalizeArtistName(value);
                    // set in author....
                    addlauthors.add(0, value);
                } else if (key.toLowerCase().equals("résumé")) {
                    String value = stripAfterText(newlinemarker,
                            results.get(key));
                    value = stripAfterText("[", value);
                    fd.setDescription(value);
                }
            }

            // now, add addlauthors and illustrators
            if (addlauthors != null) {
                if (addlillustrators != null) {
                    addlauthors.addAll(addlillustrators);
                }
                if (addlauthors.size() > 0) {
                    StringBuffer authorstr = new StringBuffer();
                    for (String artist : addlauthors) {
                        authorstr.append(artist).append(",");
                    }
                    authorstr.setLength(authorstr.length() - 1);
                    fd.setAuthors(authorstr.toString());
                }
            }
        }
    }

    private String parseOutDate(String rawdate) {
        // strip before comma
        String cleaning = "";
        int commaloc = rawdate.indexOf(",");
        if (commaloc >= 0) {
            cleaning = rawdate.substring(commaloc + 1);
        } else {
            cleaning = rawdate;
        }
        // strip all non number characters
        cleaning = cleaning.replaceAll("[^\\d.X]", "");
        // trim and return
        return cleaning.trim();
    }

    private void resultsIntoDetail(HashMap<String, String> results,
                                   FinderObject findobj) {
        BookDetailDao bdetail = findobj.getBookdetail();
        List<String> addlauthors = new ArrayList<String>();
        List<String> addlillustrators = new ArrayList<String>();
        if (results != null) {
            for (String key : results.keySet()) {
                if (key.toLowerCase().equals("indice(s) dewey")) {
                    String value = stripAfterText(" ", results.get(key));
                    bdetail.setShelfclass(value);
                } else if (key.toLowerCase().equals("autre(s) auteur(s)")) {
                    if (results.get(key) != null) {
                        // split on linebreak
                        String[] addl = results.get(key).split(
                                newlinemarkersplit);
                        // for each line - divide on last period author, role
                        for (int i = 0; i < addl.length; i++) {
                            String toparse = addl[i];
                            int lastperiod = toparse.lastIndexOf(".");
                            if (lastperiod >= 0) {
                                String rawartist = toparse.substring(0,
                                        lastperiod);
                                String rawrole = toparse
                                        .substring(lastperiod + 1);

                                // processing author - strip after (
                                rawartist = stripAfterText("(", rawartist);
                                // normalize author name
                                String artist = bMemberService
                                        .normalizeArtistName(rawartist);
                                // add to book detail depending upon role

                                if (rawrole.toLowerCase().contains("auteur")) {
                                    addlauthors.add(artist);
                                } else if (rawrole.toLowerCase().contains(
                                        "illustrateur")) {
                                    // add to illustrators
                                    addlillustrators.add(artist);
                                }
                            }
                        }
                    }
                } else if (key.toLowerCase().equals("auteur(s)")) {
                    String value = stripAfterText("(", results.get(key));
                    value = stripAfterText(newlinemarker, value);
                    // normalize author name
                    value = bMemberService.normalizeArtistName(value);
                    // set in author....
                    bdetail = bMemberService.addArtistToAuthors(value, bdetail);
                } else if (key.toLowerCase().equals("sujet(s)")) {
                    List<String> subjects = new ArrayList<String>();
                    String value = stripAfterText(newlinemarker,
                            results.get(key));
                    String[] rawsubjects = value.split("--");
                    for (int i = 0; i < rawsubjects.length; i++) {
                        subjects.add(rawsubjects[i].trim());
                    }
                    bdetail = bMemberService.insertSubjectsIntoBookDetail(
                            subjects, bdetail);
                } else if (key.toLowerCase().equals("résumé")) {
                    String value = stripAfterText(newlinemarker,
                            results.get(key));
                    value = stripAfterText("[", value);
                    bdetail.setDescription(value);
                }
            }

            // now, add addlauthors and illustrators
            if (addlauthors != null) {
                for (String artist : addlauthors) {
                    bdetail = bMemberService
                            .addArtistToAuthors(artist, bdetail);
                }
            }
            if (addlillustrators != null) {
                for (String artist : addlillustrators) {
                    bdetail = bMemberService
                            .addArtistToIllustrators(artist, bdetail);
                }
            }

        }
    }

    private String stripAfterText(String stripafter, String tostrip) {
        if (tostrip != null) {
            int location = tostrip.indexOf(stripafter);
            if (location > 0) {
                tostrip = tostrip.substring(0, location - 1);
            }
        }
        return tostrip;
    }

    private void processResponse(String responseBody,
                                 HashMap<String, String> results) {

        if (responseBody != null) {
            // Strip away chaff
            int begin = responseBody.indexOf(beginparse);
            int end = responseBody.indexOf("</td>", begin);
            String interesting = responseBody.substring(
                    begin + beginparse.length(), end);
            // replace br with linebreak markers
            interesting = interesting.replace("<br />", newlinemarker);
            // strip &#160;
            interesting = interesting.replace("&#160;", "");
            String[] chunks = interesting.split("<b>");
            if (chunks != null) {
                for (int i = 0; i < chunks.length; i++) {
                    String raw = chunks[i];
                    if (raw != null && raw.trim().length() > 0) {

                        // remove tags
                        String tagfree = removeTags(chunks[i]);
                        // unescape html characters
                        tagfree = StringEscapeUtils.unescapeHtml4(tagfree);
                        if (tagfree.contains(":")) {
                            String[] keyvalue = tagfree.split(":");
                            String key = keyvalue[0].trim();
                            String value = keyvalue[1].trim();
                            results.put(key, value);
                        }
                    }
                }
            }
        }

    }

    private String parseArkFromUrl(String link) {
        if (link != null) {
            int lastslash = link.lastIndexOf("/");
            if (lastslash >= 0) {
                return link.substring(lastslash + 1);
            }
        }
        return null;
    }


}
