/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.creditassignment.offspringpopulation;

import hh.creditassigment.CreditFunctionInputType;
import hh.creditassigment.CreditFitnessFunctionType;
import hh.creditassigment.CreditDefinedOn;
import hh.creditassignment.fitnessindicator.IIndicator;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

/**
 * This credit definition gives credit if the specified solution improves the
 * mean fitness value of a solution set
 *
 * @author Nozomi
 */
public class IndicatorImprovement extends AbstractOffspringPopulation {
    
    private final IIndicator indicator;
    
    /**
     * Constructor for indicator based set improvement credit assignment
     * @param indicator
     */
    public IndicatorImprovement(IIndicator indicator) {
        operatesOn = CreditDefinedOn.POPULATION;
        inputType = CreditFunctionInputType.SI;
        fitType = CreditFitnessFunctionType.I;
        this.indicator = indicator;
    }

    /**
     * Assumes that the offspring is the last index in the population. Returns
     * the difference between the mean fitness of the population and the
     * offspring fitness. If it is negative, it returns 0
     *
     * @param offspring solution that will receive credits
     * @param population the population to compare the offspring solutions with
     * @return the value of credit to resulting from the solution
     */
    @Override
    public double compute(Solution offspring, Population population) {
        return indicator.computeContribution(population, offspring);
    }

    @Override
    public String toString() {
        return "SI-I";
    }
}
