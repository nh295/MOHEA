function plotAOSConvergence
%plots the time history of the metrics of the AOS

problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};
% problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF10'};
% problemName = {'DTLZ1_','DTLZ2_','DTLZ3_','DTLZ4_','DTLZ7_',};
selectors = {'Probability','Adaptive'};
selectorShort = {'PM','AP'};
base = 'DoR2';

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


path = '/Users/nozomihitomi/Dropbox/MOHEA';
% path = 'C:\Users\SEAK2\Nozomi\MOHEA\';

h1 = figure(1); %IGD
clf(h1)
set(h1,'Position',[150, 300, 1500,600]);
hsubplot1 = cell(length(problemName),1);
for i=1:length(problemName)
    hsubplot1{i}=subplot(2,5,i);
end
h2 = figure(2); %fHV
clf(h2)
set(h2,'Position',[150, 100, 1500,600]);
hsubplot2 = cell(length(problemName),1);
for i=1:length(problemName)
    hsubplot2{i}=subplot(2,5,i);
end

leftPos = 0.03;
topPos = 0.7;
bottomPos = 0.25;
intervalPos = (1-leftPos)/5;
width = 0.17;
height = 0.25;

for i=1:length(problemName)
    probName = problemName{i};
    
    %get random selector AOS
    cd(strcat(path,filesep,'resultsRandom'));
    files = dir(strcat(probName,'*ParentDom*.res'));
%     tmp = csvread(files(1).name,0,1);
%     npts = size(tmp,2)-2;
npts = 90;
    rIGD = zeros(length(files),npts);
    rfHV = zeros(length(files),npts);
    for m=1:length(files)
        [rIGD(m,:),rfHV(m,:),NFE] = getMetrics(files(m).name,90);
    end
    plot(hsubplot1{i},NFE,mean(rIGD,1))
    plot(hsubplot2{i},NFE,mean(rfHV,1))
    cd(path)
    label_names = {'Rand'};
    
    c=1;
    cd(strcat(path,filesep,'results6opsInjection'));
    hold(hsubplot1{i})
    hold(hsubplot2{i})
    for j=1:length(selectors)
        for k=1:length(creditDef)
            c = c+1;
            files = dir(strcat(probName,'*',selectors{j},'*',creditDef{k},'*.res'));
%             tmp = csvread(files(1).name,0,1);
%             npts = size(tmp,2)-1;
            IGD = zeros(length(files),npts);
            fHV = zeros(length(files),npts);
            for m=1:length(files)
                [IGD(m,:),fHV(m,:),NFE] = getMetrics(files(m).name,npts);
            end
            plot(hsubplot1{i},NFE,mean(IGD,1))
            plot(hsubplot2{i},NFE,mean(fHV,1))
            
            label_names = [label_names,strcat(selectorShort{j},'-',creditShort{k})]; %concats the labels
        end
    end
    cd(path)
    
    if strcmp(probName,'UF1_')
        probName = 'UF1';
    end
    
    figure(h1) 
    hold(hsubplot1{i})
    legend(hsubplot1{i},label_names)
    set(hsubplot1{i},'TickLabelInterpreter','tex');
    xlabel(hsubplot1{i},'NFE')
    ylabel(hsubplot1{i},'IGD')
    title(hsubplot1{i},probName)
    %ensures all boxplot axes are the same size and aligned.
    if i<6
        set(hsubplot1{i},'Position',[leftPos+intervalPos*(i-1),topPos,width,height]);
    else
        set(hsubplot1{i},'Position',[leftPos+intervalPos*(i-6),bottomPos,width,height]);
    end
    
    figure(h2)
    hold(hsubplot2{i})
    legend(hsubplot2{i},label_names)
    xlabel(hsubplot2{i},'NFE')
    ylabel(hsubplot2{i},'HV')
    set(hsubplot2{i},'FontSize',12);
    title(hsubplot2{i},probName)
    %ensures all boxplot axes are the same size and aligned.
    if i<6
        set(hsubplot2{i},'Position',[leftPos+intervalPos*(i-1),topPos,width,height]);
    else
        set(hsubplot2{i},'Position',[leftPos+intervalPos*(i-6),bottomPos,width,height]);
    end
    hold off
end
end

function [IGD,fHV,NFE] = getMetrics(filename,num)
fid = fopen(filename,'r');
while(~feof(fid))
    line = strsplit(fgetl(fid),',');
    switch line{1}
        case{'InvertedGenerationalDistance'}
            IGD = readLine(line);
        case{'FastHypervolume'}
            fHV = readLine(line);
        case{'NFE'}
            NFE = readLine(line);
        otherwise
            continue;
    end
end
IGD = IGD(1:num);
fHV = fHV(1:num);
NFE = NFE(1:num);
fclose(fid);
end

function [out] = readLine(line)
out = zeros(length(line)-1,1);
for i=1:length(line)-1
       out(i)=str2double(line{i+1});
end
end