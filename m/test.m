% 
% javaaddpath('C:\Users\SEAK2\Nozomi\FRRMAB\dist\FRRMAB.jar');
% javaaddpath('C:\Users\SEAK2\Nozomi\MOHEA\dist\MOHEA.jar');
% 
% hh.credittest.HHCreditTest.main('C:\Users\SEAK2\Nozomi\MOHEA\');
% jmetal.metaheuristics.moead.MOEAD_main.main('CEC2009_UF2');

close all
myDataFRRMAB = dlmread('C:\Users\SEAK2\Nozomi\MOHEA\results\UF2_.NDpop');
theirDataFRRMAB = dlmread('C:\Users\SEAK2\Nozomi\FRRMAB\FUN0');

hold on
scatter(theirDataFRRMAB(:,1),theirDataFRRMAB(:,2),'r')
scatter(myDataFRRMAB(:,1),myDataFRRMAB(:,2),'b')
shg

% javarmpath('C:\Users\SEAK2\Nozomi\FRRMAB\dist\FRRMAB.jar');
% javarmpath('C:\Users\SEAK2\Nozomi\MOHEA\dist\MOHEA.jar');