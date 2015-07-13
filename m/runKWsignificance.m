function [out,p,avg1,avg2] = runKWsignificance(path,selector1,creditDef1,selector2,creditDef2,problemName)
%Given a path, the heuristic selector name, credit definition name, and
%problem name this function will run a kruskal wallice significance test on
%a specified metric

origin = cd(path);
file1 = dir(strcat(problemName,'_',selector1,'_',creditDef1,'.mat'));
res1 = load(file1.name,'res');
file2 = dir(strcat(problemName,'_',selector2,'_',creditDef2,'.mat'));
res2 = load(file2.name,'res');
cd(origin)

ntrials = length(res1.res.HV);
tmp = cell(ntrials,2);
for i=1:ntrials
    tmp{i,1} = '1';
    tmp{i,2} = '2';
end
labels = [tmp(:,1);tmp(:,2)];

data = [res1.res.HV;res2.res.HV];
avg1 = mean(res1.res.HV);
avg2 = mean(res2.res.HV);
if avg1>avg2
    out = -1;
elseif avg1<avg2
    out = 1;
else
    out = 0;
end
p = kruskalwallis(data,labels,'off');