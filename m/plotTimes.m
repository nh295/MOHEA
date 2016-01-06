
%plots the boxplots of each UF1-10 problem and the elapsed times for each algorithm

% problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};
problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};
selectors = {'Probability'};
selectorShort = {'PM'};

creditDef = {'ParentDec','Neighbor','DecompositionContribution',...
    'ParentDom','OffspringParetoFront','OffspringEArchive','ParetoFrontContribution','EArchiveContribution',...
    'OPa_BIR2PARENT','OPop_BIR2PARETOFRONT','OPop_BIR2ARCHIVE','CNI_BIR2PARETOFRONT','CNI_BIR2ARCHIVE'};
creditShort = {'OP-De','SI-De','CS-De',...
    'OP-Do','SI-Do-PF','SI-Do-A','CS-Do-PF','CS-Do-A',...
    'OP-R2','SI-R2-PF','SI-R2-A','CS-R2-PF','CS-R2-A'};

path = '/Users/nozomihitomi/Dropbox/MOHEA';
mres_path =strcat(path,filesep,'mResTime');

h1 = figure(1); 
clf(h1)
set(h1,'Position',[150, 300, 1200,600]);
hsubplot1 = cell(length(problemName),1);
for i=1:length(problemName)
    hsubplot1{i}=subplot(2,5,i);
end

leftPos = 0.03;
topPos = 0.7;
bottomPos = 0.25;
intervalPos = (1-leftPos)/5+0.005;
width = 0.16;
height = 0.2;

b = length(selectors)*length(creditDef);

meanET = zeros(10,b);
stdET = zeros(10,b);
for i=1:length(problemName)
    probName = problemName{i};
    %box plot colors for benchmarks
    
    dataET = zeros(30,b);
    c = 0;
    label_names_ET = {};
    for j=1:length(selectors)
        for k=1:length(creditDef)
            c = c+1;
            file = strcat(mres_path,filesep,probName,'_',selectors{j},'_',creditDef{k},'.mat');
            load(file,'res'); %assume that the reults stored in vairable named res
            dataET(:,c) = res.ET;
            meanET(i,c) = mean(res.ET);
            stdET(i,c) = std(res.ET);
            label_names_ET = [label_names_ET,strcat(selectorShort{j},'-',creditShort{k})]; %concats the labels
        end
    end
    
    if strcmp(probName,'UF1_')
        probName = 'UF1';
    end
    
    figure(h1) 
    boxplot(hsubplot1{i},dataET,label_names_ET,'boxstyle','filled','medianstyle','target','symbol','+')
    set(hsubplot1{i},'TickLabelInterpreter','tex');
    set(hsubplot1{i},'XTickLabelRotation',90);
    set(hsubplot1{i},'FontSize',13)
    title(hsubplot1{i},probName)
    %ensures all boxplot axes are the same size and aligned.
    if i<6
        set(hsubplot1{i},'Position',[leftPos+intervalPos*(i-1),topPos,width,height]);
    else
        set(hsubplot1{i},'Position',[leftPos+intervalPos*(i-6),bottomPos,width,height]);
    end
end

    saveas(h1,'ET','png');
