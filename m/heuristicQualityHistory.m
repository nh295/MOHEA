function heuristicQualityHistory(path,filenames)
%heuristicQualityHistory plots the heuriatic quality history of each heuristic used. A
%subplot shows the quality at every iteration
%filenames should come from dir and have a field "name"
import hh.*

%load in data
numfile = length(filenames);
qualHistories = cell(numfile,1);
for i=1:numfile
    heuristicQual = hh.IO.IOQualityHistory.loadHistory(strcat(path,filesep,filenames(i).name));
    heuristicsJ = heuristicQual.getHeuristics;
    qualHistory = zeros(heuristicQual.getHistory(heuristicsJ.iterator.next).size,heuristicsJ.size);
    
    iter_heuristics = heuristicsJ.iterator;s
    heur_ind = 1;
    while iter_heuristics.hasNext
        qual_ind = 1;
        iter_qual = heuristicQual.getHistory(iter_heuristics.next).iterator;
        while iter_qual.hasNext
            qualHistory(qual_ind,heur_ind) = iter_qual.next;
            qual_ind = qual_ind + 1;
        end
        heur_ind = heur_ind + 1;
    end
    qualHistories{i} = qualHistory;
end

%get heuristic labels
heuristics = cell(heuristicsJ.size,1);
iter_heuristics = heuristicsJ.iterator;
heur_ind = 1;
while iter_heuristics.hasNext
    heuristics{heur_ind} = char(iter_heuristics.next);
    heur_ind = heur_ind + 1;
end

for i=1:numfile
    plot(qualHistories{i})
end
hold off
title('Quality at iteration t')
ylabel('Quality')
xlabel('Iteration t')
legend(heuristics)
