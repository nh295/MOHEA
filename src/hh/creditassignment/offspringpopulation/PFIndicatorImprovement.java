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
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.indicator.Normalizer;
import org.moeaframework.core.indicator.jmetal.FastHypervolume;

/**
 * This credit definition gives credit if the specified solution improves the
 * mean fitness value of a solution set
 *
 * @author Nozomi
 */
public class PFIndicatorImprovement extends AbstractOffspringPopulation {
    
    private final Problem problem;
    
    private final FastHypervolume hvComputer;
    
    /**
     * Constructor for indicator based set improvement credit assignment
     */
    public PFIndicatorImprovement(Problem problem) {
        operatesOn = CreditDefinedOn.POPULATION;
        inputType = CreditFunctionInputType.SI;
        fitType = CreditFitnessFunctionType.I;
        this.problem = problem;
        Solution ref = problem.newSolution();
        Solution temp1 = problem.newSolution();
        for(int i=0; i<problem.getNumberOfObjectives(); i++){
            ref.setObjective(i,2.0);
            temp1.setObjective(i,0.0);
        }
        NondominatedPopulation refSet = new NondominatedPopulation();
        Solution temp2 = temp1.copy();
        temp1.setObjective(0, 1.0);
        temp2.setObjective(1, 1.0);
        refSet.add(temp1);
        refSet.add(temp2);
        this.hvComputer = new FastHypervolume(problem, refSet , ref);
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
        
        NondominatedPopulation ndpop = (NondominatedPopulation)population;
        NondominatedPopulation beforeAdding = new NondominatedPopulation(ndpop);
        if(ndpop.add(offspring)){
        Normalizer normalizer = new Normalizer(problem, ndpop);
        Population normalizedPopulation = normalizer.normalize(population);
        NondominatedPopulation normndpop = new NondominatedPopulation(normalizedPopulation);
        Population normalizedBeforePopulation = normalizer.normalize(beforeAdding);
        NondominatedPopulation normBeforendpop = new NondominatedPopulation(normalizedBeforePopulation);
        double hv = hvComputer.evaluate(normndpop);
        double beforehv = hvComputer.evaluate(normBeforendpop);
            return Math.max(0.0,hv-beforehv);
        }else
            return 0.0;
        
    }

    @Override
    public String toString() {
        return "SI-I";
    }
}
