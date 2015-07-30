function NDPop = readObjectives(filename,numObjectives)
%this function will read the objectives file output from MOEA Framework
%PopulationIO writeObjectives. It will return the objectives for all
%solutions in the nondominated population 

fid = fopen(filename);
maxSolution = 600;

NDPop = zeros(maxSolution,numObjectives);
count = 1;
while(~feof(fid))  
    tline = fgetl(fid);
    objs = regexp(tline,'\d*[.]\d*[\S]*\d*','match');
    for i=1:numObjectives
        NDPop(count,i) = str2double(objs{i});
    end
    count = count + 1;
end

fclose(fid);

%remove empty matrix elements
NDPop = NDPop(1:count-1,:);