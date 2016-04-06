/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.creditassignment.offspringparent;

import hh.creditassigment.CreditDefinedOn;
import hh.creditassigment.CreditFitnessFunctionType;
import org.moeaframework.core.FitnessEvaluator;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.fitness.HypervolumeFitnessEvaluator;

/**
 * This credit assignment strategies compares the offspring indicator-based
 * fitness to that of its parents
 *
 * @author nozomihitomi
 */
public class ParentIndicator extends AbstractOffspringParent {
    
    private HypervolumeFitnessEvaluator hvFitnessEvaluator;

    public ParentIndicator(Problem problem) {
        super();
        operatesOn = CreditDefinedOn.PARENT;
        fitType = CreditFitnessFunctionType.I;
        this.hvFitnessEvaluator = new HypervolumeFitnessEvaluator(problem);
    }
    

    /**
     * The offspring vs parent indicator-based credit assignment assigns the
     * difference between the offspring fitness over its parent. If it is
     * negative, it returns zero.
     *
     * @param offspring
     * @param parent
     * @param pop
     * @param removedSolution
     * @return
     */
    @Override
    public double compute(Solution offspring, Solution parent, Population pop, Solution removedSolution) {
        double offspringFit = (double) offspring.getAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE);
        double parentFit = (double) parent.getAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE);
        double minFitness = Double.POSITIVE_INFINITY;
        double maxFitness = Double.NEGATIVE_INFINITY;
        
        //find sum of the fitness minus the offspring
//        for (int i = 0; i < pop.size() - 1; i++) {
//            double fitnessval = (double) pop.get(i).getAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE);
//            minFitness = Math.min(minFitness, fitnessval);
//            maxFitness = Math.max(maxFitness, fitnessval);
//        }
//        double cred = Math.max((parentFit - offspringFit)/(parentFit), 0.0);
//        return cred;
        double hv1 = hvFitnessEvaluator.calculateIndicator(parent, offspring);
        double hv2 = hvFitnessEvaluator.calculateIndicator(offspring, parent);
        return Math.max((hv1-hv2)/hv1, 0.0);
    }

    @Override
    public String toString() {
        return "OP-I";
    }
}
