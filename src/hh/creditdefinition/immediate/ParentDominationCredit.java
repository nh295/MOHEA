/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditdefinition.immediate;

import hh.creditdefinition.ParentBasedCredit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;

/**
 * This credit definition compares offspring to its parents
 * @author Nozomi
 */
public class ParentDominationCredit extends ParentBasedCredit{
    /**
     * Credit that is assigned if the offspring dominates parent
     */
    private final double creditOffspringDominates;
    
    /**
     * Credit that is assigned if the parent dominates offspring
     */
    private final double creditParentDominates;
    
    /**
     * Credit that is assigned if neither the offspring or parent dominates the other
     */
    private final double creditNoOneDominates;
    
    /**
     * The type of dominance comparator to be used
     */
    private final DominanceComparator comparator;
    
    /**
     * Constructor to specify the types of credits that will be assigned. A default dominance comparator will be used: ParetoDominanceComparator
     * @param creditOffspringDominates Credit that is assigned if the offspring dominates parent
     * @param creditParentDominates Credit that is assigned if the parent dominates offspring
     * @param creditNoOneDominates Credit that is assigned if neither the offspring or parent dominates the other
     */
    public ParentDominationCredit(double creditOffspringDominates, double creditNoOneDominates, double creditParentDominates) {
        this.creditOffspringDominates = creditOffspringDominates;
        this.creditParentDominates = creditParentDominates;
        this.creditNoOneDominates = creditNoOneDominates;
        this.comparator = new ParetoDominanceComparator();
    }
    
    /**
     * Constructor to specify the types of credits that will be assigned and the dominance comparator to be used
     * @param creditOffspringDominates Credit that is assigned if the offspring dominates parent
     * @param creditParentDominates Credit that is assigned if the parent dominates offspring
     * @param creditNoOneDominates Credit that is assigned if neither the offspring or parent dominates the other
     * @param comparator the comparator to be used that defines dominance
     */
    public ParentDominationCredit(double creditOffspringDominates, double creditNoOneDominates, double creditParentDominates,DominanceComparator comparator) {
        this.creditOffspringDominates = creditOffspringDominates;
        this.creditParentDominates = creditParentDominates;
        this.creditNoOneDominates = creditNoOneDominates;
        this.comparator = comparator;
    }

    /**
     * Computes the credit of an offspring solution with respect to multiple 
     * parents. Can be used if a heuristic produces one offspring solution. 
     * Finds the parent that is non-dominated to compare offspring solution. If 
     * there are multiple non-dominated parents, a random non-dominated parent
     * is selected.
     * @param offspring offspring solutions that will receive credits
     * @param parents the parent solutions to compare the offspring solutions with
     * @param heuristic that created offspring solution
     * @return the value of credit to resulting from the solution
     */
    @Override
    public double compute(Solution offspring, Solution[] parents,Variation heuristic) {
        NondominatedPopulation parentPop = new NondominatedPopulation(comparator);
        parentPop.addAll(parents);
        int select = PRNG.nextInt(0, parentPop.size()-1);
        Solution refParent = parentPop.get(select);
        
        
        switch(comparator.compare(refParent, offspring)){
            case -1: 
                return creditParentDominates;
            case 0: 
                return creditNoOneDominates;
            case 1: 
                return creditOffspringDominates;
            default: throw new Error("Comparator returned invalid value: " + comparator.compare(refParent, offspring));
        }
    }

    /**
     * Computes the credit of an offspring solution with respect to multiple 
     * parents. Can be used if a heuristic produces more than one offspring 
     * solution. Finds the parent that is non-dominated to compare offspring 
     * solution. If there are multiple non-dominated parents, a random 
     * non-dominated parent is selected. All offspring solutions are compared 
     * with the selected parent solution
     * @param offsprings a list of offspring solutions that will receive credits
     * @param parents the parent solutions to compare the offspring solutions with
     * @param heuristic that created offspring solution
     * @return the value of credit to resulting from the solution
     */
    @Override
    public List<Double> computeAll(Solution[] offsprings, Solution[] parents,Variation heuristic) {
        NondominatedPopulation parentPop = new NondominatedPopulation(comparator);
        parentPop.addAll(parents);
        int select = PRNG.nextInt(0, parentPop.size()-1);
        Solution refParent = parentPop.get(select);
        
        ArrayList<Double> credits = new ArrayList();
        for(Solution soln:offsprings){
            int dom = comparator.compare(refParent, soln);
            switch(dom){
                case -1:
                    credits.add(creditParentDominates);
                    break;
                case 0:
                    credits.add(creditNoOneDominates);
                    break;
                case 1:
                    credits.add(creditOffspringDominates);
                    break;
                default: throw new Error("Comparator returned invalid value: " + dom);
            }
        }
        return credits;
    }
    
    /**
     * Returns the credit defined for when the offspring solution dominates the 
     * parent solution
     * @return the credit defined for when the offspring solution dominates the 
     * parent solution
     */
    public double getCreditOffspringDominates() {
        return creditOffspringDominates;
    }

    /**
     * Returns the credit defined for when the parent solution dominates the 
     * offspring solution
     * @return the credit defined for when the parent solution dominates the 
     * offspring solution
     */
    public double getCreditParentDominates() {
        return creditParentDominates;
    }

    /**
     * Returns the credit defined for when neither the offspring solution nor the 
     * parent solution dominates the other
     * @return the credit defined for when neither the offspring solution nor the 
     * parent solution dominates the other
     */
    public double getCreditNoOneDominates() {
        return creditNoOneDominates;
    }
    
    @Override
    public String toString() {
        return "ParentDominationCredit";
    }

    @Override
    public boolean isImmediate() {
       return true;
    }

}
