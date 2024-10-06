# Task - 1
## Explore FoundationDB and its Java Client Library

## Introduction
  - FoundationDB is a distributed database designed to handle large volumes of structured data across clusters of commodity servers.  
  - The FoundationDB client library is a client-side library that provides an interface for interacting with the FoundationDB database. 

## Prerequisites
  - Java
  - Maven

## Instructions
  - Clone: `git clone url`
  - Build: `mvn clean install`
  - Run: `mvn exec:java`

## Sub-Tasks
  - ### Sub-Task 1:
    - Explore FDB command line tool fdbcli.
    - **Operations:**
      - Set a key-value pair.
      - Get the value of a key.
      - Perform range reads.
  - ### Sub-Task 2:
    - Basic operations using Java client library.
    - **Class:** `com.example.BasicFDBOps`
      - **Operations:**
        - Set a key-value pair.
        - Get the value of a key.
        - Perform range reads.
  - ### Sub-Task 3:
    - Measure single get range performance.
    - **Class:** `com.example.SingleGetRange`
      - **Operations:**
        - Store 1000 key-value pairs.
        - Retrieve all key-value pairs.
        - Measure the time taken.
        - Get Range Modes:
          - WANT_ALL
          - EXACT
          - ITERATOR
  - ### Sub-Task 4:
    - Measure multi-get range performance.
    - **Class:** `com.example.SingleVsMultiRanges`
      - **Operations:**
        - Split the key space into 1000 key-value pairs.
        - Execute parallel get range operations.
        - Measure the time taken.
          - WANT_ALL
          - EXACT
          - ITERATOR
  - ### Sub-Task 5:
    - Understand read snapshot.
    - **Class:** `com.example.ReadSnapshot`
      - **Operations:**
        - Store some key-value pairs.
        - Initiate 2 transactions.
          - *Transaction 1:* Read all key-value pairs.
          - *Transaction 2:* Update some key-value pairs.
        - Commit both transactions.
         
  - **Sub-Task 6**:
    - Understand transaction conflict.
    - **Class:** `com.example.WriteConflict`
      - **Operations:**
        - Store 2 key-value pairs.
        - Initiate 2 transactions.
          - *Transaction 1:* Read key-1 and update key-2.
          - *Transaction 2:* Read key-2 and update key-1.
        - Commit both transactions.

