package com.example.StressOverflow.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Sorter {
    public static <T> void sort(ArrayList<T> list, Comparator<T> comparator) {
        Collections.sort(list, comparator);
    }
}
