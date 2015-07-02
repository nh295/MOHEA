/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditaggregation;

import hh.creditdefinition.Credit;
import hh.creditdefinition.DecayingCredit;
import hh.creditrepository.AbstractCreditHistory;
import java.io.Serializable;
import java.util.Iterator;

/**
 * 
 * @author nozomihitomi
 */
public class CreditAggregateSum implements ICreditAggregationStrategy, Serializable{
    private static final long serialVersionUID = 1914546705953536470L;
    
    
    public CreditAggregateSum(){
        
    }

    /**
     * Aggregates credit that are sufficiently large. The minimum threshold is 
     * set at 10^-6 the original value of the credit.
     * @param iteration the iteration to attach to the returned credit
     * @param creditHistory the credit history to aggregate over
     * @return 
     */
    @Override
    public Credit aggregateCredit(int iteration, AbstractCreditHistory creditHistory) {
        Iterator<Credit> iter =  creditHistory.iterator();
        double aggregate = 0;
        while(iter.hasNext()){
            Credit credit = iter.next();
            DecayingCredit decayCredit;
            if(credit.getClass().equals(DecayingCredit.class)){
                decayCredit = (DecayingCredit)credit;
                if(decayCredit.fractionOriginalVal(iteration)<Math.pow(10, -6))
                    break;
                else
                    aggregate+=decayCredit.getValue()*decayCredit.fractionOriginalVal(iteration);
            }else
                aggregate+=credit.getValue();
        }
        return new Credit(iteration, aggregate);
    }
}
