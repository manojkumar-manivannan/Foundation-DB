package org.example;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;
import com.apple.foundationdb.KeyValue;
import com.apple.foundationdb.Transaction;
import com.apple.foundationdb.tuple.Tuple;

import java.util.concurrent.CompletableFuture;

public class ReadSnapshot {
    private static final FDB fdb = FDB.selectAPIVersion(620);
    private static Database db;

    public ReadSnapshot() {
        db = fdb.open();
    }

    public void close() {
        db.close();
    }

    public void runTransactions() {
        // Start T1 to read keys K1, K2, K3
        CompletableFuture<Void> t1 = CompletableFuture.runAsync(() -> {
            try (Transaction tr = db.createTransaction()) {
              // Read keys K1, K2, K3
                System.out.println("Transaction T1: Reading keys K1, K2, K3");
                byte[] k1 = tr.get(Tuple.from("K1").pack()).join();
                byte[] k2 = tr.get(Tuple.from("K2").pack()).join();
                byte[] k3 = tr.get(Tuple.from("K3").pack()).join();
                System.out.println("T1 - K1: " + Tuple.fromBytes(k1) + ", K2: " + Tuple.fromBytes(k2) + ", K3: " + Tuple.fromBytes(k3));

                tr.commit().join();
                System.out.println("Transaction T1 committed.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Start T2 to update K2 and K4
        CompletableFuture<Void> t2 = CompletableFuture.runAsync(() -> {
            try (Transaction tr = db.createTransaction()) {
                System.out.println("Transaction T2: Updating K2 and K4");

                // Update keys K2, K4
                tr.set(Tuple.from("K2").pack(), Tuple.from("NewValueK2").pack());
                tr.set(Tuple.from("K4").pack(), Tuple.from("NewValueK4").pack());

                tr.commit().join();
                System.out.println("Transaction T2 committed.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        CompletableFuture.allOf(t1, t2).join();
    }

    private void storeKeyValuePairs() {
        try (Transaction tr = db.createTransaction()) {
            tr.set(Tuple.from("K1").pack(), Tuple.from("OldValueK1").pack());
            tr.set(Tuple.from("K2").pack(), Tuple.from("OldValueK2").pack());
            tr.set(Tuple.from("K3").pack(), Tuple.from("OldValueK3").pack());
            tr.set(Tuple.from("K4").pack(), Tuple.from("OldValueK4").pack());
            tr.commit().join();
            System.out.println("Initial key-value pairs stored: K1, K2, K3, K4");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void test() {
        ReadSnapshot readSnapshot = new ReadSnapshot();

        System.out.println("ReadSnapshot: Read Snapshot\n");

        readSnapshot.storeKeyValuePairs();
        readSnapshot.runTransactions();
        readSnapshot.close();

        System.out.println("=====================================");
    }
} 
