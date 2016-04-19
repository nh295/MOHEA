% plots the boxplots of each UF1-10 problem and the IGD, fast hypervolume
%(jmetal) and the additive epsilon values for each algorithm

% problemName = {'DTLZ7'};
% problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};
% problemName = {'WFG1','WFG2','WFG3','WFG4','WFG5','WFG6','WFG7','WFG8','WFG9'};
% problemName = {'DTLZ1_','DTLZ2_','DTLZ3_','DTLZ4_','DTLZ5_','DTLZ6_','DTLZ7_'};
problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10',...
            'WFG1','WFG2','WFG3','WFG4','WFG5','WFG6','WFG7','WFG8','WFG9',...
            'DTLZ1_','DTLZ2_','DTLZ3_','DTLZ4_','DTLZ5_','DTLZ6_','DTLZ7_'};
selectors = {'Probability','Adaptive'};
% selectors = {'Probability'};
selectorShort = {'PM','AP'};
% selectorShort = {'PM'};
base = 'I';

switch base
    case {'De'}
        creditDef = {'OP-De','SI-De','CS-De'};
        mode = 'MOEAD';
    case{'Do'}
        creditDef = {'OP-Do','SI-PF','CS-Do-PF'};
        mode = 'NSGAII';
    case{'I'}
        creditDef = {'OP-I','SI-I','CS-I'};
        mode = 'IBEA';
end

path = '/Users/nozomihitomi/Dropbox/MOHEA';
% path = 'C:\Users\SEAK2\Nozomi\MOHEA';
mres_path =strcat(path,filesep,'mResExperimentB2');
% res_path = '/Users/nozomihitomi/Desktop/untitled folder';

b = length(selectors)*length(creditDef);

h1 = figure(1); %IGD
clf(h1)
set(h1,'Position',[150, 300, 1200,600]);
hsubplot1 = cell(length(problemName),1);
for i=1:length(problemName)
    hsubplot1{i}=subplot(2,13,i);
end
h2 = figure(2); %fHV
clf(h2)
set(h2,'Position',[150, 100, 1200,600]);
hsubplot2 = cell(length(problemName),1);
for i=1:length(problemName)
    hsubplot2{i}=subplot(2,13,i);
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
    [benchmarkDataIGD,label_names] = getBenchmarkVals(path,probName,'finalIGD',mode);
    label_names_IGD=label_names;
    [benchmarkDatafHV,label_names] = getBenchmarkVals(path,probName,'finalHV',mode);
    label_names_fHV=label_names;
    
    [a,c] = size(benchmarkDataIGD);
    %box plot colors for benchmarks
    boxColors = 'rkm';
    
    dataIGD = cat(2,benchmarkDataIGD,zeros(a,b));
    datafHV = cat(2,benchmarkDatafHV,zeros(a,b));
    
    
    %check significance between best and default in IGD
    if strcmp(mode,'MOEAD')
        [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'DefaultMOEAD'),'MOEAD','de+pm',strcat(mres_path,filesep,'finalIGDbest1opMOEAD'),'MOEAD',probName,'finalIGD');
    elseif strcmp(mode,'NSGAII')
        [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'DefaultNSGAII'),'NSGAII','sbx+pm',strcat(mres_path,filesep,'finalIGDbest1opNSGAII'),'NSGAII',probName,'finalIGD');
    else strcmp(mode,'IBEA')
        [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'DefaultIBEA'),'IBEA','sbx+pm',strcat(mres_path,filesep,'finalIGDbest1opIBEA'),'IBEA',probName,'finalIGD');
    end
    extra = '';
    if sig==1
        extra = '(+)';
        statsIGD(i,1,3) = 1;
    elseif sig==-1
        extra = '(-)';
        statsIGD(i,1,1) = 1;
    else
        statsIGD(i,1,2) = 1;
    end
    label_names_IGD{2} = strcat(label_names_IGD{2},extra);
    %check significance between best and default in HV
     if strcmp(mode,'MOEAD')
        [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'DefaultMOEAD'),'MOEAD','de+pm',strcat(mres_path,filesep,'finalHVbest1opMOEAD'),'MOEAD',probName,'finalHV');
    elseif strcmp(mode,'NSGAII')
        [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'DefaultNSGAII'),'NSGAII','sbx+pm',strcat(mres_path,filesep,'finalHVbest1opNSGAII'),'NSGAII',probName,'finalHV');
    else strcmp(mode,'IBEA')
        [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'DefaultIBEA'),'IBEA','sbx+pm',strcat(mres_path,filesep,'finalHVbest1opIBEA'),'IBEA',probName,'finalHV');
    end
    extra = '';
    if sig==1
        extra = '(-)';
        statsfHV(i,1,1) = 1;
    elseif sig==-1
        extra = '(+)';
        statsfHV(i,1,3) = 1;
    else
        statsfHV(i,1,2) = 1;
    end
    label_names_fHV{2} = strcat(label_names_fHV{2},extra);
    
    %check significance between rand and bestin IGD
     if strcmp(mode,'MOEAD')
