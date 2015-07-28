function heuristicSelectionHistory(path,filenames)
%heuristicSelectionHistory plots the frequency of selection of a heuristic
%as a ratio wrt to the selecion of the other heuristics. Normalizes the
%search process into 100 segments because each trial may have a different
%number of iterations.
%filenames should come from dir and have a field "name"

numfile = length(filenames);

%load in data
selectionHistory = cell(numfile,1);
for i=1:numfile
    unOrderedSelectionHist = hh.IO.IOSelectionHistory.loadHistory(strcat(path,filesep,filenames(i).name));
    orderedSelectionHist = unOrderedSelectionHist.getOrderedHistory;
    thisHistory = cell(orderedSelectionHist.size,1);
    iter = orderedSelectionHist.iterator;
    ind = 1;
    while iter.hasNext
        thisHistory{ind} = char(iter.next.toString);
        ind = ind+1;
    end
    selectionHistory{i} = thisHistory;
end

%compute average selection frequency over the n trials
%divide up each trial into m segments and take the average within segments
heuristics = unique(selectionHistory{1});
num_heuristics = length(heuristics);
mSegments = 100;
selectAvgs = zeros(mSegments,num_heuristics,numfile);
for i=1:numfile
    history = selectionHistory{i};
    histLen = length(history);
    leftOver = mod(histLen,mSegments); %try to spread left over to as many segments
    interval = floor(histLen/mSegments);
    index = 1;
    for j=1:mSegments
        if j<leftOver %if there is still some left over from modulus add it to the average
            histSegment = history(index:index+interval+1);
            index = index+interval+1;
        else
            histSegment = history(index:index+interval);
            index = index+interval;
        end
        for k=1:num_heuristics
            %get numer of selected times divided by the number of total
            %selections within history segment to get a selection rate
            selectAvgs(j,k,i) = length(find(strcmp(histSegment,heuristics{k})))/length(histSegment);
        end
    end
end

avgSelectionRate = squeeze(mean(selectAvgs,3));
plot(linspace(0,100,mSegments),avgSelectionRate)
legend(heuristics)
title('Selection Rate of Each Heuristic')
xlabel('% of optimization process')
ylabel('Selection Rate')
