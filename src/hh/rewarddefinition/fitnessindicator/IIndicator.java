/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition.fitnessindicator;

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
     * Computes the contributions of specified solution to the nondominated
     * population if inserted. It is assumed that the solution has not already
     * been inserted into the population Returns the contributions in the order
     * of the population's Iterator
     *
     * @param pop the population with the solution of interest
     * @param popWOSolution the without the solution of interest
     * @param refPt some indicators require a reference point
     * @return
     */
    public double computeContribution(NondominatedPopulation pop, NondominatedPopulation popWOSolution, Solution refPt);
    
    /**
     * Computes the contributions of specified solution to the nondominated
     * population if inserted. It is assumed that the solution has not already
     * been inserted into the population Returns the contributions in the order
     * of the population's Iterator
     *
     * @param oldPopIndicatorVal the indicator value for the population without the solution of interest (previous population)
     * @param popWSolution the with the solution of interest
     * @param refPt some indicators require a reference point
     * @return
     */
    public double computeContribution( NondominatedPopulation popWSolution,double oldPopIndicatorVal, Solution refPt);

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

}
