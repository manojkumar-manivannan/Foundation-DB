package org.example;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;
import com.apple.foundationdb.KeyValue;
import com.apple.foundationdb.StreamingMode;
import com.apple.foundationdb.Transaction;
import com.apple.foundationdb.tuple.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class SingleVsMultiRanges {
    List<String> startRanges = new ArrayList<>(List.of("key_1", "key_1899", "key_2799", "key_3699", "key_4599", "key_5499", "key_6399", "key_7299", "key_8199", "key_9099"));
    List<String> endRanges = new ArrayList<>(List.of("key_1898", "key_2798", "key_3698", "key_4598", "key_5498", "key_6398", "key_7298", "key_8198", "key_9098", "key_9999"));

    private static final FDB fdb = FDB.selectAPIVersion(620);   
    private static Database db;

    public SingleVsMultiRanges() {
        db = fdb.open();
    }

    public void close() {
        db.close();
    }

    public List<KeyValue> getRangebyMode(Transaction tr, byte[] startKey, byte[] endKey, StreamingMode streamingMode) {
        return tr.getRange(startKey, endKey, 100000, false, streamingMode)
                 .asList().join();
    }


    public void measureMultiRange() {
        try {
            measureMode(StreamingMode.WANT_ALL);
            measureMode(StreamingMode.EXACT);
            measureMode(StreamingMode.ITERATOR);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void measureMode(StreamingMode mode) throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();

        // Execute parallel getRange requests
        List<CompletableFuture<List<KeyValue>>> futures = executeGetRangeInParallel(startRanges, endRanges, mode);

        List<List<KeyValue>> results = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()))
                .get();

        long endTime = System.currentTimeMillis();
        System.out.println("Mode: " + mode + "\tDuration: " + (endTime - startTime) + " ms");
    }


    private List<CompletableFuture<List<KeyValue>>> executeGetRangeInParallel(List<String> startKeys, List<String> endKeys, StreamingMode mode) {
        List<CompletableFuture<List<KeyValue>>> futures = new ArrayList<>();
        
        for (int i = 0; i < startKeys.size(); i++) {
            final int index = i;
            CompletableFuture<List<KeyValue>> future = CompletableFuture.supplyAsync(() -> {
                try (Transaction tr = db.createTransaction()) {
                    byte[] startKey = Tuple.from(startKeys.get(index)).pack();
                    byte[] endKey = Tuple.from(endKeys.get(index)).pack();
                    return getRangebyMode(tr, startKey, endKey, mode);
                }
            });
            futures.add(future);
        }
        
        return futures;
    }

    public static void test() {
        SingleVsMultiRanges ops = new SingleVsMultiRanges();

        System.out.println("Performance for GetRange: Single Mode\n");
        ops.measureMultiRange();
        System.out.println("=====================================");

        ops.close();
    }
}
