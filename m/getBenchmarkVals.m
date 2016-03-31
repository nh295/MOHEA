function [vals,out] = getBenchmarkVals(path,prob_name,indicator,mode)
%plots MOEA/D, MOEA/D-DRA, FRRMAB, eMOEA on one figure for all UF 1-10
%problems. problemm name should be like 'UF11' with no underscores.
%indicator should be something like 'HV' to define which indicator to plot.
%returns the indicator valsues for each benchmark for specified indicator
%and the labels of the names of each benchmark algorithm

path2benchmark = strcat(path,filesep,'mResExperimentB');
if strcmp(mode,'MOEAD')
    benchmark_names = {'MOEAD','best1opMOEAD','RandomMOEAD'};
elseif strcmp(mode,'SSNSGAII')
    benchmark_names = {'SSNSGAII','best1opSSNSGAII','RandomSSNSGAII'};
elseif strcmp(mode,'SSIBEA')
    benchmark_names = {'SSIBEA','best1opSSIBEA','RandomSSIBEA'};
end

vals = zeros(30,length(benchmark_names));
out = cell(1,length(benchmark_names));

for i=1:length(benchmark_names)
    %string names together to load m file containing results
    algorithm = benchmark_names{i};
    
    if strcmp(algorithm,'best1opMOEAD') || strcmp(algorithm,'best1opSSNSGAII') || strcmp(algorithm,'best1opSSIBEA')
        file = dir(strcat(path2benchmark,filesep,indicator,algorithm,filesep,prob_name,'*'));
        mfilename = strcat(path2benchmark,filesep,indicator,algorithm,filesep,file(1).name);
        str = strsplit(file(1).name,'.');
        str = strsplit(str{1},'+');
        str = strsplit(str{1},'_');
        if strcmp(algorithm,'best1opMOEAD')
            str{end-1} = 'MOEAD*';
        elseif strcmp(algorithm,'best1opSSNSGAII')
            str{end-1} = 'SSNSGAII*';
        elseif strcmp(algorithm,'best1opSSIBEA')
            str{end-1} = 'SSIBEA*';
        end
        out{i} = strcat('{',str{end-1},'-',upper(str{end}),'}');
    elseif strcmp(algorithm,'MOEAD') || strcmp(algorithm,'SSNSGAII') || strcmp(algorithm,'SSIBEA')
        file = dir(strcat(path2benchmark,filesep,'Default',algorithm,filesep,prob_name,'*', algorithm,'*.mat'));
        mfilename = strcat(path2benchmark,filesep,'Default',algorithm,filesep,file(1).name);
        out{i} = mode;
    else
        file = dir(strcat(path2benchmark,filesep,algorithm,filesep,prob_name,'*Random*.mat'));
        mfilename = strcat(path2benchmark,filesep,algorithm,filesep,file(1).name);
        out{i} = strcat('Rand-',mode);
    end
    load(mfilename)
    %assumes that mat file was saved with variable results containing all
    %MOEA indicator metrics
    vals(:,i) = getfield(res,indicator);
end
