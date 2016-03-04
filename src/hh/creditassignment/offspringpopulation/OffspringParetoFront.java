/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditassignment.offspringpopulation;

import hh.creditassigment.CreditFunctionInputType;
import hh.creditassigment.CreditFitnessFunctionType;
import hh.creditassigment.CreditDefinedOn;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

/**
 * This credit definition gives credit if the specified solution lies on the 
 * Pareto front. Credit is only assigned to the specified solution
 * @author Nozomi
 */
public class OffspringParetoFront extends AbstractOffspringPopulation{

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
    public OffspringParetoFront(double creditNonDominated,double creditDominated) {
        operatesOn = CreditDefinedOn.PARETOFRONT;
        inputType = CreditFunctionInputType.SI;
        fitType = CreditFitnessFunctionType.Do;
        this.creditDominated = creditDominated;
        this.creditNonDominated = creditNonDominated;
    }
    
    /**
     * Adds the offspring solution to the Pareto front to see if the Pareto front changes. If it changes, the heuristic will receive a reward
     * @param offspring solution that will receive credits
     * @param population the population to compare the offspring solutions with
     * @return the value of credit to resulting from the solution
     */
    @Override
    public double compute(Solution offspring, Population population) {
        if(population.getClass()!=NondominatedPopulation.class)
            throw new ClassCastException("Need to be NondominatedPopulation: " + population.getClass());
        NondominatedPopulation ndpop = (NondominatedPopulation)population;
        if(ndpop.add(offspring))
            return creditNonDominated;
        else
            return creditDominated;
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
        return "SI-PF";
    }
}
