import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HyperLogLog {


    private final int b;
    private final int m;
    private final int[] registers;
    private final double alpha;


    public HyperLogLog(int b) {
        if (b < 4 || b > 16) {
            throw new IllegalArgumentException("b parametresi 4 ile 16 arasında olmalıdır.");
        }
        this.b = b;
        this.m = 1 << b;              // 2^b
        this.registers = new int[m];  // başta hepsi 0
        this.alpha = alphaM();
    }

    private double alphaM() {
        if (m == 16) return 0.673;
        if (m == 32) return 0.697;
        if (m == 64) return 0.709;
        return 0.7213 / (1.0 + 1.079 / m);
    }


    private long hash64(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            long result = 0;
            for (int i = 0; i < 8; i++) {
                result = (result << 8) | (bytes[i] & 0xFFL);
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 bulunamadı", e);
        }
    }

    private int leadingZeros(long bits, int totalBits) {
        if (bits == 0) return totalBits + 1;
        int count = 0;
        for (int i = totalBits - 1; i >= 0; i--) {
            if ((bits & (1L << i)) != 0) break;
            count++;
        }
        return count + 1;
    }

    public void add(String item) {
        long h = hash64(item);

        int j = (int) (h >>> (64 - b));

        long remaining = h & ((1L << (64 - b)) - 1);

        int rho = leadingZeros(remaining, 64 - b);

        if (rho > registers[j]) {
            registers[j] = rho;
        }
    }

    public double count() {
        double harmonicSum = 0.0;
        for (int r : registers) {
            harmonicSum += Math.pow(2.0, -r);
        }
        double E = alpha * m * m / harmonicSum;

        if (E <= 2.5 * m) {
            int V = 0;
            for (int r : registers) {
                if (r == 0) V++;
            }
            if (V > 0) {
                E = m * Math.log((double) m / V);
            }
        }

        else if (E > Math.pow(2, 32) / 30.0) {
            E = -Math.pow(2, 32) * Math.log(1.0 - E / Math.pow(2, 32));
        }

        return E;
    }

    public HyperLogLog merge(HyperLogLog other) {
        if (this.b != other.b) {
            throw new IllegalArgumentException("b parametreleri eşit olmalıdır.");
        }
        HyperLogLog merged = new HyperLogLog(this.b);
        for (int i = 0; i < this.m; i++) {
            merged.registers[i] = Math.max(this.registers[i], other.registers[i]);
        }
        return merged;
    }

    public static double standartHata(int b) {
        int m = 1 << b;
        return 1.04 / Math.sqrt(m);
    }

    public static void hataAnalizi() {
        System.out.println("\n====================================================");
        System.out.println("  Teorik Hata Analizi: b → m → Standart Hata");
        System.out.println("====================================================");
        System.out.printf("  %-4s  %-10s  %-14s  %-14s%n", "b", "m (kova)", "Std Hata (%)", "Bellek (byte)");
        System.out.println("  --------------------------------------------------");
        for (int b = 4; b <= 16; b++) {
            int m = 1 << b;
            double hata = standartHata(b) * 100;
            System.out.printf("  %-4d  %-10d  %-13.2f%%  %-14d%n", b, m, hata, m);
        }
        System.out.println("====================================================");
        System.out.println("  Matematiksel ilişki: σ = 1.04 / √m");
        System.out.println("  b'yi 1 artırmak → hatayı √2 ≈ 1.41x azaltır");
        System.out.println("====================================================");
    }

    public static void deney(int b) {
        System.out.println("\n=======================================================");
        System.out.printf("  Deneysel Doğrulama  (b=%d, m=%d kova)%n", b, 1 << b);
        System.out.println("=======================================================");
        System.out.printf("  %-12s  %-14s  %-10s%n", "Gerçek N", "HLL Tahmin", "Hata %");
        System.out.println("  ----------------------------------------");

        int[] testDegerleri = {100, 1000, 10000, 100000, 1000000};

        for (int n : testDegerleri) {
            HyperLogLog hll = new HyperLogLog(b);
            for (int i = 0; i < n; i++) {
                hll.add("eleman_" + i);
            }
            double tahmin = hll.count();
            double hata = Math.abs(tahmin - n) / n * 100;
            System.out.printf("  %-12d  %-14.0f  %-9.2f%%%n", n, tahmin, hata);
        }
        System.out.println("=======================================================");
    }

    public static void mergeDemo() {
        System.out.println("\n=======================================================");
        System.out.println("  Birleştirme (Merge) Demosu");
        System.out.println("=======================================================");

        int b = 10;
        HyperLogLog hllA = new HyperLogLog(b);
        HyperLogLog hllB = new HyperLogLog(b);

        for (int i = 0; i < 50000; i++) {
            hllA.add("eleman_" + i);
        }

        for (int i = 30000; i < 80000; i++) {
            hllB.add("eleman_" + i);
        }

        HyperLogLog hllUnion = hllA.merge(hllB);

        int gercekUnion = 80000;

        System.out.printf("  |A|      (gerçek): 50000  → HLL: %.0f%n", hllA.count());
        System.out.printf("  |B|      (gerçek): 50000  → HLL: %.0f%n", hllB.count());
        System.out.printf("  |A ∪ B|  (gerçek): 80000  → HLL: %.0f%n", hllUnion.count());
        double hata = Math.abs(hllUnion.count() - gercekUnion) / gercekUnion * 100;
        System.out.printf("  Tahmin hatasi: %.2f%%%n", hata);
        System.out.println("=======================================================");
    }

    public static void main(String[] args) {
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║   HyperLogLog — Kardinalite Tahmini          ║");
        System.out.println("╚══════════════════════════════════════════════╝");

        hataAnalizi();
        deney(10);
        mergeDemo();

        System.out.println("\n Tüm adımlar tamamlandı.");
    }
}
