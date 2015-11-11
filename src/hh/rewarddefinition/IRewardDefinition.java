/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.rewarddefinition;

/**
 * Use this to define what solutions are included in computing and what metrics 
 * should be used to compute credit.
 * @author Nozomi
 */
public interface IRewardDefinition {
    
    /**
     * Gets the type of credit definition
     * @return 
     */
    public CreditFunctionType getType();
    
    /**
     * Gets the type of credit definition
     * @return 
     */
    public RewardDefinedOn getOperatesOn();
    
    /**
     * Clears all information stored in object. Try to reduce chance of memory leaks
     */
    public void clear();
}
