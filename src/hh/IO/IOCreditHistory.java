/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.IO;

import hh.history.CreditHistory;
import hh.rewarddefinition.Reward;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.core.Variation;

/**
 * This class is responsible for saving the history of credits received by
 * operators and other statistics regarding operators credit history.
 *
 * @author nozomihitomi
 */
public class IOCreditHistory {

    /**
     * Saves the credit history at the specified filename. The file will be a a
     * dlm file with n rows to represent the n iterations. Each column will have
     * the credits received in the ith iteration by the mth operator. If no
     * credit was received a -1 will be stored to differentiate it from a 0
     * credit
     *
     * @param creditHistory The quality history to save
     * @param filename filename including the path and the extension.
     * @param separator the type of separator desired
     * @return true if the save is successful
     */
    public static boolean saveHistory(CreditHistory creditHistory, String filename, String separator) {
        Collection<Variation> operators = creditHistory.getOperators();
        try (FileWriter fw = new FileWriter(new File(filename))) {
            for(Variation oper:operators){
                Collection<Reward> hist = creditHistory.getHistory(oper);
                int[] iters = new int[hist.size()];
                double[] vals = new double[hist.size()];
                Iterator<Reward> iter = hist.iterator();
                Reward reward = iter.next();
                iters[0]=reward.getIteration();
                vals[0]=reward.getValue();
                int prevInteration=0;
                while(iter.hasNext()){
                    Reward nextReward = iter.next();
                    int iteration = nextReward.getIteration();
                    double rewardVal = nextReward.getValue();
                    if(iters[prevInteration]==iteration){
                        vals[prevInteration]+=rewardVal;
                    }else{
                        prevInteration++;
                        iters[prevInteration] = iteration;
                        vals[prevInteration] = rewardVal;
                    }
                }
                
                fw.append("iteration" + separator);
                for(int i=0;i<prevInteration;i++){
                    fw.append(Integer.toString(iters[i]) + separator);
                }
                fw.append(Integer.toString(iters[prevInteration]) + "\n");
                
                String[] operatorName = oper.toString().split("operator.");
                String[] splitName = operatorName[operatorName.length - 1].split("@");
                fw.append(splitName[0] + separator);
                for(int i=0;i<prevInteration;i++){
                    fw.append(Double.toString(vals[i]) + separator);
                }
                fw.append(Double.toString(vals[prevInteration]) + "\n");
                
            }
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(IOQualityHistory.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
//                int maxIter = creditHistory.getMaxIteration()+1;
//        //Create matrix of data (rows are operators)(columns are credits)
//        double[][] data = new double[creditHistory.getOperators().size()][maxIter];
//        int operNum=0;
//        Collection<Variation> operators = creditHistory.getOperators();
//        for(Variation operator: creditHistory.getOperators()){
//            Arrays.fill(data[operNum], -1.0);
//            Iterator<Reward> iter = creditHistory.getHistory(operator).iterator();
//            while(iter.hasNext()){
//                Reward reward = iter.next();
//                int iteration = reward.getIteration();
//                if(data[operNum][iteration]==-1){
//                    data[operNum][iteration]=reward.getValue();
//                }else{
//                    data[operNum][iteration]+=reward.getValue();
//                }
//            }
//            operNum++;
//        }
//        
//        try (FileWriter fw = new FileWriter(new File(filename))) {
//            //write the header of the file
//            fw.append("iteration" + separator);
//            Iterator<Variation> iter = operators.iterator();
//            for(int i=0;i<operators.size()-1;i++){
//                Variation operator = iter.next();
//                String[] operatorName = operator.toString().split("operator.");
//                String[] splitName = operatorName[operatorName.length - 1].split("@");
//                fw.append(splitName[0] + separator);
//            }//print out last operator name without separator
//            Variation operator = iter.next();
//            String[] operatorName = operator.toString().split("operator.");
//            String[] splitName = operatorName[operatorName.length - 1].split("@");
//            fw.append(splitName[0] + separator);
//            fw.append("\n");
//            
//            for (int i = 0; i < maxIter; i++) {//go over iterations
//                fw.append(Integer.toString(i)+ separator);
//                for (int j = 0; j < operators.size() - 1; j++) {//go over operators
//                    fw.append(Double.toString(data[j][i]) + separator);
//                }
//                fw.append(Double.toString(data[operators.size() - 1][i]));
//                fw.append("\n");
//            }
//            fw.flush();
//        } catch (IOException ex) {
//            Logger.getLogger(IOQualityHistory.class.getName()).log(Level.SEVERE, null, ex);
//            return false;
//        }
        return true;
    }

    /**
     * Saves the credit history at the specified filename as a java Object. The
     * file an instance of CreditHistory
     *
     * @param creditHistory The quality history to save
     * @param filename filename including the path and the extension.
     */
    public static void saveHistory(CreditHistory creditHistory, String filename) {
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(filename));) {
            os.writeObject(creditHistory);
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(IOCreditHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Loads the CreditHistory instance saved by using saveHistory() from the
     * filename.
     *
     * @param filename the file name (path and extension included)
     * @return the CreditHistory instance saved by using saveHistory()
     */
    public static CreditHistory loadHistory(String filename) {
        CreditHistory hist = null;
        try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(filename))) {
            hist = (CreditHistory) is.readObject();
        } catch (IOException ex) {
            Logger.getLogger(IOCreditHistory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(IOCreditHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hist;
    }
}
