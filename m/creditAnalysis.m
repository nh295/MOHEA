function creditAnalysis(mode)


% problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7'};
problemName = {'UF1_'};
selectors = {'Random'};
selectorShort = {'AP'};
creditDef = { 'ParentDom','OffspringParetoFront','OffspringEArchive','ParetoFrontContribution','EArchiveContribution','OPa_BIR2PARENT','OPop_BIR2PARETOFRONT','OPop_BIR2ARCHIVE','CNI_BIR2PARETOFRONT','CNI_BIR2ARCHIVE'};
creditShort = {'ODP','OPopF','OPopEA','CPF','CEA','OPaR2','OPopPFR2','OPopEAR2','CPFR2','CEAR2'};
path = '/Users/nozomihitomi/Desktop/untitled folder';
origin = cd(path);
nops = 6;

switch mode
    case 1 %read in the credit csv files
        for a=1:length(problemName)
            for b=1:length(selectors)
                for c=1:length(creditDef)
                    fileType =strcat(problemName{a},selectors{b},'*', creditDef{c},'*.creditcsv');
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
                    eraCredits = java.util.HashMap;
                    for i=1:length(allcredits)
                        iter = allcredits{i}.keySet.iterator;
                        while iter.hasNext
                            operator = iter.next;
                            data = allcredits{i}.get(operator);
                            era = zeros(nepochs,1);
                            for j=1:nepochs
                                ind1 = epochLength*(j-1)<data(:,1);
                                ind2 = data(:,1)<epochLength*j;
                                credits = data(and(ind1,ind2),2);
                                era(j)=sum(credits);
                            end
                            if isempty(eraCredits.get(operator))
                                eraCredits.put(operator,era);
                            else
                                eraCredits.put(operator,eraCredits.get(operator)+era);
                            end
                        end
                    end
                    %take the average over the number of trials
                    iter2 = allcredits{i}.keySet.iterator;
                    figure
                    labels = cell(nops,1);
                    ind = 1;
                    while iter2.hasNext
                        operator = iter2.next;
                        eraCredits.put(operator,eraCredits.get(operator)/length(allcredits));
                        labels{ind}=operator;
                        plot(eraCredits.get(operator))
                        hold on
                        ind = ind +1;
                    end
                    legend(labels)
                end
            end
        end
end
