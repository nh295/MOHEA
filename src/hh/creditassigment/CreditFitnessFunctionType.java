/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.creditassigment;

/**
 * The enum for the different types of credits used
 *
 * @author nozomihitomi
 */
public enum CreditFitnessFunctionType {
    /**
     * If the fitness is based on Pareto dominance
     */
    Do,
    /**
     * If the fitness is based on decomposition methods such as in MOEA/D
     */
    De,
    /**
     * If the fitness is based on an indicator
     */
    I
}
