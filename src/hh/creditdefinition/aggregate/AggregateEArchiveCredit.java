/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditdefinition.aggregate;

import hh.creditdefinition.Credit;
import hh.creditdefinition.immediate.ImmediateEArchiveCredit;
import hh.creditrepository.CreditRepository;
import java.util.Collection;
import org.moeaframework.core.Population;
import org.moeaframework.core.Variation;

/**
 * This credit definition gives credit to the specified heuristic for all the
 * solutions it created that are  in the epsilon archive
 * @author nozomihitomi
 */
public class AggregateEArchiveCredit extends ImmediateEArchiveCredit implements IAggregateCredit{
     private AggregateParetoFrontCredit aggPFcredit;
     
    /**
     * The constructor needs the value for credit when a solution is in the 
     * e-archive and for when a solution is not in the e-archive
     * @param inArchive credit to assign when solution is in the archive 
     * @param notInArchive credit to assign when solution is not in the archive 
     * @param epsilon the epsilon to use for the e-archive
     */
    public AggregateEArchiveCredit(double inArchive, double notInArchive, double[] epsilon) {
        super(inArchive, notInArchive,epsilon);
        //by giving AggregateParetoFrontCredit a 1,0 score we can count how many
        //solutions per rank a heuristic is responsible for
        aggPFcredit = new AggregateParetoFrontCredit(inArchive, 0.0);
    }
    
    /**
     * This method counts the number of solutions the heuristic is responsible 
     * for in the given archive. For each solution it finds, it calculates the 
     * discounted credit based on the DecayingCredits. The sum total is the 
     * credits to be assigned
     * value
     * @param population
     * @param heuristic
     * @param iteration the current iteration
     * @return 
     */
    protected double compute(Population population,Variation heuristic,int iteration){
        ndpop.clear();
        ndpop.addAll(population);
        
        double sumCredit = aggPFcredit.compute(ndpop, heuristic,iteration);
        
        if(sumCredit>0){
            return sumCredit;
        }else 
            return notInArchive;
    }
    
    @Override
    public String toString() {
        return "AggregateEArchiveCredit";
    }
    
    @Override
    public boolean isImmediate() {
       return false;
    }
    
    
        @Override
    public CreditRepository compute(Population population, Collection<Variation> heuristics,int iteration) {
        CreditRepository creditRepo = new CreditRepository(heuristics);
        for(Variation heuristic:heuristics){
            creditRepo.update(heuristic, new Credit(-1,compute(population,heuristic, iteration)));
        }
        return creditRepo;
    }
}
