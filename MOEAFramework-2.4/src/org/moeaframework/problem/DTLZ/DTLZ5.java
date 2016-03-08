//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.moeaframework.problem.DTLZ;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;

/**
 * Class representing problem DTLZ5. Copied over from Jmetal but modified to
 * integrate with MOEAFramework
 */
public class DTLZ5 extends DTLZ {

    /**
     * Constructs a DTLZ5 test problem with the specified number of objectives.
     * This is equivalent to calling {@code new DTLZ5(numberOfObjectives+9,
     * numberOfObjectives)}
     *
     * @param numberOfObjectives the number of objectives for this problem
     */
    public DTLZ5(int numberOfObjectives) {
        this(numberOfObjectives + 9, numberOfObjectives);
    }

    /**
     * Constructs a DTLZ5 test problem with the specified number of variables
     * and objectives.
     *
     * @param numberOfVariables the number of variables for this problem
     * @param numberOfObjectives the number of objectives for this problem
     */
    public DTLZ5(int numberOfVariables, int numberOfObjectives) {
        super(numberOfVariables, numberOfObjectives);
    }

    @Override
    public void evaluate(Solution solution) {
        double[] theta = new double[numberOfObjectives - 1];
        double g = 0.0;

        double[] f = new double[numberOfObjectives];
        double[] x = EncodingUtils.getReal(solution);

        int k = getNumberOfVariables() - getNumberOfObjectives() + 1;

        for (int i = numberOfVariables - k; i < numberOfVariables; i++) {
            g += (x[i] - 0.5) * (x[i] - 0.5);
        }

        double t = java.lang.Math.PI / (4.0 * (1.0 + g));

        theta[0] = x[0] * java.lang.Math.PI / 2.0;
        for (int i = 1; i < (numberOfObjectives - 1); i++) {
            theta[i] = t * (1.0 + 2.0 * g * x[i]);
        }

        for (int i = 0; i < numberOfObjectives; i++) {
            
            f[i] = 1.0 + g;
            
            for (int j = 0; j < numberOfObjectives - (i + 1); j++) {
                f[i] *= java.lang.Math.cos(theta[j]);
            }
            if (i != 0) {
                int aux = numberOfObjectives - (i + 1);
                f[i] *= java.lang.Math.sin(theta[aux]);
            }
            
            solution.setObjective(i, f[i]);
        }
    }

    @Override
    public Solution generate() {
        Solution solution = newSolution();

        for (int i = 0; i < numberOfObjectives - 1; i++) {
            ((RealVariable) solution.getVariable(i)).setValue(PRNG.nextDouble());
        }

        for (int i = numberOfObjectives - 1; i < numberOfVariables; i++) {
            ((RealVariable) solution.getVariable(i)).setValue(0);
        }

        evaluate(solution);

        return solution;
    }
}
