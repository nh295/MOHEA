%Cumulative nondominated population
%add up all the nondominated populations together from one optimization
%method and plot it against the true Pareto front.


selector = 'NSGAII';
creditDef = 'NSGAII';
problemName = 'UF5';
path = 'results';

origin = cd(path);
files = dir('*.NDpop');
cumNDPop = [];
for i=1:length(files)
    a = strfind(files(i).name,selector);
    b = strfind(files(i).name,creditDef);
    c = strfind(files(i).name,problemName);
    if ~isempty(a) && ~isempty(b) && ~isempty(c)
        cumNDPop = [cumNDPop;readObjectives(files(i).name,2)];
    end
end

cd(origin)
cd('pf')
pffiles = dir([problemName,'*']);
truePF = readObjectives(pffiles(1).name,2);

cd(origin)


figure(1)
plot(truePF(:,1),truePF(:,2),'ob')
hold on
plot(cumNDPop(:,1),cumNDPop(:,2),'or')
legend(['True PF:',problemName],[selector,creditDef,problemName])
hold off
shg

