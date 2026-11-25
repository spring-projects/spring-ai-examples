/*
 * Copyright 2024 - 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ai.openai.samples.helloworld;

public class MergeSort {

    public static void mergeSort(int[] array) {
        if (array == null || array.length < 2) {
            return;
        }
        int[] tempArray = new int[array.length];
        mergeSort(array, tempArray, 0, array.length - 1);
    }

    private static void mergeSort(int[] array, int[] tempArray, int start, int end) {
        if (start < end) {
            int mid = start + (end - start) / 2;

            // Recursively sort the two halves
            mergeSort(array, tempArray, start, mid);
            mergeSort(array, tempArray, mid + 1, end);

            // Merge the sorted halves
            merge(array, tempArray, start, mid, end);
        }
    }

    private static void merge(int[] array, int[] tempArray, int start, int mid, int end) {
        // Copy data to temporary array for merging
        System.arraycopy(array, start, tempArray, start, end - start + 1);

        int leftIndex = start;
        int rightIndex = mid + 1;
        int currentIndex = start;

        // Merge the temp arrays back into the original array
        while (leftIndex <= mid && rightIndex <= end) {
            if (tempArray[leftIndex] <= tempArray[rightIndex]) {
                array[currentIndex] = tempArray[leftIndex];
                leftIndex++;
            } else {
                array[currentIndex] = tempArray[rightIndex];
                rightIndex++;
            }
            currentIndex++;
        }

        // Copy remaining elements of left half, if any
        while (leftIndex <= mid) {
            array[currentIndex] = tempArray[leftIndex];
            leftIndex++;
            currentIndex++;
        }

        // No need to copy the right half because it's already in place
    }

    public static void main(String[] args) {
        int[][] testCases = {
                {12, 11, 13, 5, 6, 7},
                {5, 5, 5, 5, 5, 5},
                {},
                {-1, -3, -2, -5, -4},
                {1, 2, 3, 4, 5, 6},
                {9, 7, 5, 3, 1, 0}
        };

        for (int i = 0; i < testCases.length; i++) {
            System.out.println("Test Case " + (i + 1) + ":");
            System.out.println("Original Array:");
            printArray(testCases[i]);

            mergeSort(testCases[i]);

            System.out.println("Sorted Array:");
            printArray(testCases[i]);
            System.out.println();
        }
    }

    private static void printArray(int[] array) {
        for (int value : array) {
            System.out.print(value + " ");
        }
        System.out.println();
    }
}
