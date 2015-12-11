function plot1Opresults

%plots the boxplots of each UF1-10 problem and the IGD, fast hypervolume
%(jmetal) and the additive epsilon values for each algorithm

problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};
% problemName = {'UF4'};
% MOEA =  {'MOEAD'};
MOEA =  {'eMOEA'};
% MOEA =  {'eMOEA','MOEAD'};
operator = {'SBX+PM','DifferentialEvolution+pm','UM','PCX+PM','UNDX+PM','SPX+PM'};
operatorName = {'SBX','DE','UM','PCX','UNDX','SPX'};

path = '/Users/nozomihitomi/Dropbox/MOHEA/';
% path = 'C:\Users\SEAK2\Nozomi\MOHEA\';
% path = 'C:\Users\SEAK1\Dropbox\MOHEA\';
res_path =strcat(path,'mRes1opNew');

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

statsIGD = zeros(length(problemName),b,3);
statsfHV = zeros(length(problemName),b,3);

boxColors = '';

for i=1:length(problemName)
    probName = problemName{i};
    dataIGD = zeros(a,b);
    datafHV = zeros(a,b);
    
    label_names_IGD={};
    label_names_fHV={};
    c=0;
    for j=1:length(operator)
        minIGD = inf;
        maxHV = 0;
        for k=1:length(MOEA)
            c = c+1;
            file = strcat(res_path,filesep,probName,'_',MOEA{k},'_',operator{j},'.mat');
            load(file); %assume that the reults stored in vairable named res
            dataIGD(:,c) = res.IGD;
            datafHV(:,c) = res.fHV;
            if(mean(dataIGD(:,c))<minIGD)
                minIGD = mean(dataIGD(:,c));
                minIGDind = c;
            end
            if(mean(datafHV(:,c))>maxHV)
                maxHV = mean(datafHV(:,c));
                maxHVind = c;
            end
            
            if strcmp(MOEA{k},'MOEAD')
                [~,sig]=significance(res,strcat(res_path,filesep,probName,'_MOEAD_DifferentialEvolution+pm.mat'));
                boxColors = strcat(boxColors,'r');
            elseif strcmp(MOEA{k},'eMOEA')
                [~,sig]=significance(res,strcat(res_path,filesep,probName,'_eMOEA_SBX+PM.mat'));
                boxColors = strcat(boxColors,'b');
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
            if strcmp(MOEA{k},'MOEAD')
                label_names_IGD = [label_names_IGD,strcat('DRA-',operatorName{j},extra)]; %concats the labels
            elseif strcmp(MOEA{k},'eMOEA')
                label_names_IGD = [label_names_IGD,strcat('\epsilonMOEA-',operatorName{j},extra)]; %concats the labels
            end
            
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
            if strcmp(MOEA{k},'MOEAD')
                label_names_fHV = [label_names_fHV,strcat('DRA-',operatorName{j},extra)]; %concats the labels
            elseif strcmp(MOEA{k},'eMOEA')
                label_names_fHV = [label_names_fHV,strcat('\epsilonMOEA-',operatorName{j},extra)]; %concats the labels
            end
            
        end
    end
    
    
    if strcmp(probName,'UF1_')
        probName = 'UF1';
    end
    
    figure(h1)
    [~,ind]=min(mean(dataIGD,1));
    label_names_IGD{ind} = strcat('\bf{',label_names_IGD{ind},'}');
    boxplot(hsubplot1{i},dataIGD,label_names_IGD,'colors',boxColors,'boxstyle','filled','medianstyle','target','symbol','o')
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
    [~,ind]=max(mean(datafHV,1));
    label_names_fHV{ind} = strcat('\bf{',label_names_fHV{ind},'}');
    boxplot(hsubplot2{i},datafHV,label_names_fHV,'colors',boxColors,'boxstyle','filled','medianstyle','target','symbol','o')
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
saveas(h1,strcat('Both','1opIGD'),'fig');
saveas(h2,strcat('Both','1opHV'),'fig');

statsIGD = squeeze(sum(statsIGD,1))
statsfHV = squeeze(sum(statsfHV,1))
end

function [p,sig] = significance(data,basecasefile)
f_names = fieldnames(data);
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