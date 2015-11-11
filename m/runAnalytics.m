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

problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7'};%,'UF8','UF9','UF10'};%,'UF11','UF12','UF13'};
% problemName = {'UF11','UF12','UF13'};
selectors = {'Probability','Adaptive'};
creditDef = { 'OPa_BIHVPARENT','OPa_BIR2PARENT','OPop_BIR2PARETOFRONT','OPop_BIHVPARETOFRONT','OPop_BIR2ARCHIVE','OPop_BIHVARCHIVE'};
% creditDef = { 'Parent','OffspringParetoFront','OffspringEArchive','ParetoFrontContribution','EArchiveContribution'};
% selectors = {'eMOEA'};
% creditDef = {'EArchiveContribution'};
% problemName = {'UF10'};

% path ='/Users/nozomihitomi/Dropbox/MOHEA/';
path = 'C:\Users\SEAK2\Nozomi\MOHEA';

ind = 1;
for j=1:length(selectors)
    for i=1:length(creditDef)
%         figure
        for k=1:length(problemName)
%             [AEI,GD,HV,fHV,IGD] = getAllResults(strcat(path,filesep,'results6opsInjection'),selectors{j},creditDef{i},problemName{k});
            [AEI,GD,Inj,fHV,IGD] = getAllResults(strcat(path,filesep,'results'),selectors{j},creditDef{i},problemName{k});
            res.GD = squeeze(GD(:,end));
            res.AEI = squeeze(AEI(:,end));
            res.Inj = squeeze(Inj(:,end));
            res.fHV = squeeze(fHV(:,end));
            res.IGD = squeeze(IGD(:,end));
            save(strcat(problemName{k},'_',selectors{j},'_',creditDef{i},'.mat'),'res');
            
%             subplot(2,ceil(length(problemName)/2),k)
%             title(problemName{k});
%             hold on
%             [data,labels,p,avg1,avg2] = runKWsignificance(path,selectors{j},creditDef{i},'eMOEA',problemName{k});
%             if p<0.05 && avg1<avg2
%                 fprintf(['LOSE:',strcat(problemName{k},'_',selectors{j},'_',creditDef{i}),'avg1: %d , avg2: %d\n'],avg1,avg2);
%                 xlabel('Lose')
%             end
%             if p<0.05 && avg1>avg2
%                 fprintf(['WIN:',strcat(problemName{k},'_',selectors{j},'_',creditDef{i}),'avg1: %d , avg2: %d\n'],avg1,avg2);
%                 xlabel('Win')
%             end
%             boxplot(data,labels)
        end
%         Xlabels{ind} = strcat(selectorsAbv{j},'-',credDefAbv{i});
        ind = ind + 1;
    end
end

