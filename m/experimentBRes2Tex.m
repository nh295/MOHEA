

%      'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10',...
problemName = {'WFG1','WFG2','WFG3','WFG4','WFG5','WFG6','WFG7','WFG8','WFG9',...
    'UF1_','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10',...
    'DTLZ1','DTLZ2','DTLZ3','DTLZ4','DTLZ5','DTLZ6','DTLZ7'};
selector = {'Probability','Adaptive'};
type = {'OP','SI','CS'};
fitness = 'I';
path = '/Users/nozomihitomi/Dropbox/MOHEA/mResExperimentB2';
% indicator = 'finalHV';
indicator = 'finalIGD';
origin = cd(path);

string = '';
for i=1:length(problemName)
    string = sprintf('%s%s ',string,problemName{i});
    
    %get best default-operator MOEA
    if strcmp(fitness,'De')
        if strcmp(indicator,'finalIGD')
            cd(strcat(path,filesep,'DefaultMOEAD'))
        elseif strcmp(indicator,'finalHV')
            cd(strcat(path,filesep,'DefaultMOEAD'))
        end
    elseif strcmp(fitness,'Do')
        if strcmp(indicator,'finalIGD')
            cd(strcat(path,filesep,'DefaultNSGAII'))
        elseif strcmp(indicator,'finalHV')
            cd(strcat(path,filesep,'DefaultNSGAII'))
        end
    elseif strcmp(fitness,'I')
        if strcmp(indicator,'finalIGD')
            cd(strcat(path,filesep,'DefaultIBEA'))
        elseif strcmp(indicator,'finalHV')
            cd(strcat(path,filesep,'DefaultIBEA'))
        end
    end
    files = dir(strcat(problemName{i},'*'));
    load(files.name);
    data = getfield(res,indicator);
    string = sprintf('%s & %1.4f & (%1.3f)',string, mean(data),std(data));
    
    %get best single-operator MOEA
    if strcmp(fitness,'De')
        if strcmp(indicator,'finalIGD')
            cd(strcat(path,filesep,'finalIGDbest1opMOEAD'))
        elseif strcmp(indicator,'finalHV')
            cd(strcat(path,filesep,'finalHVbest1opMOEAD'))
        end
    elseif strcmp(fitness,'Do')
        if strcmp(indicator,'finalIGD')
            cd(strcat(path,filesep,'finalIGDbest1opNSGAII'))
        elseif strcmp(indicator,'finalHV')
            cd(strcat(path,filesep,'finalHVbest1opNSGAII'))
        end
    elseif strcmp(fitness,'I')
        if strcmp(indicator,'finalIGD')
            cd(strcat(path,filesep,'finalIGDbest1opIBEA'))
        elseif strcmp(indicator,'finalHV')
            cd(strcat(path,filesep,'finalHVbest1opIBEA'))
        end
    end
    files = dir(strcat(problemName{i},'*'));
    load(files.name);
    data = getfield(res,indicator);
    string = sprintf('%s & %1.4f & (%1.3f)',string, mean(data),std(data));
    
    %get random select
    if strcmp(fitness,'De')
        cd(strcat(path,filesep,'RandomMOEAD'))
    elseif strcmp(fitness,'Do')
        cd(strcat(path,filesep,'RandomNSGAII'))
    elseif strcmp(fitness,'I')
        cd(strcat(path,filesep,'RandomIBEA'))
    end
    files = dir(strcat(problemName{i},'*'));
    load(files.name);
    data = getfield(res,indicator);
    string = sprintf('%s & %1.4f & (%1.3f)',string, mean(data),std(data));
    
    cd(path)
    %get all tested AOS
    for j=1:length(selector)
        for k = 1:length(type);
            files = dir(strcat(problemName{i},'*',selector{j},'*',type{k},'*',fitness,'*'));
            load(files.name);
            data = getfield(res,indicator);
            string = sprintf('%s & %1.4f & (%1.3f)',string, mean(data),std(data));
        end
    end
    string = sprintf('%s\\\\ \n',string);
end

fprintf('%s',string);
cd(origin);