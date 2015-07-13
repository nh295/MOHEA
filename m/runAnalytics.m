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
problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};
selectors = {'Probability','Adaptive'};
creditDef = { 'Parent','ImmediateParetoFront','ImmediateEArchive' };
% problemName = {'UF2'};

path ='/Users/nozomihitomi/Dropbox/MOHEA/mRes';

ind = 1;
for j=1:length(selectors)
    for i=1:length(creditDef)
        for k=1:length(problemName)
%             [AEI,GD,HV,IGD] = getAllResults(path,selectors{j},creditDef{i},problemName{k});
%             res.GD = squeeze(GD(:,end));
%             res.AEI = squeeze(AEI(:,end));
%             res.HV = squeeze(HV(:,end));
%             res.IGD = squeeze(IGD(:,end));
%             save(strcat(problemName{k},'_',selectors{j},'_',creditDef{i},'.mat'),'res');

            [out,p,avg1,avg2] = runKWsignificance(path,selectors{j},creditDef{i},'Random','Parent',problemName{k});
            if p<0.05 && avg1<avg2
                fprintf(['LOSE:',strcat(problemName{k},'_',selectors{j},'_',creditDef{i}),'avg1: %d , avg2: %d\n'],avg1,avg2);
            end
            if p<0.05 && avg1>avg2
                fprintf(['WIN:',strcat(problemName{k},'_',selectors{j},'_',creditDef{i}),'avg1: %d , avg2: %d\n'],avg1,avg2);
            end
        end
%         Xlabels{ind} = strcat(selectorsAbv{j},'-',credDefAbv{i});
        ind = ind + 1;
    end
end

