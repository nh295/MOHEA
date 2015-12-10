function [AEI,GD,fHV,IGD,ET] = getMOEAIndicators(filename)
%reads the csv values starting from the 2nd column
%filename must include path and extension
%EI is the epsilon indicator
%GD is the generational distnace
%HV is the hypervolume
%IGD is the inverted generational distance

% data = csvread(filename,0,1);
fid = fopen(filename,'r');
while(~feof(fid))
    line = strsplit(fgetl(fid),',');
    switch line{1}
        case{'AdditiveEpsilonIndicator'}
            AEI = readLine(line);
        case{'GenerationalDistance'}
            GD = readLine(line);
        case{'InvertedGenerationalDistance'}
            IGD = readLine(line);
%         case{'Number of Injections'}
%             Inj = readLine(line);
%         case{'NFE'}
        case{'Elapsed Time'}
            ET = readLine(line);
        case{'FastHypervolume'}
            fHV = readLine(line);
        otherwise
            continue;
    end
end
fclose(fid);
%get end of run indicator values
AEI = AEI(end);
% Inj = Inj(end);
GD = GD(end);
IGD = IGD(end);
fHV = fHV(end);
ET = ET(end);
end

function [out] = readLine(line)
out = zeros(length(line)-1,1);
for i=1:length(line)-1
       out(i)=str2double(line{i+1});
end
end