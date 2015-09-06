/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition.offspringparent;

import hh.rewarddefinition.RewardDefinedOn;
import hh.rewarddefinition.fitnessindicator.IBinaryIndicator;
import java.util.ArrayList;
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
public class IBEABinaryIndicator extends AbstractOffspringParent {

    /**
     * IBEA scaling factor
     */
    private final double kappa;

    /**
     * Bounds used to rescale the objectives in the population
     */
    private double[] lowBound;
    private double[] upBound;

    /**
     * The maximum indicator value over all pair wise comparisons in population
     */
    private double maxIndicatorVal;

    /**
     * indicator values used to find maximum. Values are stored to quickly
     * updated the maximum value;
     */
    private ArrayList<ArrayList<Double>> indicatorVals;

    /**
     * The binary indicator to use
     */
    private final IBinaryIndicator indicator;

    /**
     * Solution indices that give maxIndicatorVal
     */
    private int[] maxPair = new int[]{0, 0};

    /**
     * Some indicators need a reference point like hypervolume and the R family
     * indicators
     */
    private final Solution refPoint;

    /**
     *
     * @param indicator The indicator to use
     * @param kappa the IBEA parameter to scale indicator values
     * @param refPoint
     */
    public IBEABinaryIndicator(IBinaryIndicator indicator, double kappa, Solution refPoint) {
        this.indicator = indicator;
        this.refPoint = refPoint;
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
    private double measureQuality(Solution soln1, Solution soln2) {
        Solution normSoln1 = new Solution(normalizeObjectives(soln1));
        Solution normSoln2 = new Solution(normalizeObjectives(soln2));
        Solution normRefPt = new Solution(normalizeObjectives(refPoint));
        return indicator.compute(normSoln1, normSoln2, normRefPt);
    }

    /**
     * This method compares the fitness of the offspring and its parent wrt to
     * the population.
     *
     * @param offspring
     * @param parent of the offspring solution
     * @param pop population is assumed to contain both the parent solution and
     * the offspring solution
     * @param removedSolution the solution index that was just removed from the
     * population
     * @return the positive percent improvement in fitness. If no improvement
     * then return 0.0
     */
    @Override
    public double compute(Solution offspring, Solution parent, Population pop, int removedSolution) {
        Population clonePop = new Population(pop);
        //only run on initial run.
        if (lowBound == null || upBound == null) {
            computeBounds(clonePop);
            maxIndicatorVal = maxIndicatorVal(clonePop);
        } else {
            //updates the bounds based on just the new solution
            updateBounds(offspring);
            if (removedSolution != -1) { //if this condition is met then the offspring replaced a solution in the population
                maxIndicatorVal = updateMaxVal(removedSolution, pop, offspring);
            }
        }
        if (removedSolution != -1) //if this condition is met then the offspring replaced a solution in the population
        {
            clonePop.remove(clonePop.size() - 1); //removes offspring from pop to compute its fitness
        }
        double offspringFitness = computeFitness(offspring, clonePop, maxIndicatorVal);

        //compute fitness of parent
        clonePop.remove(parent);
        clonePop.add(offspring);
        double parentFitness = computeFitness(parent, clonePop, maxIndicatorVal);

        double improvement = (offspringFitness - parentFitness) / parentFitness;
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
        indicatorVals = new ArrayList<>(pop.size());
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < pop.size(); i++) {
            indicatorVals.add(new ArrayList<Double>(pop.size()));
            for (int j = 0; j < pop.size(); j++) {
                if (i == j) {
                    indicatorVals.get(i).add(j, Double.NEGATIVE_INFINITY);
                } else {
                    double val = measureQuality(pop.get(i), pop.get(j));
                    indicatorVals.get(i).add(j, val);
                    if (max < val) {
                        maxPair[0] = i;
                        maxPair[1] = j;
                        max = val;
                    }
                }
            }
        }
        return max;
    }

    /**
     * Faster way to update the maximum indicator value with one new solution
     *
     * @param removedSolution the index of the solution that the new solution
     * replaced
     * @param pop the population including the offspring solution
     * @param offspring new solution added to the population
     * @return
     */
    private double updateMaxVal(int removedSolution, Population pop, Solution offspring) {
        indicatorVals.remove(removedSolution);
        //treat indicatorVals like a matrix of indicator values. Rows correspond to indicator value of A to B. Columns correspond to indicator value of B to A
        for (int i = 0; i < indicatorVals.size(); i++) {
            ArrayList<Double> row = indicatorVals.get(i);
            row.remove(removedSolution);
            row.add(measureQuality(pop.get(i), offspring));
        }
        indicatorVals.add(new ArrayList<Double>(pop.size()));
        for (int j = 0; j < pop.size() - 1; j++) {
            double val = measureQuality(offspring, pop.get(j));
            indicatorVals.get(pop.size() - 1).add(j, val);
        }
        //indicator val of A to A (itself) is negative infinity
        indicatorVals.get(pop.size() - 1).add(pop.size() - 1, Double.NEGATIVE_INFINITY);

        // only look for max indicator value if one of the solutions in the previous pair is no longer in the population
        if (removedSolution == maxPair[0] || removedSolution == maxPair[1]) {
            double max = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < pop.size(); i++) {
                for (int j = 0; j < pop.size(); j++) {
                    double val = indicatorVals.get(i).get(j);
                    if (max < val) {
                        maxPair[0] = i;
                        maxPair[1] = j;
                        max = val;
                    }
                }
            }
            maxIndicatorVal = max;
        }
        return maxIndicatorVal;
    }

    @Override
    public String toString() {
        return "OPa_" + indicator.toString() + operatesOn;
    }

}
