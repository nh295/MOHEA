function selected = util_select()
%UTILSELECT select the subproblems according to the subproblems' utility
%
% the 10-tournament selection is used here.
%
    global subproblems params objDim rnduni;
    
    selected =1:objDim; % set the initial selection of the first m weight, the edging subproboelm.
    candidate = objDim+1:length(subproblems);
    
    selSize = ceil(params.popsize/params.selportion);
    
    % should allow change from params.
    toursize = 10;
    
    while(length(selected)<selSize)
        remainingSize = length(candidate);
        %[r, rnduni] = crandom(rnduni);
        r = rand;
        bestidx = ceil(r*remainingSize);
        
        for i=2:toursize
            %[r, rnduni] = crandom(rnduni);
            r = rand;
            idx = ceil(r*remainingSize);
            
            if (subproblems(candidate(idx)).utility > ...
                    subproblems(candidate(bestidx)).utility)
                bestidx = idx;
            end
        end
        
        index = candidate(bestidx);
        %remove the bestindex from candidate set.
        candidate = setdiff(candidate,index);
        %add the bestindex to the selection.
        %selected = union(selected, bestidx);
        selected = [selected, index];
    end
end
