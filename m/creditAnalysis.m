function creditAnalysis(mode)


% problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7'};%,'UF8','UF9','UF10'};
% problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7'};
problemName = {'WFG1'};
selectors = {'Probability'};
% creditDef = {'OP-De','SI-De','CS-De'};
% creditDef = {'OP-Do','SI-PF','CS-Do-PF'};
creditDef = {'OP-R2','SI-I','CS-I'};
path = '/Users/nozomihitomi/Dropbox/MOHEA/';
% path = 'C:\Users\SEAK1\Dropbox\MOHEA\';
% respath = strcat(path,'results');
respath = strcat(path,'credits');
origin = cd(respath);
ops = {'SBX+PM','DifferentialEvolution+PM', 'UM','UNDX+PM','SPX+PM','PCX+PM'};
nops = length(ops);
labels = {'SBX','DE', 'UM','UNDX','SPX','PCX'};
close all
switch mode
    case 1 %read in the credit csv files
        nFiles = length(problemName)*length(selectors)*length(creditDef);
        filesProcessed = 0;
        h = waitbar(filesProcessed/nFiles,'Processing files...');
        for a=1:length(problemName)
            filesProcessed = filesProcessed + 1;
            waitbar(filesProcessed/nFiles);
            for b=1:length(selectors)
                for c=1:length(creditDef)
                    fileType =strcat(problemName{a},'*',selectors{b},'*', creditDef{c},'*.creditcsv');
                    files = dir(fileType);
                    if(length(files)~=30)
                        error('Missing some files. Only found %f files. Looking for 30 files',length(files));
                    end
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
                    save(strcat(problemName{a},'_',selectors{b},'_',creditDef{c},'_credit.mat'),'allcredits');
                end
            end
        end
        close(h)
        cd(origin);
        
        
    case 2 %analyze the files. mode 1 already assumed run
        nepochs = 100;
        maxEval = 25000;
        epochLength = maxEval/nepochs;
        nFiles = length(problemName)*length(selectors)*length(creditDef);
        filesProcessed = 0;
        h = waitbar(filesProcessed/nFiles,'Processing files...');
        for a=1:length(problemName)
            for b=1:length(selectors)
                for c=1:length(creditDef)
                    filesProcessed = filesProcessed + 1;
                    waitbar(filesProcessed/nFiles);
                    load(strcat(problemName{a},'_',selectors{b},'_',creditDef{c},'_credit.mat'));
                    eraCreditsAllOp = java.util.HashMap;
                    eraCreditVel = java.util.HashMap;
                    eraSelectionFreq = java.util.HashMap;
                    maximumCreditValue = 0;
%                      allcredits = cred;
                    for i=1:length(allcredits)
                        iter = allcredits{i}.keySet.iterator;
                        totalEpochSelection = zeros(nepochs,1);
                        epochSelectionFreq = java.util.HashMap;
                        maxCredit = -inf;
                        minCredit = inf;
                        rawEraCredits = java.util.HashMap;
                        %go through the operators
                        while iter.hasNext
                            operator = iter.next;
                            data = allcredits{i}.get(operator);
                            ind = isnan(data(:,2));
                            data(ind,2) = 0;
                            maxCredit = max([maxCredit;data(:,2)]);
                            minCredit = min([minCredit;data(:,2)]);
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
                            rawEraCredits.put(operator,eraCreditOneOp);
                            epochSelectionFreq.put(operator,eraSelectionOneOp);

                        end
                        maximumCreditValue = max([maxCredit,maximumCreditValue]);
                        iter = allcredits{i}.keySet.iterator;
                        while iter.hasNext
                            operator = iter.next;
                            %normalize credits
                            normEraCreditOneOp = rawEraCredits.get(operator);