%         [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'RandomMOEAD'),'Random','OP-De',strcat(mres_path,filesep,'finalIGDbest1opMOEAD'),'MOEAD',probName,'finalIGD');
        [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'RandomMOEAD'),'Random','OP-De',strcat(mres_path,filesep,'DefaultMOEAD'),'MOEAD',probName,'finalIGD');
    elseif strcmp(mode,'NSGAII')
%         [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'RandomNSGAII'),'Random','OP-Do',strcat(mres_path,filesep,'finalIGDbest1opNSGAII'),'NSGAII',probName,'finalIGD');
        [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'RandomNSGAII'),'Random','OP-Do',strcat(mres_path,filesep,'DefaultNSGAII'),'NSGAII',probName,'finalIGD');
    else strcmp(mode,'IBEA')
%         [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'RandomIBEA'),'Random','OP-I',strcat(mres_path,filesep,'finalIGDbest1opIBEA'),'IBEA',probName,'finalIGD');
        [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'RandomIBEA'),'Random','OP-I',strcat(mres_path,filesep,'DefaultIBEA'),'IBEA',probName,'finalIGD');
     end
    extra = '';
    if sig==1
        extra = '(-)';
        statsIGD(i,3,3) = 1;
    elseif sig==-1
        extra = '(+)';
        statsIGD(i,3,1) = 1;
    else
        statsIGD(i,3,2) = 1;
    end
    label_names_IGD{3} = strcat(label_names_IGD{3},extra);
    if strcmp(mode,'MOEAD')
%         [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'RandomMOEAD'),'Random','OP-De',strcat(mres_path,filesep,'finalHVbest1opMOEAD'),'MOEAD',probName,'finalHV');
        [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'RandomMOEAD'),'Random','OP-De',strcat(mres_path,filesep,'DefaultMOEAD'),'MOEAD',probName,'finalHV');
    elseif strcmp(mode,'NSGAII')
%         [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'RandomNSGAII'),'Random','OP-Do',strcat(mres_path,filesep,'finalHVbest1opNSGAII'),'NSGAII',probName,'finalHV');
        [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'RandomNSGAII'),'Random','OP-Do',strcat(mres_path,filesep,'DefaultNSGAII'),'NSGAII',probName,'finalHV');
    else strcmp(mode,'IBEA')
%         [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'RandomIBEA'),'Random','OP-I',strcat(mres_path,filesep,'finalHVbest1opIBEA'),'IBEA',probName,'finalHV');
        [p,sig] = runMWUsignificance(strcat(mres_path,filesep,'RandomIBEA'),'Random','OP-I',strcat(mres_path,filesep,'DefaultIBEA'),'IBEA',probName,'finalHV');        
     end
    extra = '';
    if sig==1
        extra = '(+)';
        statsfHV(i,3,1) = 1;
    elseif sig==-1
        extra = '(-)';
        statsfHV(i,3,3) = 1;
    else
        statsfHV(i,3,2) = 1;
    end
    label_names_fHV{3} = strcat(label_names_fHV{3},extra);
    
    for j=1:length(selectors)
        for k=1:length(creditDef)
            c = c+1;
            origin = cd(mres_path);
            file = dir(strcat(probName,'*',selectors{j},'*',creditDef{k},'.mat'));
            load(file.name,'res'); %assume that the reults stored in vairable named res
            cd(origin);
            
            dataIGD(:,c) = res.finalIGD;
            datafHV(:,c) = res.finalHV;
            
            if strcmp(mode,'MOEAD');
