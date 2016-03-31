

problemName = {'WFG1','WFG2','WFG3','WFG4','WFG5','WFG6','WFG7','WFG8','WFG9',...
%     'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10',...
    'DTLZ1','DTLZ2','DTLZ3','DTLZ4','DTLZ5','DTLZ6','DTLZ7'};
selector = {'Probability','Adaptive'};
type = {'OP','SI','CS'};
fitness = 'Do';
path = '/Users/nozomihitomi/Dropbox/MOHEA/mResExperimentB';
% indicator = 'finalHV';
indicator = 'finalIGD';
origin = cd(path);

string = '';
for i=1:length(problemName)
    string = sprintf('%s%s ',string,problemName{i});
    %get best single-operator MOEA
    if strcmp(fitness,'De')
        if strcmp(indicator,'finalIGD')
            cd(strcat(path,filesep,'finalIGDbest1opMOEAD'))
        elseif strcmp(indicator,'finalHV')
            cd(strcat(path,filesep,'finalHVbest1opMOEAD'))
        end
    elseif strcmp(fitness,'Do')
        if strcmp(indicator,'finalIGD')
            cd(strcat(path,filesep,'finalIGDbest1opSSNSGAII'))
        elseif strcmp(indicator,'finalHV')
            cd(strcat(path,filesep,'finalHVbest1opSSNSGAII'))
        end
    elseif strcmp(fitness,'I')
        if strcmp(indicator,'finalIGD')
            cd(strcat(path,filesep,'finalIGDbest1opSSIBEA'))
        elseif strcmp(indicator,'finalHV')
            cd(strcat(path,filesep,'finalHVbest1opSSIBEA'))
        end
    end
    files = dir(strcat(problemName{i},'*'));
    load(files.name);
    data = getfield(res,indicator);
    string = sprintf('%s & %1.2e & (%1.1e)',string, mean(data),std(data));
    
    %get random select
    if strcmp(fitness,'De')
        cd(strcat(path,filesep,'RandomMOEAD'))
    elseif strcmp(fitness,'Do')
        cd(strcat(path,filesep,'RandomSSNSGAII'))
    elseif strcmp(fitness,'I')
        cd(strcat(path,filesep,'RandomSSIBEA'))
    end
    files = dir(strcat(problemName{i},'*'));
    load(files.name);
    data = getfield(res,indicator);
    string = sprintf('%s & %1.2e & (%1.1e)',string, mean(data),std(data));
    
    cd(path)
    %get all tested AOS
    for j=1:length(selector)
        for k = 1:length(type);
            files = dir(strcat(problemName{i},'*',selector{j},'*',type{k},'*',fitness,'*'));
            load(files.name);
            data = getfield(res,indicator);
            string = sprintf('%s & %1.2e & (%1.1e)',string, mean(data),std(data));
        end
    end
    string = sprintf('%s\\\\ \n',string);
end

fprintf('%s',string);
cd(origin);