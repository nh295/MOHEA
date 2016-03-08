/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moea;

import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

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
     * The fitness comparator for comparing solutions based on their fitness.
     */
    private FitnessComparator fitnessComparator;

    /**
     * The selection operator.
     */
    private Selection selection;

    /**
     * The variation operator.
     */
    private Variation variation;
    
    /**
     * The utopia point (point not dominated by any individual in the population)
     */
    private Solution utopiaPt;

    /**
     * Constructs a new IBEA instance.
     *
     * @param problem the problem
     * @param archive the external archive; or {@code null} if no external
     * archive is used
     * @param initialization the initialization operator
     * @param variation the variation operator
     */
    public R2MOEA(Problem problem, NondominatedPopulation archive,
            Initialization initialization, Variation variation) {
        super(problem, new Population(), archive, initialization);
        this.variation = variation;
        this.fitnessEvaluator = fitnessEvaluator;

        fitnessComparator = new FitnessComparator(
                fitnessEvaluator.areLargerValuesPreferred());
        selection = new TournamentSelection(fitnessComparator);
    }

    @Override
    protected void initialize() {
        super.initialize();

        fitnessEvaluator.evaluate(population);
    }

    @Override
    protected void iterate() {
        Population offspring = new Population();
        int populationSize = population.size();

        while (offspring.size() < populationSize) {
            Solution[] parents = selection.select(variation.getArity(),
                    population);
            Solution[] children = variation.evolve(parents);

            offspring.addAll(children);
        }

        evaluateAll(offspring);
        population.addAll(offspring);
        fitnessEvaluator.evaluate(population);

        while (population.size() > populationSize) {
            int worstIndex = findWorstIndex();
            fitnessEvaluator.removeAndUpdate(population, worstIndex);
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
    
    private void updateUtopiaPoint(){
        
    }

}
