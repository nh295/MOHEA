/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditdefinition.immediate;

import hh.creditdefinition.PopulationBasedCredit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * This credit definition gives credit if the specified solution lies on the 
 * Pareto front. Credit is only assigned to the specified solution
 * @author Nozomi
 */
public class ImmediateParetoFrontCredit extends PopulationBasedCredit {

    /**
     * Credit received if a new solution is nondominated with respect to the population
     */
    protected final double creditNonDominated;
    
    /**
     * Credit received if a new solution is dominated with respect to the population
     */
    protected final double creditDominated;


    /**
     * Constructor to specify the credits that are assigned when a solution is 
     * nondominated or dominated with respect to the given population
     * @param creditNonDominated credit to assign when solution is nondominated with respect to the given population
     * @param creditDominated credit to assign when solution is dominated with respect to the given population
     */
    public ImmediateParetoFrontCredit(double creditNonDominated,double creditDominated) {
        this.creditDominated = creditDominated;
        this.creditNonDominated = creditNonDominated;
    }
    
    /**
     * Computes the credit of an offspring solution with respect to the population
     * @param offspring solution that will receive credits
     * @param population the population to compare the offspring solutions with
     * @param heuristic that created offspring solution
     * @return the value of credit to resulting from the solution
     */
    @Override
    public double compute(Solution offspring, Population population,Variation heuristic) {
        NondominatedPopulation ndpop;
        ndpop = new NondominatedPopulation();
        ndpop.addAll(population);
        
        if(ndpop.add(offspring))
            return creditNonDominated;
        else
            return creditDominated;
    }
    
    /**
     * Computes the credit of an offspring solution with respect to the population
     * @param offsprings a list of offspring solutions that will receive credits
     * @param population the population to compare the offspring solutions with
     * @param heuristic that created offspring solution
     * @return the value of credit to resulting from the solution
     */
    @Override
    public List<Double> computeAll(Solution[] offsprings, Population population,Variation heuristic) {
        List offspringList = Arrays.asList(offsprings);
        Collections.shuffle(offspringList);
        
        NondominatedPopulation ndpop = new NondominatedPopulation(population);
        
        Iterator<Solution> iter = offspringList.iterator();
        ArrayList<Double> creditvals = new ArrayList();
        while(iter.hasNext()){
            if(ndpop.add(iter.next()))
                creditvals.add(creditNonDominated);
            else
                creditvals.add(creditDominated);
        }
        return creditvals;
    }
    
    /**
     * Gets the credit assigned to solutions that are non dominated with respect to the population
     * @return the credit assigned to solutions that are non dominated with respect to the population
     */
    public double getCreditNonDominated() {
        return creditNonDominated;
    }
    
    /**
     * Gets the credit assigned to solutions that are dominated with respect to the population
     * @return the credit assigned to solutions that are dominated with respect to the population
     */
    public double getCreditDominated() {
        return creditDominated;
    }
    
    @Override
    public String toString() {
        return "ImmediateParetoFrontCredit";
    }

    @Override
    public boolean isImmediate() {
        return true;
    }
}
