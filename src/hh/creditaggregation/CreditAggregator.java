/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditaggregation;

import hh.creditdefinition.Credit;
import hh.credithistory.ICreditHistory;
import java.io.Serializable;

/**
 * 
 * @author nozomihitomi
 */
public class CreditAggregator implements Serializable{
    private static final long serialVersionUID = 1914546705953536470L;
    
    private SumCredits summer;
    
    private MeanCredits averager;
    
    public CreditAggregator(){
        summer = new SumCredits();
        averager = new MeanCredits();
    }

    public Credit sum(int iteration, ICreditHistory history){
        return summer.aggregateCredit(iteration, history);
    }
    
    public Credit mean(int iteration, ICreditHistory history){
        return averager.aggregateCredit(iteration, history);
    }
}
