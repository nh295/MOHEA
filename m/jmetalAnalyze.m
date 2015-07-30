function [res1,res2]=jmetalAnalyze(path1,path2,problem)

masterPath = 'C:\Users\SEAK2\Nozomi\MOHEA\mRes';
javaaddpath('\dist\MOHEA.jar');
import org.moeaframework.*

probfactory = org.moeaframework.core.spi.ProblemFactory.getInstance();
prob = probfactory.getProblem(problem);
refset = probfactory.getReferemceSet(problem);
HVindicator = org.moeaframework.core.indicator.Hypervolume(prob,refset);
IGDIndicator = org.moeaframework.core.indicator.InvertedGenerationalDistance(prob,refset);


origin = cd(path1);
files1 = dir('FUN*');
res1 = zeros(length(files1),2);
for i=1:length(files)
    ndpop = loadObjs(files1);
    res1(i,1)=HVindicator.evaluate(ndpop);
    res1(i,2)=IGDIndicator.evaluate(ndpop);
end

cd(path2)
files2 = dir('FUN*');
res2 = zeros(length(files2),2);
for i=1:length(files)
    ndpop = loadObjs(files1);
    res2(i,1)=HVindicator.evaluate(ndpop);
    res2(i,2)=IGDIndicator.evaluate(ndpop);
end
cd(origin)

javarmpath('\dist\MOHEA.jar');


end

function ndpop = loadObjs(filename)
objs = dlmread(strcat(path1,filesep,filename));
[a,~] = size(objs);
ndpop = org.moeaframework.core.NondominatedPopulation;
for j=1:a
    ndpop.add(Solution(objs(j,:)));
end
end
