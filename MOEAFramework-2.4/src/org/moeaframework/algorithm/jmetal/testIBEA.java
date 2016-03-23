/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.moeaframework.algorithm.jmetal;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.core.PopulationIO;
import org.moeaframework.core.Problem;
import org.moeaframework.core.spi.ProblemFactory;

/**
 *
 * @author nozomihitomi
 */
public class testIBEA {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Problem prob = ProblemFactory.getInstance().getProblem("DTLZ1_3");
        IBEAjMetal alg = new IBEAjMetal(prob, 105, 105, 30000);
        alg.run();
        try {
            PopulationIO.write(new File("/Users/nozomihitomi/Dropbox/MOHEA/IBEAjmetal"),alg.getArchive());
        } catch (IOException ex) {
            Logger.getLogger(testIBEA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
