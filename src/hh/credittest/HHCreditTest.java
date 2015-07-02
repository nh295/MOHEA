/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package hh.credittest;

import hh.creditdefinition.immediate.ImmediateEArchiveCredit;
import hh.creditdefinition.ICreditDefinition;
import hh.creditdefinition.aggregate.AggregateEArchiveCredit;
import hh.creditdefinition.aggregate.AggregateParetoFrontCredit;
import hh.creditdefinition.aggregate.AggregateParetoRankCredit;
import hh.creditdefinition.immediate.ParentDominationCredit;
import hh.creditdefinition.immediate.ImmediateParetoFrontCredit;
import hh.creditdefinition.immediate.ImmediateParetoRankCredit;
import hh.credithistory.CreditHistory;
import hh.creditrepository.CreditHistoryRepository;
import hh.heuristicselectors.AdaptivePursuit;
import hh.heuristicselectors.DMAB;
import hh.heuristicselectors.ProbabilityMatching;
import hh.heuristicselectors.RandomSelect;
import hh.nextheuristic.INextHeuristic;
import java.util.ArrayList;
import java.util.Properties;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Variation;
import org.moeaframework.core.spi.OperatorFactory;
import org.moeaframework.core.spi.ProblemFactory;
import org.moeaframework.util.TypedProperties;

/**
 *
 * @author nozomihitomi
 */
public class HHCreditTest {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        String[] problems = new String[]{"UF1","UF2","UF3","UF4","UF5","UF6","UF7","UF8","UF9","UF10"};
        String[] problems = new String[]{" "};
        String[] epsilons = new String[]{"0.001","0.005","0.0008","0.005","0.000001","0.000001","0.005","0.0045","0.008","0.001"};
        
        for(int j=0;j<problems.length;j++){
            
            String path;
            if(args.length == 0 )
                path = "/Users/nozomihitomi/Dropbox/MOHEA";
            else
                path = args[0];
            
//            String probName = "UF"+args[1];
             String probName = "UF5";
//            String probName = problems[j];
            System.out.println(probName);
            Problem prob = ProblemFactory.getInstance().getProblem(probName);
            
            String eps = epsilons[j];
            int nobjs = prob.getNumberOfObjectives();
            String epsilon = "";
            double[] epsilonDouble = new double[nobjs];
            for(int i=0;i<nobjs;i++){
                epsilon+=eps;
                if(i<nobjs-1)
                    epsilon+=",";
                epsilonDouble[i]=Double.parseDouble(eps);
            }
            
            //Setup algorithm parameters
            Properties prop = new Properties();
            prop.put("populationSize", "100");
//            prop.put("alpha", args[2]);
            prop.put("alpha", "0.6");
            prop.put("epsilon", epsilon);
            
            int numberOfSeeds = 2;
            int maxEvaluations = 10100;
            
            //Choose heuristics to be applied
            ArrayList<Variation> heuristics = new ArrayList<>();
            OperatorFactory of = OperatorFactory.getInstance();
            Properties heuristicProp = new Properties();
            heuristics.add(of.getVariation("um", heuristicProp, prob));
            heuristics.add(of.getVariation("sbx", heuristicProp, prob));
            heuristics.add(of.getVariation("de", heuristicProp, prob));
            heuristics.add(of.getVariation("pcx", heuristicProp, prob));
            heuristics.add(of.getVariation("undx", heuristicProp, prob));
            heuristics.add(of.getVariation("spx", heuristicProp, prob));
            
            
            //setup algorithm
            double pmin = 0.05;
            CreditHistoryRepository creditRepo = new CreditHistoryRepository(heuristics, new CreditHistory());
            INextHeuristic[] selectors= new INextHeuristic[]{
                new RandomSelect(creditRepo),
                new ProbabilityMatching(creditRepo, pmin),
                new AdaptivePursuit(creditRepo, pmin),
                new DMAB(creditRepo, 0.01, 0.1, 10)
            };
            
            ICreditDefinition[] credDef = new ICreditDefinition[]{
                new ParentDominationCredit(1, 0, 0),
                new ImmediateParetoFrontCredit(1.0, 0.0),
                new ImmediateEArchiveCredit(1, 0,epsilonDouble),
                new ImmediateParetoRankCredit(1.0, 0.0,5),
                new AggregateParetoFrontCredit(1.0,0.0),
                new AggregateEArchiveCredit(1.0, 0.0, epsilonDouble),
                new AggregateParetoRankCredit(1.0, 0.0,5)
            };
            
            for(INextHeuristic selector : selectors) {
                for (ICreditDefinition credDef1 : credDef) {
                    //loop through the set of algorithms to experiment with
                    TestRun test = new TestRun(path, prob, probName, 
                            new TypedProperties(prop), selector, credDef1, 
                            heuristics, epsilonDouble, maxEvaluations);
                    
                    //create a list to use foreach method
                    ArrayList<Integer> parList = new ArrayList<>(numberOfSeeds);
                    for(int k=0;k<numberOfSeeds;k++){
                        parList.add(k);
                        test.run();
                    }
//                    parList.stream().forEach(testRunz -> {
//                        test.run();
//                    });
                    
                    System.out.println("Finished "+prob.getName() + "_"+ selector+"_"+credDef1+"\n\n");
                }
            }
        }
    }
}
