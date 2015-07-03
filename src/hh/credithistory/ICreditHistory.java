/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.credithistory;

import hh.creditdefinition.Credit;
import java.util.LinkedList;

/**
 *
 * @author Nozomi
 */
public interface ICreditHistory extends Iterable<Credit>{
    
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
     * Returns the latest credit in the history
     * @return 
     */
    public Credit getLatest();
    
    /**
     * Returns an instance of credit history.
     * @return an instance of credit history
     */
    public ICreditHistory getInstance();
    
    /**
     * Clears the stored credit history.
     */
    public void clear();
    
    /**
     * Returns the number of credits stored in the history
     * @return the number of credits stored in the history
     */
    public int size();
}
