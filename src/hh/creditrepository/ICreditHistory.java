/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditrepository;

import hh.creditdefinition.Credit;
import java.util.LinkedList;

/**
 *
 * @author Nozomi
 */
public interface ICreditHistory {
    
    /**
     * Adds credit to the history
     * @param credit to add to the history
     */
    public void addCredit(Credit credit);
    
    /**
     * Returns the credit history
     * @return the credit history
     */
    public LinkedList<Credit> getHistory();
    
    /**
     * Returns an instance of credit history
     * @return an instance of credit history
     */
    public ICreditHistory getInstance();
    
    /**
     * Returns the average credit, averaged over all credits stored in the history
     * @param iteration the iteration to average from the beginning of the history
     * @return the average credit, averaged over all credits stored in the history
     */
    public Credit getAverageCredit(int iteration);
    
    /**
     * Returns the sum total credit, summed over all credits stored in the history
     * @param iteration the iteration to sum until from the beginning of the history
     * @return the sum total credit, summed over all credits stored in the history
     */
    public Credit getSumCredit(int iteration);
    
    /**
     * Clears the stored credit history.
     */
    public void clear();
}
