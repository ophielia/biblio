package meg.biblio.catalog;

import meg.biblio.catalog.db.PublisherRepository;
import meg.biblio.catalog.db.SubjectRepository;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.common.AppSettingService;
import meg.biblio.search.SearchService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
                } catch (InterruptedException ex) {
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
