/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition.offspringparent;

import hh.rewarddefinition.RewardDefinedOn;
import hh.rewarddefinition.fitnessindicator.IBinaryIndicator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

/**
 * These indicators take the form of the indicators used in IBEA. It uses the
 * population to compute the fitness of individuals via quality indicators. It
 * does not compute the quality of the population
 *
 * Indicator fitness calculation based on Zitzler, E., & Simon, K. (2004).
 * Indicator-Based Selection in Multiobjective Search. 8th International
 * Conference on Parallel Problem Solving from Nature (PPSN VIII), 832–842.
 * doi:10.1007/978-3-540-30217-9_84
 *
 * @author nozomihitomi
 */
public class IBEABinaryIndicator extends AbstractOffspringParent{

    /**
     * IBEA scaling factor
     */
    private final double kappa;


    /**
     * Bounds used to rescale the objectives in the population
     */
    private double[] lowBound;
    private double[] upBound;
    
    private final IBinaryIndicator indicator;

    /**
     * 
     * @param indicator The indicator to use
     * @param kappa the IBEA parameter to scale indicator values
     */
    public IBEABinaryIndicator(IBinaryIndicator indicator,double kappa) {
        this.indicator = indicator;
        this.kappa = kappa;
        //has to be the population because parent may not lie on PF or in archive
        this.operatesOn = RewardDefinedOn.POPULATION;
    }

    /**
     * Computes the solution's fitness like in IBEA. Like 'loss' in quality if
     * solution is not in population
     *
     * @param soln
     * @param pop
     * @param c the max indicator value over all pairwise comparisons in the
     * population
     * @return
     */
    private double computeFitness(Solution soln, Population pop, double c) {
        double fitness = 0.0;
        for (Solution solution : pop) {
            fitness += -Math.exp(-measureQuality(solution, soln) / (c * kappa));
        }
        return fitness;
    }

    /**
     * This method measures the fitness of a solution if soln2 is the solution
     * removed from the population
     *
     * @param soln1 the solution from the population
     * @param soln2 the solution removed from the population
     * @return
     */
    private double measureQuality(Solution soln1, Solution soln2){
        Solution normSoln1 = new Solution(normalizeObjectives(soln1));
        Solution normSoln2 = new Solution(normalizeObjectives(soln2));
        NondominatedPopulation popA = new NondominatedPopulation();
        popA.add(normSoln1);
        NondominatedPopulation popB = new NondominatedPopulation();
        popB.add(normSoln2);
        return indicator.compute(popA, popB);
    }
    
    /**
     * This method compares the fitness of the offspring and its parent wrt to
     * the population.
     *
     * @param offspring
     * @param parent of the offspring solution
     * @param pop population is assumed to contain the parent solution but not
     * the offspring solution
     * @return the positive percent improvement in fitness. If no improvement then return 0.0
     */
    @Override
    public double compute(Solution offspring, Solution parent, Population pop) {   
        if (lowBound == null || upBound == null) {
            computeBounds(pop);
        }
        updateBounds(offspring);
        
        //compute fitness of offspring
        pop.add(offspring);
        double maxIVal = maxIndicatorVal(pop);
        pop.remove(pop.size() - 1); //remove offspring that was just added
        double offspringFitness = computeFitness(offspring, pop, maxIVal);

        //compute fitness of parent
        pop.remove(parent);
        pop.add(offspring);
        double parentFitness = computeFitness(parent, pop, maxIVal);
        pop.add(parent);

        double improvement = (offspringFitness-parentFitness)/parentFitness;
        if (improvement > 0) {
            return improvement;
        } else {
            return 0.0;
        }
    }

    /**
     * Computes the lower and upper bounds on each of the objectives for the
     * solutions in the population
     *
     * @param pop
     */
    private void computeBounds(Population pop) {
        int numObj = pop.get(0).getNumberOfObjectives();
        lowBound = new double[numObj];
        upBound = new double[numObj];
        for (int i = 0; i < numObj; i++) {
            lowBound[i] = Double.POSITIVE_INFINITY;
            upBound[i] = Double.NEGATIVE_INFINITY;
        }
        for (Solution solution : pop) {
            updateBounds(solution);
        }
    }

    /**
     * Updates the lower and upper bounds on each of the objectives of the
     * population based on the insertion of a new solution
     *
     * @param soln new solution entering the population
     */
    private void updateBounds(Solution soln) {
        for (int i = 0; i < soln.getNumberOfObjectives(); i++) {
            upBound[i] = Math.max(upBound[i], soln.getObjective(i));
            lowBound[i] = Math.min(lowBound[i], soln.getObjective(i));

        }
    }

    /**
     * Normalizes the objectives to the solution based on the upper and lower
     * bounds of the objectives of the solutions in the population
     *
     * @param solution
     * @return
     */
    private double[] normalizeObjectives(Solution solution) {
        double[] normalizedObjs = new double[solution.getNumberOfObjectives()];
        for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
            normalizedObjs[i] = (solution.getObjective(i) - lowBound[i]) / (upBound[i] - lowBound[i]);
        }
        return normalizedObjs;
    }

    /**
     * Compute maximum indicator value
     *
     * @param pop from which to obtain the maximum indicator value
     * @return the maximum indicator value
     */
    private double maxIndicatorVal(Population pop) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < pop.size(); i++) {
            for (int j = 0; j < pop.size(); j++) {
                if (i == j) {
                    continue;
                }
                max = Math.max(max, measureQuality(pop.get(i), pop.get(j)));
            }
        }
        return max;
    }

    @Override
    public String toString() {
        return "OPa_"+indicator.toString() + operatesOn;
    }

    
}
