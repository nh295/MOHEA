function plot1Opresults

%plots the boxplots of each UF1-10 problem and the IGD, fast hypervolume
%(jmetal) and the additive epsilon values for each algorithm

problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};
% problemName = {'UF4'};
% MOEA =  {'MOEAD'};
MOEA =  {'eMOEA'};
operator = {'sbx+pm','de+pm','um','pcx+pm','undx+pm','spx+pm'};
operatorName = {'sbx','de','um','pcx','undx','spx'};

path = '/Users/nozomihitomi/Dropbox/MOHEA/';
% path = 'C:\Users\SEAK2\Nozomi\MOHEA\';
res_path =strcat(path,'mRes1op');

a = 30; %number of trials
b = length(MOEA)*length(operator);

h1 = figure(1); %IGD
h1.Position=[128 253 1401 328];
clf(h1)

h2 = figure(2); %fHV
h2.Position=[128 253 1401 328];
clf(h2)

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
                extra = '(+)';
            elseif sig.IGD==-1
                extra = '(-)'; 
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
    
    figure(h1)
    subplot(2,5,i);
    fprintf('%s: %s does best in IGD\n',probName,operator{minIGDind});
    boxplot(dataIGD,label_names_IGD)
    set(findobj(gca,'Type','text'),'FontSize',16)
    title(probName)
    pause(0.1)
    
    figure(h2)
    subplot(2,5,i);
    fprintf('%s: %s does best in HV\n',probName,operator{maxHVind});
    boxplot(datafHV,label_names_fHV)
    set(findobj(gca,'Type','text'),'FontSize',16)
    title(probName)
    pause(0.1)
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