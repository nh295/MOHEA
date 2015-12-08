function [vals,out] = getBenchmarkVals(path,prob_name,indicator,mode)
%plots MOEA/D, MOEA/D-DRA, FRRMAB, eMOEA on one figure for all UF 1-10
%problems. problemm name should be like 'UF11' with no underscores.
%indicator should be something like 'HV' to define which indicator to plot.
%returns the indicator valsues for each benchmark for specified indicator
%and the labels of the names of each benchmark algorithm

% path2benchmark = '/Users/nozomihitomi/Dropbox/MOHEA/Benchmarks';
path2benchmark = strcat(path,filesep,'Benchmarks');
if strcmp(mode,'MOEAD')
    benchmark_names = {'best1opMOEAD','RandomMOEAD'};
elseif strcmp(mode,'eMOEA')
    benchmark_names = {'best1opeMOEA','RandomeMOEA'};
end

% benchmark_names = {'best1opMOEAD','RandomMOEAD'};

vals = zeros(30,length(benchmark_names));
out = cell(1,length(benchmark_names));

for i=1:length(benchmark_names)
    %string names together to load m file containing results
    algorithm = benchmark_names{i};
    
    if strcmp(algorithm,'best1opeMOEA') || strcmp(algorithm,'best1opMOEAD')
        file = dir(strcat(path2benchmark,filesep,algorithm,filesep,prob_name,'*'));
        mfilename = strcat(path2benchmark,filesep,algorithm,filesep,file(1).name);
        str = strsplit(file(1).name,'.');
        str = strsplit(str{1},'+');
        str = strsplit(str{1},'_');
        if strcmp(algorithm,'best1opeMOEA')
            str{end-1} = '\epsilonMOEA';
        elseif strcmp(algorithm,'best1opMOEAD')
            str{end-1} = 'DRA';
        end
        out{i} = strcat('{',str{end-1},'-',upper(str{end}),'}');
    elseif strcmp(algorithm,'MOEADDRA') || strcmp(algorithm,'eMOEA')
        mfilename = strcat(path2benchmark,filesep,algorithm,filesep,prob_name,'_', algorithm,'.mat');
        if  strcmp(algorithm,'MOEADDRA')
            out{i} = strcat('DRA-DE');
        elseif strcmp(algorithm,'eMOEA')
            out{i} = '\epsilonMOEA-SBX';
        end
    else
        mfilename = strcat(path2benchmark,filesep,algorithm,filesep,prob_name,'_', algorithm,'.mat');
        str = strsplit(benchmark_names{i},'m');
        if strcmp(algorithm,'RandomeMOEA')
            str{end} = '\epsilonMOEA';
        elseif strcmp(algorithm,'RandomMOEAD')
            str{end} = 'DRA';
        end
        out{i} = strcat(str{end},'-Rand');
    end
    load(mfilename)
    %assumes that mat file was saved with variable results containing all
    %MOEA indicator metrics
    vals(:,i) = getfield(res,indicator);
end
