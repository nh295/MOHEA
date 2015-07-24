function [mAEI,mGD,mHV,mIGD] = meanHistory(AEI,GD,HV,IGD)
%This function takes matrices of indicators and averages over the number of
%trials. The matrices of nxm have n trials and m points.

mAEI = mean(AEI,1);
mGD = mean(GD,1);
mHV = mean(HV,1);
mIGD = mean(IGD,1);