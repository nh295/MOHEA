function [R, prefix, problemName, dim, pareto, objd]=getproblem(problem)
    R = [0;0];
    prefix='';
    problemName='';
    dim = 2;
    %100 evaluations.
    %first need to load the kno1's front.
    switch (lower(problem))
      case {'cec01_30','uf1'}
    	pf = importdata('cec09/pf_data/UF1.dat');
        pareto=pf';
        problemName = 'CEC0901_30';
        prefix = 'CEC0901_30_run_';
        R=[10;10];
        dim=30;
        objd=2;
      case {'cec02_30','uf2'}
        pf = importdata('cec09/pf_data/UF2.dat');
        pareto=pf';
        problemName = 'CEC0902_30';
        prefix = 'CEC0902_30_run_';
        R=[10;10];
        dim=30;
        objd=2;
      case {'cec03_30','uf3'}
        pf = importdata('cec09/pf_data/UF3.dat');
        pareto=pf';
        problemName = 'CEC0903_30';
        prefix = 'CEC0903_30_run_';
        R=[10;10];
        dim=30;
        objd=2;
      case {'cec04_30','uf4'}
    	pf = importdata('cec09/pf_data/UF4.dat');
        pareto=pf';
        problemName = 'CEC0904_30';
        prefix = 'CEC0904_30_run_';
        R=[10;10];
        dim=30;
        objd=2;
      case {'cec05_30','uf5'}
        pf = importdata('cec09/pf_data/UF5.dat');
        pareto=pf';
        problemName = 'CEC0905_30';
        prefix = 'CEC0905_30_run_';
        R=[10;10];
        dim=30;
        objd=2;
      case {'cec06_30','uf6'}
    	pf = importdata('cec09/pf_data/UF6.dat');
        pareto=pf';
        problemName = 'CEC0906_30';
        prefix = 'CEC0906_30_run_';
        R=[10;10];
        dim=30;
        objd=2;
      case {'cec07_30','uf7'}
    	pf = importdata('cec09/pf_data/UF7.dat');
        pareto=pf';
        problemName = 'CEC0907_30';
        prefix = 'CEC0907_30_run_';
        R=[10;10];
        dim=30;
        objd=2;
      case {'cec08_30','uf8'}
        pf = importdata('cec09/pf_data/UF8.dat');
        pareto=pf';
        problemName = 'CEC0908_30';
        prefix = 'CEC0908_30_run_';
        R=[10;10;10];
        dim=30;
        objd=3;        
      case {'cec09_30','uf9'}
        pf = importdata('cec09/pf_data/UF9.dat');
        pareto=pf';
        problemName = 'CEC0909_30';
        prefix = 'CEC0909_30_run_';
        R=[10;10;10];
        dim=30;
        objd=3;        
      case {'cec10_30','uf10'}
    	pf = importdata('cec09/pf_data/UF10.dat');
        pareto=pf';
        problemName = 'CEC0910_30';
        prefix = 'CEC0910_30_run_';
        R=[10;10;10];
        dim=30;
        objd=3;
      case 'cec_r2_dtlz2'
    	pf = importdata('cec09/pf_data/R2_DTLZ2_M5.dat');
        pareto=pf';
        problemName = 'CEC0910_30';
        prefix = 'CEC_R2_DTLZ2_M5';
        R=[10;10;10;10;10];
        dim=30;
        objd=5;
      case 'cec_r2_dtlz3'
    	pf = importdata('cec09/pf_data/R3_DTLZ3_M5.dat');
        pareto=pf';
        problemName = 'CEC0910_30';
        prefix = 'CEC_R2_DTLZ3_M5';
        R=[10;10;10;10;10];
        dim=30;
        objd=5;
      case 'cec_wfg1'
    	pf = importdata('cec09/pf_data/WFG1_M5.dat');
        pareto=pf';
        problemName = 'CEC_WFG1_M5';
        prefix = 'CEC0910_30_run_';
        R=[10;10;10;10;10];
        dim=30;
        objd=5;
      otherwise 
        error('not right');
    end
end
