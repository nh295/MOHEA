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
        return Math.max((offspringFit - parentFit)/parentFit, 0.0);
//        double cred = Math.max(hvFitnessEvaluator.calculateIndicator(parent, offspring),0.0);
//        return cred;
    }

    @Override
    public String toString() {
        return "OP-I";
    }
}
