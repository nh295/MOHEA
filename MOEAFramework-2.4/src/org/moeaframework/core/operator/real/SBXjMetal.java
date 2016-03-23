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
package org.moeaframework.core.operator.real;

import java.io.Serializable;
import org.moeaframework.core.ParallelPRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.variable.RealVariable;

/**
 * This class allows to apply a SBX crossover operator using two parent
 * solutions (Double encoding). A {@link RepairDoubleSolution} object is used to
 * decide the strategy to apply when a value is out of range.
 *
 * The implementation is based on the NSGA-II code available in
 * <a href="http://www.iitk.ac.in/kangal/codes.shtml">http://www.iitk.ac.in/kangal/codes.shtml</a>
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 * @author Juan J. Durillo
 */
public class SBXjMetal implements Variation, Serializable {

    /**
     * EPS defines the minimum difference allowed between real values
     */
    private static final double EPS = 1.0e-14;
    private static final long serialVersionUID = -8370286514034379962L;

    private double distributionIndex;
    private double crossoverProbability;
    private ParallelPRNG pprng;

    /**
     * Constructor
     */
    public SBXjMetal(double crossoverProbability, double distributionIndex) {
        this.crossoverProbability = crossoverProbability;
        this.distributionIndex = distributionIndex;
        pprng = new ParallelPRNG();
    }

    /* Getters */
    public double getCrossoverProbability() {
        return crossoverProbability;
    }

    public double getDistributionIndex() {
        return distributionIndex;
    }

    /**
     * doCrossover method
     */
    private Solution[] doCrossover(
            double probability, Solution parent1, Solution parent2) {
        Solution[] offspring = new Solution[2];

        offspring[0] = parent1.copy();
        offspring[1] = parent2.copy();

        int i;
        double rand;
        double y1, y2, lowerBound, upperBound;
        double c1, c2;
        double alpha, beta, betaq;
        double valueX1, valueX2;

        if (pprng.nextDouble() <= probability) {
            for (i = 0; i < parent1.getNumberOfVariables(); i++) {
                RealVariable var1 = (RealVariable) parent1.getVariable(i);
                RealVariable var2 = (RealVariable) parent1.getVariable(i);

                RealVariable newVar1 = var1.copy();
                RealVariable newVar2 = var2.copy();

                valueX1 = var1.getValue();
                valueX2 = var2.getValue();
                if (pprng.nextDouble() <= 0.5) {
                    if (Math.abs(valueX1 - valueX2) > EPS) {

                        if (valueX1 < valueX2) {
                            y1 = valueX1;
                            y2 = valueX2;
                        } else {
                            y1 = valueX2;
                            y2 = valueX1;
                        }

                        lowerBound = var1.getLowerBound();
                        upperBound = var1.getUpperBound();

                        rand = pprng.nextDouble();
                        beta = 1.0 + (2.0 * (y1 - lowerBound) / (y2 - y1));
                        alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));

                        if (rand <= (1.0 / alpha)) {
                            betaq = Math.pow(rand * alpha, (1.0 / (distributionIndex + 1.0)));
                        } else {
                            betaq = Math
                                    .pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex + 1.0));
                        }
                        c1 = 0.5 * (y1 + y2 - betaq * (y2 - y1));

                        beta = 1.0 + (2.0 * (upperBound - y2) / (y2 - y1));
                        alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));

                        if (rand <= (1.0 / alpha)) {
                            betaq = Math.pow((rand * alpha), (1.0 / (distributionIndex + 1.0)));
                        } else {
                            betaq = Math
                                    .pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex + 1.0));
                        }
                        c2 = 0.5 * (y1 + y2 + betaq * (y2 - y1));

                        if (c1 < lowerBound) {
                            c1 = lowerBound;
                        } else if (c1 > upperBound) {
                            c1 = upperBound;
                        }

                        if (c2 < lowerBound) {
                            c2 = lowerBound;
                        } else if (c2 > upperBound) {
                            c2 = upperBound;
                        }

                        newVar1.setValue(c1);
                        newVar2.setValue(c2);

                        if (pprng.nextDouble() <= 0.5) {
                            offspring[0].setVariable(i, newVar2);
                            offspring[1].setVariable(i, newVar1);
                        } else {
                            offspring[0].setVariable(i, newVar1);
                            offspring[1].setVariable(i, newVar2);
                        }
                    } else {
                        offspring[0].setVariable(i, newVar1);
                        offspring[1].setVariable(i, newVar2);
                    }
                } else {
                    offspring[0].setVariable(i, newVar1);
                    offspring[1].setVariable(i, newVar2);
                }
            }
        }

        return offspring;
    }

    @Override
    public int getArity() {
        return 2;
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        return doCrossover(crossoverProbability, parents[0], parents[1]);
    }
}
