%plots the boxplots of each UF1-10 problem and the IGD, fast hypervolume
%(jmetal) and the additive epsilon values for each algorithm

problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};
selectors = {'Probability','Adaptive'};
selectorShort = {'PM','AP'};
creditDef = { 'Parent','OffspringParetoFront','OffspringEArchive','ParetoFrontContribution','EArchiveContribution'};
creditShort = {'ODP','OPopF','OPopEA','CPF','CEA'};

% path ='/Users/nozomihitomi/Dropbox/MOHEA/mres';
path = 'C:\Users\SEAK2\Nozomi\MOHEA\mResDEopsInjection';

b = length(selectors)*length(creditDef);

h1 = figure(1); %IGD
h2 = figure(2); %fHV
h3 = figure(3); %AEI
h4 = figure(4); %# injections

%box plot colors for benchmarks
boxColors = 'rgkcm';

for i=1:length(problemName)
    probName = problemName{i};
    [benchmarkDataIGD,label_names] = getBenchmarkVals(probName,'IGD');
    [benchmarkDatafHV,~] = getBenchmarkVals(probName,'fHV');
    [benchmarkDataAEI,~] = getBenchmarkVals(probName,'AEI');
    [a,c] = size(benchmarkDataIGD);
    dataIGD = cat(2,benchmarkDataIGD,zeros(a,b));
    datafHV = cat(2,benchmarkDatafHV,zeros(a,b));
    dataAEI = cat(2,benchmarkDataAEI,zeros(a,b));
    dataInj = zeros(a,b);
    
    for j=1:length(selectors)
        for k=1:length(creditDef)
            c = c+1;
            file = strcat(path,filesep,probName,'_',selectors{j},'_',creditDef{k},'.mat');
            load(file,'res'); %assume that the reults stored in vairable named res
            dataIGD(:,c) = res.IGD;
            datafHV(:,c) = res.fHV;
            dataAEI(:,c) = res.AEI;
            dataInj(:,c-size(benchmarkDataIGD,2)) = res.Inj;
            label_names = {label_names{:},strcat(selectorShort{j},'_',creditShort{k})}; %concats the labels
            boxColors = [boxColors,'b'];
        end
    end
    
    figure(h1)
    subplot(2,5,i);
    boxplot(dataIGD,label_names,'labelorientation','inline','colors',boxColors,'plotstyle','compact')
    title(probName)
    
    figure(h2)
    subplot(2,5,i);
    boxplot(datafHV,label_names,'labelorientation','inline','colors',boxColors,'plotstyle','compact')    
    title(probName)
    
    figure(h3)
    subplot(2,5,i);
    boxplot(dataAEI,label_names,'labelorientation','inline','colors',boxColors,'plotstyle','compact')
    title(probName)
    
    figure(h4)
    subplot(2,5,i);
    boxplot(dataInj,{label_names{size(benchmarkDataIGD,2)+1:end}},'labelorientation','inline','colors',boxColors,'plotstyle','compact')
    title(probName)
    hold off
end