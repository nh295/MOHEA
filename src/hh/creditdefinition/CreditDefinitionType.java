/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditdefinition;

/**
     * The enum for the different types of credits used
 * @author nozomihitomi
 */
public enum CreditDefinitionType {
    /**
     * If the credit of a solution is computed with respect to a population
     */
    POPULATION,
    
    /**
     * If the credit of a solution is computed with respect to its parents
     */
    PARENT,
    
    /**
     * If the credit of a solution is computed with respect to the archive population
     */
    ARCHIVE
}
