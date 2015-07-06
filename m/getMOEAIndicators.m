function [EI,GD,HV] = getMOEAIndicators(filename)
%reads the csv values starting from the 2nd column
%filename must include path and extension
%EI is the epsilon indicator
%GD is the generational distnace
%HV is the hypervolume

data = csvread(filename,0,1);

%sometimes there are 0.0 values added to end, so get rid of them
data = data(:,sum(data,1)>0);

%get end of run indicator values
EI = data(1,end);
GD = data(2,end);
HV = data(3,end);
