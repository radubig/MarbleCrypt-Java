public class CryptoCoin {
    static final double s_initial_ammount = 90;
    public CryptoCoin() {
        m_balance = s_initial_ammount;
    }
    public CryptoCoin(double balance) {
        m_balance = balance;
    }
    public boolean Pay(double ammount) {
        if (ammount > m_balance)
            return false;
        m_balance -= ammount;
        return true;
    }
    public void Add(double ammount) {
        m_balance += ammount;
    }
    public double Balance() {
        return m_balance;
    }
    @Override
    public String toString() {
        long bits = Double.doubleToLongBits(m_balance);
        return Long.toHexString(bits);
    }

    private double m_balance;
}
