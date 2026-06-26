package com.project.auth_app_backend;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthAppBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthAppBackendApplication.class, args);
		
        // bubbleSort();
		float f = 3.34f;
		System.out.println(f);
		
		// List.of() creates an unmodifiable list directly
		List<String> fruits = List.of("Apple", "Banana", "Cherry");
		String str = "Hello";
		char[] charArray = str.toCharArray();
		
		for (char c: charArray) {
			System.out.println(c);
		}
		
		System.out.println("======================");
		
		for (int i=0; i < charArray.length; i++) {
			System.out.println(charArray[i]);
		}
		
		System.out.println("======================");
		
		for (int i=charArray.length - 1; i >= 0 ; i--) {
			System.out.println(charArray[i]);
		}
		
		System.out.println("======================");
		
		String str2 = "the   sky is blue";
		String str2Arr[] = str2.trim().split("\\s+");
		
		StringBuffer reversStr2 = new StringBuffer();
		
		for (int i=str2Arr.length - 1; i >= 0 ; i--) {
			reversStr2.append(str2Arr[i]);
			if (i != 0) {
				reversStr2.append(" ");
			}
			System.out.println(str2Arr[i]);
		}
		
		System.out.println(reversStr2);
		// This will crash with UnsupportedOperationException:
		// fruits.add("Date");
		System.out.println("======================");
		int numarr[] = {4, 3, 2, 7, 8, 2, 3, 1};
		HashSet<Integer> unique = new HashSet<Integer>();
		List<Integer> duplicate = new ArrayList<>();
		
		for (int num: numarr) {
			if (!unique.add(num) && !duplicate.contains(args)) {
				duplicate.add(num);
			}
		}
		
		System.out.println(unique);
		System.out.println(duplicate);
		
		System.out.println("======================");
		Integer a = 10;
		Integer b = a;
		
		System.out.println(a == b);
		System.out.println("logest substring======================");
		
		String str4 = "pwwwke";
		String str4Substr = "";
		char[] charArray1 = str4.toCharArray();
		HashSet<Character> strset = new HashSet<Character>();
		
		for (char ch: charArray1) {
			if (strset.add(ch)) {
				str4Substr += String.valueOf(ch);
			}
		}
		
		System.out.println(str4Substr);
		
	}
	
	public static void bubbleSort() {
		int[] arr = {64, 34, 25, 12, 22, 11, 90};

        int n = arr.length;
        boolean swapped;

        // Outer loop for each pass
        for (int i = 0; i < n; i++) {
            swapped = false;

            // Inner loop for comparison
            for (int j = 0; j < n - i - 1; j++) {
                // If current element is greater than next, swap them
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }

            // If no two elements were swapped by inner loop, array is sorted
            if (!swapped) break;
        }
        
     // Printing the sorted array
        for (int num : arr) {
            System.out.print(num + " ");
        }
    }

}
