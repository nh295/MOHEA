function plot1Opresults

%plots the boxplots of each UF1-10 problem and the IGD, fast hypervolume
%(jmetal) and the additive epsilon values for each algorithm
% 
problemName = {'DTLZ7'};
% problemName = {'UF1','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};
% problemName = { 'DTLZ1','DTLZ2','DTLZ3','DTLZ4','DTLZ5','DTLZ6','DTLZ7'};
%  problemName = {'WFG1','WFG2','WFG3','WFG4','WFG5','WFG6','WFG7','WFG8','WFG9'};
% MOEA =  {'MOEAD','NSGAII','IBEA'};
MOEA =  {'MOEAD'};
% MOEA =  {'IBEA'};
% MOEA =  {'NSGAII'};
operator = {'sbx+pm','de+pm','um','pcx+pm','undx+pm','spx+pm'};
operatorName = {'SBX','DE','UM','PCX','UNDX','SPX'};

path = '/Users/nozomihitomi/Dropbox/MOHEA/';
% path = 'C:\Users\SEAK2\Nozomi\MOHEA\';
% path = 'C:\Users\SEAK1\Dropbox\MOHEA\';
res_path =strcat(path,'mResExperimentA2');

a = 30; %number of trials
b = length(MOEA)*length(operator);

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

statsfinalIGD = zeros(length(problemName),b,3);
statsfinalHV = zeros(length(problemName),b,3);

bestOpsIGD = cell(length(problemName)*length(MOEA),1);
bestOpsHV = cell(length(problemName)*length(MOEA),1);

boxColors = '';

for i=1:length(problemName)
    probName = problemName{i};
    datafinalIGD = zeros(a,b);
    datafinalHV = zeros(a,b);
    
    label_names_IGD={};
    label_names_finalHV={};
    c=0;
    for k=1:length(MOEA)
%     for j=1:length(operator)
        minIGD = inf;
        maxHV = 0;
        for j=1:length(operator)
% %         for k=1:length(MOEA)
            c = c+1;
            file = strcat(res_path,filesep,probName,'_',MOEA{k},'_',operator{j},'.mat');
            load(file); %assume that the reults stored in vairable named res
            datafinalIGD(:,c) = res.finalIGD(:,end);
            datafinalHV(:,c) = res.finalHV(:,end);
            if(mean(datafinalIGD(:,c))<minIGD)
                minIGD = mean(datafinalIGD(:,c));
            end
            if(mean(datafinalHV(:,c))>maxHV)
                maxHV = mean(datafinalHV(:,c));
            end
            
            if strcmp(MOEA{k},'MOEAD')
                [~,sig]=significance(res,strcat(res_path,filesep,probName,'_MOEAD_de+pm.mat'));
                boxColors = strcat(boxColors,'r');
            elseif strcmp(MOEA{k},'IBEA')
                [~,sig]=significance(res,strcat(res_path,filesep,probName,'_IBEA_sbx+pm.mat'));
                boxColors = strcat(boxColors,'b');
            elseif strcmp(MOEA{k},'NSGAII')
                [~,sig]=significance(res,strcat(res_path,filesep,probName,'_NSGAII_sbx+pm.mat'));
                boxColors = strcat(boxColors,'g');
            end
            extra = '';
            if sig.finalIGD==1
                extra = '(-)';
                statsfinalIGD(i,c,3) = 1;
            elseif sig.finalIGD==-1
                extra = '(+)';
                statsfinalIGD(i,c,1) = 1;
            else
                statsfinalIGD(i,c,2) = 1;
            end
            if strcmp(MOEA{k},'MOEAD')
                label_names_IGD = [label_names_IGD,strcat('DRA-',operatorName{j},extra)]; %concats the labels
            elseif strcmp(MOEA{k},'IBEA')
                label_names_IGD = [label_names_IGD,strcat('IBEA-',operatorName{j},extra)]; %concats the labels
            elseif strcmp(MOEA{k},'NSGAII')
                label_names_IGD = [label_names_IGD,strcat('NSGA-',operatorName{j},extra)]; %concats the labels
            end
            
            extra = '';
           if sig.finalHV==1
                extra = '(+)';
                statsfinalHV(i,c,1) = 1;
            elseif sig.finalHV==-1
                extra = '(-)';
                statsfinalHV(i,c,3) = 1;
            else
                statsfinalHV(i,c,2) = 1;
           end
            if strcmp(MOEA{k},'MOEAD')
                label_names_finalHV = [label_names_finalHV,strcat('DRA-',operatorName{j},extra)]; %concats the labels
            elseif strcmp(MOEA{k},'IBEA')
                label_names_finalHV = [label_names_finalHV,strcat('IBEA-',operatorName{j},extra)]; %concats the labels
            elseif strcmp(MOEA{k},'NSGAII')
                label_names_finalHV = [label_names_finalHV,strcat('NSGA-',operatorName{j},extra)]; %concats the labels
            end
        end
    end
    
    
    if strcmp(probName,'UF1_')
        probName = 'UF1';
    end
    
    figure(h1)
    [~,ind]=min(mean(datafinalIGD,1));
    meanIGD = sprintf(' %0.3e',mean(datafinalIGD(:,ind)));
    stdIGD = sprintf('%0.3e',std(datafinalIGD(:,ind)));
     bestOpsIGD{i} = strcat(operatorName{ind},' & ', meanIGD,' (',stdIGD,')');
    label_names_IGD{ind} = strcat('\bf{',label_names_IGD{ind},'}');
    boxplot(hsubplot1{i},datafinalIGD,label_names_IGD,'colors',boxColors,'boxstyle','filled','medianstyle','target','symbol','+')
    title(hsubplot1{i},probName)
    set(hsubplot1{i},'TickLabelInterpreter','tex');
    set(hsubplot1{i},'XTickLabelRotation',90);
    set(hsubplot1{i},'FontSize',12)
    %ensures all boxplot axes are the same size and aligned.
    if i<6
        set(hsubplot1{i},'Position',[leftPos+intervalPos*(i-1),topPos,width,height]);
    else
        set(hsubplot1{i},'Position',[leftPos+intervalPos*(i-6),bottomPos,width,height]);
    end
    
    figure(h2)
    [~,ind]=max(mean(datafinalHV,1));
    meanHV = sprintf(' %0.3e',mean(datafinalHV(:,ind)));
    stdHV = sprintf('%0.3e',std(datafinalHV(:,ind)));
     bestOpsHV{i} = strcat(operatorName{ind},' & ', meanHV,' (',stdHV,')');
    label_names_finalHV{ind} = strcat('\bf{',label_names_finalHV{ind},'}');
    boxplot(hsubplot2{i},datafinalHV,label_names_finalHV,'colors',boxColors,'boxstyle','filled','medianstyle','target','symbol','+')
    title(hsubplot2{i},probName)
    set(hsubplot2{i},'TickLabelInterpreter','tex');
    set(hsubplot2{i},'XTickLabelRotation',90);
    set(hsubplot2{i},'FontSize',12)
    %ensures all boxplot axes are the same size and aligned.
    if i<6
        set(hsubplot2{i},'Position',[leftPos+intervalPos*(i-1),topPos,width,height]);
    else
        set(hsubplot2{i},'Position',[leftPos+intervalPos*(i-6),bottomPos,width,height]);
    end
end
% saveas(h1,strcat('Both','1opIGD'),'fig');
% saveas(h2,strcat('Both','1opHV'),'fig');

statsfinalIGD = squeeze(sum(statsfinalIGD,1))
statsfinalHV = squeeze(sum(statsfinalHV,1))

disp('Best in IGD')
for i=1:length(problemName)
   disp(bestOpsIGD{i});
end
disp('Best in HV')
for i=1:length(problemName)
   disp(bestOpsHV{i});
end
end

function [p,sig] = significance(data,basecasefile)
f_names = {'finalHV','finalIGD'};
p = struct;
sig = struct;
load(basecasefile,'res'); %assume that the reults stored in vairable named res

for i=1:length(f_names)
    f_name = f_names{i};
    data1 = getfield(data,f_name);
    data2 = getfield(res,f_name);
    [metric_p,h] = ranksum(data1,data2);
    if h==1 %then significant difference and medians are different
        med_diff = median(data1)-median(data2);
        if med_diff < 0
            metric_sig = -1;
        else
            metric_sig = 1;
        end
    else
        metric_sig = 0;
    end
    p = setfield(p,f_name,metric_p);
    sig = setfield(sig,f_name,metric_sig);
end
end