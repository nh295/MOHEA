function [v, x] = evaluate( prob, x )
%EVALUATE function evaluate an individual structure of a vector point with
%the given multiobjective problem.

%   Detailed explanation goes here
%   prob: is the multiobjective problem.
%   x: is a vector point, or a individual structure.
%   v: is the result objectives evaluated by the mop.
%   x: if x is a individual structure, then x's objective field is modified
%   with the evaluated value and pass back.

    
    if isstruct(x)
        X = x.parameter;
        v = prob.func(X);
        x.objective=v;
    else
        X = x;
        v = prob.func(X);
    end
    
    % set the evaluation counter.
    global evalCounter;
    evalCounter = evalCounter+1;
    %global idealpoint;
    %idealpoint = min(idealpoint, v);
end