# HyperLogLog – Kardinalite Tahmini

## Proje Hakkında

Bu proje, büyük veri kümelerinde **benzersiz eleman sayısını (cardinality)** yaklaşık olarak tahmin etmek için kullanılan **HyperLogLog algoritmasının Java implementasyonudur**.

HyperLogLog, çok büyük veri setlerinde tüm elemanları saklamadan **çok az bellek kullanarak** benzersiz eleman sayısını tahmin edebilen **olasılıksal bir veri yapısıdır**.

Bu proje aşağıdaki özellikleri içerir:

* HyperLogLog algoritmasının temel implementasyonu
* SHA-256 tabanlı hash fonksiyonu
* Kardinalite tahmini
* Teorik hata analizi
* Deneysel doğrulama
* HyperLogLog birleştirme (merge) işlemi

---

# Algoritma Mantığı

HyperLogLog algoritması şu adımlarla çalışır:

1. Her eleman bir **hash fonksiyonundan** geçirilir.
2. Hash değerinin ilk `b` biti kullanılarak bir **register (kova)** seçilir.
3. Kalan bitlerdeki **ilk 1 bitine kadar olan sıfır sayısı** hesaplanır.
4. Bu değer register içinde saklanır.
5. Tüm register değerleri kullanılarak **harmonik ortalama** üzerinden kardinalite tahmini yapılır.

---

# Matematiksel Temel

Tahmin formülü:

```
E = α * m² / Σ(2^-M[i])
```

Burada:

* `m = 2^b` → register sayısı
* `α` → düzeltme sabiti
* `M[i]` → i. register değeri

Standart hata:

```
σ = 1.04 / √m
```

---

# Proje Yapısı

```
HyperLogLog/
│
├── HyperLogLog.java
└── README.md
```

---

# Kullanım

### Derleme

```bash
javac HyperLogLog.java
```

### Çalıştırma

```bash
java HyperLogLog
```

---

# Program Çıktısı

Program çalıştırıldığında üç bölüm gösterilir:

### 1️⃣ Teorik Hata Analizi

```
b     m (kova)     Std Hata (%)     Bellek (byte)
4     16           26.00%           16
10    1024         3.25%            1024
16    65536        0.41%            65536
```

---

### 2️⃣ Deneysel Doğrulama

```
Gerçek N      HLL Tahmin      Hata %
100           102             2.00%
1000          987             1.30%
10000         10120           1.20%
```

---

### 3️⃣ Merge (Birleştirme) Demosu

```
|A|      (gerçek): 50000  → HLL: 49850
|B|      (gerçek): 50000  → HLL: 50120
|A ∪ B|  (gerçek): 80000  → HLL: 80340
Tahmin hatası: 0.42%
```

---

# Bellek Avantajı

HyperLogLog algoritmasının en büyük avantajı **çok düşük bellek kullanımıdır**.

Örnek:

| Gerçek Veri     | HashSet Bellek | HyperLogLog Bellek |
| --------------- | -------------- | ------------------ |
| 1 milyon eleman | ~16 MB         | ~1 KB              |

---

# Özellikler

* O(n) veri ekleme
* Sabit bellek kullanımı
* Büyük veri setleri için ideal
* Dağıtık sistemlerde **merge** desteği

---

# Kullanım Alanları

HyperLogLog aşağıdaki sistemlerde yaygın olarak kullanılır:

* Web analitiği (benzersiz ziyaretçi sayısı)
* Büyük veri sistemleri
* Veritabanı optimizasyonu
* Dağıtık veri işleme sistemleri
* Log analizi

---

# Kaynaklar

* Flajolet, Philippe. *HyperLogLog: the analysis of a near-optimal cardinality estimation algorithm*
* Google BigQuery
* Redis HyperLogLog

---

# Lisans

Bu proje eğitim amaçlı geliştirilmiştir.
