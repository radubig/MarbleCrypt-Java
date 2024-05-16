import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class AuditService implements IAuditService {
    private File auditFile;
    private FileWriter fileWriter;

    public boolean IsAuditFileOpen() {
        return auditFile != null;
    }

    @Override
    public void OpenAuditFile(String filePath) {
        try {
            auditFile = new File(filePath);
            auditFile.createNewFile();
            fileWriter = new FileWriter(auditFile, true);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void CloseAuditFile() {
        try {
            fileWriter.close();
            auditFile = null;
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void WriteAudit(String action, Date date) {
        try {
            fileWriter.write(action + "," + date.toString() + "\n");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
