/* Copyright 2009-2016 David Hadka
 *
 * This file is part of the MOEA Framework.
 *
 * The MOEA Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The MOEA Framework is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the MOEA Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.moeaframework.core.fitness;

import org.moeaframework.core.FitnessEvaluator;
import org.moeaframework.core.FrameworkException;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.indicator.Normalizer;

/**
 * Abstract class for assigning fitnesses based on an indicator.
 */
public abstract class IndicatorFitnessEvaluator implements FitnessEvaluator {

    /**
     * The problem.
     */
    private Problem problem;

    /**
     * Scaling factor for fitness calculation.
     */
    private static final double kappa = 0.05;

    /**
     * Record of the maximum indicator value from the last call to
     * {@link #evaluate(Population)}.
     */
    private double maxAbsIndicatorValue;

    /**
     * Record of the fitness components from the last call to
     * {@link #evaluate(Population)}.
     */
    private double[][] fitcomp;

    private double[] upperbound;

    private double[] lowerbound;

    private double[] maxij;

    private boolean needsEvaulate;

    private boolean resumFitness;

    private Population normalizedPopulation;

    private int evalCounter;

    private int updateCounter;

    /**
     * Constructs an indicator-based fitness for the specified problem.
     *
     * @param problem the problem
     */
    public IndicatorFitnessEvaluator(Problem problem) {
        this.problem = problem;
        needsEvaulate = true;
        maxij = new double[2];
    }

    /**
     * Returns the problem.
     *
     * @return the problem
     */
    public Problem getProblem() {
        return problem;
    }

    /*
     * The following method is modified from the IBEA implementation for the
     * PISA framework, available at <a href="http://www.tik.ee.ethz.ch/pisa/">
     * PISA Homepage</a>.
     * 
     * Copyright (c) 2002-2003 Swiss Federal Institute of Technology,
     * Computer Engineering and Networks Laboratory. All rights reserved.
     * 
     * PISA - A Platform and Programming Language Independent Interface for
     * Search Algorithms.
     * 
     * IBEA - Indicator Based Evoluationary Algorithm - A selector module
     * for PISA
     * 
     * Permission to use, copy, modify, and distribute this software and its
     * documentation for any purpose, without fee, and without written
     * agreement is hereby granted, provided that the above copyright notice
     * and the following two paragraphs appear in all copies of this
     * software.
     * 
     * IN NO EVENT SHALL THE SWISS FEDERAL INSTITUTE OF TECHNOLOGY, COMPUTER
     * ENGINEERING AND NETWORKS LABORATORY BE LIABLE TO ANY PARTY FOR DIRECT,
     * INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF
     * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE SWISS
     * FEDERAL INSTITUTE OF TECHNOLOGY, COMPUTER ENGINEERING AND NETWORKS
     * LABORATORY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
     * 
     * THE SWISS FEDERAL INSTITUTE OF TECHNOLOGY, COMPUTER ENGINEERING AND
     * NETWORKS LABORATORY, SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING,
     * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
     * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS
     * ON AN "AS IS" BASIS, AND THE SWISS FEDERAL INSTITUTE OF TECHNOLOGY,
     * COMPUTER ENGINEERING AND NETWORKS LABORATORY HAS NO OBLIGATION TO
     * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
     */
    @Override
    public void evaluate(Population population) {
        Normalizer normalizer = new Normalizer(problem, population);
        normalizedPopulation = normalizer.normalize(population);

        upperbound = normalizer.getMaximum();
        lowerbound = normalizer.getMinimum();

        // compute fitness components
        fitcomp = new double[population.size()*2+1][population.size()*2+1];
        maxAbsIndicatorValue = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < population.size(); i++) {
            double sum = 0.0;
            for (int j = 0; j < population.size(); j++) {
                if (i != j) {
                fitcomp[j][i] = calculateIndicator(normalizedPopulation.get(j),
                        normalizedPopulation.get(i));
                if (Math.abs(fitcomp[j][i]) > maxAbsIndicatorValue) {
                    maxAbsIndicatorValue = Math.abs(fitcomp[j][i]);
                    maxij[0] = j;
                    maxij[1] = i;
                }
                    sum += Math.exp((-fitcomp[j][i] / maxAbsIndicatorValue) / kappa);
                }
            }
            population.get(i).setAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE, sum);
        }

