/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition.offspringparent;

import hh.rewarddefinition.FitnessFunctionType;
import hh.rewarddefinition.RewardDefinedOn;
import hh.rewarddefinition.fitnessindicator.HypervolumeIndicator;
import hh.rewarddefinition.fitnessindicator.IIndicator;
import hh.rewarddefinition.fitnessindicator.R2Indicator;
import java.util.Arrays;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.ParetoDominanceComparator;

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
public class OPBinaryIndicator extends AbstractOffspringParent {

    /**
     * IBEA scaling factor
     */
    private final double kappa;

    /**
     * The binary indicator to use
     */
    private final IIndicator indicator;

    /**
     * Reference point for indicators
     */
    private final Solution refPoint;

    /**
     *
     * @param indicator The indicator to use
     * @param kappa the IBEA parameter to scale indicator values
     * @param prob
     */
    public OPBinaryIndicator(IIndicator indicator, double kappa, Problem prob) {
        this.indicator = indicator;
        this.kappa = kappa;
        //has to be the population because parent may not lie on PF or in archive
        this.operatesOn = RewardDefinedOn.PARENT;
        this.fitType = FitnessFunctionType.R2;

        if (indicator.getClass().equals(HypervolumeIndicator.class)) {
            double[] hvRefPoint = new double[prob.getNumberOfObjectives()];
            Arrays.fill(hvRefPoint, 2.0);
            refPoint = new Solution(hvRefPoint);
        }else if(indicator.getClass().equals(R2Indicator.class)){
            double[] r2RefPoint = new double[prob.getNumberOfObjectives()];
            Arrays.fill(r2RefPoint, -1.0); //since everything is normalized, utopia point is 0 vector
            refPoint = new Solution(r2RefPoint);
        }else
            refPoint = null;
    }

    /**
     * This method measures the fitness of a solution if soln2 is the solution
     * removed from the population
     *
     * @param soln1 the solution from the population
     * @param soln2 the solution removed from the population
     * @return
     */
    private double indicatorVal(Solution soln1, Solution soln2) {
        Solution normSoln1 = new Solution(normalizeObjectives(soln1));
        Solution normSoln2 = new Solution(normalizeObjectives(soln2));
        return indicator.compute(normSoln1, normSoln2, refPoint);
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
    public double compute(Solution offspring, Solution parent, Population pop, Solution removedSolution) {

//only run on initial run.
        if (sortedObjs == null) {
            computeBounds(pop);
        } else {

            //updates the bounds based on just the new incoming solution and the removed solution
            updateBounds(offspring, removedSolution);

        }

        //compute fitness of parent and offspring
//        double parentFitness = computeFitness(parent, pop);
//        double offspringFitness = computeFitness(parent, pop);
//
//        double improvement = (offspringFitness - parentFitness) / parentFitness;
        double improvement = indicatorVal(parent, offspring);
        if (improvement > 0) {
            return improvement;
        } else {
            return 0.0;
        }
    }

    @Override
    public String toString() {
        return "OP-" + indicator.toString() + operatesOn;
    }

}
