%plots the boxplots of each UF1-10 problem and the IGD, fast hypervolume
%(jmetal) and the additive epsilon values for each algorithm

problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};
% problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF10'};
% problemName = {'DTLZ1_','DTLZ2_','DTLZ3_','DTLZ4_','DTLZ7_',};
selectors = {'Probability','Adaptive'};
selectorShort = {'PM','AP'};
base = 'De';

switch base
    case {'De'}
        creditDef = {'ParentDec','Neighbor','DecompositionContribution'};
        creditShort = {'OP-De','SI-De','CS-De'};
        mode = 'MOEAD';
    case{'DoR2'}
        creditDef = {'ParentDom','OffspringParetoFront','OffspringEArchive','ParetoFrontContribution','EArchiveContribution',...
            'OPa_BIR2PARENT','OPop_BIR2PARETOFRONT','OPop_BIR2ARCHIVE','CNI_BIR2PARETOFRONT','CNI_BIR2ARCHIVE'};
        creditShort = {'OP-Do','SI-Do-PF','SI-Do-A','CS-Do-PF','CS-Do-A','OP-R2','SI-R2-PF','SI-R2-A','CS-R2-PF','CS-R2-A',...
            'OP-R2','SI-R2-PF','SI-R2-A','CS-R2-PF','CS-R2-A'};
        mode = 'eMOEA';
end

% creditDef = {'ParentDec','Neighbor','DecompositionContribution',...
%     'ParentDom','OffspringParetoFront','OffspringEArchive','ParetoFrontContribution','EArchiveContribution',...
%     'OPa_BIR2PARENT','OPop_BIR2PARETOFRONT','OPop_BIR2ARCHIVE','CNI_BIR2PARETOFRONT','CNI_BIR2ARCHIVE'};
% creditShort = {'OP-De','SI-De','CS-De',...
%     'OP-Do','SI-Do-PF','SI-Do-A','CS-Do-PF','CS-Do-A','OP-R2','SI-R2-PF','SI-R2-A','CS-R2-PF','CS-R2-A',...
%     'OP-R2','SI-R2-PF','SI-R2-A','CS-R2-PF','CS-R2-A'};

% path = '/Users/nozomihitomi/Dropbox/MOHEA';
path = 'C:\Users\SEAK2\Nozomi\MOHEA\';
res_path =strcat(path,filesep,'mRes6opsInjection');
% res_path = '/Users/nozomihitomi/Desktop/untitled folder';

b = length(selectors)*length(creditDef);

h1 = figure(1); %IGD
set(h1,'Position',[150, 500, 1500,600]);
h2 = figure(2); %fHV
set(h2,'Position',[150, 100, 1500,600]);
h3 = figure(3); %AEI
clf(h1)
clf(h2)
clf(h3)
% h4 = figure(4); %# injections

leftPos = 0.03;
topPos = 0.7;
bottomPos = 0.25;
intervalPos = (1-leftPos)/5;
width = 0.17;
height = 0.25;

%box plot colors for benchmarks
boxColors = 'rkm';

for i=1:length(problemName)
    probName = problemName{i};
    [benchmarkDataIGD,label_names] = getBenchmarkVals(path,probName,'IGD',mode);
    [benchmarkDatafHV,~] = getBenchmarkVals(path,probName,'fHV',mode);
    [benchmarkDataAEI,~] = getBenchmarkVals(path,probName,'AEI',mode);
    [a,c] = size(benchmarkDataIGD);
    
    dataIGD = cat(2,benchmarkDataIGD,zeros(a,b));
    datafHV = cat(2,benchmarkDatafHV,zeros(a,b));
    dataAEI = cat(2,benchmarkDataAEI,zeros(a,b));
