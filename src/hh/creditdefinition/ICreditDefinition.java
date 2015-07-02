/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditdefinition;

import org.moeaframework.core.Variation;

/**
 * Use this to define what solutions are included in computing and what metrics 
 * should be used to compute credit.
 * @author Nozomi
 */
public interface ICreditDefinition {
    
    /**
     * Gets the type of credit definition
     * @return 
     */
    public CreditDefinitionType getType();
    
    /**
     * Returns true if this credit type is an immediate. Returns false if it is aggregate
     * @return  true if this credit type is an immediate. Returns false if it is aggregate
     */
    public boolean isImmediate();
    
}
