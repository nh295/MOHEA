/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.IO;

import hh.rewarddefinition.Reward;
import hh.creditrepository.CreditHistoryRepository;
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
 * This class is responsible for saving the heuristic credit history and 
 * other statistics regarding heuristic credit history.
 * @author nozomihitomi
 */
public class IOCreditHistory {
    /**
     * Saves the credit history at the specified filename. The file will be a
     * list of the credits received by each heuristic selected in order from 
     * beginning to end separated by the desired separator. Each row in the 
     * file will contain the history of one heuristic, with the heuristic name 
     * at the beginning of the row
     * @param creditRepo The credit repository to save
     * @param filename filename including the path and the extension.
     * @param separator the type of separator desired
     * @return true if the save is successful
     */
    public static boolean saveHistory(CreditHistoryRepository creditRepo,String filename,String separator) {
        try(FileWriter fw = new FileWriter(new File(filename))){
            Iterator<Variation> heuristicIter = creditRepo.getHeuristics().iterator();
            while(heuristicIter.hasNext()){
                Variation heuristic = heuristicIter.next();
                Iterator<Reward> historyIter= creditRepo.getHistory(heuristic).getHistory().iterator();
                String[] heuristicName = heuristic.toString().split("operator.");
                String[] splitName = heuristicName[heuristicName.length-1].split("@");
                fw.append(splitName[0]+separator);
                while(historyIter.hasNext()){
                    fw.append(Double.toString(historyIter.next().getValue()));
                    if(historyIter.hasNext())
                        fw.append(separator);
                }
                if(heuristicIter.hasNext())
                    fw.append("\n");
            }
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(IOCreditHistory.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
     /**
     * Saves the credit history at the specified filename as a java Object. The 
     * file an instance of  CreditHistoryRepository
     * @param creditRepo The credit repository to save
     * @param filename filename including the path and the extension.
     */
    public static void saveHistory(CreditHistoryRepository creditRepo,String filename){
        try(ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(filename));){
            os.writeObject(creditRepo);
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(IOCreditHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
    /**
     * Loads the CreditRepository instance saved by using saveHistory() from the filename. 
     * @param filename the file name (path and extension included)
     * @return the CreditRepository instance saved by using saveHistory()
     */
    public static CreditHistoryRepository loadHistory(String filename){
        CreditHistoryRepository repo = null;
        try(ObjectInputStream is = new ObjectInputStream( new FileInputStream( filename ))){
           repo = (CreditHistoryRepository)is.readObject();
        } catch (IOException ex) {
            Logger.getLogger(IOCreditHistory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(IOCreditHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return repo;
    }
}
