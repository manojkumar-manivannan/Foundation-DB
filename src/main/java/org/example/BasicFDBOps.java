package org.example;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;
import com.apple.foundationdb.KeyValue;
import com.apple.foundationdb.tuple.Tuple;

import java.util.List;

public class BasicFDBOps {

    private static final FDB fdb = FDB.selectAPIVersion(620); 
    private static Database db;

    public BasicFDBOps() {
        db = fdb.open();
    }

    public void close() {
        // Destroy the database connection
        // Delete all the data in the database

        // db.run(tr -> {
        //     tr.clear(Tuple.from("").pack(), Tuple.from("").pack());
        //     return null;
        // });
        db.close();
    }

    public void set(String key, String value) {
        db.run(tr -> {
            tr.set(Tuple.from(key).pack(), Tuple.from(value).pack());
            return null;
        });
    }

    public String get(String key) {
        return db.run(tr -> {
            byte[] result = tr.get(Tuple.from(key).pack()).join();
            return result == null ? null : Tuple.fromBytes(result).getString(0);
        });
    }

    public List<String> getRange(String startKey, String endKey) {
        return db.run(tr -> {
            List<KeyValue> results = tr.getRange(Tuple.from(startKey).pack(), Tuple.from(endKey).pack())
                                      .asList().join();
            return results.stream()
                          .map(kv -> Tuple.fromBytes(kv.getKey()).getString(0) + ": " + Tuple.fromBytes(kv.getValue()).getString(0))
                          .toList();
        });
    }
    public static void test() {
        BasicFDBOps ops = new BasicFDBOps();

        System.out.println("=====================================");
        System.out.println("BasicFDBOps: Get, Set, GetRange\n");

        // Set operation
        System.out.println("Set key: name_first with value: Manojkumar");
        System.out.println("Set key: name_last with value: Manivannan");
        ops.set("name_first", "Manojkumar");
        ops.set("name_last", "Manivannan");

        // Get operation
        String value = ops.get("name_last");
        System.out.println("Get key: name_last with value: " + value);

        // Get range operation
        List<String> result = ops.getRange("name_first", "name_lasts");
        System.out.println("Get range from name_first to name_last: " + result);

        System.out.println("=====================================");
        ops.close();
    }
}
