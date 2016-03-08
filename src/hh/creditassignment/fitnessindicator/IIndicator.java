/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.creditassignment.fitnessindicator;

import java.util.ArrayList;
import java.util.List;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

/**
 * Used for binary indicators
 *
 * @author nozomihitomi
 */
public interface IIndicator {

    /**
     * Computes the contributions of each of the solutions in the nondominated
     * population. Returns a list of contributions in the order of the
     * population's index from get() method
     *
     * @param popA
     * @param refPt some indicators require a reference point
     * @return a list of contributions in the order of the population's Iterator
     */
    public List<Double> computeContributions(NondominatedPopulation popA, Solution refPt);

    /**
     * Computes the indicator value when comparing solution A to solution B.
     * Used in IBEA setting
     *
     * @param solnA
     * @param solnB
     * @param refPt some indicators require a reference point
     * @return
     */
    public double compute(Solution solnA, Solution solnB, Solution refPt);

    /**
     * Computes the contributions of specified solution to the nondominated
     * population if inserted. It is assumed that the solution has already been
     * inserted into the population. Returns the contribution of the offspring
     * to the indicator value
     *
     * @param pop the population with the solution of interest. objectives are
     * normalized
     * @param offspring the offspring just inserted into the population.
     * objectives are normalized
     * @param refPt some indicators require a reference point
     * @return
     */
    public double computeContribution(NondominatedPopulation pop, Solution offspring, Solution refPt);

}
