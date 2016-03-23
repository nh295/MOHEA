/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.moea;

import java.util.Collection;
import java.util.Iterator;
import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import static org.moeaframework.core.FastNondominatedSorting.CROWDING_ATTRIBUTE;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.ParallelPRNG;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.ChainedComparator;
import org.moeaframework.core.comparator.CrowdingComparator;
import org.moeaframework.core.comparator.RankComparator;
import org.moeaframework.core.operator.TournamentSelection;

/**
 * Steady state NSGAII uses Efficient Nondomination Level Update from Li, Ke,
 * Kalyanmoy Deb, Qingfu Zhang, Senior Member, and Qiang Zhang. 2015. “Efficient
 * Non-Domination Level Update Method for Steady-State Evolutionary
 * Multi-Objective Optimization.” COIN Report Number 2015022
 *
 * @author nozomihitomi
 */
public class SteadyStateNSGAII extends NSGAII {

    protected SteadyStateFastNonDominatedSorting enlu;

    private ParallelPRNG pprng;

    public SteadyStateNSGAII(Problem problem, NondominatedSortingPopulation population,
            EpsilonBoxDominanceArchive archive, Selection selection, Variation variation, Initialization initialization) {
        super(problem, population, archive,
                selection, variation, initialization);
        pprng = new ParallelPRNG();

    }

    @Override
    protected void initialize() {
        super.initialize();

        // rank the solutions
        enlu = new SteadyStateFastNonDominatedSorting();
        enlu.evaluate(population);
    }

    @Override
    public void iterate() {
        Population offspring = new Population();

        Solution[] parents = selection.select(variation.getArity(),
                population);
        Solution[] children = variation.evolve(parents);

        offspring.addAll(children);

        evaluateAll(offspring);

        for (Solution child : offspring) {
            enlu.addSolution(child, population);
            int removeIndex = findWorstSolution();
            population.remove(removeIndex);
        }
    }

    /**
     * Gets the solution to remove from the population. The solution that is the
     * most crowded in the last front will be removed
     */
    protected int findWorstSolution() {
        int removeIndex = -1;
        Collection<Integer> lastFront = enlu.getLastFront();
        if (lastFront.size() <= 2) {
            Iterator<Integer> iter = lastFront.iterator();
            int rand = pprng.nextInt(lastFront.size());
            for (int i = 0; i < lastFront.size(); i++) {
                int index = iter.next();
                if (i == rand) {
                    removeIndex = index;
                }
            }
        } else {
            Population lastFrontPop = new Population();
            for (Integer index : enlu.getLastFront()) {
                lastFrontPop.add(population.get(index));
            }
            enlu.updateCrowdingDistance(lastFrontPop);

            //find solution with lowest crowding distance
            double minCrowdingDist = Double.POSITIVE_INFINITY;
            for (Integer index : enlu.getLastFront()) {
                double dist = (double) population.get(index).getAttribute(CROWDING_ATTRIBUTE);
                if (dist < minCrowdingDist) {
                    minCrowdingDist = dist;
                    removeIndex = index;
                }
            }
        }
        return removeIndex;
    }
}
