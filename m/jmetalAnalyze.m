function jmetalAnalyze

javaaddpath(strcat('dist',filesep,'MOHEA.jar'));

origin = cd;
% 
% h1 = figure(1);
% h2 = figure(2);

probs = 1:10;

for i=1:length(probs)
    
    cd(origin)
    
    probfactory = org.moeaframework.core.spi.ProblemFactory.getInstance();
    problem = strcat('UF',num2str(probs(i)));
    prob = probfactory.getProblem(problem);
    refset = probfactory.getReferenceSet(problem);
    ref = loadObjs2(strcat('pf',filesep,problem,'.dat'));
    HVindicator = org.moeaframework.core.indicator.Hypervolume(prob,refset);
    GDindicator = org.moeaframework.core.indicator.GenerationalDistance(prob,refset);
    AEI = org.moeaframework.core.indicator.AdditiveEpsilonIndicator(prob,refset);
    if probs(i)<=7
        refPoint = org.moeaframework.core.Solution([2.0,2.0]);
    else
        refPoint = org.moeaframework.core.Solution([2.0,2.0,2.0]);
    end
    FastHVindicator = org.moeaframework.core.indicator.jmetal.FastHypervolume(prob,refset,refPoint);
    IGDIndicator = org.moeaframework.core.indicator.InvertedGenerationalDistance(prob,refset);
    
   cd('Benchmarks');
   resPath = cd;
    
    %compute MOEAD metrics
%     cd(strcat(resPath,filesep,'MOEAD'));
%     files = dir(strcat('MOEAD_CEC2009_',problem,'_FUN*'));
%     res1 = zeros(length(files),5);
%     for j=1:length(files)
%         ndpop = loadObjs1(files(j).name,refPoint,prob);
%         res1(j,1)=FastHVindicator.evaluate(ndpop);
%         res1(j,2)=IGDIndicator.evaluate(ndpop);
%         res1(j,3)=GDindicator.evaluate(ndpop);
%         res1(j,4)=HVindicator.evaluate(ndpop);
%         res1(j,5)=AEI.evaluate(ndpop);
% %         set = loadObjs2(files(j).name);
% %         res1(j,1) = computeHV(set,[2,2],'min');
% %         res1(j,2) = computeIGD(set,ref);
%     end
% res.GD = res1(:,3);
% res.AEI = res1(:,5);
% res.HV = res1(:,4);
% res.fHV = res1(:,1);
% res.IGD = res1(:,2);
% save(strcat(problem,'_MOEAD','.mat'),'res');
    
%     %compute MOEAD-DRA metrics
%     cd(strcat(resPath,filesep,'MOEADDRA'));
%     files = dir(strcat('MOEAD_DRA_CEC2009_',problem,'_FUN*'));
%     res2 = zeros(length(files),5);
%     for j=1:length(files)
%         ndpop = loadObjs1(files(j).name,refPoint,prob);
%         res2(j,1)=FastHVindicator.evaluate(ndpop);
%         res2(j,2)=IGDIndicator.evaluate(ndpop);
%         res2(j,3)=GDindicator.evaluate(ndpop);
%         res2(j,4)=HVindicator.evaluate(ndpop);
%         res2(j,5)=AEI.evaluate(ndpop);
% %         set = loadObjs2(files(j).name);
% %         res2(j,1) = computeHV(set,[2,2],'min');
% %         res2(j,2) = computeIGD(set,ref);
%     end
% res.GD = res2(:,3);
% res.AEI = res2(:,5);
% res.HV = res2(:,4);
% res.fHV = res2(:,1);
% res.IGD = res2(:,2);
% save(strcat(problem,'_MOEADDRA','.mat'),'res');
%     
% %     %compute Random selection metrics
% % %     cd(strcat(resPath,filesep,'Random',filesep,problem));
% %     files = dir(strcat('Rand_CEC2009_',problem,'_FUN*'));
% %     res3 = zeros(length(files),2);
% %     for j=1:length(files)
% %         ndpop = loadObjs1(files(j).name);
% %         res3(j,1)=FastHVindicator.evaluate(ndpop);
% %         res3(j,2)=IGDIndicator.evaluate(ndpop);
% % %         set = loadObjs2(files(j).name);
% % %         res3(j,1) = computeHV(set,[2,2],'min');
% % %         res3(j,2) = computeIGD(set,ref);
% %     end
%     
%     %compute FRRMAB metrics
%     cd(strcat(resPath,filesep,'FRRMAB'));
%     files = dir(strcat('MOEADDRA_MAB_CEC2009_',problem,'_FUN*'));
%     res4 = zeros(length(files),5);
%     for j=1:length(files)
%         ndpop = loadObjs1(files(j).name,refPoint,prob);
%         res4(j,1)=FastHVindicator.evaluate(ndpop);
%         res4(j,2)=IGDIndicator.evaluate(ndpop);
%         res4(j,3)=GDindicator.evaluate(ndpop);
%         res4(j,4)=HVindicator.evaluate(ndpop);
%         res4(j,5)=AEI.evaluate(ndpop);
% %         set = loadObjs2(files(j).name);
% %         res4(j,1) = computeHV(set,[2,2],'min');
% %         res4(j,2) = computeIGD(set,ref);
%     end
%         res.GD = res4(:,3);
% res.AEI = res4(:,5);
% res.HV = res4(:,4);
% res.fHV = res4(:,1);
% res.IGD = res4(:,2);
% save(strcat(problem,'_FRRMAB','.mat'),'res');

    %compute FRRPM metrics
    cd(strcat(resPath,filesep,'MOEADPM'));
    files = dir(strcat('MOEADDRA_PM_CEC2009_',problem,'_FUN*'));
    res1 = zeros(length(files),5);
    for j=1:length(files)
        ndpop = loadObjs1(files(j).name,refPoint,prob);
        res1(j,1)=FastHVindicator.evaluate(ndpop);
        res1(j,2)=IGDIndicator.evaluate(ndpop);
        res1(j,3)=GDindicator.evaluate(ndpop);
        res1(j,4)=HVindicator.evaluate(ndpop);
        res1(j,5)=AEI.evaluate(ndpop);
