/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditaggregation;

import hh.creditdefinition.Credit;
import hh.credithistory.ICreditHistory;

/**
 * Aggregates the credits by taking the mean of all credits in history.
 * @author nozomihitomi
 */
public class MeanCredits extends SumCredits{
    
    
    /**
     * Aggregates credit that are sufficiently large. The minimum threshold is 
     * set at 10^-6 the original value of the credit.
     * @param iteration the iteration to attach to the returned credit
     * @param creditHistory the credit history to aggregate over
     * @return 
     */
    @Override
    public Credit aggregateCredit(int iteration, ICreditHistory creditHistory) {
        double creditSum = super.aggregateCredit(iteration, creditHistory).getValue();
        return new Credit(iteration,creditSum/(double)creditHistory.size());
    }
    
}
