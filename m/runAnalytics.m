%runs the post-run analysis
% close all;
% origin = cd(strcat(cd,filesep,'results-EOSS',filesep,'PM'));
% 
% javaaddpath([origin,filesep,'dist',filesep,'HHCreditTest.jar']);
% 
% %find all files ending in .credit
% creditFiles = dir('*.credit');
% %find all files ending in .hist
% histFiles = dir('*.hist');
% 
% % figure(1)
% % heuristicSelectionHistory(cd,histFiles);
% figure(2)
% heuristicCreditHistory(cd,creditFiles);
% 
% 
% cd(origin);


% gets all the best, worst and mean values for each indicator
% also finds the % past 90% attainment on hypervolume

% selectors = {'Random','Probability','Adaptive','DMAB'};
% creditDef = {'Parent','ImmediateParetoFront','ImmediateParetoRank','ImmediateEArchive'...
%     'AggregateParetoFront','AggregateParetoRank','AggregateEArchive'};
% problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};
selectors = {'NSGAII'};
creditDef = {'NSGAII'};
problemName = {'UF2'};

% selectorsAbv = {'R','PM','AP','DMAB'};
% credDefAbv = {'P','IPF','IPR','IEA','APF','APR','AEA'};
% Xlabels = cell(length(credDefAbv)*length(selectorsAbv),1);
% Ylabels = {'UF1','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};

selectorsAbv = {'R'};
credDefAbv = {'P'};
Xlabels = cell(length(credDefAbv)*length(selectorsAbv),1);
Ylabels = {'UF2'};

bestVals = zeros(length(creditDef)*length(selectors),length(problemName),3);
worstVals = zeros(length(creditDef)*length(selectors),length(problemName),3);
avgVals = zeros(length(creditDef)*length(selectors),length(problemName),3);
HVattainmentPer = zeros(length(creditDef)*length(selectors),length(problemName));
AllHVvals = zeros(length(creditDef)*length(selectors),length(problemName),30);
AllIGDvals = zeros(length(creditDef)*length(selectors),length(problemName),30);

path ='C:\Users\SEAK2\Nozomi\MOHEA\results';

ind = 1;
for j=1:length(selectors)
    for i=1:length(creditDef)
        for k=1:length(problemName)
%             fprintf('%s %s\n',selectors{j},creditDef{i});
            [AEI,GD,HV,IGD] = getAllResults(path,selectors{j},creditDef{i},problemName{k});
            bestVals(ind,k,1) = min(AEI(:,end));
            bestVals(ind,k,2) = min(IGD(:,end));
            bestVals(ind,k,3) = max(HV(:,end));
            worstVals(ind,k,1) = max(AEI(:,end));
            worstVals(ind,k,2) = max(IGD(:,end));
            worstVals(ind,k,3) = min(HV(:,end));
            avgVals(ind,k,1) = mean(AEI(:,end));
            avgVals(ind,k,2) = mean(IGD(:,end));
            avgVals(ind,k,3) = mean(HV(:,end));
            AllHVvals(ind,k,:) = HV(:,end);
            AllIGDvals(ind,k,:) = IGD(:,end);
        end
        Xlabels{ind} = strcat(selectorsAbv{j},'-',credDefAbv{i});
        ind = ind + 1;
    end
end
% plotMetrics(bestVals(:,:,3),Xlabels,Ylabels);
