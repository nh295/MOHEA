/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moea;

import hh.creditassignment.fitnessindicator.DoubleComparator;
import hh.creditassignment.fitnessindicator.SortedLinkedList;
import java.util.ArrayList;
import java.util.Comparator;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.ParallelPRNG;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.fitness.IndicatorFitnessEvaluator;

/**
 * R2 MOEA is steady state indicator based algorithm developed by Diaz et al. It
 * is an indicator based algorithm that uses nondominated sorting, then R2
 * contribution to achieve a total order of the individuals in the population
 *
 *
 * References:
 * <p>
 * <ol>
 * <li>
 * "A ranking method based on the R2 indicator for many-objective optimization"
 * 2013 IEEE Congress on Evolutionary Computation
 * </ol>
 *
 * @author SEAK2
 */
public class R2MOEA extends AbstractEvolutionaryAlgorithm {

    /**
     * The r2rank comparator.
     */
    private R2RankComparator rankComparator;
    
    /**
     * The r2contribution comparator.
     */
    private R2ContributionComparator contributionComparator;

    /**
     * The variation operator.
     */
    private Variation variation;

    /**
     * The utopia point (point not dominated by any individual in the
     * population)
     */
    private Solution utopia;

    /**
     * Only maintain the min objectives and the max objectives
     */
    private Solution minObjs;
    private Solution maxObjs;

    /**
     * stores all the objective values in sorted order to get bounds
     */
    private ArrayList<SortedLinkedList<Double>> sortedObjs;

    /**
     * the number of children to create in each generation
     */
    private final int childNumber;

    /**
     * parallel purpose random generator
     */
    private final ParallelPRNG pprng;

    /**
     * Constructs a new IBEA instance.
     *
     * @param problem the problem
     * @param childNumer the number of children to create in each generation
     * @param archive the external archive; or {@code null} if no external
     * archive is used
     * @param initialization the initialization operator
     * @param variation the variation operator
     * @param fitnessEvaluator fitnessEvaluator the indicator fitness evaluator
     * to use (e.g., hypervolume additive-epsilon indicator)
     */
    public R2MOEA(Problem problem, int childNumer, NondominatedPopulation archive,
            Initialization initialization, Variation variation, IndicatorFitnessEvaluator fitnessEvaluator) {
        super(problem, new Population(), archive, initialization);
        this.childNumber = childNumer;
        this.variation = variation;
        this.comparator = new R2RankComparator();
        this.pprng = new ParallelPRNG();
    }

    @Override
    protected void initialize() {
        super.initialize();

        computeUtopia(population);
        computeBounds(population);
    }

    @Override
    protected void iterate() {
        Population offspring = new Population();

        while (offspring.size() < childNumber) {
            Solution[] parents = selectParents(variation.getArity(),
                    population);
            Solution[] children = variation.evolve(parents);
            offspring.addAll(children);
        }

        evaluateAll(offspring);
        population.addAll(offspring);
        evaluateFitness(population);

        //remove a parent for every offspring created
        for (Solution child : offspring) {
            updateBoundsInsert(child);
            updateMinMax();

            int worstIndex = findWorstIndex();

            updateBoundsRemove(population.get(worstIndex));
            updateUtopia(child);
            updateMinMax();
        }
    }

    /**
     * Returns the index of the solution with the worst fitness value.
     *
     * @return the index of the solution with the worst fitness value
     */
    private int findWorstIndex() {
        int worstIndex = 0;

        for (int i = 1; i < population.size(); i++) {
            if (fitnessComparator.compare(population.get(worstIndex),
                    population.get(i)) == -1) {
                worstIndex = i;
            }
        }

        return worstIndex;
    }

