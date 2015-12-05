function [AEI,GD,fHV,IGD] = getAllResults(path,selector,creditDef,problemName)
%Given a path, the heuristic selector name, credit definition name, and
%problem name this function will get all the results from the given path 
%with files that include the selector name and the credit definition name. 
%
%This function returns the epsilon indicator EI, generational distance GD,
%hyper volume HV, and inverted generational distance as a n x m matrix where n is the number files
%containing the selector name and credit definition name and m is the
%number of values collected per file.

origin = cd(path);
files = dir(strcat(problemName,'*',selector,'*',creditDef,'*.res'));
cd(origin)
nfiles = length(files);
npts = 1;
AEI  = zeros(nfiles,npts);
GD  = zeros(nfiles,npts);
fHV  = zeros(nfiles,npts);
IGD  = zeros(nfiles,npts);
for i=1:nfiles
    [tAEI,tGD,tfHV,tIGD] = getMOEAIndicators(strcat(path,filesep,files(i).name));
    AEI(i,:) = tAEI;
    GD(i,:) = tGD;
    fHV(i,:) = tfHV;
    IGD(i,:) = tIGD;
end
end
