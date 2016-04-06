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
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

public class R2FitnessEvaluator extends IndicatorFitnessEvaluator {

    private final R2Indicator r2indicator;

    /**
     *
     * @param problem
     * @param numVecs
     * @param offset after normalizing, the offset is added to the points to try
     * to
     */
    public R2FitnessEvaluator(Problem problem, int numVecs, double offset) {
        super(problem);
        Solution refPoint = problem.newSolution();
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            refPoint.setObjective(i, -offset);
        }
        this.r2indicator = new R2Indicator(problem, numVecs, refPoint);
    }
    
    public void setRefPoint(Solution solution){
        r2indicator.setReferencePoint(solution);
    }
    
    public Solution getRefPoint(){
        return r2indicator.getReferencePoint();
    }

    @Override
    public double calculateIndicator(Solution solution1, Solution solution2) {
        return r2indicator.compute(solution1, solution2);
    }

    @Override
    public boolean areLargerValuesPreferred() {
        return false;
    }

}
