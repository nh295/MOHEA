function util_update()
%Search Utility updation.
%   This function update the subproblem's search utility, using the
%   improvement of the subproblems' objective vaue since the last caculation.
    
    global subproblems idealpoint;
    shrehold= 0.001;

    allweight = [subproblems.weight];
    curps = [subproblems.curpoint];
    oldps = [subproblems.oldpoint];
    newobj=subobjective(allweight, [curps.objective], idealpoint, 'te');
    oldobj=subobjective(allweight, [oldps.objective], idealpoint, 'te' );
    
    delta =  oldobj - newobj;
    
    %in rare case the delta can be less then 0. change it in that
    %situition.
    %[subproblems(delta<0).curpoint] = subproblems(delta<0).oldpoint;
    %[subproblems(delta<0).utility] = deal(1);
    
    [subproblems(delta>=shrehold).utility] = deal(1.0);
    
    %update = and(delta<shrehold, delta>=0);
    %update = and(delta<shrehold, delta>=0);
    update = delta<shrehold;
    % make the updation.
    if any(update)
        util = (0.95 + 0.05*delta(update)/shrehold) ... 
            .*[subproblems(update).utility];
        %util = min(util, 1.0);
        cellutil = num2cell(util);
        [subproblems(update).utility] = cellutil{:};
    end
    
    %back up the old optimal values.
    [subproblems.oldpoint] = subproblems.curpoint;
end
