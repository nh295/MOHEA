problemName = {'UF1','UF2','UF3','UF4','UF5','UF6','UF7','UF8','UF9','UF10'};

path = 'C:\Users\SEAK2\Nozomi\FRRMAB\results';
selectors = {'Random','FRRMAB'};


for i=1:length(problemName)
    figure(1)
    h1 = subplot(2,ceil(length(problemName)/2),i);
    hold on
    figure(2)
    h2 = subplot(2,ceil(length(problemName)/2),i);
    hold on
    results = cell(length(selectors),1);
    for j=1:length(selectors)
        results{j} = jmetalAnalyze(strcat(path,filesep,selectors{j},filesep,problemName{i}),problemName{i});
    end
    [a,~] = size(results{1});
    resHV = zeros(a,length(selectors));
    resIGD = zeros(a,length(selectors));
    for k=1:length(selectors)
        tmp = results{k};
        resHV(:,k) = tmp(:,1);
        resIGD(:,k) = tmp(:,2);
        %comparing to random selection
        [~,h] = ranksum(resHV(:,1),resHV(:,k));
        if h==1
            title(h1,num2str(k))
        end
        [~,h] = ranksum(resHV(:,1),resHV(:,k));
        if h==1
            title(h1,num2str(k))
        end
    end
    boxplot(h1,resHV);
    boxplot(h2,resIGD);
end
        
        