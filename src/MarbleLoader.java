import Exceptions.FileLoadException;
import Exceptions.ResourceLoadException;
import org.jsfml.graphics.Texture;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;
import java.sql.ResultSet;

public class MarbleLoader {
    public MarbleLoader() {
        for (MarbleRarity rarity : MarbleRarity.values()) {
            m_marbles.put(rarity, new Vector<>());
        }
    }

    public static class MarbleData {
        public MarbleData(String name, Texture texture) {
            this.name = name;
            this.texture = texture; // shallow copy
        }
        public String name;
        public Texture texture;
    }

    public int GetTotalDistinctMarbles() {
        int total = 0;
        for (MarbleRarity rarity : m_marbles.keySet()) {
            total += m_marbles.get(rarity).size();
        }
        total += total * (total - 1) / 2;
        return total;
    }

    /*
    public void LoadMarbleData(String filePath, Vector<Texture> textures) throws FileLoadException {
        // Open file
        File file = new File(filePath);
        Scanner fin;
        try {
            fin = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new FileLoadException(filePath);
        }
        while(fin.hasNextLine()) {
            String line = fin.nextLine();
            String[] tokens = line.split(" ");
            int rarity = Integer.parseInt(tokens[0]);
            String name = tokens[1];
            int texID = Integer.parseInt(tokens[2]);

            // Ensure the texture exists in memory
            if (texID > textures.size() - 1)
                throw new RuntimeException("Marble " + name + " does not have the texture loaded into memory!");

            switch (rarity) {
                case 1: m_marbles.get(MarbleRarity.Normal).add(new MarbleData(name, textures.get(texID))); break;
                case 2: m_marbles.get(MarbleRarity.Rare).add(new MarbleData(name, textures.get(texID))); break;
                case 3: m_marbles.get(MarbleRarity.SuperRare).add(new MarbleData(name, textures.get(texID))); break;
                case 4: m_marbles.get(MarbleRarity.UltraRare).add(new MarbleData(name, textures.get(texID))); break;
                case 5: m_marbles.get(MarbleRarity.Legendary).add(new MarbleData(name, textures.get(texID))); break;
                default: throw new RuntimeException("Marble " + name + " has invalid rarity!");
            }
        }
        fin.close();
    }
    */
    public void LoadMarbleData(Vector<Texture> textures) throws RuntimeException, SQLException {
        ResultSet rs = DbManager.getInstance().GetMarbleData();

        while(rs.next()) {
            int rarity = rs.getInt("rarity");
            String name = rs.getString("name");
            int texID = rs.getInt("texture_slot");

            // Ensure the texture exists in memory
            if (texID > textures.size() - 1)
                throw new RuntimeException("Marble " + name + " does not have the texture loaded into memory!");

            switch (rarity) {
                case 1: m_marbles.get(MarbleRarity.Normal).add(new MarbleData(name, textures.get(texID))); break;
                case 2: m_marbles.get(MarbleRarity.Rare).add(new MarbleData(name, textures.get(texID))); break;
                case 3: m_marbles.get(MarbleRarity.SuperRare).add(new MarbleData(name, textures.get(texID))); break;
                case 4: m_marbles.get(MarbleRarity.UltraRare).add(new MarbleData(name, textures.get(texID))); break;
                case 5: m_marbles.get(MarbleRarity.Legendary).add(new MarbleData(name, textures.get(texID))); break;
                default: throw new RuntimeException("Marble " + name + " has invalid rarity!");
            }
        }
    }

    public MarbleData GetRandomMarbleData(MarbleRarity rarity) {
        Generator gen = new Generator();
        gen.SetRange(0, m_marbles.get(rarity).size());
        return m_marbles.get(rarity).get(gen.Generate());
    }

    private HashMap<MarbleRarity, Vector<MarbleData>> m_marbles = new HashMap<>();
}