%                 [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'finalIGDbest1opMOEAD'),'MOEAD',probName,'finalIGD');
%                 [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'DefaultMOEAD'),'MOEAD',probName,'finalIGD');
                [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'RandomMOEAD'),'Random_OP-De',probName,'finalIGD');
            elseif strcmp(mode,'NSGAII')
%                 [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'finalIGDbest1opNSGAII'),'NSGAII',probName,'finalIGD');
                [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'DefaultNSGAII'),'NSGAII',probName,'finalIGD');
%                 [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'RandomNSGAII'),'Random_OP-Do',probName,'finalIGD');
            elseif strcmp(mode,'IBEA')
%                 [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'finalIGDbest1opIBEA'),'IBEA',probName,'finalIGD');
%                 [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'DefaultIBEA'),'IBEA',probName,'finalIGD');
                [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'RandomIBEA'),'Random_OP-I',probName,'finalIGD');
            end
            extra = '';
            if sig==1
                extra = '(-)';
                statsIGD(i,c,3) = 1;
            elseif sig==-1
                extra = '(+)';
                statsIGD(i,c,1) = 1;
            else
                statsIGD(i,c,2) = 1;
            end
            label_names_IGD = [label_names_IGD,strcat(selectorShort{j},'-',creditDef{k},extra)]; %concats the labels
            
            if strcmp(mode,'MOEAD');
                [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'finalHVbest1opMOEAD'),'MOEAD',probName,'finalHV');
%                 [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'DefaultMOEAD'),'MOEAD',probName,'finalHV');
                [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'RandomMOEAD'),'Random_OP-De',probName,'finalHV');
            elseif strcmp(mode,'NSGAII')
%                 [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'finalHVbest1opNSGAII'),'NSGAII',probName,'finalHV');
                [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'DefaultNSGAII'),'NSGAII',probName,'finalHV');
%                 [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'RandomNSGAII'),'Random_OP-Do',probName,'finalHV');
            elseif strcmp(mode,'IBEA')
%                 [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'finalHVbest1opIBEA'),'IBEA',probName,'finalHV');
%                 [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'DefaultIBEA'),'IBEA',probName,'finalHV');
                [p,sig] = runMWUsignificance(mres_path,selectors{j},creditDef{k},strcat(mres_path,filesep,'RandomIBEA'),'Random_OP-I',probName,'finalHV');

            end
            extra = '';
            if sig==1
                extra = '(+)';
                statsfHV(i,c,1) = 1;
            elseif sig==-1
                extra = '(-)';
                statsfHV(i,c,3) = 1;
            else
                statsfHV(i,c,2) = 1;
            end
            label_names_fHV = {label_names_fHV{:},strcat(selectorShort{j},'-',creditDef{k},extra)}; %concats the labels
            boxColors = strcat(boxColors,'b');
        end
    end
    
    if strcmp(probName,'UF1_')
        probName = 'UF1';
    end
    
    figure(h1) 
    [~,ind]=min(mean(dataIGD,1));
    label_names_IGD{ind} = strcat('\bf{',label_names_IGD{ind},'}');
    boxplot(hsubplot1{i},dataIGD(:,1:end),label_names_IGD(:,1:end),'colors',boxColors(:,1:end),'boxstyle','filled','medianstyle','target','symbol','+')
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
    boxplot(hsubplot2{i},datafHV(:,1:end),label_names_fHV(:,1:end),'colors',boxColors(:,1:end),'boxstyle','filled','medianstyle','target','symbol','+')
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
% % 


% saveas(h1,strcat(base,'_IGD'),'fig');
% saveas(h2,strcat(base,'_HV'),'fig');
