function [vals,out] = getBenchmarkVals(path,prob_name,indicator,mode)
%plots MOEA/D, MOEA/D-DRA, FRRMAB, eMOEA on one figure for all UF 1-10
%problems. problemm name should be like 'UF11' with no underscores.
%indicator should be something like 'HV' to define which indicator to plot.
%returns the indicator valsues for each benchmark for specified indicator
%and the labels of the names of each benchmark algorithm

path2benchmark = strcat(path,filesep,'mResExperimentB2');
if strcmp(mode,'MOEAD')
    benchmark_names = {'MOEAD','best1opMOEAD','RandomMOEAD'};
elseif strcmp(mode,'NSGAII')
    benchmark_names = {'NSGAII','best1opNSGAII','RandomNSGAII'};
elseif strcmp(mode,'IBEA')
    benchmark_names = {'IBEA','best1opIBEA','RandomIBEA'};
end

vals = zeros(30,length(benchmark_names));
out = cell(1,length(benchmark_names));

for i=1:length(benchmark_names)
    %string names together to load m file containing results
    algorithm = benchmark_names{i};
    
    if strcmp(algorithm,'best1opMOEAD') || strcmp(algorithm,'best1opNSGAII') || strcmp(algorithm,'best1opIBEA')
        file = dir(strcat(path2benchmark,filesep,indicator,algorithm,filesep,prob_name,'*'));
        mfilename = strcat(path2benchmark,filesep,indicator,algorithm,filesep,file(1).name);
        str = strsplit(file(1).name,'.');
        str = strsplit(str{1},'+');
        str = strsplit(str{1},'_');
        if strcmp(algorithm,'best1opMOEAD')
            str{end-1} = 'MOEAD*';
        elseif strcmp(algorithm,'best1opNSGAII')
            str{end-1} = 'NSGAII*';
        elseif strcmp(algorithm,'best1opIBEA')
            str{end-1} = 'IBEA*';
        end
        out{i} = strcat(str{end-1},'-',upper(str{end}));
    elseif strcmp(algorithm,'MOEAD') || strcmp(algorithm,'NSGAII') || strcmp(algorithm,'IBEA')
        file = dir(strcat(path2benchmark,filesep,'Default',algorithm,filesep,prob_name,'*', algorithm,'*.mat'));
        mfilename = strcat(path2benchmark,filesep,'Default',algorithm,filesep,file(1).name);
        out{i} = mode;
    else
        file = dir(strcat(path2benchmark,filesep,algorithm,filesep,prob_name,'*Random*.mat'));
        mfilename = strcat(path2benchmark,filesep,algorithm,filesep,file(1).name);
        out{i} = strcat('Rand-',mode);
    end
    load(mfilename);
    %assumes that mat file was saved with variable results containing all
    %MOEA indicator metrics
    vals(:,i) = getfield(res,indicator);
end
