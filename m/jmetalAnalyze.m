function jmetalAnalyze

javaaddpath(strcat('dist',filesep,'MOHEA.jar'));

origin = cd;
cd ..;


h1 = figure(1);
h2 = figure(2);

probs = [1,4];

for i=1:length(probs)
    
    cd(origin)
    
    probfactory = org.moeaframework.core.spi.ProblemFactory.getInstance();
    problem = strcat('UF',num2str(probs(i)));
    prob = probfactory.getProblem(problem);
    refset = probfactory.getReferenceSet(problem);
    ref = loadObjs2(strcat('pf',filesep,problem,'.dat'));
    HVindicator = org.moeaframework.core.indicator.Hypervolume(prob,refset);
    refPoint = org.moeaframework.core.Solution([2.0,2.0]);
    FastHVindicator = org.moeaframework.core.indicator.jmetal.FastHypervolume(prob,refset,refPoint);
    IGDIndicator = org.moeaframework.core.indicator.InvertedGenerationalDistance(prob,refset);
    
    cd ..;
    cd(strcat('FRRMAB',filesep,'results',filesep,'500 trials'));
    resPath = cd;
    
    %compute MOEAD metrics
%     cd(strcat(resPath,filesep,'MOEAD_CEC2009_',problem));
    files = dir(strcat('MOEAD_CEC2009_',problem,'_FUN*'));
    res1 = zeros(length(files),2);
    for j=1:length(files)
        ndpop = loadObjs1(files(j).name);
        res1(j,1)=FastHVindicator.evaluate(ndpop);
        res1(j,2)=IGDIndicator.evaluate(ndpop);
%         set = loadObjs2(files(j).name);
%         res1(j,1) = computeHV(set,[2,2],'min');
%         res1(j,2) = computeIGD(set,ref);
    end
    
    %compute MOEAD-DRA metrics
%     cd(strcat(resPath,filesep,'MOEADDRA',filesep,problem));
    files = dir(strcat('MOEADDRA_CEC2009_',problem,'_FUN*'));
    res2 = zeros(length(files),2);
    for j=1:length(files)
        ndpop = loadObjs1(files(j).name);
        res2(j,1)=FastHVindicator.evaluate(ndpop);
        res2(j,2)=IGDIndicator.evaluate(ndpop);
%         set = loadObjs2(files(j).name);
%         res2(j,1) = computeHV(set,[2,2],'min');
%         res2(j,2) = computeIGD(set,ref);
    end
    
    %compute Random selection metrics
%     cd(strcat(resPath,filesep,'Random',filesep,problem));
    files = dir(strcat('Rand_CEC2009_',problem,'_FUN*'));
    res3 = zeros(length(files),2);
    for j=1:length(files)
        ndpop = loadObjs1(files(j).name);
        res3(j,1)=FastHVindicator.evaluate(ndpop);
        res3(j,2)=IGDIndicator.evaluate(ndpop);
%         set = loadObjs2(files(j).name);
%         res3(j,1) = computeHV(set,[2,2],'min');
%         res3(j,2) = computeIGD(set,ref);
    end
    
    %compute FRRMAB metrics
%     cd(strcat(resPath,filesep,'FRRMAB',filesep,problem));
    files = dir(strcat('FRRMAB_CEC2009_',problem,'_FUN*'));
    res4 = zeros(length(files),2);
    for j=1:length(files)
        ndpop = loadObjs1(files(j).name);
        res4(j,1)=FastHVindicator.evaluate(ndpop);
        res4(j,2)=IGDIndicator.evaluate(ndpop);
%         set = loadObjs2(files(j).name);
%         res4(j,1) = computeHV(set,[2,2],'min');
%         res4(j,2) = computeIGD(set,ref);
    end
    
    %plot boxplot and mean for hypervolume
    figure(h1);
    subplot(1,2,i);
%     boxplot([res1(:,1),res2(:,1),res3(:,1),res4(:,1)],'colors','rgbk','labels',{'MOEAD','DRA','Uniform','FRRMAB'});
    boxplot([res3(:,1),res4(:,1)],'colors','rgbk','labels',{'Uniform','FRRMAB'});
    hold on
%     scatter(1,mean(res1(:,1)),'rs','MarkerFaceColor','b');
%     scatter(2,mean(res2(:,1)),'gs','MarkerFaceColor','b');
    scatter(3,mean(res3(:,1)),'bs','MarkerFaceColor','b');
    scatter(4,mean(res4(:,1)),'ks','MarkerFaceColor','k');
    hold off
    title(problem)
    
    %plot boxplot and mean for inverted generational distance
    figure(h2);
    subplot(1,2,i);
%     boxplot([res1(:,2),res2(:,2),res3(:,2),res4(:,2)],'colors','rgbk','labels',{'MOEAD','DRA','Uniform','FRRMAB'});
boxplot([res3(:,2),res4(:,2)],'colors','rgbk','labels',{'Uniform','FRRMAB'});
    hold on
%     scatter(1,mean(res1(:,2)),'bs','MarkerFaceColor','b');
%     scatter(2,mean(res2(:,2)),'bs','MarkerFaceColor','b');
    scatter(3,mean(res3(:,2)),'bs','MarkerFaceColor','b');
    scatter(4,mean(res4(:,2)),'ks','MarkerFaceColor','k');
    hold off
    title(problem)
end


cd(origin)

javarmpath(strcat('dist',filesep,'MOHEA.jar'));

end

function ndpop = loadObjs1(filename)
objs = dlmread(strcat(filename));
[a,~] = size(objs);
ndpop = org.moeaframework.core.NondominatedPopulation;
for j=1:a
    ndpop.add(org.moeaframework.core.Solution(objs(j,:)));
end
end

function ref = loadObjs2(filename)
ref = dlmread(strcat(filename));
end
