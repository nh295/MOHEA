//  IBEA.java
//
//  Author:
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
// This implementation is based on the PISA code:
// http://www.tik.ee.ethz.ch/sop/pisa/selectors/ibea/?page=ibea.php
package org.moeaframework.algorithm.jmetal;

import java.util.ArrayList;
import java.util.List;
import static org.moeaframework.core.FitnessEvaluator.FITNESS_ATTRIBUTE;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.FitnessComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.operator.real.PM;
import org.moeaframework.core.operator.real.SBX;
import org.moeaframework.core.operator.real.SBXjMetal;

/**
 * This class implements the IBEA algorithm
 */
public class IBEAjMetal {

    private Problem problem;

    private List<List<Double>> indicatorValues;
    private double maxIndicatorValue;

    private int populationSize;
    private int archiveSize;
    private int maxEvaluations;

    private Population archive;

    private SBXjMetal crossoverOperator;
    private PM mutationOperator;
    private Selection selectionOperator;
    private ParetoDominanceComparator dominanceComparator;

    /**
     * Constructor
     */
    public IBEAjMetal(Problem problem, int populationSize, int archiveSize, int maxEvaluations) {
        this.problem = problem;
        this.populationSize = populationSize;
        this.archiveSize = archiveSize;
        this.maxEvaluations = maxEvaluations;
        this.crossoverOperator = new SBXjMetal(0.9, 20.0);
        this.mutationOperator = new PM(1 / problem.getNumberOfObjectives(), 20.0);
        this.selectionOperator = new TournamentSelection(new FitnessComparator(false));
        this.dominanceComparator = new ParetoDominanceComparator();
    }

    /**
     * Execute() method
     */
    public void run() {
        int evaluations;
        Population solutionSet;

        //Initialize the variables
        solutionSet = new Population();
        archive = new Population();
        evaluations = 0;

        //-> Create the initial solutionSet
        RandomInitialization init = new RandomInitialization(problem, populationSize);
        solutionSet.addAll(init.initialize());
        for(Solution sln : solutionSet){
            problem.evaluate(sln);
            evaluations++;
        }

        while (evaluations < maxEvaluations) {
             if (evaluations % 1000 == 0) {
                System.out.println(evaluations);
            }
            Population union = new Population();
            union.addAll(solutionSet);
            union.addAll(archive);
            calculateFitness(union);
            archive = union;

            while (archive.size() > populationSize) {
                removeWorst(archive);
            }

            // Create a new offspringPopulation
            Solution[] parents = selectionOperator.select(crossoverOperator.getArity(), archive);

            //make the crossover
            Population offspring = new Population();
            Solution[] crossed = crossoverOperator.evolve(parents);
            for (Solution crossedChild : crossed) {
                offspring.addAll(mutationOperator.evolve(new Solution[]{crossedChild}));
            }
            for (Solution child : offspring) {
                problem.evaluate(child);

                evaluations++;
            }

            solutionSet = offspring;
        }
    }

    /**
     * Calculates the hypervolume of that portion of the objective space that is
     * dominated by individual a but not by individual b
     */
    double calculateHypervolumeIndicator(Solution solutionA, Solution solutionB, int d,
            double maximumValues[], double minimumValues[]) {
        double a, b, r, max;
        double volume;
        double rho = 2.0;

        r = rho * (maximumValues[d - 1] - minimumValues[d - 1]);
        max = minimumValues[d - 1] + r;

        a = solutionA.getObjective(d - 1);
        if (solutionB == null) {
            b = max;
        } else {
            b = solutionB.getObjective(d - 1);
        }

        if (d == 1) {
            if (a < b) {
                volume = (b - a) / r;
            } else {
                volume = 0;
            }
        } else {
            if (a < b) {
                volume
                        = calculateHypervolumeIndicator(solutionA, null, d - 1, maximumValues, minimumValues) * (b
                        - a) / r;
                volume
                        += calculateHypervolumeIndicator(solutionA, solutionB, d - 1, maximumValues, minimumValues)
                        * (max - b) / r;
            } else {
                volume
                        = calculateHypervolumeIndicator(solutionA, solutionB, d - 1, maximumValues, minimumValues)
                        * (max - a) / r;
            }
        }

        return (volume);
    }

