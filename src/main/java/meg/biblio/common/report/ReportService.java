package meg.biblio.common.report;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.apache.fop.apps.FOPException;


public interface ReportService {

	byte[] produceTableReport(TableReport report) throws FOPException, TransformerException, JAXBException, IOException;


}
