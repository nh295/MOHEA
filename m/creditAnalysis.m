function creditAnalysis(mode)


% problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};
problemName = {'UF8'};
selectors = {'Random'};
creditDef = {'ParentDec','Neighbor','DecompositionContribution',...
    'ParentDom','OffspringParetoFront','OffspringEArchive','ParetoFrontContribution','EArchiveContribution',...
    'OPa_BIR2PARENT','OPop_BIR2PARETOFRONT','OPop_BIR2ARCHIVE','CNI_BIR2PARETOFRONT','CNI_BIR2ARCHIVE'};
path = '/Users/nozomihitomi/Dropbox/MOHEA/';
% path = 'C:\Users\SEAK1\Dropbox\MOHEA\';
respath = strcat(path,'mResCredits');
origin = cd(respath);
nops = 6;

switch mode
    case 1 %read in the credit csv files
        for a=1:length(problemName)
            for b=1:length(selectors)
                for c=1:length(creditDef)
                    fileType =strcat(problemName{a},'*',selectors{b},'*', creditDef{c},'*.creditcsv');
                    files = dir(fileType);
                    allcredits  = cell(length(files),1);
                    for i=1:length(files)
                        expData = java.util.HashMap;
                        fid = fopen(files(i).name,'r');
                        while(feof(fid)==0)
                            raw_iteration = strsplit(fgetl(fid),',');
                            raw_credits = strsplit(fgetl(fid),',');
                            op_data = zeros(length(raw_iteration)-1,2);
                            for j=2:length(raw_credits)
                                op_data(j,1)=str2double(raw_iteration{j}); %iteration
                                op_data(j,2)=str2double(raw_credits{j}); %credit
                            end
                            expData.put(raw_credits{1},op_data);
                        end
                        fclose(fid);
                        allcredits{i} = expData;
                    end
                    %save files
                    save(strcat(problemName{a},'_',selectors{b},'_',creditDef{c},'credit.mat'),'expData');
                end
            end
        end
        cd(origin);
        
        
    case 2 %analyze the files. mode 1 already assumed run
        nepochs = 100;
        maxEval = 300000;
        epochLength = maxEval/nepochs;
        for a=1:length(problemName)
            for b=1:length(selectors)
                for c=1:length(creditDef)
                    load(strcat(problemName{a},'_',selectors{b},'_',creditDef{c},'credit.mat'));
                    eraCreditsAllOp = java.util.HashMap;
                    eraCreditVel = java.util.HashMap;
                    eraSelectionFreq = java.util.HashMap;
                    for i=1:length(allcredits)
                        iter = allcredits{i}.keySet.iterator;
                        totalEpochSelection = zeros(nepochs,1);
                        epochSelectionFreq = java.util.HashMap;
                        while iter.hasNext
                            operator = iter.next;
                            data = allcredits{i}.get(operator);
                            eraCreditOneOp = zeros(nepochs,1);
                            eraSelectionOneOp = zeros(nepochs,1);
                            for j=1:nepochs
                                %find indices that lie within epoch
                                ind1 = epochLength*(j-1)<data(:,1);
                                ind2 = data(:,1)<epochLength*j;
                                epoch = data(and(ind1,ind2),:);
                                epochCredits = epoch(:,2);
                                eraCreditOneOp(j)=mean(epochCredits);
                                %count the unique iterations for which the
                                %operator gets a reward. This corresponds
                                %to when it was selected
                                iters = unique(epoch(:,1));
                                eraSelectionOneOp(j) = length(iters);
                                totalEpochSelection(j) = totalEpochSelection(j) + eraSelectionOneOp(j);
                            end
                            epochSelectionFreq.put(operator,eraSelectionOneOp);
                            if isempty(eraCreditsAllOp.get(operator))
                                eraCreditsAllOp.put(operator,eraCreditOneOp);
                                eraCreditVel.put(operator,diff(eraCreditOneOp));
                            else
                                eraCreditsAllOp.put(operator,eraCreditsAllOp.get(operator)+eraCreditOneOp);
                                eraCreditVel.put(operator,eraCreditVel.get(operator)+diff(eraCreditOneOp));
                            end
                        end
                        %normalize seleciton to be ratio
                        iter = allcredits{i}.keySet.iterator;
                        while iter.hasNext
                            operator = iter.next;
                            freqOperatorSelected = epochSelectionFreq.get(operator)./totalEpochSelection;
                            if isempty(eraSelectionFreq.get(operator))
                                eraSelectionFreq.put(operator,freqOperatorSelected);
                            else
                                eraSelectionFreq.put(operator,eraSelectionFreq.get(operator)+freqOperatorSelected);
                            end
                        end
                    end
                    
                    %take the average over the number of trials
                    iter2 = allcredits{i}.keySet.iterator;
                    labels = cell(nops,1);
                    ind = 1;
                    subtitle = 'Selection rate = \n';
                    cred = zeros(nepochs,allcredits{i}.keySet.size);
                    credVel = zeros(nepochs-1,allcredits{i}.keySet.size);
                    sel = zeros(nepochs,allcredits{i}.keySet.size);
                    while iter2.hasNext
                        operator = iter2.next;
                        %take the average over the trials
                        eraCreditsAllOp.put(operator,eraCreditsAllOp.get(operator)/length(allcredits));
                        eraCreditVel.put(operator,eraCreditVel.get(operator)/length(allcredits));
                        eraSelectionFreq.put(operator,eraSelectionFreq.get(operator)/length(allcredits));
                        
                        labels{ind}=operator;
                        subtitle = strcat(subtitle,operator,':\t',sprintf('%.4f',mean(eraSelectionFreq.get(operator))),'\n');
                        cred(:,ind) = eraCreditsAllOp.get(operator);
                        credVel(:,ind) = eraCreditVel.get(operator);
                        sel(:,ind) = eraSelectionFreq.get(operator);
                        ind = ind +1;
                    end
                    plotName = strcat(problemName{a},'_',creditDef{c});
                    h1=figure(1);
                    plot(cred);
                    legend(labels)
                    xlabel('Epoch')
                    ylabel('Average credits earned in epoch')
                    title(strcat(problemName{a},'_',creditDef{c},'_credit'))
                    saveas(h1,strcat(plotName,'_credit'),'fig');
                    saveas(h1,strcat(plotName,'_credit'),'jpeg');
                    
                    h2=figure(2);
                    plot(abs(credVel));
                    xlabel('Epoch')
                    ylabel('Speed in the change of the average credits earned in epoch')
                    legend(labels)
                    title(strcat(problemName{a},'_',creditDef{c},'_velocity'))
                    saveas(h2,strcat(plotName,'_velocity'),'fig');
                    saveas(h2,strcat(plotName,'_velocity'),'jpeg');
                    
                    h3=figure(3);
                    area(sel);
                    axis([0,nepochs,0,1.5])
                    xlabel('Epoch')
                    ylabel('Average rate of selection in epoch')
                    legend(labels)
                    title(strcat(problemName{a},'_',creditDef{c},'_select'))
                    %create textbox that shows selection frequency
                    t = annotation('textbox');
                    t.String = sprintf(subtitle);
                    t.Position=[0.1500    0.67    0.3589    0.2333];
                    t.HorizontalAlignment = 'right';
                    saveas(h3,strcat(plotName,'_select'),'fig');
                    saveas(h3,strcat(plotName,'_select'),'jpeg');
                    clf(h1)
                    clf(h2)
                    clf(h3)
                end
            end
        end
end
