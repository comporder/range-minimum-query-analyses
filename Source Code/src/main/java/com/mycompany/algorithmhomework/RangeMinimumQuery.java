/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.algorithmhomework;

/**
 *
 * @author Emir Özalp
 *
 */
import java.io.PrintWriter;
import java.util.*;

public class RangeMinimumQuery {

    // Sparse Table Algorithm
    public static class SparseTable {

        private int[][] sparseTable;  // Sparse table to store minimum values for ranges
        private int[] logValues;  // Log values used to quickly calculate range sizes

        // Constructor to initialize the sparse table
        public SparseTable(int[] arr) {
            int size = arr.length;
            int maxLog = (int) (Math.log(size) / Math.log(2)) + 1;

            sparseTable = new int[size][maxLog];  // Create sparse table to store minimum values
            logValues = new int[size + 1];  // Array for storing precomputed log values

            // Precompute log values
            for (int i = 2; i <= size; i++) {
                logValues[i] = logValues[i / 2] + 1;
            }

            // Initialize the first column of the table with the array values
            for (int i = 0; i < size; i++) {
                sparseTable[i][0] = arr[i];
            }

            // Build the sparse table using dynamic programming
            for (int j = 1; (1 << j) <= size; j++) {
                for (int i = 0; i + (1 << j) - 1 < size; i++) {
                    sparseTable[i][j] = Math.min(sparseTable[i][j - 1], sparseTable[i + (1 << (j - 1))][j - 1]);
                }
            }
        }

        // Query function to get the minimum value in range [start, end]
        public int query(int start, int end) {
            int j = logValues[end - start + 1];
            return Math.min(sparseTable[start][j], sparseTable[end - (1 << j) + 1][j]);
        }
    }

    // Blocking Algorithm
    public static class Blocking {

        private int[] blockMins;  // Store minimum values of blocks
        private int blockSize;  // Size of each block
        private int[] arr;  // Input array

        // Constructor to preprocess the array into blocks
        public Blocking(int[] arr) {
            this.arr = arr;
            int n = arr.length;
            blockSize = (int) Math.sqrt(n);  // Block size is the square root of the array size
            int numBlocks = (n + blockSize - 1) / blockSize;  // Calculate the number of blocks
            blockMins = new int[numBlocks];  // Create an array to store minimum values for each block

            // Precompute minimum values for each block
            for (int i = 0; i < numBlocks; i++) {
                int blockStart = i * blockSize;
                int blockEnd = Math.min(blockStart + blockSize, n);
                blockMins[i] = arr[blockStart];
                for (int j = blockStart + 1; j < blockEnd; j++) {
                    blockMins[i] = Math.min(blockMins[i], arr[j]);
                }
            }
        }

        // Query function to get the minimum value in range [start, end]
        public int query(int start, int end) {
            int minVal = Integer.MAX_VALUE;
            while (start <= end) {
                if (start % blockSize == 0 && start + blockSize - 1 <= end) {
                    // If the current range is a complete block
                    minVal = Math.min(minVal, blockMins[start / blockSize]);
                    start += blockSize;
                } else {
                    // If the range is a partial block, check element by element
                    minVal = Math.min(minVal, arr[start]);
                    start++;
                }
            }
            return minVal;
        }
    }

    // Precompute All Algorithm: Precompute minimum for all subarrays
    public static class PrecomputeAll {

        private int[][] minTable;  // Table to store minimum values for all subarrays

        // Constructor to preprocess the array and fill the table
        public PrecomputeAll(int[] arr) {
            int n = arr.length;
            minTable = new int[n][n];

            // Precompute minimum values for all subarrays
            for (int i = 0; i < n; i++) {
                minTable[i][i] = arr[i];  // Single element subarray
                for (int j = i + 1; j < n; j++) {
                    minTable[i][j] = Math.min(minTable[i][j - 1], arr[j]);
                }
            }
        }

