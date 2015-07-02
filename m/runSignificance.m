function [pvals,sig_better] = runSignificance(data,Xlabels,Ylabels)
%this function will test each column of data with the 1st column of data:
%if the first column is random, it'll see which methods do significantly
%better or worse than random selection

[hhNum,probNum,~]=size(data);


pvals = zeros(hhNum,probNum);
sig_better = zeros(hhNum,probNum);

for i=1:probNum
    firstCol = squeeze(data(1,i,:));
    for j=2:hhNum
        [P,~,STATS] = anova1([firstCol,squeeze(data(j,i,:))],[],'off');
        pvals(j,i) = P;
        
        
        mean_diff = STATS.means(1)-STATS.means(2);
        if(mean_diff>0 && P<0.05) %95 percent confidence
            sig_better(j,i) = -1; %random did better
        elseif(mean_diff<0 && P<0.05)
            sig_better(j,i) = 1; %hyper-heuristic did better
        end
    end
end


figure
ax = gca;
hold on
for y=1:probNum
    for x=1:hhNum
        scatter(x,y,200,sig_better(x,y),'s','filled')
    end
end

ax.XTick = 1:21;
ax.XTickLabel = Xlabels;
ax.XTickLabelRotation = 90;
ax.YTick = 1:10;
ax.YTickLabel = Ylabels;
load whiteMiddleColorMap
colormap(map)
colorbar

ylabel('Problem Instance')
xlabel('Hyper-heuristic')
axis([0,hhNum+1,0,probNum+1])