%                             normEraCreditOneOp = (rawEraCredits.get(operator)-minCredit)/(maxCredit-minCredit);
                            if isempty(eraCreditsAllOp.get(operator))
                                eraCreditsAllOp.put(operator,normEraCreditOneOp);
                                eraCreditVel.put(operator,diff(normEraCreditOneOp));
                            else
                                eraCreditsAllOp.put(operator,eraCreditsAllOp.get(operator)+normEraCreditOneOp);
                                eraCreditVel.put(operator,eraCreditVel.get(operator)+diff(normEraCreditOneOp));
                            end
                            %normalize seleciton to be ratio
                            freqOperatorSelected = epochSelectionFreq.get(operator)./totalEpochSelection;
                            if isempty(eraSelectionFreq.get(operator))
                                eraSelectionFreq.put(operator,freqOperatorSelected);
                            else
                                eraSelectionFreq.put(operator,eraSelectionFreq.get(operator)+freqOperatorSelected);
                            end
                        end
                    end
                    disp(maximumCreditValue)
                    %take the average over the number of trials
                    subtitle = 'Selection rate = \n';
                    cred = zeros(nepochs,allcredits{i}.keySet.size);
                    credVel = zeros(nepochs-1,allcredits{i}.keySet.size);
                    sel = zeros(nepochs,allcredits{i}.keySet.size);
                    for i=1:nops
                        operator = ops{i};
                        %take the average over the trials
                        eraCreditsAllOp.put(operator,eraCreditsAllOp.get(operator)/length(allcredits));
                        eraCreditVel.put(operator,eraCreditVel.get(operator)/length(allcredits));
                        eraSelectionFreq.put(operator,eraSelectionFreq.get(operator)/length(allcredits));
                        
                        subtitle = strcat(subtitle,operator,':\t',sprintf('%.4f',mean(eraSelectionFreq.get(operator))),'\n');
                        cred(:,i) = eraCreditsAllOp.get(operator);
                        credVel(:,i) = eraCreditVel.get(operator);
                        sel(:,i) = eraSelectionFreq.get(operator);
                    end
                    
                    
                    h1=figure(1);
                    subplot(1,nFiles,filesProcessed)
                    plot(cred);
                    save(strcat(problemName{a},'_',selectors{b},'_',creditDef{c},'_credit','.mat'),'cred','labels','allcredits');
                    legend(labels)
                    xlabel('Epoch')
                    ylabel('Average credits earned in epoch')
                    title(strcat(problemName{a},'  ',creditDef{c},' credit','allcredits'))
                    
                    h2=figure(2);
                    subplot(1,nFiles,filesProcessed)
                    plot(abs(credVel));
                    save(strcat(problemName{a},'_',selectors{b},'_',creditDef{c},'_creditVel','.mat'),'credVel','labels');
                    xlabel('Epoch')
                    ylabel('Speed in the change of the average credits earned in epoch')
                    legend(labels)
                    title(strcat(problemName{a},'  ',creditDef{c},' velocity'))
                    
                    h3=figure(3);
                    subplot(1,nFiles,filesProcessed)
                    area(sel);
                    save(strcat(problemName{a},'_',selectors{b},'_',creditDef{c},'_sel','.mat'),'sel','labels');
                    axis([0,nepochs,0,1.5])
                    xlabel('Epoch')
                    ylabel('Average rate of selection in epoch')
                    legend(labels)
                    title(strcat(problemName{a},'  ',creditDef{c},' select'))
                    %create textbox that shows selection frequency
%                     t = annotation('textbox');
%                     t.String = sprintf(subtitle);
%                     t.Position=[0.1500    0.67    0.3589    0.2333];
%                     t.HorizontalAlignment = 'right';
                    clear allcredits;
                end
            end
        end
        close(h)
        
        plotName = strcat(problemName{a},'_',creditDef{c});
        saveas(h1,strcat(plotName,'_credit'),'fig');
        saveas(h1,strcat(plotName,'_credit'),'png');
        saveas(h2,strcat(plotName,'_velocity'),'fig');
        saveas(h2,strcat(plotName,'_velocity'),'png');
        saveas(h3,strcat(plotName,'_select'),'fig');
        saveas(h3,strcat(plotName,'_select'),'png');
        clf(h1)
        clf(h2)
        clf(h3)
end
