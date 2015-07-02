function MOEAindicators

origin = cd('/Users/nozomihitomi/Dropbox/Cornell/CEE 6660 Systems Engineering Under Uncertainty/HHCreditTest/results');

[FILENAME, PATHNAME, ~] = uigetfile('*.res', 'Pick indicator results files', '*.res','MultiSelect','on');

cd(origin)

%load in all data
numfile = length(FILENAME);
indicatorHistory = cell(numfile,1);
for i=1:numfile
    indicatorHistory{i} = csvread(strcat(PATHNAME,FILENAME{i}),0,1);
end

%get indicator labels
fid = fopen(strcat(PATHNAME,FILENAME{i}));
[a,b] = size(indicatorHistory{i});
indicators = cell(a,1);
for i=1:a
    tmp = textscan(fid, '%s',b,'Delimiter',',');
    tmp2 = tmp{1,1};
    indicators(i) = tmp2(1,1);
end
fclose(fid);

%find the NFE label
NFEind = find(strcmp('NFE',indicators));
%for some reason csvread reads in one column too many
%find where NFEind == 0 and delete those columns

for i=1:numfile
    tmp = indicatorHistory{i};
    indicatorHistory{i} = tmp(:,find(tmp(NFEind,:))~=0);
end

%loop over the number of indicators
subplotnum = 1;
for k=1:a
    if k==NFEind %skip over the NFE indicator
        continue
    end
    subplot(a-1,1,subplotnum)
    subplotnum = subplotnum + 1;
    hold on
    %loop over the number of trials
    for i=1:numfile
        hist = indicatorHistory{i};
        plot(hist(NFEind,:),hist(k,:),'g');
    end
    xlabel('NFE')
    ylabel(indicators{k})
    title(strcat('History of ',indicators{k}))
    hold off
end
