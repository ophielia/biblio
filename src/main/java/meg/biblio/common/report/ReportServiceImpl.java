package meg.biblio.common.report;

import meg.biblio.common.AppSettingService;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {

    @Autowired
    AppSettingService appSetting;

    private FopFactory fopFactory = FopFactory.newInstance();
    private TransformerFactory tFactory = TransformerFactory.newInstance();

    @Override
    public byte[] produceTableReport(TableReport report) throws FOPException, TransformerException, JAXBException, IOException {
        String cxslbase = "META-INF/web-resources/transform/";
        String xsl = appSetting.getSettingAsString("biblio.report.tablereportxsl");
        xsl = cxslbase + xsl;


        if (report != null && report.getTableValue() != null && report.getTableValue().size() > 0) {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {

                Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
                JAXBContext context = JAXBContext
                        .newInstance(TableReport.class);
                JAXBSource source = new JAXBSource(context, report);

                // Setup Transformer
                Resource resource = new ClassPathResource(xsl);
                Source xsltSrc = new StreamSource(resource.getFile());
                Transformer transformer = tFactory.newTransformer(xsltSrc);

                // Make sure the XSL transformation's result is piped through to
                // FOPx
                Result res = new SAXResult(fop.getDefaultHandler());

                // Start the transformation and rendering process
                transformer.transform(source, res);

                return out.toByteArray();

            } finally {
                out.close();
            }
        }
        return null;
    }


}