%         set = loadObjs2(files(j).name);
%         res1(j,1) = computeHV(set,[2,2],'min');
%         res1(j,2) = computeIGD(set,ref);
    end
res.GD = res1(:,3);
res.AEI = res1(:,5);
res.HV = res1(:,4);
res.fHV = res1(:,1);
res.IGD = res1(:,2);
save(strcat(problem,'_MOEADPM','.mat'),'res');
    
%     %plot boxplot and mean for hypervolume
%     figure(h1);
%     subplot(1,2,i);
% %     boxplot([res1(:,1),res2(:,1),res3(:,1),res4(:,1)],'colors','rgbk','labels',{'MOEAD','DRA','Uniform','FRRMAB'});
%     boxplot([res3(:,1),res4(:,1)],'colors','rgbk','labels',{'Uniform','FRRMAB'});
%     hold on
% %     scatter(1,mean(res1(:,1)),'rs','MarkerFaceColor','b');
% %     scatter(2,mean(res2(:,1)),'gs','MarkerFaceColor','b');
%     scatter(3,mean(res3(:,1)),'bs','MarkerFaceColor','b');
%     scatter(4,mean(res4(:,1)),'ks','MarkerFaceColor','k');
%     hold off
%     title(problem)
%     
%     %plot boxplot and mean for inverted generational distance
%     figure(h2);
%     subplot(1,2,i);
% %     boxplot([res1(:,2),res2(:,2),res3(:,2),res4(:,2)],'colors','rgbk','labels',{'MOEAD','DRA','Uniform','FRRMAB'});
% boxplot([res3(:,2),res4(:,2)],'colors','rgbk','labels',{'Uniform','FRRMAB'});
%     hold on
% %     scatter(1,mean(res1(:,2)),'bs','MarkerFaceColor','b');
% %     scatter(2,mean(res2(:,2)),'bs','MarkerFaceColor','b');
%     scatter(3,mean(res3(:,2)),'bs','MarkerFaceColor','b');
%     scatter(4,mean(res4(:,2)),'ks','MarkerFaceColor','k');
%     hold off
%     title(problem)
end


cd(origin)

javarmpath(strcat('dist',filesep,'MOHEA.jar'));

end

%Loads in the points, but only adds them to the ndpop if they dominate
%refPoint. ndPop is also subject to epsilon dominance to be fair in
%comparison
function ndpop = loadObjs1(filename,refPoint,prob)
objs = dlmread(strcat(filename));
elem_geq_refpt_objs = false(size(objs));
for i=1:refPoint.getNumberOfObjectives
    elem_geq_refpt_objs(:,i) = objs(:,i)<=refPoint.getObjective(i-1);
end
pts_dom_refpt=sum(elem_geq_refpt_objs,2)==refPoint.getNumberOfObjectives;
pop_dom_refpt = objs(pts_dom_refpt,:);

[a,~] = size(pop_dom_refpt);

%set epsilon archive
epsilon = org.moeaframework.analysis.sensitivity.EpsilonHelper.getEpsilon(prob);
ndpop = org.moeaframework.core.EpsilonBoxDominanceArchive(epsilon);
for j=1:a
    ndpop.add(org.moeaframework.core.Solution(pop_dom_refpt(j,:)));
end
end

function ref = loadObjs2(filename)
ref = dlmread(strcat(filename));
end