        // Query function to get the minimum value in range [start, end]
        public int query(int start, int end) {
            return minTable[start][end];
        }
    }

    // No Precompute Algorithm: Calculate minimum on the fly without preprocessing
    public static class PrecomputeNone {

        private int[] arr;  // Input array

        // Constructor to initialize the array
        public PrecomputeNone(int[] arr) {
            this.arr = arr;
        }

        // Query function to get the minimum value in range [start, end]
        public int query(int start, int end) {
            int minVal = Integer.MAX_VALUE;
            // Check each element in the range and update the minimum
            for (int i = start; i <= end; i++) {
                minVal = Math.min(minVal, arr[i]);
            }
            return minVal;
        }
    }

    public static void main(String[] args) {
        int[] arraySizes = {1000, 2000, 5000, 10000}; // Array sizes for testing
        int[] queryCounts = {100, 500, 1000}; // Different numbers of queries

        try (PrintWriter writer = new PrintWriter("results.csv")) {
            writer.println("Algorithm,ArraySize,QueryCount,PreprocessTime,QueryTime");

            // Loop over different array sizes and query counts
            for (int arraySize : arraySizes) {
                for (int queryCount : queryCounts) {
                    int[] array = new Random().ints(arraySize, 0, 1000).toArray(); // Generate random array
                    int[][] queries = new int[queryCount][2]; // Store queries
                    Random rand = new Random();
                    for (int i = 0; i < queryCount; i++) {
                        int l = rand.nextInt(arraySize);
                        int r = rand.nextInt(arraySize - l) + l;
                        queries[i][0] = l;
                        queries[i][1] = r;
                    }

                    // Test Sparse Table Algorithm
                    long start = System.nanoTime();
                    SparseTable sparseTable = new SparseTable(array);  // Initialize sparse table
                    long preprocessTime = System.nanoTime() - start;

                    start = System.nanoTime();
                    for (int[] query : queries) {
                        sparseTable.query(query[0], query[1]);  // Query the sparse table
                    }
                    long queryTime = System.nanoTime() - start;

                    writer.printf("SparseTable,%d,%d,%d,%d\n", arraySize, queryCount, preprocessTime, queryTime);

                    // Test Blocking Algorithm
                    start = System.nanoTime();
                    Blocking blocking = new Blocking(array);  // Initialize blocking
                    preprocessTime = System.nanoTime() - start;

                    start = System.nanoTime();
                    for (int[] query : queries) {
                        blocking.query(query[0], query[1]);  // Query the blocking algorithm
                    }
                    queryTime = System.nanoTime() - start;
                    writer.printf("Blocking,%d,%d,%d,%d\n", arraySize, queryCount, preprocessTime, queryTime);

                    // Test No Precompute Algorithm
                    start = System.nanoTime();
                    PrecomputeNone noPrecompute = new PrecomputeNone(array);  // Initialize no precompute
                    preprocessTime = System.nanoTime() - start;

                    start = System.nanoTime();
                    for (int[] query : queries) {
                        noPrecompute.query(query[0], query[1]);  // Query the no precompute algorithm
                    }
                    queryTime = System.nanoTime() - start;

                    writer.printf("NoPrecompute,%d,%d,%d,%d\n", arraySize, queryCount, preprocessTime, queryTime);

                    // Test Precompute All Algorithm
                    start = System.nanoTime();
                    PrecomputeAll precomputeAll = new PrecomputeAll(array);  // Initialize precompute all
                    preprocessTime = System.nanoTime() - start;

                    start = System.nanoTime();
                    for (int[] query : queries) {
                        precomputeAll.query(query[0], query[1]);  // Query the precompute all algorithm
                    }
                    queryTime = System.nanoTime() - start;

                    writer.printf("PrecomputeAll,%d,%d,%d,%d\n", arraySize, queryCount, preprocessTime, queryTime);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Experiment results saved to results.csv");  // Output result file
    }
}