//		sumFitness(population,population.size());
        needsEvaulate = false;
        resumFitness = false;
        evalCounter++;
    }

    private void sumFitness(Population population, int lastIndex) {
        // calculate fitness from fitness components
        for (int i = 0; i < lastIndex; i++) {
            double sum = 0.0;

            for (int j = 0; j < lastIndex; j++) {
                if (i != j) {
                    sum += Math.exp((-fitcomp[j][i] / maxAbsIndicatorValue) / kappa);
                }
            }

            population.get(i).setAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE, sum);
        }
        resumFitness = false;
    }

    /**
     * After calling {@link #evaluate(Population)}, this method is used to
     * iteratively remove solutions from the population while updating the
     * fitness value. There must be no other modifications to the population
     * between invocations of {@link #evaluate(Population)} and this method
     * other than removing solutions using this method.
     *
     * @param population the population
     * @param removeIndex the index to remove
     */
    public void removeAndUpdate(Population population, int removeIndex) {
        if (fitcomp == null) {
            throw new FrameworkException("evaluate must be called first");
        }

            for (int i = 0; i < population.size(); i++) {
                if (i != removeIndex) {
                    Solution solution = population.get(i);
                    double fitness = (Double) solution.getAttribute(
                            FitnessEvaluator.FITNESS_ATTRIBUTE);

                    fitness -= Math.exp((-fitcomp[removeIndex][i] / maxAbsIndicatorValue) / kappa);

                    solution.setAttribute(FITNESS_ATTRIBUTE, fitness);
                }
            }

            for (int i = 0; i < population.size(); i++) {
                for (int j = removeIndex + 1; j < population.size(); j++) {
                    fitcomp[i][j - 1] = fitcomp[i][j];
                }

                if (i > removeIndex) {
                    fitcomp[i - 1] = fitcomp[i];
                }
            }

            population.remove(removeIndex);
    }

    /**
     * After calling {@link #evaluate(Population)}, this method is used to
     * iteratively add solutions to the population while updating the fitness
     * value and updating the upper and lower bounds or the max indicator
     * value. There must be no other modifications to the population between
     * invocations of {@link #evaluate(Population)} and this method other than
     * adding solutions using this method.
     *
     * @param population the population
     * @param solution the solution to add
     */
    public void addAndUpdate(Population population, Solution solution) {
        if (fitcomp == null) {
            throw new FrameworkException("evaluate must be called first");
        }

        population.add(solution);

        if (needsEvaulate) {
            evaluate(population);
        } else {
            //if solution has objectives outside of the bounds, reevaluate the fitnesses
            for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
                if (solution.getObjective(i) < lowerbound[i]
                        || solution.getObjective(i) > upperbound[i]) {
                    evaluate(population);
                    return;
                }
            }
            //add solution to normalized population after deciding that bounds do not need update
            normalizedPopulation.add(normalize(solution));
            updateCounter++;

            //if solution does not have objectives outside of the current bounds can add it in the fitcomp matrix
            int lastIndex = population.size() - 1;
            for (int i = 0; i < population.size(); i++) {
                fitcomp[i][lastIndex] = calculateIndicator(normalizedPopulation.get(i),
                        normalizedPopulation.get(lastIndex));
                if (Math.abs(fitcomp[i][lastIndex]) > maxAbsIndicatorValue) {
                    maxAbsIndicatorValue = Math.abs(fitcomp[i][lastIndex]);
                    maxij[0] = i;
                    maxij[1] = lastIndex;
                    resumFitness = true;
                }

                fitcomp[lastIndex][i] = calculateIndicator(normalizedPopulation.get(lastIndex),
                        normalizedPopulation.get(i));
                if (Math.abs(fitcomp[lastIndex][i]) > maxAbsIndicatorValue) {
                    maxAbsIndicatorValue = Math.abs(fitcomp[lastIndex][i]);
                    maxij[0] = lastIndex;
                    maxij[1] = i;
                    resumFitness = true;
                }
            }
            if (resumFitness) {
                sumFitness(population, population.size());
            } else {
                //update values for all incumbent solutions
                for (int i = 0; i < population.size() - 1; i++) {
                    double prevFitness = (double) population.get(i).getAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE);
                    double newFitness = prevFitness + Math.exp((-fitcomp[lastIndex][i] / maxAbsIndicatorValue) / kappa);
                    population.get(i).setAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE, newFitness);
                }

                //calcuate fitness for new individual
                double sum = 0.0;
                for (int i = 0; i < population.size() - 1; i++) {
                    sum += Math.exp((-fitcomp[i][lastIndex] / maxAbsIndicatorValue) / kappa);
                }
                population.get(lastIndex).setAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE, sum);
            }
        }

    }

    /**
     * After calling {@link #evaluate(Population)}, this method is used to
     * iteratively add solutions to the population while updating the fitness
     * value. This method does NOT update the upper and lower bounds or the max indicator
     * value There must be no other modifications to the population between
     * invocations of {@link #evaluate(Population)} and this method other than
     * adding solutions using this method.
     *
     * @param population the population
     * @param solution the solution to add
     */
    public void addAndUpdateFitnessOnly(Population population, Solution solution) {
        if (fitcomp == null) {
            throw new FrameworkException("evaluate must be called first");
        }

        population.add(solution);
        
        //add solution to normalized population after deciding that bounds do not need update
        normalizedPopulation.add(normalize(solution));

        int lastIndex = population.size() - 1;
        for (int i = 0; i < population.size(); i++) {
                fitcomp[i][lastIndex] = calculateIndicator(normalizedPopulation.get(i),
                        normalizedPopulation.get(lastIndex));
                fitcomp[lastIndex][i] = calculateIndicator(normalizedPopulation.get(lastIndex),
                        normalizedPopulation.get(i));
        }
                //update values for all incumbent solutions
                for (int i = 0; i < population.size() - 1; i++) {
                    double prevFitness = (double) population.get(i).getAttribute("prevfitness");
                    double newFitness = prevFitness + Math.exp((-fitcomp[lastIndex][i] / maxAbsIndicatorValue) / kappa);
                    population.get(i).setAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE, newFitness);
                }

                //calcuate fitness for new individual
                double sum = 0.0;
                for (int i = 0; i < population.size() - 1; i++) {
                    sum += Math.exp((-fitcomp[i][lastIndex] / maxAbsIndicatorValue) / kappa);
                }
                population.get(lastIndex).setAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE, sum);
                population.get(lastIndex).setAttribute("prevfitness", sum);
    }

    private void findMaxIndicatorValue(Population population) {
        maxAbsIndicatorValue = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < population.size(); i++) {
            for (int j = 0; j < population.size(); j++) {
                if (Math.abs(fitcomp[i][j]) > maxAbsIndicatorValue) {
                    maxAbsIndicatorValue = Math.abs(fitcomp[i][j]);
                    maxij[0] = i;
                    maxij[1] = j;
                }
            }
        }
    }

    public Solution normalize(Solution solution) {
        Solution out = solution.copy();
        for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
            out.setObjective(i, (solution.getObjective(i) - lowerbound[i]) / (upperbound[i] - lowerbound[i]));
        }
        return out;
    }

    public double[] getUpperbound() {
        return upperbound;
    }

    public double[] getLowerbound() {
        return lowerbound;
    }

    /**
     * Returns the indicator value relative to the two solutions.
     *
     * @param solution1 the first solution
     * @param solution2 the second solution
     * @return the indicator value relative to the two solutions
     */
    public abstract double calculateIndicator(Solution solution1,
            Solution solution2);

}
