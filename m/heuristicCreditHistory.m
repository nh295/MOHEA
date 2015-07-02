function heuristicCreditHistory(path,filenames)
%heuristicCreditHistory plots the credit history of each heuristic used. A
%subplot shows 1) the credits at every iteration and 2) the cumulative
%credits over time
%filenames should come from dir and have a field "name"

import hh.*

%load in data
numfile = length(filenames);
creditHistories = cell(numfile,1);
for i=1:numfile
    creditHistoryRepo = hh.IO.IOCreditHistory.loadHistory(strcat(path,filesep,filenames(i).name));
    heuristicsJ = creditHistoryRepo.getHeuristics;
    creditHistory = zeros(creditHistoryRepo.getHistory(heuristicsJ.iterator.next).getHistory.size,heuristicsJ.size);
    
    iter_heuristics = heuristicsJ.iterator;
    heur_ind = 1;
    while iter_heuristics.hasNext
        cred_ind = 1;
        iter_credit = creditHistoryRepo.getHistory(iter_heuristics.next).iterator;
        while iter_credit.hasNext
            creditHistory(cred_ind,heur_ind) = iter_credit.next.getValue;
            cred_ind = cred_ind + 1;
        end
        heur_ind = heur_ind + 1;
    end
    creditHistories{i} = creditHistory;
end

%get heuristic labels
heuristics = cell(heuristicsJ.size,1);
iter_heuristics = heuristicsJ.iterator;
heur_ind = 1;
while iter_heuristics.hasNext
    heuristics{heur_ind} = char(iter_heuristics.next);
    heur_ind = heur_ind + 1;
end

%plot credits for all n trials
scattercolor = {'b','r','g','m','c','k','b','r','g','m','c','k'};
scattermarker = {'.','.','.','.','.','.','d','d','d','d','d','d'};
plotcolors = {'-b','-r','-g','-m','-c','-k',...
    ':b',':r',':g',':m',':c',':k'};

subplot(2,1,1)
hold on
for i=1:numfile
    credits =creditHistories{i};
    [~,c] = size(credits);
    for j=1:c
        scatter(1:length(credits(:,j)),credits(:,j),20,scattercolor{j},scattermarker{j});
    end
end
hold off
title('Credits at iteration t')
ylabel('Credits')
xlabel('Iteration t')
legend(heuristics)

%plot cumulative credits for all n trials
subplot(2,1,2)
hold on
for i=1:numfile
    credits =creditHistories{i};
    [~,c] = size(credits);
    for j=1:c
        plot(cumsum(credits(:,j)),plotcolors{j});
    end
end
hold off
title('Cumulative Sum of Credits Over Time')
ylabel('Cumulative sum of credits')
xlabel('iteration')
legend(heuristics)