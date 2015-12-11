/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.IO;


import hh.history.OperatorSelectionHistory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.core.Variation;

/**
 * This class is responsible for saving the heuristic selection history and 
 * other statistics regarding heuristic selection history.
 * @author nozomihitomi
 */
public class IOSelectionHistory implements Serializable{
    private static final long serialVersionUID = -1566649977387423354L;
    
    public IOSelectionHistory(){
        super();
    }
    
    /**
     * Saves the selection history at the specified filename. The file will be a
     * list of the heuristics selected in order from beginning to end separated
     * by the desired separator
     * @param history The history to save
     * @param filename filename including the path and the extension.
     * @param separator the type of separator desired
     * @return true if the save is successful
     */
    public  boolean saveHistory(OperatorSelectionHistory history,String filename,String separator) {
        try(FileWriter fw = new FileWriter(new File(filename))){
            ArrayList<Variation> orderedHistory = history.getOrderedHistory();
            ArrayList<Integer> orderedTime = history.getOrderedSelectionTime();

            for(int i = 0; i < orderedHistory.size(); i++) {
                fw.append(Integer.toString(orderedTime.get(i)));
                fw.append(separator);
                String[] heuristicName = orderedHistory.get(i).toString().split("operator.");
                String[] splitName = heuristicName[heuristicName.length - 1].split("@");
                fw.append(splitName[0]);
                if(!orderedHistory.isEmpty())
                    fw.append("\n");
            }
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(IOSelectionHistory.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    /**
     * Saves the number of times an operator is selected to the specified filename. The file will be a
     * list of the heuristics selected in order from beginning to end separated
     * by the desired separator
     * @param history The history to save
     * @param filename filename including the path and the extension.
     * @param separator the type of separator desired
     * @return true if the save is successful
     */
    public boolean saveSelectionCount(OperatorSelectionHistory history,String filename,String separator) {
        try(FileWriter fw = new FileWriter(new File(filename))){
            Collection<Variation> operators = history.getOperators();
            for(Variation operator:operators){
                String[] heuristicName = operator.toString().split("operator.");
                String[] splitName = heuristicName[heuristicName.length-1].split("@");
                fw.append(splitName[0]+separator);
                fw.append(Integer.toString(history.getSelectedTimes(operator))+"\n");
            }
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(IOSelectionHistory.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    /**
     * Saves the OperatorSelectionHistory  at the specified filename as a java Object. The 
 file an instance of  IOperatorSelectionHistory
     * @param history The credit repository to save
     * @param filename filename including the path and the extension.
     */
    public void saveHistory(OperatorSelectionHistory history,String filename){
        try(ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(filename));){
            os.writeObject(history);
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(IOSelectionHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
    /**
     * Loads the IOperatorSelectionHistory instance saved by using saveHistory() from the filename. 
     * @param filename the file name (path and extension included)
     * @return the CreditRepository instance saved by using saveHistory()
     */
    public OperatorSelectionHistory loadHistory(String filename){
        OperatorSelectionHistory history = null;
        try(ObjectInputStream is = new ObjectInputStream( new FileInputStream( filename ))){
           history = (OperatorSelectionHistory)is.readObject();
        } catch (IOException ex) {
            Logger.getLogger(IOSelectionHistory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(IOSelectionHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return history;
    }
    
    /**
     * Saves the frequency of selection for each heuristic in the stored 
     * selection to the desired filename. Each entry is saved as the heuristic 
     * name and the number of times that heuristic was selected, and is 
     * separated by the desired separator.
     * @param history The history to save
     * @param filename name of the file with extension
     * @param separator the desired separator
     * @return True if save is successful, otherwise false
     */
    public boolean saveSelectionFrequency(OperatorSelectionHistory history,String filename,String separator) {
        try(FileWriter fw = new FileWriter(new File(filename))){
            Iterator<Variation> iter = history.getOperators().iterator();
            while(iter.hasNext()){
                Variation heuristic = iter.next();
                fw.append(heuristic.toString()+separator+history.getSelectedTimes(heuristic));
                if(iter.hasNext())
                    fw.append(separator);
            }
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(IOSelectionHistory.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
}
