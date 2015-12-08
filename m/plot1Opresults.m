function plot1Opresults

%plots the boxplots of each UF1-10 problem and the IGD, fast hypervolume
%(jmetal) and the additive epsilon values for each algorithm

problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};
% problemName = {'UF4'};
% MOEA =  {'MOEAD'};
MOEA =  {'eMOEA'};
operator = {'sbx+pm','de+pm','um','pcx+pm','undx+pm','spx+pm'};
operatorName = {'SBX','DE','UM','PCX','UNDX','SPX'};

path = '/Users/nozomihitomi/Dropbox/MOHEA/';
% path = 'C:\Users\SEAK2\Nozomi\MOHEA\';
res_path =strcat(path,'mRes1op');

a = 30; %number of trials
b = length(MOEA)*length(operator);

h1 = figure(1); %IGD
set(h1,'Position',[150, 500, 600,420]);
h2 = figure(2); %fHV
set(h2,'Position',[150, 100, 600,420]);
h1 = figure(1); %IGD
clf(h1)
clf(h2)

leftPos = 0.075;
topPos = 0.65;
bottomPos = 0.15;
intervalPos = (1-leftPos)/5;
width = 0.115;
height = 0.3;

for i=1:length(problemName)
    probName = problemName{i};
    dataIGD = zeros(a,b);
    datafHV = zeros(a,b);
    
    label_names_IGD={};
    label_names_fHV={};
    c=0;
    for j=1:length(MOEA)
        minIGD = inf;
        maxHV = 0;
        for k=1:length(operator)
            c = c+1;
            file = strcat(res_path,filesep,probName,'_',MOEA{j},'_',operator{k},'.mat');
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
            
            if strcmp(MOEA{j},'MOEAD')
                [~,sig]=significance(res,strcat(res_path,filesep,probName,'_MOEAD_de+pm.mat'));
            elseif strcmp(MOEA{j},'eMOEA')
                [~,sig]=significance(res,strcat(res_path,filesep,probName,'_eMOEA_sbx+pm.mat'));
            end
            extra = '';
            if sig.IGD==1
                extra = '(-)';
            elseif sig.IGD==-1
                extra = '(+)'; 
            end
            label_names_IGD = [label_names_IGD,strcat(operatorName{k},extra)]; %concats the labels
            extra = '';
            if sig.fHV==1
                extra = '(+)';
            elseif sig.fHV==-1
                extra = '(-)'; 
            end
            label_names_fHV = [label_names_fHV,strcat(operatorName{k},extra)]; %concats the labels
        end
    end
    
    
    if strcmp(probName,'UF1_')
        probName = 'UF1';
    end
    
    figure(h1)
    pause(0.2)
    subplot(2,5,i);
    [~,ind]=min(mean(dataIGD,1));
    label_names_IGD{ind} = strcat('\bf{',label_names_IGD{ind},'}');
    boxplot(dataIGD,label_names_IGD,'boxstyle','filled','medianstyle','target','symbol','o')
    set(findobj(gca,'Type','text'),'FontSize',16)
    title(probName)
    set(gca,'TickLabelInterpreter','tex');
    set(gca,'XTickLabelRotation',90);
    set(gca,'FontSize',12)
    %ensures all boxplot axes are the same size and aligned.
    if i==1
        fig1Pos = get(gca,'Position');
        set(gca,'Position',[leftPos,topPos,width,height]);
    elseif i<6
        figPos = get(gca,'Position');
        set(gca,'Position',[leftPos+intervalPos*(i-1),topPos,width,height]);
    elseif i==6
        fig6Pos = get(gca,'Position');
        set(gca,'Position',[leftPos,bottomPos,width,height]);
    else
        figPos = get(gca,'Position');
        set(gca,'Position',[leftPos+intervalPos*(i-6),bottomPos,width,height]);
    end
    
    figure(h2)
    pause(0.2)
    subplot(2,5,i);
    [~,ind]=max(mean(datafHV,1));
    label_names_fHV{ind} = strcat('\bf{',label_names_fHV{ind},'}');
    boxplot(datafHV,label_names_fHV,'boxstyle','filled','medianstyle','target','symbol','o')
    set(findobj(gca,'Type','text'),'FontSize',16)
    title(probName)
    set(gca,'TickLabelInterpreter','tex');
    set(gca,'XTickLabelRotation',90);
    set(gca,'FontSize',12)
    %ensures all boxplot axes are the same size and aligned.
    if i==1
        fig1Pos = get(gca,'Position');
        set(gca,'Position',[leftPos,topPos,width,height]);
    elseif i<6
        figPos = get(gca,'Position');
        set(gca,'Position',[leftPos+intervalPos*(i-1),topPos,width,height]);
    elseif i==6
        fig6Pos = get(gca,'Position');
        set(gca,'Position',[leftPos,bottomPos,width,height]);
    else
        figPos = get(gca,'Position');
        set(gca,'Position',[leftPos+intervalPos*(i-6),bottomPos,width,height]);
    end
end
saveas(h1,strcat(MOEA{1},'1opIGD'),'fig');
saveas(h2,strcat(MOEA{1},'1opHV'),'fig');
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