function plotMetrics(data,Xlabels,Ylabels)

[hhNum,probNum]=size(squeeze(data));

figure
ax = gca;
hold on

for x=1:hhNum
    for y=1:probNum
        scatter(x,y,200,data(x,y),'s','filled')
    end
end

ax.XTick = 1:28;
ax.XTickLabel = Xlabels;
ax.XTickLabelRotation = 90;
ax.YTick = 1:10;
ax.YTickLabel = Ylabels;

load('gray2ColorMap.mat')
colormap(gray2)
colorbar

axis([0,hhNum+1,0,probNum+1])
