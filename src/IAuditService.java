import java.util.Date;

public interface IAuditService {
    void OpenAuditFile(String filePath);
    void CloseAuditFile();
    void WriteAudit(String action, Date date);
}
