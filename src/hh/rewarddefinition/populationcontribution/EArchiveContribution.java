/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.rewarddefinition.populationcontribution;

import hh.rewarddefinition.RewardDefinedOn;

/**
 * This credit definition gives credit to the specified heuristic for all the
 * solutions it created that are  in the epsilon archive
 * @author nozomihitomi
 */
public class EArchiveContribution extends ParetoFrontContribution{
     
    /**
     * The constructor needs the value for credit when a solution is in the 
     * e-archive and for when a solution is not in the e-archive
     * @param inArchive credit to assign when solution is in the archive 
     * @param notInArchive credit to assign when solution is not in the archive 
     */
    public EArchiveContribution(double inArchive, double notInArchive) {
        super(inArchive,notInArchive);
        this.operatesOn = RewardDefinedOn.ARCHIVE;
    }
    
    @Override
    public String toString() {
        return "CS-Do-A";
    }
    
}
