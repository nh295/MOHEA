function [ET] = getTime(path,selector,creditDef,problemName)
%reads the csv values starting from the 2nd column
%filename must include path and extension

origin = cd(path);
files = dir(strcat(problemName,'*',selector,'*',creditDef,'*.res'));
cd(origin)
nfiles = length(files);
npts = 1;
ET = zeros(nfiles,npts);
for i=1:nfiles
    tET = getRunTime(strcat(path,filesep,files(i).name));
    ET(i,:) = tET;
end
end

function ET = getRunTime(filename)
fid = fopen(filename,'r');
while(~feof(fid))
    line = strsplit(fgetl(fid),',');
    switch line{1}
        case{'Elapsed Time'}
            ET = readLine(line);
        otherwise
            continue;
    end
end
fclose(fid);
%get end of run indicator values
ET = ET(end);
end

function [out] = readLine(line)
out = zeros(length(line)-1,1);
for i=1:length(line)-1
       out(i)=str2double(line{i+1});
end
end