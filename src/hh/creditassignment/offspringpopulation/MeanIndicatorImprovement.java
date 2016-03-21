/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.creditassignment.offspringpopulation;

import hh.creditassigment.CreditFunctionInputType;
import hh.creditassigment.CreditFitnessFunctionType;
import hh.creditassigment.CreditDefinedOn;
import org.moeaframework.core.FitnessEvaluator;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

/**
 * This credit definition gives credit if the specified solution improves the
 * mean fitness value of a solution set
 *
 * @author Nozomi
 */
public class MeanIndicatorImprovement extends AbstractOffspringPopulation {

    /**
     * Constructor for indicator based set improvement credit assignment
     */
    public MeanIndicatorImprovement() {
        operatesOn = CreditDefinedOn.PARETOFRONT;
        inputType = CreditFunctionInputType.SI;
        fitType = CreditFitnessFunctionType.I;
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
        double sum = 0;
        //find sum of the fitness minus the offspring
        for (int i = 0; i < population.size() - 1; i++) {
            sum += (double) population.get(i).getAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE);
        }
        double mean = sum / (population.size() - 1);
        double offspringFit = (double) offspring.getAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE);
        return Math.max((offspringFit - mean)/mean, 0.0);
    }

    @Override
    public String toString() {
        return "SI-I-Pop";
    }
}
