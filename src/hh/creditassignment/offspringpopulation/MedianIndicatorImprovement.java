/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.creditassignment.offspringpopulation;

import hh.creditassigment.CreditFunctionInputType;
import hh.creditassigment.CreditFitnessFunctionType;
import hh.creditassigment.CreditDefinedOn;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.moeaframework.core.FitnessEvaluator;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

/**
 * This credit definition gives credit if the specified solution improves the
 * mean fitness value of a solution set
 *
 * @author Nozomi
 */
public class MedianIndicatorImprovement extends AbstractOffspringPopulation {
    
    private final Percentile medianCompute;

    /**
     * Constructor for indicator based set improvement credit assignment
     */
    public MedianIndicatorImprovement() {
        operatesOn = CreditDefinedOn.POPULATION;
        inputType = CreditFunctionInputType.SI;
        fitType = CreditFitnessFunctionType.I;
        medianCompute = new Percentile(50.0);
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
        
        double[] fitnessvals = new double[population.size()];
        //find sum of the fitness minus the offspring
        for (int i = 0; i < population.size() - 1; i++) {
            fitnessvals[i] = (double) population.get(i).getAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE);
        }
        double median = medianCompute.evaluate(fitnessvals, 50.0);
        double offspringFit = (double) offspring.getAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE);
        return Math.max((offspringFit - median)/median, 0.0);
    }

    @Override
    public String toString() {
        return "SI-I-Pop";
    }
}
