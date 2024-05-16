import Exceptions.FileLoadException;
import Exceptions.FontLoadException;
import Exceptions.TextureLoadException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        DbManager dbManager = DbManager.getInstance();
        try {
            GameApp app = GameApp.getInstance();
            app.SetResolution(1340, 820);
            app.SetFramerateLimit(144);
            app.InitWindow();

            if (args.length == 1 && Objects.equals(args[0], "-cheats"))
                app.EnableCheats();

            app.Run();
        } catch (FileLoadException e) {
            System.err.println("[Resourse Load Exception]: " + e.getMessage());
        } catch (FontLoadException e) {
            System.err.println("[Font Load Exception]: " + e.getMessage());
        } catch (TextureLoadException e) {
            System.err.println("[Texture Load Exception]: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[SQL Exception]: " + Arrays.toString(e.getStackTrace()));
        } finally {
            dbManager.close();
        }
    }
}