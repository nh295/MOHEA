function plot_subps( subproblems )
%PLOT_SUBPS Summary of this function goes here
%   Detailed explanation goes here
    pareto=[subproblems.curpoint];
    pp=[pareto.objective];
    scatter(pp(1,:), pp(2,:));
end