%         dataInj = zeros(a,b);
    
    label_names_IGD=label_names;
    label_names_fHV=label_names;
    label_names_AEI=label_names;
    
    for j=1:length(selectors)
        for k=1:length(creditDef)
            c = c+1;
            file = strcat(res_path,filesep,probName,'_',selectors{j},'_',creditDef{k},'.mat');
            load(file,'res'); %assume that the reults stored in vairable named res
            dataIGD(:,c) = res.IGD;
            datafHV(:,c) = res.fHV;
            dataAEI(:,c) = res.AEI;
            
            %             dataInj(:,c-size(benchmarkDataIGD,2)) = res.Inj;
            if strcmp(creditDef{k},'ParentDec')||strcmp(creditDef{k},'Neighbor')||strcmp(creditDef{k},'DecompositionContribution')
                [p,sig] = runMWUsignificance(path,selectors{j},creditDef{k},'best1opMOEAD',probName);
            else
                [p,sig] = runMWUsignificance(path,selectors{j},creditDef{k},'best1opeMOEA',probName);
            end
            extra = '';
            if sig.IGD==1
                extra = '(+)';
            elseif sig.IGD==-1
                extra = '(-)';
            end
            label_names_IGD = [label_names_IGD,strcat(selectorShort{j},'-',creditShort{k},extra)]; %concats the labels
            extra = '';
            if sig.fHV==1
                extra = '(+)';
            elseif sig.fHV==-1
                extra = '(-)';
            end
            label_names_fHV = {label_names_fHV{:},strcat(selectorShort{j},'-',creditShort{k},extra)}; %concats the labels
            extra = '';
            if sig.AEI==1
                extra = '(+)';
            elseif sig.AEI==-1
                extra = '(-)';
            end
            label_names_AEI = {label_names_AEI{:},strcat(selectorShort{j},'-',creditShort{k},extra)}; %concats the labels
            boxColors = [boxColors,'b'];
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
    boxplot(dataIGD,label_names_IGD,'colors',boxColors,'boxstyle','filled','medianstyle','target','symbol','+')
    set(gca,'TickLabelInterpreter','tex');
    set(gca,'XTickLabelRotation',90);
    set(gca,'FontSize',12)
    title(probName)
    %ensures all boxplot axes are the same size and aligned.
    if i==1
        set(gca,'Position',[leftPos,topPos,width,height]);
    elseif i<6
        set(gca,'Position',[leftPos+intervalPos*(i-1),topPos,width,height]);
    else
        set(gca,'Position',[leftPos+intervalPos*(i-6),bottomPos,width,height]);
    end
    
    pause(0.2)
    figure(h2)
    subplot(2,5,i);
    [~,ind]=max(mean(datafHV,1));
    label_names_fHV{ind} = strcat('\bf{',label_names_fHV{ind},'}');
    boxplot(datafHV,label_names_fHV,'colors',boxColors,'boxstyle','filled','medianstyle','target','symbol','+')
    set(gca,'TickLabelInterpreter','tex');
    set(gca,'XTickLabelRotation',90);
    set(gca,'FontSize',12);
    title(probName)
    %ensures all boxplot axes are the same size and aligned.
    if i==1
        set(gca,'Position',[leftPos,topPos,width,height]);
    elseif i<6
        set(gca,'Position',[leftPos+intervalPos*(i-1),topPos,width,height]);
    else
        set(gca,'Position',[leftPos+intervalPos*(i-6),bottomPos,width,height]);
    end
    
    pause(0.2)
    figure(h3)
    subplot(2,5,i);
    boxplot(datafHV,label_names_AEI,'colors',boxColors,'boxstyle','filled','medianstyle','target','symbol','o')
    set(gca,'TickLabelInterpreter','tex');
    set(gca,'XTickLabelRotation',90);
    set(gca,'FontSize',12)
    title(probName)
    
    %     figure(h4)
    %     subplot(2,5,i);
    %     boxplot(dataInj,{label_names{size(benchmarkDataIGD,2)+1:end}},'labelorientation','inline','colors',boxColors,'plotstyle','compact')
    %     title(probName)
    hold off
end