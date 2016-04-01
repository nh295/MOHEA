function [p,sig] = runMWUsignificance(mResPath1,selector1,creditDef1,mResPath2,benchmark,problemName,indicator)
%Given a path, the heuristic selector name, credit definition name, and
%problem name this function will run a Mann Whitney U test (aka Wilcoxon
%rank sum test) to compare a method to the benchmark

origin = cd(mResPath1);
file1 = dir(strcat(problemName,'*',selector1,'*',creditDef1,'.mat'));
res1 = load(file1.name,'res');

cd(mResPath2)
file2 = dir(strcat(problemName,'*',benchmark,'*.mat'));
res2 = load(file2.name,'res');
cd(origin)

data1 = getfield(res1.res,indicator);
data2 = getfield(res2.res,indicator);
[p,h] = ranksum(data1,data2);
if h==1 %then significant difference and medians are different
    med_diff = median(data1)-median(data2);
    if med_diff < 0
        sig = -1;
    else
        sig = 1;
    end
else
    sig = 0;
end