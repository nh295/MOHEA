function [res1]=jmetalAnalyze(path1,problem)

masterPath = 'C:\Users\SEAK2\Nozomi\MOHEA\';
origin = cd(masterPath);
javaaddpath(strcat(masterPath,'dist\MOHEA.jar'));
import org.moeaframework.*

probfactory = org.moeaframework.core.spi.ProblemFactory.getInstance();
prob = probfactory.getProblem(problem);
refset = probfactory.getReferenceSet(problem);
HVindicator = org.moeaframework.core.indicator.Hypervolume(prob,refset);
IGDIndicator = org.moeaframework.core.indicator.InvertedGenerationalDistance(prob,refset);


cd(path1);
files1 = dir('FUN*');
res1 = zeros(length(files1),2);
for i=1:length(files1)
    ndpop = loadObjs(files1(i).name);
    res1(i,1)=HVindicator.evaluate(ndpop);
    res1(i,2)=IGDIndicator.evaluate(ndpop);
end

HVindicator = [];
IGDIndicator = [];
ndpop = [];
refset = [];
prob = [];
probfactory = [];

javarmpath(strcat(masterPath,'dist\MOHEA.jar'));

cd(origin)

end

function ndpop = loadObjs(filename)
objs = dlmread(filename);
[a,~] = size(objs);
ndpop = org.moeaframework.core.NondominatedPopulation;
for j=1:a
    ndpop.add(org.moeaframework.core.Solution(objs(j,:)));
end
end
