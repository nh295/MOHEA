/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition;

/**
 * The enum for the different types of credits used
 *
 * @author nozomihitomi
 */
public enum FitnessFunctionType {
    /**
     * If the fitness is based on Pareto dominance
     */
    Do,
    /**
     * If the fitness is based on decomposition methods such as in MOEA/D
     */
    De,
    /**
     * If the fitness is based on the R2 indicator
     */
    R2
}
