%runs the post-run analysis
%reads .res file and puts all results data into one mfile

problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};%,'UF11','UF12','UF13'};
% problemName = {'DTLZ1_','DTLZ2_','DTLZ3_','DTLZ4_','DTLZ7_',};
% selectors = {'Probability','Adaptive'};
% creditDef = { 'ParentDom','OffspringParetoFront','OffspringEArchive','ParetoFrontContribution','EArchiveContribution','OPa_BIR2PARENT','OPop_BIR2PARETOFRONT','OPop_BIR2ARCHIVE','CNI_BIR2PARETOFRONT','CNI_BIR2ARCHIVE','ParentDec','Neighbor','DecompositionContribution'};
% creditDef = { 'OPa_BIR2PARENT','OPop_BIR2PARETOFRONT','OPop_BIR2ARCHIVE','CNI_BIR2PARETOFRONT','CNI_BIR2ARCHIVE'};
selectors = {'eMOEA'};
% creditDef = {'ParentDec','Neighbor','DecompositionContribution'};
creditDef = {'sbx+pm','de+pm','um','pcx+pm','undx+pm','spx+pm'};
% problemName = {'UF10'};

path ='/Users/nozomihitomi/Dropbox/MOHEA/';
% path ='/Users/nozomihitomi/Desktop/untitled folder';
% path = 'C:\Users\SEAK2\Nozomi\MOHEA';
nFiles = length(problemName)*length(selectors)*length(creditDef);
filesProcessed = 1;
h = waitbar(filesProcessed/nFiles,'Processing files...');
ind = 1;
for j=1:length(selectors)
    for i=1:length(creditDef)
%         figure
        for k=1:length(problemName)
%             [AEI,GD,fHV,IGD] = getAllResults(strcat(path),selectors{j},creditDef{i},problemName{k});
            [AEI,GD,fHV,IGD] = getAllResults(strcat(path,filesep,'results1op'),selectors{j},creditDef{i},problemName{k});
            res.GD = squeeze(GD(:,end));
            res.AEI = squeeze(AEI(:,end));
%             res.Inj = squeeze(Inj(:,end));
            res.fHV = squeeze(fHV(:,end));
            res.IGD = squeeze(IGD(:,end));
            save(strcat(problemName{k},'_',selectors{j},'_',creditDef{i},'.mat'),'res');

        filesProcessed = filesProcessed + 1;
        waitbar(filesProcessed/nFiles,h);
        end
        ind = ind + 1;
    end
end
close(h)

