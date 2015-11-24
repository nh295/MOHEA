/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.IO;

import hh.history.OperatorQualityHistory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.core.Variation;

/**
 * This class is responsible for saving the heuristic quality history and 
 * other statistics regarding heuristic quality history.
 * @author nozomihitomi
 */
public class IOQualityHistory {
    /**
     * Saves the quality history at the specified filename. The file will be a
     * list of the operator qualities at every iteration in order from 
     * beginning to end separated by the desired separator. Each row in the 
     * file will contain the history of one operator, with the operator name 
     * at the beginning of the row
     * @param qualityHistory The quality history  to save
     * @param filename filename including the path and the extension.
     * @param separator the type of separator desired
     * @return true if the save is successful
     */
    public static boolean saveHistory(OperatorQualityHistory qualityHistory,String filename,String separator) {
        try(FileWriter fw = new FileWriter(new File(filename))){
            Iterator<Variation> heuristicIter = qualityHistory.getOperators().iterator();
            while(heuristicIter.hasNext()){
                Variation heuristic = heuristicIter.next();
                Iterator<Double> historyIter= qualityHistory.getHistory(heuristic).iterator();
                String[] heuristicName = heuristic.toString().split("operator.");
                String[] splitName = heuristicName[heuristicName.length-1].split("@");
                fw.append(splitName[0]+separator);
                while(historyIter.hasNext()){
                    fw.append(Double.toString(historyIter.next()));
                    if(historyIter.hasNext())
                        fw.append(separator);
                }
                if(heuristicIter.hasNext())
                    fw.append("\n");
            }
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(IOQualityHistory.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
     /**
     * Saves the quality history at the specified filename as a java Object. The 
 file an instance of  OperatorQualityHistory
     * @param qualityHistory The quality history  to save
     * @param filename filename including the path and the extension.
     */
    public static void saveHistory(OperatorQualityHistory qualityHistory,String filename){
        try(ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(filename));){
            os.writeObject(qualityHistory);
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(IOQualityHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
    /**
     * Loads the OperatorQualityHistory instance saved by using saveHistory() from the filename. 
     * @param filename the file name (path and extension included)
     * @return the OperatorQualityHistory instance saved by using saveHistory()
     */
    public static OperatorQualityHistory loadHistory(String filename){
        OperatorQualityHistory hist = null;
        try(ObjectInputStream is = new ObjectInputStream( new FileInputStream( filename ))){
           hist = (OperatorQualityHistory)is.readObject();
        } catch (IOException ex) {
            Logger.getLogger(IOQualityHistory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(IOQualityHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hist;
    }
}
