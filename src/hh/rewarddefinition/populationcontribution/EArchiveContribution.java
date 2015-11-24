/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.rewarddefinition.populationcontribution;

import hh.rewarddefinition.Reward;
import hh.rewarddefinition.RewardDefinedOn;
import java.util.Collection;
import java.util.HashMap;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * This credit definition gives credit to the specified heuristic for all the
 * solutions it created that are  in the epsilon archive
 * @author nozomihitomi
 */
public class EArchiveContribution extends AbstractPopulationContribution{
    private final ParetoFrontContribution aggPFcredit;
    
    /**
     * The amount of credit to give if operator did not create any solutions in archive
     */
    private final double notInArchive;
    
    /**
     * The amount of credit to give for every solution is in Archive
     */
    private final double inArchive;
     
    /**
     * The constructor needs the value for credit when a solution is in the 
     * e-archive and for when a solution is not in the e-archive
     * @param inArchive credit to assign when solution is in the archive 
     * @param notInArchive credit to assign when solution is not in the archive 
     */
    public EArchiveContribution(double inArchive, double notInArchive) {
        super();
        operatesOn = RewardDefinedOn.ARCHIVE;
        this.notInArchive = notInArchive;
        this.inArchive = inArchive;
        //by giving AggregateParetoFrontCredit a 1,0 score we can count how many
        //solutions per rank a heuristic is responsible for
        aggPFcredit = new ParetoFrontContribution(this.inArchive, 0.0);
    }
    
    /**
     * This method counts the number of solutions the heuristic is responsible 
     * for in the given archive. For each solution it finds, it calculates the 
     * discounted credit based on the DecayingCredits. The sum total is the 
     * credits to be assigned
     * value
     * @param archive
     * @param heuristic
     * @param iteration the current iteration
     * @return 
     */
    protected double compute(Iterable<Solution> archive,Variation heuristic,int iteration){
        double sumCredit = aggPFcredit.compute(archive, heuristic,iteration);
        if(sumCredit>0){
            return sumCredit;
        }else 
            return notInArchive;
    }
    
    @Override
    public String toString() {
        return "EArchiveContribution";
    }
    
    /**
     * 
     * @param population the archive to check 
     * @param enteringSolutions not used
     * @param removedSolutions not used
     * @param heuristics
     * @param iteration
     * @return 
     */
    @Override
    public HashMap<Variation, Reward> compute(NondominatedPopulation population,Collection<Solution> enteringSolutions,Collection<Solution> removedSolutions, Collection<Variation> heuristics, int iteration) {
        HashMap<Variation,Reward> credits = new HashMap();
        for(Variation heuristic:heuristics){
            credits.put(heuristic, new Reward(iteration,compute(population,heuristic, iteration)));
        }
        return credits;
    }
}
