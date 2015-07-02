function [HV] = getIndicatorForRefSet(path2Jar,problemName)
%This function gets the hypervolume for the reference set
%the filename should  contain the path and the extension.
%The path is where the jar file is stored


origin = cd(path2Jar);
javaaddpath HHCreditTest.jar
import org.moeaframework.*
cd(origin)

probFactory = org.moeaframework.core.spi.ProblemFactory.getInstance;
problem = probFactory.getProblem(problemName);
refPop = probFactory.getReferenceSet(problemName);
hyperVolumeObj = org.moeaframework.core.indicator.Hypervolume(problem,refPop);
list = java.util.ArrayList;
iterator = refPop.iterator;
while iterator.hasNext
    list.add(iterator.next);
end
HV = double(hyperVolumeObj.calculateHypervolume(list,list.size,problem.getNumberOfObjectives));