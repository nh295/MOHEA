%runs the post-run analysis
%reads .res file and puts all results data into one mfile

% problemName = {%'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9'};%,'UF10'};%,...
%     'DTLZ1','DTLZ2','DTLZ3','DTLZ4','DTLZ5','DTLZ6','DTLZ7',...
%     'WFG1','WFG2','WFG3','WFG4','WFG5','WFG6','WFG7','WFG8','WFG9'};
problemName = {'DTLZ1_3'};
% selectors = {'Probability','Adaptive'};
selectors = {'Random'};
% creditDef = { 'ParentDom','OffspringParetoFront','OffspringEArchive','ParetoFrontContribution','EArchiveContribution'};
% creditDef = {'OPa_BIR2PARENT','OPop_BIR2PARETOFRONT','OPop_BIR2ARCHIVE','CNI_BIR2PARETOFRONT','CNI_BIR2ARCHIVE'};
% creditDef = {'ParentDec','Neighbor','DecompositionContribution'};
creditDef = {'OP-I'};
% creditDef = {'sbx+pm','de+pm','um','pcx+pm','undx+pm','spx+pm'};
% problemName = {'UF10'};

path ='/Users/nozomihitomi/Dropbox/MOHEA';
% path ='/Users/nozomihitomi/Desktop/untitled folder';
% path = 'C:\Users\SEAK2\Nozomi\MOHEA';
% path = 'C:\Users\SEAK1\Dropbox\MOHEA';
nFiles = length(problemName)*length(selectors)*length(creditDef);
filesProcessed = 1;
h = waitbar(filesProcessed/nFiles,'Processing files...');
ind = 1;
for j=1:length(selectors)
    for i=1:length(creditDef)
%         figure
        for k=1:length(problemName)
            [AEI,GD,fHV,IGD,finalHV,ET] = getAllResults(strcat(path,filesep,'resultsMac/AddUpdate'),selectors{j},creditDef{i},problemName{k});
            if(isempty(ET))
                disp(strcat(problemName{k},selectors{j},creditDef{i}))
            end
            res.GD = squeeze(GD(:,end));
            res.AEI = squeeze(AEI(:,end));
            res.fHV = squeeze(fHV(:,end));
            res.IGD = squeeze(IGD(:,end));
            res.finalHV = finalHV;
            res.ET = squeeze(ET(:,end));
            save(strcat(problemName{k},'_',selectors{j},'_',creditDef{i},'.mat'),'res');

        filesProcessed = filesProcessed + 1;
        waitbar(filesProcessed/nFiles,h);
        end
        ind = ind + 1;
    end
end
close(h)

