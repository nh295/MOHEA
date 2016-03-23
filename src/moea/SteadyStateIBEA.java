/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moea;

import org.moeaframework.algorithm.IBEA;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.fitness.IndicatorFitnessEvaluator;

/**
 *
 * @author nozomihitomi
 */
public class SteadyStateIBEA extends IBEA {

    public SteadyStateIBEA(Problem problem, NondominatedPopulation archive, Initialization initialization, Variation variation, IndicatorFitnessEvaluator fitnessEvaluator) {
        super(problem, archive, initialization, variation, fitnessEvaluator);
    }

    @Override
    protected void iterate() {
        Population offspring = new Population();

        Solution[] parents = selection.select(variation.getArity(), population);
        Solution[] children = variation.evolve(parents);

        offspring.addAll(children);

        evaluateAll(offspring);
        for(Solution child : offspring){
            population.add(child);
            fitnessEvaluator.evaluate(population);
            
            int worstIndex = findWorstIndex();
            fitnessEvaluator.removeAndUpdate(population, worstIndex);
        }
    }

}