    /**
     * This structure stores the indicator values of each pair of elements
     */
    public void computeIndicatorValuesHD(Population solutionSet, double[] maximumValues,
            double[] minimumValues) {
        List<Solution> A, B;
        // Initialize the structures
        indicatorValues = new ArrayList<List<Double>>();
        maxIndicatorValue = -Double.MAX_VALUE;

        for (int j = 0; j < solutionSet.size(); j++) {
            A = new ArrayList<>(1);
            A.add(solutionSet.get(j));

            List<Double> aux = new ArrayList<Double>();
            for (Solution solution : solutionSet) {
                B = new ArrayList<>(1);
                B.add(solution);

                int flag = dominanceComparator.compare(A.get(0), B.get(0));

                double value;
                if (flag == -1) {
                    value
                            = -calculateHypervolumeIndicator(A.get(0), B.get(0), problem.getNumberOfObjectives(),
                                    maximumValues, minimumValues);
                } else {
                    value = calculateHypervolumeIndicator(B.get(0), A.get(0), problem.getNumberOfObjectives(),
                            maximumValues, minimumValues);
                }

                //Update the max value of the indicator
                if (Math.abs(value) > maxIndicatorValue) {
                    maxIndicatorValue = Math.abs(value);
                }
                aux.add(value);
            }
            indicatorValues.add(aux);
        }
    }

    /**
     * Calculate the fitness for the individual at position pos
     */
    public void fitness(Population solutionSet, int pos) {
        double fitness = 0.0;
        double kappa = 0.05;

        for (int i = 0; i < solutionSet.size(); i++) {
            if (i != pos) {
                fitness += Math.exp((-1 * indicatorValues.get(i).get(pos) / maxIndicatorValue) / kappa);
            }
        }
        solutionSet.get(pos).setAttribute(FITNESS_ATTRIBUTE, fitness);
    }

    /**
     * Calculate the fitness for the entire population.
     */
    public void calculateFitness(Population solutionSet) {
        // Obtains the lower and upper bounds of the population
        double[] maximumValues = new double[problem.getNumberOfObjectives()];
        double[] minimumValues = new double[problem.getNumberOfObjectives()];

        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            maximumValues[i] = -Double.MAX_VALUE;
            minimumValues[i] = Double.MAX_VALUE;
        }

        for (Solution solution : solutionSet) {
            for (int obj = 0; obj < problem.getNumberOfObjectives(); obj++) {
                double value = solution.getObjective(obj);
                if (value > maximumValues[obj]) {
                    maximumValues[obj] = value;
                }
                if (value < minimumValues[obj]) {
                    minimumValues[obj] = value;
                }
            }
        }

        computeIndicatorValuesHD(solutionSet, maximumValues, minimumValues);
        for (int pos = 0; pos < solutionSet.size(); pos++) {
            fitness(solutionSet, pos);
        }
    }

    /**
     * Update the fitness before removing an individual
     */
    public void removeWorst(Population solutionSet) {

        // Find the worst;
        double worst = (double) solutionSet.get(0).getAttribute(FITNESS_ATTRIBUTE);
        int worstIndex = 0;
        double kappa = 0.05;

        for (int i = 1; i < solutionSet.size(); i++) {
            if ((double) solutionSet.get(i).getAttribute(FITNESS_ATTRIBUTE) > worst) {
                worst = (double) solutionSet.get(i).getAttribute(FITNESS_ATTRIBUTE);
                worstIndex = i;
            }
        }

        // Update the population
        for (int i = 0; i < solutionSet.size(); i++) {
            if (i != worstIndex) {
                double fitness = (double) solutionSet.get(i).getAttribute(FITNESS_ATTRIBUTE);
                fitness -= Math.exp((-indicatorValues.get(worstIndex).get(i) / maxIndicatorValue) / kappa);
                solutionSet.get(i).setAttribute(FITNESS_ATTRIBUTE, fitness);
            }
        }

        // remove worst from the indicatorValues list
        indicatorValues.remove(worstIndex);
        for (List<Double> anIndicatorValues_ : indicatorValues) {
            anIndicatorValues_.remove(worstIndex);
        }

        solutionSet.remove(worstIndex);
    }

    public Population getArchive() {
        return archive;
    }
    
    
}
