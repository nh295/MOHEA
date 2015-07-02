/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditdefinition;

/**
 *
 * @author nozomihitomi
 */
public abstract class ArchiveBasedCredit extends PopulationBasedCredit{
    /**
     * Gets the type of credit definition
     * @return 
     */
    @Override
    public CreditDefinitionType getType() {
        return CreditDefinitionType.ARCHIVE;
    }
}
