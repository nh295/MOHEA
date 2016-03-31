function [fHV,IGD,finalHV,finalIGD,ET] = getMOEAIndicators(filename,npts)
%reads the csv values starting from the 2nd column
%filename must include path and extension
%npts is the number of first readings desired
%EI is the epsilon indicator
%GD is the generational distnace
%HV is the hypervolume
%IGD is the inverted generational distance

try
    % data = csvread(filename,0,1);
    fid = fopen(filename,'r');
    IGD = zeros(1,npts);
    fHV = zeros(1,npts);
    ET = zeros(1,npts);
    while(~feof(fid))
        line = strsplit(fgetl(fid),',');
        switch line{1}
            case{'InvertedGenerationalDistance'}
                tIGD = readLine(line);
                %         case{'Number of Injections'}
                %             Inj = readLine(line);
                %         case{'NFE'}
            case{'Elapsed Time'}
                tET = readLine(line);
            case{'FastHypervolume'}
                tfHV = readLine(line);
            case{'Final HV'}
                finalHV = readLine(line);
            case{'Final IGD'}
                finalIGD = readLine(line);
            otherwise
                continue;
        end
    end
    fclose(fid);
    len = length(tIGD);
    %get end of run indicator values
    temp = min([len,npts]);
    IGD(1:temp) = tIGD(1:temp);
    fHV(1:temp) = tfHV(1:temp);
    ET(1:temp) = tET(1:temp);
catch ME
    warning(strcat('Problem file: ', filename));
    rethrow(ME);
end
end

function [out] = readLine(line)
out = zeros(length(line)-1,1);
for i=1:length(line)-1
    out(i)=str2double(line{i+1});
end
end