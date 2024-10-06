package org.example;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;
import com.apple.foundationdb.KeyValue;
import com.apple.foundationdb.Transaction;
import com.apple.foundationdb.tuple.Tuple;

import java.util.concurrent.CompletableFuture;

public class TransactionConflict {
    private static final FDB fdb = FDB.selectAPIVersion(620);
    private static Database db;

    public TransactionConflict() {
        db = fdb.open();
    }

    public void close() {
        db.close();
    }

    public void runTransactions() {

        // Start transaction T1 to read K1 and update K2
        CompletableFuture<Void> t1 = CompletableFuture.runAsync(() -> {
            try (Transaction tr = db.createTransaction()) {
                // Read K1 in T1
                System.out.println("Transaction T1: Reading K1");
                byte[] k1Value = tr.get(Tuple.from("K1").pack()).join();
                System.out.println("T1 - K1: " + Tuple.fromBytes(k1Value));
                
                // Update K2 in T1
                tr.set(Tuple.from("K2").pack(), Tuple.from("NewValueK2").pack());

                try {
                    tr.commit().join();
                    System.out.println("Transaction T1 committed.");
                } catch (Exception e) {
                    System.out.println("Transaction T1 aborted due to conflict.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Start transaction T2 to read K2 and update K1
        CompletableFuture<Void> t2 = CompletableFuture.runAsync(() -> {
            try (Transaction tr = db.createTransaction()) {
                // Read K2 in T2
                System.out.println("Transaction T2: Reading K2");
                byte[] k2Value = tr.get(Tuple.from("K2").pack()).join();
                System.out.println("T2 - K2: " + Tuple.fromBytes(k2Value));

                // Update K1 in T2
                tr.set(Tuple.from("K1").pack(), Tuple.from("NewValueK1").pack());

                try {
                    tr.commit().join();
                    System.out.println("Transaction T2 committed.");
                } catch (Exception e) {
                    System.out.println("Transaction T2 aborted due to conflict.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        CompletableFuture.allOf(t1, t2).join();
    }

    private void storeKeyValuePairs() {
        try (Transaction tr = db.createTransaction()) {
            // Store arbitrary key-value pairs
            tr.set(Tuple.from("K1").pack(), Tuple.from("OldValueK1").pack());
            tr.set(Tuple.from("K2").pack(), Tuple.from("OldValueK2").pack());
            tr.commit().join();
            System.out.println("Initial key-value pairs stored: K1, K2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void test() {
        TransactionConflict transactionConflict = new TransactionConflict();
        System.out.println("TransactionConflict: Transaction Conflict\n");

        transactionConflict.storeKeyValuePairs();
        transactionConflict.runTransactions();
        transactionConflict.close();
        
        System.out.println("=====================================");
    }
}

