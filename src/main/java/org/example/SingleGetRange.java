package org.example;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;
import com.apple.foundationdb.KeyValue;
import com.apple.foundationdb.Range;
import com.apple.foundationdb.StreamingMode;
import com.apple.foundationdb.Transaction;
import com.apple.foundationdb.async.AsyncIterator;
import com.apple.foundationdb.tuple.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SingleGetRange {

    private static final FDB fdb = FDB.selectAPIVersion(620);   
    private static Database db;

    public SingleGetRange() {
        db = fdb.open();
    }

    public void storeKeyValuePairs() {
        db.run(tr -> {
            for (int i = 1; i <= 10000; i++) {
                String key = "key_" + i;
                String value = "val_" + i;
                tr.set(Tuple.from(key).pack(), Tuple.from(value).pack());
            }
            return null;
        });
        
    }

    public List<String> getRangebyMode(byte[] startKey, byte[] endKey, StreamingMode mode) {
        return db.run(tr -> {
            List<KeyValue> results = tr.getRange(startKey, endKey, 10000,false,mode)
                                      .asList().join();
            return results.stream()
                          .map(kv -> Tuple.fromBytes(kv.getKey()).getString(0) + ": " + Tuple.fromBytes(kv.getValue()).getString(0))
                          .toList();
        });
    }

    public void measureSingleRange() {
        
        measureMode(StreamingMode.WANT_ALL);
        measureMode(StreamingMode.EXACT);
        measureMode(StreamingMode.ITERATOR);
    }

    public void measureMode( StreamingMode streamingMode) {
        long startTime;
        long endTime;

        startTime = System.currentTimeMillis();
        getRangebyMode( new byte[]{0x00}, new byte[]{(byte)0xff},streamingMode);
        endTime = System.currentTimeMillis();
        
        System.out.println("Mode: " + streamingMode + "\tDuration: " + (endTime - startTime) + " ms");
    }
    public void close() {
        db.close();
    }

    public static void test() {
        SingleGetRange ops = new SingleGetRange();
        
        System.out.println("Performance for GetRange: Single Mode\n");
        // Step 1: Store 10k key-value pairs
        ops.storeKeyValuePairs();

        // Step 2: Measure performance for each getRange mode
        ops.measureSingleRange();
        System.out.println("=====================================");
        ops.close();
    }
}
