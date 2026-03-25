package org.puregxl.core;

public class CosineSimilarity {
    public static double calculate(double[] vectorA, double[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("向量维度不一致");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (normA * normB);
    }



    public int findKthLargest(int[] nums, int k) {
        return quickSort(nums, 0, nums.length - 1, nums.length, k);
    }

    public int quickSort(int[] nums, int left, int right, int size, int k) {
        int temp = nums[right];
        int startIndex = left;
        for (int i = left; i < right ; i++) {
            if (nums[i] < temp) {
                swap(nums, i, startIndex);
                startIndex++;
            }
        }
        swap(nums, startIndex, right);
        if (startIndex == size - k) {
            return nums[startIndex];
        } else if (startIndex > size - k) {
            return quickSort(nums, left, startIndex - 1, size, k);
        } else {
            return quickSort(nums, startIndex + 1, right, size, k);
        }
    }

    public void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }



}
