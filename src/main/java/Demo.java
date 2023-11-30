import com.amazon.textract.service.S3UploadDocumento;
import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import static org.apache.logging.log4j.LogManager.*;


public class Demo {
    private static final Logger log = getLogger(Demo.class.getName());

    public static void main(String[] args) {
        Application.launch(AppRunner.class, args);
    }

    public static class AppRunner extends Application {
        @Override
        public void start(Stage primaryStage) {
            try {
                String sourceLanguage = "en";
                String destinationLanguage = "pt";

                log.info("Começando com a tradução");

                S3UploadDocumento uploader = new S3UploadDocumento(primaryStage);
                String caminho = uploader.escolherCaminho(primaryStage);

                DemoPdfFromLocalPdf localPdf = new DemoPdfFromLocalPdf();
                String caminhoPdfTraduzido = localPdf.run(caminho, "./documents/testeCompleto01" + destinationLanguage + ".pdf", sourceLanguage, destinationLanguage, true);
                //String generatedFilePathMin =  localPdf.run(caminho, "./documents/teste01" + destinationLanguage + ".pdf", sourceLanguage, destinationLanguage, false);

                uploader.uploadFile(caminhoPdfTraduzido);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
