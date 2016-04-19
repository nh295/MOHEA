%runs the post-run analysis
%reads .res file and puts all results data into one mfile

% problemName = {'UF1','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10',...
%     'DTLZ1','DTLZ2','DTLZ3','DTLZ4','DTLZ5','DTLZ6','DTLZ7',...
%     'WFG1','WFG2','WFG3','WFG4','WFG5','WFG6','WFG7','WFG8','WFG9'};
% problemName = {%'UF1','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10',...
%      'DTLZ1_3','DTLZ2_3','DTLZ3_3','DTLZ4_3','DTLZ5_3','DTLZ6_3','DTLZ7_3',...
%      'WFG1_2','WFG2_2','WFG3_2','WFG4_2','WFG5_2','WFG6_2','WFG7_2','WFG8_2','WFG9_2'};

problemName = {'UF8'};
selectors = {'Probability'};
% selectors = {'Adaptive'};
% selectors = {'Random'};
% selectors = {'IBEA'};
% selectors = {'MOEAD','SSIBEA','SSNSGAII'};
% creditDef = { 'OP-Do','SI-PF','CS-Do-PF','OP-I','SI-I','CS-I','OP-De','SI-De','CS-De'};
% creditDef = {'OP-R2','SI-I','CS-I'};
creditDef = {'CS-I'};
% creditDef = {'sbx+pm','de+pm','um','pcx+pm','undx+pm','spx+pm'};

path ='/Users/nozomihitomi/Dropbox/MOHEA';
% path ='/Users/nozomihitomi/Desktop/untitled folder';
% path = 'C:\Users\SEAK2\Nozomi\MOHEA\';
% path = 'C:\Users\SEAK1\Dropbox\MOHEA';
nFiles = length(problemName)*length(selectors)*length(creditDef);
filesProcessed = 1;
h = waitbar(filesProcessed/nFiles,'Processing files...');
ind = 1;
for j=1:length(selectors)
    for i=1:length(creditDef)
%         figure
        for k=1:length(problemName)
            [fHV,IGD,finalHV,finalIGD,ET] = getAllResults(strcat(path,filesep,'Experiment B2 Data'),selectors{j},creditDef{i},problemName{k});
            if(isempty(ET))
                disp(strcat(problemName{k},selectors{j},creditDef{i}))
            end
            res.fHV = squeeze(fHV);
            res.IGD = squeeze(IGD);
            res.finalHV = finalHV;
            res.finalIGD = finalIGD;
            res.ET = squeeze(ET);
            save(strcat(problemName{k},'_',selectors{j},'_',creditDef{i},'.mat'),'res');

        filesProcessed = filesProcessed + 1;
        waitbar(filesProcessed/nFiles,h);
        end
        ind = ind + 1;
    end
end
close(h)
