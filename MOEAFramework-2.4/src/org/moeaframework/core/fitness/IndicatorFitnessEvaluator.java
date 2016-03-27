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

import java.util.LinkedList;
import org.moeaframework.core.FitnessEvaluator;
import org.moeaframework.core.FrameworkException;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
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
    private LinkedList<LinkedList<Double>> fitcomp;

    private double[] upperbound;

    private double[] lowerbound;

    /**
     * Indices of the max indicator value
     */
    private double[] maxij;

    private Population normalizedPopulation;

    /**
     * Flag that tracks changes to lower/upper bounds and max indicator value;
     * if true, use evaluate and not addAndUpdate
     */
    private boolean needEvaluated;

    /**
     * Constructs an indicator-based fitness for the specified problem.
     *
     * @param problem the problem
     */
    public IndicatorFitnessEvaluator(Problem problem) {
        this.problem = problem;
        maxij = new double[2];
        needEvaluated = true;
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
        fitcomp = new LinkedList<>();
        maxAbsIndicatorValue = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < population.size(); i++) {
            fitcomp.add(new LinkedList<Double>());
            for (int j = 0; j < population.size(); j++) {
                fitcomp.get(i).add(j, calculateIndicator(normalizedPopulation.get(i),
                        normalizedPopulation.get(j)));

                if (Math.abs(fitcomp.get(i).get(j)) > maxAbsIndicatorValue) {
                    maxAbsIndicatorValue = Math.abs(fitcomp.get(i).get(j));
                    maxij[0] = i;
                    maxij[1] = j;
                }
            }
        }

        // calculate fitness from fitness components
        for (int i = 0; i < population.size(); i++) {
            double sum = 0.0;

            for (int j = 0; j < population.size(); j++) {
                if (i != j) {
                    sum += Math.exp((-fitcomp.get(j).get(i) / maxAbsIndicatorValue) / kappa);
                }
            }

            population.get(i).setAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE, sum);
        }
        needEvaluated = false;
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

        //If the solution that is being removed is responsible for the max 
        //indicator value or the upper/lower bounds then next time new solution
        //is added, use evalueate method 
        if (removeIndex == maxij[0]) {
            needEvaluated = true;
        }
        if (removeIndex == maxij[1]) {
            needEvaluated = true;
        }
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            Solution solution = population.get(i);
            if (solution.getObjective(i) == upperbound[i] || solution.getObjective(i) == lowerbound[i]) {
                needEvaluated = true;
                break;
            }
        }

        for (int i = 0; i < population.size(); i++) {
            if (i != removeIndex) {
                Solution solution = population.get(i);
                double fitness = (Double) solution.getAttribute(
                        FitnessEvaluator.FITNESS_ATTRIBUTE);

                fitness -= Math.exp((-fitcomp.get(removeIndex).get(i) / maxAbsIndicatorValue) / kappa);

                solution.setAttribute(FITNESS_ATTRIBUTE, fitness);
            }
        }

        for (int i = 0; i < population.size(); i++) {
            fitcomp.get(i).remove(removeIndex);
        }
        fitcomp.remove(removeIndex);

        population.remove(removeIndex);
        normalizedPopulation.remove(removeIndex);
    }

    /**
     * After calling {@link #evaluate(Population)}, this method is used to
     * iteratively add solutions to the population while updating the fitness
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
        normalizedPopulation.add(normalize(solution));
        if (!needEvaluated && inRange(solution)) {
            //if solution does not have objectives outside of the current bounds can add it in the fitcomp matrix
            int lastIndex = population.size() - 1;
            fitcomp.add(lastIndex, new LinkedList<Double>());
            for (int i = 0; i < population.size(); i++) {
                fitcomp.get(i).add(lastIndex, calculateIndicator(normalizedPopulation.get(i),
                        normalizedPopulation.get(lastIndex)));
                if (Math.abs(fitcomp.get(i).get(lastIndex)) > maxAbsIndicatorValue) {
                    evaluate(population);
                    return;
                }

                fitcomp.get(lastIndex).add(i, calculateIndicator(normalizedPopulation.get(lastIndex),
                        normalizedPopulation.get(i)));
                if (Math.abs(fitcomp.get(lastIndex).get(i)) > maxAbsIndicatorValue) {
                    evaluate(population);
                    return;
                }
            }

            //just update values for all incumbent solutions with new fitness of 
            for (int i = 0; i < population.size(); i++) {
                if (i != lastIndex) {
                    double prevFitness = (double) population.get(i).getAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE);
                    double newFitness = prevFitness + Math.exp((-fitcomp.get(lastIndex).get(i) / maxAbsIndicatorValue) / kappa);
                    population.get(i).setAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE, newFitness);
                }
            }

            //calcuate fitness for new individual
            double sum = 0.0;
            for (int i = 0; i < population.size(); i++) {
                if (i != lastIndex) {
                    sum += Math.exp((-fitcomp.get(i).get(lastIndex) / maxAbsIndicatorValue) / kappa);
                }
            }
            population.get(lastIndex).setAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE, sum);

        } else {
            evaluate(population);
        }
    }

    /**
     * Normalizes solution using current upper and lower bounds
     *
     * @param solution
     * @return
     */
    private Solution normalize(Solution solution) {
        Solution out = solution.copy();
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            out.setObjective(i, (solution.getObjective(i) - lowerbound[i]) / (upperbound[i] - lowerbound[i]));
        }
        return out;
    }

    /**
     * Checks if the solution is within the current lower and upper bounds
     *
     * @return true if the solution lies within the current bounds
     */
    private boolean inRange(Solution solution) {
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            if (solution.getObjective(i) < lowerbound[i]) {
                return false;
            }
            if (solution.getObjective(i) > upperbound[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the indicator value relative to the two solutions.
     *
     * @param solution1 the first solution
     * @param solution2 the second solution
     * @return the indicator value relative to the two solutions
     */
    protected abstract double calculateIndicator(Solution solution1,
            Solution solution2);
}
