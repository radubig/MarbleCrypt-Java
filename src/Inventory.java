import Exceptions.FileLoadException;
import Exceptions.TextureLoadException;
import org.jsfml.graphics.Texture;

import java.io.*;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Vector;

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

    public void LoadMarbleData(String filePath) throws FileLoadException {
        m_marble_loader.LoadMarbleData(filePath, m_textures);
    }

    public void SetDefault() {
        m_wallet = new CryptoCoin(CryptoCoin.s_initial_ammount);
        m_marbles.clear();
        m_generator.ResetPrice();
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

        m_marbles.add(new Marble(m_marble_loader.GetRandomMarbleData(rarity), rarity));
        return true;
    }

    public void CollectAll() {
        double totalSum = 0;
        for (Marble marble : m_marbles)
        {
            double ammount = marble.GetYield();
            totalSum += ammount;
            m_wallet.Add(ammount);
            marble.CollectYield();
        }
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
        if (m_marbles.get(index_first).GetRarity() == MarbleRarity.Legendary &&
            m_marbles.get(index_second).GetRarity() == MarbleRarity.Legendary)
        {
            m_marbles.add(new Marble(
                m_marbles.get(index_first).GetName() + m_marbles.get(index_second).GetName(),
                Marble.CalculateDailyYield(MarbleRarity.Mythic),
                m_marbles.get(index_first).GetTexture1(),
                m_marbles.get(index_second).GetTexture1(),
                MarbleRarity.Mythic
            ));
        }
        else
        {
            MarbleRarity rarity = m_marbles.get(index_first).GetRarity();
            if (m_marbles.get(index_second).GetRarity().compareTo(rarity) > 0)
                rarity = m_marbles.get(index_second).GetRarity();

            m_marbles.add(new Marble(
                m_marbles.get(index_first).GetName() + m_marbles.get(index_second).GetName(),
                Marble.CalculateDailyYield(rarity) * 5,
                m_marbles.get(index_first).GetTexture1(),
                m_marbles.get(index_second).GetTexture1(),
                rarity
            ));
        }

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


    // Loading and saving progress
    public void SaveInventory() {
        try {
            File file = new File(s_savefile);
            file.createNewFile();
            FileWriter fout = new FileWriter(file);

            // Save current wallet information
            fout.write(m_wallet.toString() + "\n");
            fout.write(m_generator.toString() + "\n");

            // Save Inventory
            fout.write(m_marbles.size() + "\n");
            for (Marble marble : m_marbles) {
                fout.write(marble.toString() + "\n");
                fout.write(FindTextureSolt(marble.GetTexture1()) + "\n");
                fout.write(FindTextureSolt(marble.GetTexture2()) + "\n");
            }

            fout.close();
            System.out.println("Game saved.");
        } catch (IOException e) {
            System.out.println("Error saving game.");
        }
    }

    public void LoadInventory() {
        File file = new File(s_savefile);
        if (!file.exists()) {
            System.out.println("No save file found.");
            return;
        }

        try {
            Scanner fin = new Scanner(file);
            m_wallet = new CryptoCoin(ConvertHexToReal(fin.nextLine()));
            m_generator.ResetPrice(ConvertHexToReal(fin.nextLine()));

            m_marbles.clear();
            int size = Integer.parseInt(fin.nextLine());
            for (int i = 0; i < size; i++) {
                String name = fin.nextLine();
                long daily_yield = Long.parseLong(fin.nextLine());
                long timestamp = Long.parseLong(fin.nextLine());
                MarbleRarity rarity = MarbleRarity.valueOf(fin.nextLine());
                int t1 = Integer.parseInt(fin.nextLine());
                int t2 = Integer.parseInt(fin.nextLine());
                Texture texture1 = t1 == -1 ? null : m_textures.get(t1);
                Texture texture2 = t2 == -1 ? null : m_textures.get(t2);

                m_marbles.add(new Marble(name, daily_yield, texture1, texture2, rarity, new Date(timestamp)));
            }

            fin.close();
            System.out.println("Inventory loaded.");
        } catch (FileNotFoundException e) {
            System.out.println("Error loading save file.");
        }
    }


    // Cheats:
    public void GenerateEachRarity() {
        m_marbles.add(new Marble(m_marble_loader.GetRandomMarbleData(MarbleRarity.Normal), MarbleRarity.Normal));
        m_marbles.add(new Marble(m_marble_loader.GetRandomMarbleData(MarbleRarity.Rare), MarbleRarity.Rare));
        m_marbles.add(new Marble(m_marble_loader.GetRandomMarbleData(MarbleRarity.SuperRare), MarbleRarity.SuperRare));
        m_marbles.add(new Marble(m_marble_loader.GetRandomMarbleData(MarbleRarity.UltraRare), MarbleRarity.UltraRare));
        m_marbles.add(new Marble(m_marble_loader.GetRandomMarbleData(MarbleRarity.Legendary), MarbleRarity.Legendary));
        // No mythtic marble
    }


    private static final String s_savefile = "save.dat";
    private Vector<Texture> m_textures;
    private CryptoCoin m_wallet;
    private Vector<Marble> m_marbles;
    private Generator m_generator;
    private MarbleLoader m_marble_loader;
}
