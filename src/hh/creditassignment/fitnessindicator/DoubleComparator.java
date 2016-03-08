/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.creditassignment.fitnessindicator;

import java.util.Comparator;

/**
 *
 * @author SEAK2
 */
//comparator for doubles used to sort objectives
public class DoubleComparator implements Comparator<Double> {

    @Override
    public int compare(Double a, Double b) {
        return Double.compare(a, b);
    }
}
