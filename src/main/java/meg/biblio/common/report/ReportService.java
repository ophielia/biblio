package meg.biblio.common.report;

import org.apache.fop.apps.FOPException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import java.io.IOException;


public interface ReportService {

    byte[] produceTableReport(TableReport report) throws FOPException, TransformerException, JAXBException, IOException;


}
