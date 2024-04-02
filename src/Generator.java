import java.util.Random;

public class Generator {
    static final double s_initial_cost = 1.0;
    public Generator() {
        m_cost = s_initial_cost;
    }
    public Generator(double cost) {
        m_cost = cost;
    }
    public void ResetPrice() {
        m_cost = s_initial_cost;
    }
    public void ResetPrice(double cost) {
        m_cost = cost;
    }
    public double GetPrice() {
        return Math.floor(m_cost);
    }
    public int Generate() {
        this.RiseCost();
        return m_rng.nextInt(m_range_min, m_range_max);
    }
    public void SetRange(int min, int max) {
        m_range_min = min;
        m_range_max = max;
    }
    @Override
    public String toString() {
        long bits = Double.doubleToLongBits(m_cost);
        return Long.toHexString(bits);
    }

    private double m_cost;
    private final Random m_rng = new Random();
    private int m_range_min = 0;
    private int m_range_max = 1;
    private void RiseCost() {
        m_cost *= 1.24;
    }
}
