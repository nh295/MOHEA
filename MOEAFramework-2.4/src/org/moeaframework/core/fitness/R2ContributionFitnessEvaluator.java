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

import hh.creditassignment.fitnessindicator.R2Indicator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.moeaframework.core.FitnessEvaluator;
import static org.moeaframework.core.FitnessEvaluator.FITNESS_ATTRIBUTE;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.indicator.Hypervolume;

public class R2ContributionFitnessEvaluator implements FitnessEvaluator {

    private final Problem problem;
    
    private final R2Indicator r2indicator;
    
    private final Solution refPoint;
    
    private final double offset;
    
    /**
     *
     * @param problem
     * @param numVecs
     * @param offset after normalizing, the offset is added to the points to try to 
     */
    public R2ContributionFitnessEvaluator(Problem problem, int numVecs, double offset) {
        super();
        this.problem = problem;
        this.r2indicator = new R2Indicator(problem.getNumberOfObjectives(), numVecs);
        this.offset = offset;
        this.refPoint = problem.newSolution();
        for(int i=0; i<problem.getNumberOfObjectives(); i++){
            refPoint.setObjective(i, -1.0);
        }
    }
    
    @Override
    public void evaluate(Population population) {
        List<Solution> solutions = normalize(population);
        List<Double> r2contributions = r2indicator.computeContributions(new NondominatedPopulation(solutions), refPoint);

        for (int i = 0; i < population.size(); i++) {
            population.get(i).setAttribute(FITNESS_ATTRIBUTE, r2contributions.get(i));
        }
    }

    private List<Solution> normalize(Population population) {
        List<Solution> result = new ArrayList<Solution>();

        double[] min = new double[problem.getNumberOfObjectives()];
        double[] max = new double[problem.getNumberOfObjectives()];

        Arrays.fill(min, Double.POSITIVE_INFINITY);
        Arrays.fill(max, Double.NEGATIVE_INFINITY);

        for (Solution solution : population) {
            for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
                min[i] = Math.min(min[i], solution.getObjective(i));
                max[i] = Math.max(max[i], solution.getObjective(i));
            }
        }

        for (Solution solution : population) {
            Solution newSolution = solution.copy();

            for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
                newSolution.setObjective(i, ((newSolution.getObjective(i) - min[i])  / (max[i] - min[i])) + offset);
            }

            result.add(newSolution);
        }

        return result;
    }

    @Override
    public boolean areLargerValuesPreferred() {
        return false;
    }

}
