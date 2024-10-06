package org.example;

public class Main 
{
    public static void main( String[] args )
    {
        BasicFDBOps.test();
        SingleGetRange.test();
        SingleVsMultiRanges.test();
        ReadSnapshot.test();
        TransactionConflict.test();
    }
}
