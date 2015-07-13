/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition;

/**
 * The enum for the the element on which the rewards are assigned
 * @author SEAK2
 */
public enum RewardDefinedOn {
    /**
     * If the credit of a solution is computed with respect to a population
     */
    POPULATION,
    /**
     * If the credit of a solution is computed with respect to its parents
     */
    PARENT,
    
    /**
     * If the credit of a solution is computed with respect to the pareto front
     */
    PARETOFRONT,
    
    /**
     * If the credit of a solution is computed with respect to the archive
     * population
     */
    ARCHIVE
}
