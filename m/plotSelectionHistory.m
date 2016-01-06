function plotSelectionHistory(mode)

path = '/Users/nozomihitomi/Dropbox/MOHEA';
% path = 'C:\Users\SEAK1\Dropbox\MOHEA';
respath = strcat(path,filesep,'resultsCreditsNew');
% respath = strcat(path,filesep,'results');
javaaddpath(strcat(path,filesep,'dist',filesep,'MOHEA.jar'));
import hh.history.OperatorSelectionHistory.*
import hh.IO.IOSelectionHistory.*

iosh = hh.IO.IOSelectionHistory;

problemName = {'UF10'};
selectors = {'Probability'};
% creditDef = {'ParentDec','Neighbor','DecompositionContribution',...
%     'ParentDom','OffspringParetoFront','OffspringEArchive','ParetoFrontContribution','EArchiveContribution',...
%     'OPa_BIR2PARENT','OPop_BIR2PARETOFRONT','OPop_BIR2ARCHIVE','CNI_BIR2PARETOFRONT','CNI_BIR2ARCHIVE'};
% shortCreditName = {'OP-De','SI-De','CS-De',...
%     'OP-Do','SI-Do-PF','SI-Do-A','CS-Do-PF','CS-Do-A',...
%     'OP-R2','SI-R2-PF','SI-R2-A','CS-R2-PF','CS-R2-A'};
creditDef = {'DecompositionContribution'};
shortCreditName = {'CS-De'};

nepochs = 100;
maxEval = 300000;
epochLength = maxEval/nepochs;
nFiles = length(problemName)*length(selectors)*length(creditDef);
filesProcessed = 0;

ops = {'SBX+PM','DifferentialEvolution+PM', 'UM','UNDX+PM','SPX+PM','PCX+PM'};
nops = length(ops);
labels = {'SBX','DE', 'UM','UNDX','SPX','PCX'};

for a=1:length(problemName)
    for b=1:length(selectors)
        for c=1:length(creditDef)
            switch(mode)
                case {1}
                    h = waitbar(filesProcessed/nFiles,'Processing files...');
                    %process files to get selection counts per epoch for each file
                    files = dir(strcat(respath,filesep,problemName{a},'*',selectors{b},'*',creditDef{c},'*hist'));
                    allSelections = cell(length(files),1);
                    for i=1:length(files)
                        waitbar(i/length(files),h);
                        hist = iosh.loadHistory(strcat(respath,filesep,files(i).name));
                        eraSelectionFreq = java.util.HashMap;
                        opIter = hist.getOperators.iterator;
                        while(opIter.hasNext)
                            eraSelectionFreq.put(opIter.next,zeros(nepochs+1,1));
                        end
                        
                        opHistory = hist.getOrderedHistory;
                        selHitory = hist.getOrderedSelectionTime;
                        epochSelectionCounts = zeros(nepochs+1,1);
                        for j=0:selHitory.size-1
                            tmpHist = eraSelectionFreq.get(opHistory.get(j));
                            epochNum = floor(selHitory.get(j)/epochLength)+1;
                            tmpHist(epochNum)=tmpHist(epochNum)+1;
                            eraSelectionFreq.put(opHistory.get(j),tmpHist);
                            epochSelectionCounts(epochNum) = epochSelectionCounts(epochNum) + 1;
                        end
                        
                        %normalize the epoch selection counts by the total number
                        %of selections in each epoch
                        opIter = hist.getOperators.iterator;
                        while(opIter.hasNext)
                            op = opIter.next;
                            eraSelectionFreq.put(op, eraSelectionFreq.get(op)./epochSelectionCounts);
                        end
                        
                        allSelections{i}=eraSelectionFreq;
                    end
                    save(strcat(problemName{a},'_',selectors{b},'_',shortCreditName{c},'_selAll','.mat'),'allSelections');
                    close(h)
                case{2}
                    load(strcat(problemName{a},'_',selectors{b},'_',shortCreditName{c},'_selAll','.mat'),'allSelections');
                    %combine data of all files
                    combinedSelectionCounts = java.util.HashMap;
                    for i=1:length(allSelections)
                        iter = allSelections{i}.keySet.iterator;
                        while(iter.hasNext)
                            op = iter.next;
                            if isempty(combinedSelectionCounts.get(op))
                                combinedSelectionCounts.put(op.toString,allSelections{i}.get(op));
                            else
                                combinedSelectionCounts.put(op.toString,combinedSelectionCounts.get(op)+allSelections{i}.get(op));
                            end
                        end
                    end
                    %take the mean over all trials
                    sel = zeros(nepochs+1,allSelections{1}.keySet.size);
                    for i=1:nops
                        iter = allSelections{i}.keySet.iterator;
                        op='';
                        while(~strcmp(op,ops{i}))
                            op = iter.next.toString;
                        end
                        sel(:,i) = combinedSelectionCounts.get(op);
                    end
                    area(sel);
                    save(strcat(problemName{a},'_',selectors{b},'_',shortCreditName{c},'_sel','.mat'),'sel');
                    axis([0,nepochs,0,1.5])
                    xlabel('Epoch')
                    ylabel('Average rate of selection in epoch')
                    legend(labels)
                    title(strcat(problemName{a},'  ',shortCreditName{c},' select'))
                    %create textbox that shows selection frequency
%                     t = annotation('textbox');
%                     t.Position=[0.1500    0.67    0.3589    0.2333];
%                     t.HorizontalAlignment = 'right';
            end
        end
    end
end

javarmpath(strcat(path,filesep,'dist',filesep,'MOHEA.jar'));




