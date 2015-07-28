function [data,labels,p,avg1,avg2] = runKWsignificance(path,selector1,creditDef1,benchmark,problemName)
%Given a path, the heuristic selector name, credit definition name, and
%problem name this function will run a kruskal wallice significance test on
%a specified metric

origin = cd(strcat(path,filesep,'mRes'));
file1 = dir(strcat(problemName,'_',selector1,'_',creditDef1,'.mat'));
res1 = load(file1.name,'res');

cd(strcat(path,filesep,'Benchmarks',filesep,benchmark))
file2 = dir(strcat(problemName,'_',benchmark,'.mat'));
res2 = load(file2.name,'res');
cd(origin)

ntrials = length(res1.res.IGD);
tmp = cell(ntrials,2);
for i=1:ntrials
    tmp{i,1} = '1';
    tmp{i,2} = '2';
end
labels = [tmp(:,1);tmp(:,2)];

data = [res1.res.IGD;res2.res.IGD];
avg1 = mean(res1.res.IGD);
avg2 = mean(res2.res.IGD);

if isnan(avg1)
    error(sprintf('Avg1 is a nan: %s',file1.name));
end
if isnan(avg2)
    error(sprintf('Avg2 is a nan: %s',file2.name));
end
p = kruskalwallis(data,labels,'off');