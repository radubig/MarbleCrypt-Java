import Exceptions.FileLoadException;
import Exceptions.TextureLoadException;
import org.jsfml.graphics.Texture;

import java.io.*;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Inventory {
    public Inventory() {
        m_textures = new Vector<>();
        m_wallet = new CryptoCoin();
        m_marbles = new Vector<>();
        m_generator = new Generator();
        m_marble_loader = new MarbleLoader();
        m_generator.SetRange(1, 101);
    }

    public void LoadTextures(String filePath) throws FileLoadException, TextureLoadException {
        File file = new File(filePath);
        Scanner fin;
        try {
            fin = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new Exceptions.FileLoadException(filePath);
        }
        while (fin.hasNextLine()) {
            String line = fin.nextLine();
            Texture texture = new Texture();
            try {
                texture.loadFromFile(Paths.get(line));
            } catch (IOException e) {
                throw new Exceptions.TextureLoadException(line);
            }
            m_textures.add(texture);
        }
        fin.close();
    }

    public void LoadMarbleData() throws RuntimeException, SQLException {
        m_marble_loader.LoadMarbleData(m_textures);
    }

    public void SetDefault() {
        m_wallet = new CryptoCoin(CryptoCoin.s_initial_ammount);
        m_marbles.clear();
        m_generator.ResetPrice();
        DbManager.getInstance().SetAmount(m_wallet.toString());
        DbManager.getInstance().SetPrice(m_generator.toString());
        DbManager.getInstance().DeleteAllMarbles();
        System.out.println("Inventory reset.");
    }

    public double GetBalance() {
        return m_wallet.Balance();
    }

    public double GetNewMarblePrice() {
        return m_generator.GetPrice();
    }

    public int GetMarblesSize() {
        return m_marbles.size();
    }

    public boolean BuyMarble() {
        if (!m_wallet.Pay(m_generator.GetPrice()))
            return false;
        int chance = m_generator.Generate();
        DbManager.getInstance().SetPrice(m_generator.toString());

        MarbleRarity rarity;
        if (chance <= 54)
            rarity = MarbleRarity.Normal;
        else if (chance <= 84)
            rarity = MarbleRarity.Rare;
        else if (chance <= 96)
            rarity = MarbleRarity.SuperRare;
        else if (chance <= 99)
            rarity = MarbleRarity.UltraRare;
        else
            rarity = MarbleRarity.Legendary;

        Marble marble = new Marble(m_marble_loader.GetRandomMarbleData(rarity), rarity);
        marble.AddToDb(FindTextureSolt(marble.GetTexture1()), FindTextureSolt(marble.GetTexture2()));
        m_marbles.add(marble);
        return true;
    }

    public void CollectAll() {
        double totalSum = 0;
        for (Marble marble : m_marbles)
        {
            double ammount = marble.GetYield();
            totalSum += ammount;
            marble.CollectYield();
        }
        m_wallet.Add(totalSum);
        System.out.println("Collected " + totalSum + " $MTK.");
    }

    public Marble GetMarble(int index)
    {
        return m_marbles.get(index);
    }

    public void AddCoins(double ammount) {
        m_wallet.Add(ammount);
    }

    public void FuseMarbles(int index_first, int index_second) {
        if (index_first >= m_marbles.size() || index_second >= m_marbles.size())
            throw new RuntimeException("Indicies provided for fusing are out of bounds!");

        if (index_first > index_second) {
            int aux = index_first;
            index_first = index_second;
            index_second = aux;
        }

        // Create new marble based on previous 2 marbles
        Marble marble;
        if (m_marbles.get(index_first).GetRarity() == MarbleRarity.Legendary &&
            m_marbles.get(index_second).GetRarity() == MarbleRarity.Legendary)
        {
            marble = new Marble(
                m_marbles.get(index_first).GetName() + m_marbles.get(index_second).GetName(),
                Marble.CalculateDailyYield(MarbleRarity.Mythic),
                m_marbles.get(index_first).GetTexture1(),
                m_marbles.get(index_second).GetTexture1(),
                MarbleRarity.Mythic
            );
        }
        else
        {
            MarbleRarity rarity = m_marbles.get(index_first).GetRarity();
            if (m_marbles.get(index_second).GetRarity().compareTo(rarity) > 0)
                rarity = m_marbles.get(index_second).GetRarity();

            marble = new Marble(
                m_marbles.get(index_first).GetName() + m_marbles.get(index_second).GetName(),
                Marble.CalculateDailyYield(rarity) * 5,
                m_marbles.get(index_first).GetTexture1(),
                m_marbles.get(index_second).GetTexture1(),
                rarity
            );
        }
        marble.AddToDb(FindTextureSolt(marble.GetTexture1()), FindTextureSolt(marble.GetTexture2()));
        m_marbles.add(marble);

        m_marbles.get(index_first).DeleteFromDb();
        m_marbles.get(index_second).DeleteFromDb();
        m_marbles.remove(index_second);
        m_marbles.remove(index_first);
    }

    public boolean IsFusable(int index_first, int index_second) {
        // Returns false if the indicies provided are out of bounds or if the selected marbles are not basic marbles
        // Also returns false if the two basic marbles have the same texture
        // Otherwise returns true

        if (index_first >= m_marbles.size() || index_second >= m_marbles.size())
            return false;
        if (m_marbles.get(index_first).GetTexture2() != null ||
            m_marbles.get(index_second).GetTexture2() != null)
            return false;
        if (m_marbles.get(index_first).GetTexture1() == m_marbles.get(index_second).GetTexture1())
            return false;

        return true;
    }

    public void BurnMarble(int index) {
        double value = GetBurnValue(index);
        m_marbles.get(index).DeleteFromDb();
        m_marbles.remove(index);
        m_wallet.Add(value);
    }

    public double GetBurnValue(int index) {
        double value = m_generator.GetPrice() / 4;
        switch (m_marbles.get(index).GetRarity())
        {
            case Rare -> value *= 2;
            case SuperRare -> value *= 4;
            case UltraRare -> value *= 6;
            case Legendary -> value *= 10;
            case Mythic -> value *= 20;
        }
        return value;
    }

    public int FindTextureSolt(Texture texture) {
        if (texture == null)
            return -1;

        int index = m_textures.indexOf(texture);
        if (index == -1)
            throw new RuntimeException("Invalid texture reference detected!");

        return index;
    }

    public static double ConvertHexToReal(String hexvalue) {
        long bits = Long.parseLong(hexvalue, 16);
        return Double.longBitsToDouble(bits);
    }

    int GetTotalDistinctMarbles() {
        return m_marble_loader.GetTotalDistinctMarbles();
    }

    int GetCurrentDistinctMarbles() {
        HashSet<String> s = new HashSet<>();
        for (Marble marble : m_marbles) {
            int tex1 = FindTextureSolt(marble.GetTexture1());
            int tex2 = FindTextureSolt(marble.GetTexture2());
            if (tex1 == -1)
                continue;
            if (tex2 == -1)
                s.add(Integer.toString(tex1));
            else {
                if (tex1 > tex2) {
                    int aux = tex1;
                    tex1 = tex2;
                    tex2 = aux;
                }
                s.add(tex1 + "_" + tex2);
            }
        }

        return s.size();
    }

    public void LoadInventory() {
        try {
            m_wallet = new CryptoCoin(ConvertHexToReal(DbManager.getInstance().GetAmount()));
            m_generator.ResetPrice(ConvertHexToReal(DbManager.getInstance().GetPrice()));

            m_marbles.clear();
            ResultSet rs = DbManager.getInstance().GetMarbles();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                long daily_yield = rs.getLong("daily_yield");
                long timestamp = rs.getLong("timestamp");
                MarbleRarity rarity = MarbleRarity.valueOf(rs.getString("rarity"));
                int t1 = rs.getInt("texture1");
                int t2 = rs.getInt("texture2");
                Texture texture1 = t1 == -1 ? null : m_textures.get(t1);
                Texture texture2 = t2 == -1 ? null : m_textures.get(t2);

                m_marbles.add(new Marble(id, name, daily_yield, texture1, texture2, rarity, new Date(timestamp)));
            }
            System.out.println("Inventory loaded.");

        } catch (SQLException e) {
            System.err.println("[SQL Exception]: " + Arrays.toString(e.getStackTrace()));
        }
    }

    // Cheats:
    public void GenerateEachRarity() {
        Marble marble;

        marble = new Marble(m_marble_loader.GetRandomMarbleData(MarbleRarity.Normal), MarbleRarity.Normal);
        marble.AddToDb(FindTextureSolt(marble.GetTexture1()), FindTextureSolt(marble.GetTexture2()));
        m_marbles.add(marble);

        marble = new Marble(m_marble_loader.GetRandomMarbleData(MarbleRarity.Rare), MarbleRarity.Rare);
        marble.AddToDb(FindTextureSolt(marble.GetTexture1()), FindTextureSolt(marble.GetTexture2()));
        m_marbles.add(marble);

        marble = new Marble(m_marble_loader.GetRandomMarbleData(MarbleRarity.SuperRare), MarbleRarity.SuperRare);
        marble.AddToDb(FindTextureSolt(marble.GetTexture1()), FindTextureSolt(marble.GetTexture2()));
        m_marbles.add(marble);

        marble = new Marble(m_marble_loader.GetRandomMarbleData(MarbleRarity.UltraRare), MarbleRarity.UltraRare);
        marble.AddToDb(FindTextureSolt(marble.GetTexture1()), FindTextureSolt(marble.GetTexture2()));
        m_marbles.add(marble);

        marble = new Marble(m_marble_loader.GetRandomMarbleData(MarbleRarity.Legendary), MarbleRarity.Legendary);
        marble.AddToDb(FindTextureSolt(marble.GetTexture1()), FindTextureSolt(marble.GetTexture2()));
        m_marbles.add(marble);

        // No mythtic marble
    }


    private static final String s_savefile = "save.dat";
    private Vector<Texture> m_textures;
    private CryptoCoin m_wallet;
    private Vector<Marble> m_marbles;
    private Generator m_generator;
    private MarbleLoader m_marble_loader;
}
