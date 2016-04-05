/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.creditassignment.populationcontribution;

import hh.hyperheuristics.SerializableVal;
import hh.creditassigment.CreditFunctionInputType;
import hh.creditassigment.CreditFitnessFunctionType;
import hh.creditassigment.Credit;
import hh.creditassigment.CreditDefinedOn;
import java.util.Collection;
import java.util.HashMap;
import org.moeaframework.core.FitnessEvaluator;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * This credit assignment rewards an operator proportional to the sum of the
 * fitness values of the solutions it has created in the set. The credit values
 * are normalized such that the lowest credit received by an operator is 0 while
 * the highest is 1.0
 *
 * @author SEAK2
 */
public class IndicatorContribution extends AbstractPopulationContribution {

    /**
     *
     * @param operatesOn Enum to specify whether to compare the improvement on
     * the population or the archive
     */
    public IndicatorContribution(CreditDefinedOn operatesOn) {
        this.operatesOn = operatesOn;
        this.fitType = CreditFitnessFunctionType.I;
        this.inputType = CreditFunctionInputType.CS;
    }

    /**
     * @param population
     * @param heuristics
     * @param iteration
     * @return
     */
    @Override
    public HashMap<Variation, Credit> compute(Population population,
            Collection<Variation> heuristics, int iteration) {

        HashMap<String, Double> creditVals = new HashMap<>();
        for (Variation operator : heuristics) {
            creditVals.put(operator.toString(), 0.0);
        }

        double minCredit = Double.POSITIVE_INFINITY;
        double maxCredit = Double.NEGATIVE_INFINITY;
        for (Solution soln : population) {
            double fitness = (double) soln.getAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE);
            if (soln.hasAttribute("heuristic")) {
                String operator = ((SerializableVal) soln.getAttribute("heuristic")).getSval();
                creditVals.put(operator, creditVals.get(operator) + fitness);
            }
        }
        
        for (Variation heuristic : heuristics) {
            minCredit = Math.min(minCredit, creditVals.get(heuristic.toString()));
            maxCredit = Math.max(maxCredit, creditVals.get(heuristic.toString()));
        }

        //normalize the reward values
        HashMap<Variation, Credit> rewards = new HashMap();
        for (Variation heuristic : heuristics) {
            double normalizedCredit = Math.max(0.0,(creditVals.get(heuristic.toString()) - minCredit) / (maxCredit - minCredit));
            rewards.put(heuristic, new Credit(iteration, normalizedCredit));
        }
        return rewards;
    }

    @Override
    public String toString() {
        return "CS-I-" + operatesOn;
    }

}
