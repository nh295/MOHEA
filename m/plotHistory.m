function plotHistory(path,hyperheuristic,problem,saveFilename)
%plots the mean histories of AEI, GD, HV, and IGD for the specified
%hyperheuristics defintion for the problem. Multiple hyperheuristics are
%allowed. Only one problem is allowed. path is where the .res files are
%located

h=figure;
for i=1:length(hyperheuristic)
    [AEI,GD,HV,IGD] = getAllResults(path,hyperheuristic(i).selector,hyperheuristic(i).credDef,problem);
    mAEi = mean(AEI,1);mGD = mean(GD,1); mHV = mean(HV,1); mIGD = mean(IGD,1);
    subplot(4,1,1);
    hold on;
    plot(mAEi);
    subplot(4,1,2);
    hold on;
    plot(mGD);
    subplot(4,1,3);
    hold on;
    plot(mHV);
    subplot(4,1,4);
    hold on;
    plot(mIGD);
end

legend('Random','PM-CEA','AP-CEA')
subplot(4,1,1);
title(problem)
if(~isempty(saveFilename))
    print(h,'-dtiff',saveFilename);
end