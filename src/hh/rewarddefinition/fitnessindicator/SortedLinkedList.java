/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition.fitnessindicator;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * This is a doubly linked list that is used to maintain a sorted list. Sorting
 * is O(nlog(n)), look up for max and min are O(1), insertion and deletion is
 * O(1), search in sorted list is O(log(n)).
 *
 * @author SEAK2
 */
public class SortedLinkedList<T> extends LinkedList<T> {

    private static final long serialVersionUID = -6089720372001116175L;

    private final Comparator<T> comparator;

    /**
     * Constructor takes in unsorted list and sorts it. Takes O(nlog(n)) where n
     * is number of items in collection
     *
     * @param unSortedList collection of items to sort
     * @param comparator comparator to use to sort
     */
    public SortedLinkedList(Collection<T> unSortedList, Comparator<T> comparator) {
        super(unSortedList);
        this.sort();
        this.comparator = comparator;
    }
    
    /**
     * Sorts the collection
     */
    public final void sort(){
        Collections.sort(this, comparator);
    }

    /**
     * Finds index of item in sorted list (smallest value at 0th index) using
     * binary search method. This will not return an error if the list is not
     * sorted. If list is not sorted, a nonsensical index is returned
     *
     * @param item the item to find in the list
     * @return the index of the item in the list
     */
    public int binaryFind(T item) {
        return binaryFind(item, 0, this.size());
    }

    /**
     * Finds index of item in sorted list (smallest value at 0th index) with
     * maximum and minimum index bounds using binary search method. This will
     * not return an error if the list is not sorted. If list is not sorted, a
     * nonsensical index is returned
     *
     * @param item the item to find in the list
     * @param min the min of the range to search
     * @param max the max of the range to search
     * @return the index of the item in the list
     */
    public int binaryFind(T item, int min, int max) {
        try{
        if (max < min) {
            throw new IllegalArgumentException("Item not found in list");
        } else {
            int mid = (max - min) / 2 + min;
            if (comparator.compare(this.get(mid), item) == 1) {
                return binaryFind(item, min, mid - 1);
            } else if (comparator.compare(this.get(mid), item) == -1) {
                return binaryFind(item, mid + 1, max);
            } else {
                return mid;
            }
        }
        }catch(IndexOutOfBoundsException ex){
            System.out.println("Item not found in list: Index exceeeded");
            throw ex;
        }
    }

    /**
     * Finds index to insert item in sorted list (smallest value at 0th index)
     * with maximum and minimum index bounds using binary search method. This
     * will not return an error if the list is not sorted. If list is not
     * sorted, a nonsensical index is returned
     *
     * @param item the item to insert in the list
     * @param min the min of the range to consider for insertion
     * @param max the max of the range to consider for insertion
     * @return the index to in the list where the item was inserted
     */
    private int binaryFindInsert(T item, int min, int max) {
        if(flaggedAddFirst(item))
            return 0;
        else if(flaggedAddLast(item))
            return this.size()-1;
        else{
            int index = recursFindInsert(item, min, max);
            super.add(index, item);
            for(int i=1;i<this.size();i++){
                if(comparator.compare(this.get(i-1), this.get(i))==1)
                    System.out.println("");
            }
            return index;
        }
    }
    
    private int recursFindInsert(T item, int min, int max){
        if (max - min < 2) {
//            if (comparator.compare(this.get(min), item) == 1) {
//                return min;
//            } else if (comparator.compare(this.get(min), item) == -1) {
//                return max;
//            } else {
                return min + 1;
//            }
        } else {
            int mid = (max - min) / 2 + min;
            if (comparator.compare(this.get(mid), item) == 1) {
                return recursFindInsert(item, min, mid);
            } else if (comparator.compare(this.get(mid), item) == -1) {
                return recursFindInsert(item, mid, max);
            } else {
                return mid + 1;
            }
        }
    }

    @Override
    /**
     * Can add a collection of items to list, but will sort after addition
     */
    public boolean addAll(int i, Collection<? extends T> clctn) {
        boolean flag = super.addAll(i, clctn);
        this.sort();
        return flag;
    }

    @Override
    /**
     * Can add a collection of items to list, but will sort after addition
     */
    public boolean addAll(Collection<? extends T> clctn) {
        boolean flag = super.addAll(clctn);
        this.sort();
        return flag;
    }

    @Override
    /**
     * Will add item in index to maintain sorted list
     */
    public boolean add(T e) {
        binaryFindInsert(e,0,this.size()-1);
        return true;
    }

    @Override
    /**
     * Can add to end of list if order is maintained. Otherwise, item is not inserted.
     */
    public void addLast(T e) {
        flaggedAddLast(e);
    }
    
    /**
     * Adds to end of list if order is maintained. Boolean returned true if item was added. Else false;
     * @param e
     * @return 
     */
    private boolean flaggedAddLast(T e){
        int compare = comparator.compare(this.getLast(), e);
        if(compare!=1){
            super.addLast(e);
            return true;
        }
        return false;
    }

    @Override
    /**
     * Can add to head of list if order is maintained. Otherwise, item is not inserted.
     */
    public void addFirst(T e) {
        flaggedAddFirst(e);
    }
    
    /**
     * Adds to head of list if order is maintained. Boolean returned true if item was added. Else false;
     * @param e
     * @return 
     */
    private boolean flaggedAddFirst(T e){
        int compare = comparator.compare(this.getFirst(), e);
        if(compare!=-1){
            super.addFirst(e);
            return true;
        }
        return false;
    }
    
    
}
