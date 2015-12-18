%plots the boxplots of each UF1-10 problem and the IGD, fast hypervolume
%(jmetal) and the additive epsilon values for each algorithm

% problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};
problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};
% problemName = {'DTLZ1_','DTLZ2_','DTLZ3_','DTLZ4_','DTLZ7_',};
selectors = {'Probability','Adaptive'};
selectorShort = {'PM','AP'};
base = 'De';

switch base
    case {'De'}
        creditDef = {'ParentDec','Neighbor','DecompositionContribution'};
        creditShort = {'OP-De','SI-De','CS-De'};
        mode = 'MOEAD';
    case{'Do'}
        creditDef = {'ParentDom','OffspringParetoFront','OffspringEArchive','ParetoFrontContribution','EArchiveContribution'};
        creditShort = {'OP-Do','SI-Do-PF','SI-Do-A','CS-Do-PF','CS-Do-A','OP-R2','SI-R2-PF','SI-R2-A','CS-R2-PF','CS-R2-A'};
        mode = 'eMOEA';
    case{'R2'}
        creditDef = {'OPa_BIR2PARENT','OPop_BIR2PARETOFRONT','OPop_BIR2ARCHIVE','CNI_BIR2PARETOFRONT','CNI_BIR2ARCHIVE'};
        creditShort = {'OP-R2','SI-R2-PF','SI-R2-A','CS-R2-PF','CS-R2-A'};
%         creditDef = {'OPa_BIR2PARENT','OPop_BIR2ARCHIVE','CNI_BIR2PARETOFRONT','CNI_BIR2ARCHIVE'};
%         creditShort = {'OP-R2','SI-R2-A','CS-R2-PF','CS-R2-A'};
        mode = 'eMOEA';
end

% creditDef = {'ParentDec','Neighbor','DecompositionContribution',...
%     'ParentDom','OffspringParetoFront','OffspringEArchive','ParetoFrontContribution','EArchiveContribution',...
%     'OPa_BIR2PARENT','OPop_BIR2PARETOFRONT','OPop_BIR2ARCHIVE','CNI_BIR2PARETOFRONT','CNI_BIR2ARCHIVE'};
% creditShort = {'OP-De','SI-De','CS-De',...
%     'OP-Do','SI-Do-PF','SI-Do-A','CS-Do-PF','CS-Do-A','OP-R2','SI-R2-PF','SI-R2-A','CS-R2-PF','CS-R2-A',...
%     'OP-R2','SI-R2-PF','SI-R2-A','CS-R2-PF','CS-R2-A'};

path = '/Users/nozomihitomi/Dropbox/MOHEA';
% path = 'C:\Users\SEAK2\Nozomi\MOHEA\';
mres_path =strcat(path,filesep,'mRes6opsInjectionNew');
% res_path = '/Users/nozomihitomi/Desktop/untitled folder';

b = length(selectors)*length(creditDef);

h1 = figure(1); %IGD
clf(h1)
set(h1,'Position',[150, 300, 1200,600]);
hsubplot1 = cell(length(problemName),1);
for i=1:length(problemName)
    hsubplot1{i}=subplot(2,5,i);
end
h2 = figure(2); %fHV
clf(h2)
set(h2,'Position',[150, 100, 1200,600]);
hsubplot2 = cell(length(problemName),1);
for i=1:length(problemName)
    hsubplot2{i}=subplot(2,5,i);
end

leftPos = 0.03;
topPos = 0.7;
bottomPos = 0.25;
intervalPos = (1-leftPos)/5+0.005;
width = 0.16;
height = 0.2;

statsIGD = zeros(length(problemName),b,3);
statsfHV = zeros(length(problemName),b,3);

dataET = zeros(30,6,length(problemName));

