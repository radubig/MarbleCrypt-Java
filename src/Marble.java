import org.jsfml.graphics.Texture;

import java.util.Date;

public class Marble {
    public static long CalculateDailyYield(MarbleRarity rarity)
    {
        switch (rarity) {
            case Normal -> {return 200;}
            case Rare -> {return 320;}
            case SuperRare -> {return 510;}
            case UltraRare -> {return 820;}
            case Legendary -> {return 2000;}
            case Mythic -> {return 16000;}
            default -> {return 0;}
        }
    }

    // Create a new marble on the spot
    public Marble(String name, long daily_yield, Texture texture_1, Texture texture_2, MarbleRarity rarity) {
        m_name = name;
        m_daily_yield = daily_yield;
        m_texture = texture_1;
        m_texture_2 = texture_2;
        m_rarity = rarity;
        m_timepoint_last_yield = new Date();
    }

    // Create a new marble and provide the timepoint of the last yield
    public Marble(int id, String name, long daily_yield, Texture texture_1, Texture texture_2, MarbleRarity rarity, Date timepoint_last_yield) {
        this.id = id;
        m_name = name;
        m_daily_yield = daily_yield;
        m_texture = texture_1;
        m_texture_2 = texture_2;
        m_rarity = rarity;
        m_timepoint_last_yield = timepoint_last_yield;
    }

    // Create a new marble from MarbleData
    public Marble(MarbleLoader.MarbleData data, MarbleRarity rarity) {
        m_name = data.name;
        m_texture = data.texture;
        m_texture_2 = null;
        m_rarity = rarity;
        m_timepoint_last_yield = new Date();
        switch (rarity) {
            case Normal -> m_daily_yield = 200;
            case Rare -> m_daily_yield = 320;
            case SuperRare -> m_daily_yield = 510;
            case UltraRare -> m_daily_yield = 820;
            case Legendary -> m_daily_yield = 2000;
            case Mythic -> m_daily_yield = 16000;
        }
    }

    public Texture GetTexture1() {
        return m_texture;
    }

    public Texture GetTexture2() {
        return m_texture_2;
    }

    public long GetYield() {
        Date now = new Date();
        // Get the time difference in seconds
        long dur = (now.getTime() - m_timepoint_last_yield.getTime()) / 1000;
        double total_yield  = (double) dur * YieldPerSec();
        if (total_yield > m_daily_yield)
            return m_daily_yield;
        return (long) total_yield;
    }

    public long GetDailyYield() {
        return m_daily_yield;
    }

    public MarbleRarity GetRarity() {
        return m_rarity;
    }

    public void CollectYield() {
        if (GetYield() > 0) {
            m_timepoint_last_yield = new Date();
            DbManager.getInstance().UpdateMarble(this.id, m_timepoint_last_yield.getTime());
        }
    }

    public String GetName() {
        return m_name;
    }

    @Override
    public String toString() {
        return m_name + "\n" +
               m_daily_yield + "\n" +
               m_timepoint_last_yield.getTime() + "\n" +
               m_rarity.toString();
    }

    public void AddToDb(int texture1, int texture2) {
        int id = DbManager.getInstance().CreateMarble(m_name, m_daily_yield, m_timepoint_last_yield.getTime(), m_rarity.toString(), texture1, texture2);
        this.id = id;
    }

    public void DeleteFromDb() {
        DbManager.getInstance().DeleteMarble(this.id);
    }

    public int id;
    private String m_name;
    private Texture m_texture; // Pointer
    private Texture m_texture_2; // Pointer
    private MarbleRarity m_rarity;
    private Date m_timepoint_last_yield;
    private long m_daily_yield;
    private double YieldPerSec() {
        return (double) m_daily_yield / (24 * 60 * 60);
    }
}