    private void updateMinMax() {
        minObjs = problem.newSolution();
        maxObjs = problem.newSolution();
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            minObjs.setObjective(i, sortedObjs.get(i).getFirst());
            maxObjs.setObjective(i, sortedObjs.get(i).getLast());
        }
    }

    /**
     * Finds the utopia point in the population
     *
     * @param population the population to find the utopia point
     * @return the utopia point
     */
    private Solution computeUtopia(Population population) {
        utopia = problem.newSolution();
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            double min = Double.MAX_VALUE;
            for (Solution soln : population) {
                min = Math.min(min, soln.getObjective(i));
            }
            utopia.setObjective(i, min);
        }
        return utopia;
    }

    /**
     * Updates the utopia based on the new incoming solution. This method
     * assumes that the utopia point never deteriorates
     *
     * @param offspring new solution entering the population
     * @return the updated utopia point
     */
    private Solution updateUtopia(Solution offspring) {
        for (int i = 0; i < offspring.getNumberOfObjectives(); i++) {
            utopia.setObjective(i, Math.min(utopia.getObjective(i), offspring.getObjective(i)));
        }
        return utopia;
    }

    /**
     * Normalizes the objective vector of a given individual
     *
     * @param solution
     * @return
     */
    private double[] normalizeObjectives(Solution solution) {
        double[] normalizedObjs = new double[solution.getNumberOfObjectives()];
        for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
            double lowBound = minObjs.getObjective(i);
            double upBound = maxObjs.getObjective(i);
            normalizedObjs[i] = (solution.getObjective(i) - lowBound) / (upBound - lowBound);
        }
        return normalizedObjs;
    }

    //Computes the bounds on population
    private void computeBounds(Population pop) {
        sortedObjs = new ArrayList<>(pop.get(0).getNumberOfObjectives());
        for (int i = 0; i < pop.get(0).getNumberOfObjectives(); i++) {
            ArrayList<Double> objs = new ArrayList(pop.size());
            for (Solution soln : pop) {
                objs.add(soln.getObjective(i));
            }
            sortedObjs.add(new SortedLinkedList<>(objs, new DoubleComparator()));
        }
    }

    //updates the bounds based on a solution exiting the population
    private void updateBoundsRemove(Solution removedSoln) {
        for (int i = 0; i < removedSoln.getNumberOfObjectives(); i++) {
            int index = sortedObjs.get(i).binaryFind(removedSoln.getObjective(i));
            sortedObjs.get(i).remove(index);
        }
    }

    //updates the bounds based on a solution exiting the population
    private void updateBoundsInsert(Solution newSolution) {
        for (int i = 0; i < newSolution.getNumberOfObjectives(); i++) {
            sortedObjs.get(i).add(newSolution.getObjective(i));
        }
    }

    /**
     * This implements fast R2 sorting from Diaz-Manriquez et al. 2013. A
     * ranking method based on the R2 indicator for many objective optimization
     *
     * @param population
     */
    private void evaluateFitness(Population population) {

    }

    /**
     * This method is a binary tournament selection of parents based on the fitness values (i.e. R2 contribution rank)
     * @param arity
     * @param population
     * @return 
     */
    private Solution[] selectParents(int arity, Population population) {
        Solution[] result = new Solution[arity];

        for (int i = 0; i < arity; i++) {
            Solution winner = population.get(pprng.nextInt(population.size()));

            for (int j = 1; j < 2; j++) {
                Solution candidate = population
                        .get(pprng.nextInt(population.size()));

                int flag = comparator.compare(winner, candidate);

                if (flag > 0) {
                    winner = candidate;
                }
            }
        }

        return result;
    }

    /**
     * This comparator compares the R2 rank computed from fast R2 sorting
     */
    private class R2RankComparator implements Comparator<Solution>{
        @Override
        public int compare(Solution t, Solution t1) {
            int rank1 = (int)t.getAttribute("rank");
            int rank2 = (int)t.getAttribute("rank");
            return Integer.compare(rank1, rank2);
        }
    }
    
    /**
     * This comparator compares the R2 contribution
     */
    private class R2ContributionComparator implements Comparator<Solution>{
        @Override
        public int compare(Solution t, Solution t1) {
            double ca1 = (double)t.getAttribute("contribution");
            double ca2 = (double)t.getAttribute("contribution");
            return Double.compare(ca1, ca2);
        }
    }
}
