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
public enum RewardDefinitionType {

    /**
     * If the reward is assigned based on the solution quality improvement of the offspring over the parent solution
     */
    OFFSPRINGPARENT,
    /**
     * If the reward is assigned based on the quality improvement of the population of a single offspring solution
     */
    OFFSPRINGPOPULATION,
    /**
     * If the reward is assigned based on the contribution of a heuristic to the population or archive
     */
    POPULATIONCONTRIBUTION
}
