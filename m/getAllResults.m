function [AEI,GD,HV,IGD] = getAllResults(path,selector,creditDef,problemName)
%Given a path, the heuristic selector name, credit definition name, and
%problem name this function will get all the results from the given path 
%with files that include the selector name and the credit definition name. 
%
%This function returns the epsilon indicator EI, generational distance GD,
%hyper volume HV, and inverted generational distance IGD as a n x m matrix
%where n is the number files containing the selector name and credit
%definition name and m is the number of values collected per file.

origin = cd(path);
files = dir('*.res');
nfiles = length(files);
AEI  = zeros(nfiles,1); %additive epsilon indicator
GD  = zeros(nfiles,1); %generational distance
HV  = zeros(nfiles,1); %hypervolume
IGD  = zeros(nfiles,1); %inverted generational distance
yesFile = false(nfiles,1);
for i=1:nfiles
    a = strfind(files(i).name,selector);
    b = strfind(files(i).name,creditDef);
    c = strfind(files(i).name,problemName);
    if ~isempty(a) && ~isempty(b) && ~isempty(c)
        [tAEI,tGD,tHV,tIGD] = getMOEAIndicators(strcat(path,filesep,files(i).name));
        AEI(i) = tAEI(end);
        GD(i) = tGD(end);
        HV(i) = tHV(end);
        IGD(i) = tIGD(end);
        yesFile(i) = true;
    end
end

cd(origin)

AEI = AEI(yesFile,:);
GD = GD(yesFile,:);
HV = HV(yesFile,:);

end