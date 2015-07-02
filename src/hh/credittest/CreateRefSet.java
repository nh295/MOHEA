/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.credittest;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PopulationIO;
import org.moeaframework.core.Solution;

/**
 *
 * @author nozomihitomi
 */
public class CreateRefSet {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        NondominatedPopulation ndpop = new NondominatedPopulation();
        Solution soln2 = new Solution(60,4,1);
        soln2.setObjectives(new double[]{14242.336996263304,0,0,0});
        soln2.setConstraints(new double[]{0});
        Solution soln3 = new Solution(60,4,1);
        soln3.setObjectives(new double[]{0,1,1,1});
        soln3.setConstraints(new double[]{0});
        ndpop.add(soln2);
        ndpop.add(soln3);
        
        File file = new File("EOSSref.ref");
        try {
            PopulationIO.writeObjectives(file, ndpop);
        } catch (IOException ex) {
            Logger.getLogger(CreateRefSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
