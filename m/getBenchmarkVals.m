function [vals,benchmark_names] = getBenchmarkVals(path,prob_name,indicator)
%plots MOEA/D, MOEA/D-DRA, FRRMAB, eMOEA on one figure for all UF 1-10
%problems. problemm name should be like 'UF11' with no underscores.
%indicator should be something like 'HV' to define which indicator to plot.
%returns the indicator valsues for each benchmark for specified indicator
%and the labels of the names of each benchmark algorithm

% path2benchmark = '/Users/nozomihitomi/Dropbox/MOHEA/Benchmarks';
path2benchmark = strcat(path,'Benchmarks');
benchmark_names = {'MOEADDRA','MOEADPM', 'eMOEA','Random'};

vals = zeros(30,length(benchmark_names));

for i=1:length(benchmark_names)
    %string names together to load m file containing results
    algorithm = benchmark_names{i};
    mfilename = strcat(path2benchmark,filesep,algorithm,filesep,prob_name,'_', algorithm,'.mat');
    load(mfilename)
    %assumes that mat file was saved with variable results containing all
    %MOEA indicator metrics
    vals(:,i) = getfield(res,indicator);
end
