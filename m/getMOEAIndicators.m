function [EI,GD,HV] = getMOEAIndicators(filename)
%reads the csv values starting from the 2nd column
%filename must include path and extension
%EI is the epsilon indicator
%GD is the generational distnace
%HV is the hypervolume

data = csvread(filename,0,1);
EI = data(1,1);
GD = data(2,1);
HV = data(3,1);