for i=1:length(problemName)
    probName = problemName{i};
    [benchmarkDataIGD,label_names] = getBenchmarkVals(path,probName,'IGD',mode);
    [benchmarkDatafHV,~] = getBenchmarkVals(path,probName,'fHV',mode);
    [a,c] = size(benchmarkDataIGD);
    %box plot colors for benchmarks
    boxColors = 'rkm';
    
    dataIGD = cat(2,benchmarkDataIGD,zeros(a,b));
    datafHV = cat(2,benchmarkDatafHV,zeros(a,b));
    
    label_names_IGD=label_names;
    label_names_fHV=label_names;
    
    %check significance between best and default
    if strcmp(mode,'MOEAD')
        [p,sig] = runMWUsignificance(path,strcat(path,filesep,'Benchmarks',filesep,'MOEADDRA'),'','','best1opMOEAD',probName);
    else
        [p,sig] = runMWUsignificance(path,strcat(path,filesep,'Benchmarks',filesep,'eMOEA'),'','','best1opeMOEA',probName);
    end
    extra = '';
    if sig.IGD==1
        extra = '(-)';
        statsIGD(i,1,3) = 1;
    elseif sig.IGD==-1
        extra = '(+)';
        statsIGD(i,1,1) = 1;
    else
        statsIGD(i,1,2) = 1;
    end
    extra = '';
    if sig.fHV==1
        extra = '(+)';
        statsfHV(i,1,1) = 1;
    elseif sig.fHV==-1
        extra = '(-)';
        statsfHV(i,1,3) = 1;
    else
        statsfHV(i,1,2) = 1;
    end
    label_names_IGD{1} = strcat(label_names_IGD{1},extra);
    label_names_fHV{1} = strcat(label_names_fHV{1},extra);
    
    %check significance between rand and best
    if strcmp(mode,'MOEAD')
        [p,sig] = runMWUsignificance(path,strcat(path,filesep,'Benchmarks',filesep,'Random',mode),'','','best1opMOEAD',probName);
    else
        [p,sig] = runMWUsignificance(path,strcat(path,filesep,'Benchmarks',filesep,'Random',mode),'','','best1opeMOEA',probName);
    end
    extra = '';
    if sig.IGD==1
        extra = '(-)';
        statsIGD(i,3,3) = 1;
    elseif sig.IGD==-1
        extra = '(+)';
        statsIGD(i,3,1) = 1;
    else
        statsIGD(i,3,2) = 1;
    end
    extra = '';
    if sig.fHV==1
        extra = '(+)';
        statsfHV(i,3,1) = 1;
    elseif sig.fHV==-1
        extra = '(-)';
        statsfHV(i,3,3) = 1;
    else
        statsfHV(i,3,2) = 1;
    end
    label_names_IGD{3} = strcat(label_names_IGD{3},extra);
    label_names_fHV{3} = strcat(label_names_fHV{3},extra);
    
    
    for j=1:length(selectors)
        for k=1:length(creditDef)
            c = c+1;
            file = strcat(mres_path,filesep,probName,'_',selectors{j},'_',creditDef{k},'.mat');
            load(file,'res'); %assume that the reults stored in vairable named res
            dataIGD(:,c) = res.IGD;
            datafHV(:,c) = res.fHV;
            dataET(:,c-3,i) = res.ET;
            
            %             dataInj(:,c-size(benchmarkDataIGD,2)) = res.Inj;
            if strcmp(mode,'MOEAD')
%                 [p,sig] = runMWUsignificance(path,mres_path,selectors{j},creditDef{k},'best1opMOEAD',probName);
                [p,sig] = runMWUsignificance(path,mres_path,selectors{j},creditDef{k},'RandomMOEAD',probName);
            else
%                 [p,sig] = runMWUsignificance(path,mres_path,selectors{j},creditDef{k},'best1opeMOEA',probName);
%                 [p,sig] = runMWUsignificance(path,selectors{j},creditDef{k},'eMOEA',probName);
                [p,sig] = runMWUsignificance(path,mres_path,selectors{j},creditDef{k},'RandomeMOEA',probName);
            end
            extra = '';
            if sig.IGD==1
                extra = '(-)';
                statsIGD(i,c,3) = 1;
            elseif sig.IGD==-1
                extra = '(+)';
                statsIGD(i,c,1) = 1;
            else
                statsIGD(i,c,2) = 1;
            end
            label_names_IGD = [label_names_IGD,strcat(selectorShort{j},'-',creditShort{k},extra)]; %concats the labels
            extra = '';
            if sig.fHV==1
                extra = '(+)';
                statsfHV(i,c,1) = 1;
            elseif sig.fHV==-1
                extra = '(-)';
                statsfHV(i,c,3) = 1;
            else
                statsfHV(i,c,2) = 1;
            end
            label_names_fHV = {label_names_fHV{:},strcat(selectorShort{j},'-',creditShort{k},extra)}; %concats the labels
            boxColors = strcat(boxColors,'b');
        end
    end
    
    if strcmp(probName,'UF1_')
        probName = 'UF1';
    end
    
    figure(h1) 
    [~,ind]=min(mean(dataIGD,1));
    label_names_IGD{ind} = strcat('\bf{',label_names_IGD{ind},'}');
    boxplot(hsubplot1{i},dataIGD,label_names_IGD,'colors',boxColors,'boxstyle','filled','medianstyle','target','symbol','+')
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
    
    figure(h2)
    [~,ind]=max(mean(datafHV,1));
    label_names_fHV{ind} = strcat('\bf{',label_names_fHV{ind},'}');
    boxplot(hsubplot2{i},datafHV,label_names_fHV,'colors',boxColors,'boxstyle','filled','medianstyle','target','symbol','+')
    set(hsubplot2{i},'TickLabelInterpreter','tex');
    set(hsubplot2{i},'XTickLabelRotation',90);
    set(hsubplot2{i},'FontSize',13);
    title(hsubplot2{i},probName)
    %ensures all boxplot axes are the same size and aligned.
    if i<6
        set(hsubplot2{i},'Position',[leftPos+intervalPos*(i-1),topPos,width,height]);
    else
        set(hsubplot2{i},'Position',[leftPos+intervalPos*(i-6),bottomPos,width,height]);
    end
end

statsIGD = squeeze(sum(statsIGD,1))
statsfHV = squeeze(sum(statsfHV,1))
disp('mean time')
avgTime = squeeze(mean(dataET,1))';
stdTime = squeeze(std(dataET,1))';
% % 


saveas(h1,strcat(base,'_IGD'),'fig');
saveas(h2,strcat(base,'_HV'),'fig');
