/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.qualityestimation;

import hh.rewarddefinition.Reward;
import hh.rewarddefinition.DecayingReward;
import hh.credithistory.ICreditHistory;
import java.util.Iterator;

/**
 * Aggregates the credits by summing all credits in history.
 * @author nozomihitomi
 */
public class SumRewards implements IQualityEstimation{
    private static final long serialVersionUID = 593719540602667696L;

    
    /**
     * Aggregates credit that are sufficiently large. The minimum threshold is 
     * set at 10^-6 the original value of the credit.
     * @param iteration the iteration to attach to the returned credit
     * @param creditHistory the credit history to aggregate over
     * @return 
     */
    @Override
    public Reward aggregateCredit(int iteration, ICreditHistory creditHistory) {
        Iterator<Reward> iter =  creditHistory.iterator();
        double aggregate = 0;
        while(iter.hasNext()){
            Reward credit = iter.next();
            DecayingReward decayCredit;
            if(credit.getClass().equals(DecayingReward.class)){
                decayCredit = (DecayingReward)credit;
                if(decayCredit.fractionOriginalVal(iteration)<Math.pow(10, -6))
                    break;
                else
                    aggregate+=decayCredit.getValue()*decayCredit.fractionOriginalVal(iteration);
            }else
                aggregate+=credit.getValue();
        }
        return new Reward(iteration, aggregate);
    }
    
}
